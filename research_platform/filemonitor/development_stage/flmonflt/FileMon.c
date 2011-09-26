

#include <fltKernel.h>
#include <wdm.h>
#include <Ntstrsafe.h>
#include "Version.h"
#include "FileMon.h"
#include "FlTracker.h"
#include "FlDirectoryList.h"
#include "FlTimerThread.h"
#include "FlUcFilename.h"
#include "fldebug.h"






//-------------------------------------------------------------------------
// Prototypes
//-------------------------------------------------------------------------
NTSTATUS DriverUnload (
	IN FLT_FILTER_UNLOAD_FLAGS Flags
	);

FLT_PREOP_CALLBACK_STATUS
	FlPreCreate (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID *CompletionContext
	);

FLT_POSTOP_CALLBACK_STATUS
	FlPostCreate (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__in_opt PVOID CompletionContext,
	__in FLT_POST_OPERATION_FLAGS Flags
	);


FLT_PREOP_CALLBACK_STATUS
	FlPreCleanup (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID *CompletionContext
	);

FLT_PREOP_CALLBACK_STATUS
	FlPreRead (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID *CompletionContext
	);


NTSTATUS
	FlUserspaceConnect(
		__in PFLT_PORT ClientPort,
		__in PVOID ServerPortCookie,
		__in_bcount(SizeOfContext) PVOID ConnectionContext,
		__in ULONG SizeOfContext,
		__deref_out_opt PVOID *ConnectionCookie
	);

VOID
	FlUserspaceDisconnect(
		__in_opt PVOID ConnectionCookie
	);


NTSTATUS
	FlUserspaceNotify (
			IN PVOID PortCookie,
			IN PVOID InputBuffer OPTIONAL,
			IN ULONG InputBufferLength,
			OUT PVOID OutputBuffer OPTIONAL,
			IN ULONG OutputBufferLength,
			OUT PULONG ReturnOutputBufferLength
	);


//-------------------------------------------------------------------------
// Initialisation fields
//-------------------------------------------------------------------------


PFLDRIVERDATA	DriverInfoData;
OCTRACKER	OcDirectoryTracker;





const FLT_OPERATION_REGISTRATION Callbacks[] = 
{
	{ 
		IRP_MJ_CREATE,
		0,
		FlPreCreate,
		FlPostCreate
	},

	{ 
		IRP_MJ_CLEANUP,
		0,
		FlPreCleanup,
		NULL
	},

	{ 
		IRP_MJ_READ,
		0,
		FlPreRead,
		NULL
	},

	{ IRP_MJ_OPERATION_END}
};


const FLT_CONTEXT_REGISTRATION ContextRegistration[] = 
{
	{ FLT_STREAMHANDLE_CONTEXT,
	0,
	NULL,
	FLT_VARIABLE_SIZED_CONTEXTS,
	'flMN'},

	{ FLT_CONTEXT_END }
};


const FLT_REGISTRATION FilterRegistration = 
{

	sizeof( FLT_REGISTRATION ),         //  Size
	FLT_REGISTRATION_VERSION,           //  Version
	0,                                  //  Flags
	ContextRegistration,                //  Context Registration.
	Callbacks,                          //  Operation callbacks
	DriverUnload,                       //  FilterUnload
	NULL,								//  InstanceSetup*
	NULL,								//  InstanceQueryTeardown*
	NULL,                               //  InstanceTeardownStart
	NULL,                               //  InstanceTeardownComplete
	NULL,                               //  GenerateFileName
	NULL,                               //  GenerateDestinationFileName
	NULL                                //  NormalizeNameComponent
};



//-------------------------------------------------------------------------
// Implementation
//-------------------------------------------------------------------------



/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
NTSTATUS
	DriverEntry (
	IN PDRIVER_OBJECT DriverObject,
	IN PUNICODE_STRING RegistryPath
	)
{

	NTSTATUS status = STATUS_SUCCESS;
	OBJECT_ATTRIBUTES oa;
	PSECURITY_DESCRIPTOR sd;
	UNICODE_STRING uniString;

	UNREFERENCED_PARAMETER(RegistryPath);



	DBGPRINT_ARG1("[flmonflt] Filemonitor Filter Driver. Florian Praxmair, Version: %s\n", 
		VERSION);

#ifdef CHECKED
	DBGPRINT("[flmonflt] Checked build.\n");
#endif


	// init driver status memory
	//
	DriverInfoData = ExAllocatePool(NonPagedPool, sizeof(FLDRIVERDATA));

	if (!DriverInfoData)
	{
		return STATUS_NO_MEMORY;
	}

	DriverInfoData->DriverObject = DriverObject;
	DriverInfoData->ClientPort = NULL;
	DriverInfoData->TimerThread = NULL;
	//DriverInfoData->TrackerLastEntryValid = FALSE;


	DriverInfoData->TrackerLastPath.Buffer = ExAllocatePool(NonPagedPool, MAX_PATH_LEN * sizeof(WCHAR));
	DriverInfoData->TrackerLastPath.MaximumLength = MAX_PATH_LEN * sizeof(WCHAR);
	DriverInfoData->TrackerLastPath.Length = 0;

	// initialize directory list
	//
	DirectoriesToWatchInit();

	// create and start timer thread
	//
	status = FlTimerThreadCreate();

	if (!NT_SUCCESS(status)) 
	{
		DriverInfoData->TrackerLastPath.MaximumLength = 0;
		DriverInfoData->TrackerLastPath.Length = 0;
		ExFreePool(DriverInfoData->TrackerLastPath.Buffer);

		DBGPRINT_ARG1("[flmonflt] Timer thread creation failed. Code: 0x%x\n", 
			status);

		return status;
	}
	
	// register filter
	//
	status = FltRegisterFilter( DriverObject,
		&FilterRegistration,
		&DriverInfoData->Filter);


	if (!NT_SUCCESS(status)) 
	{
		DBGPRINT("[flmonflt] Filter registration failed.");

		switch (status)
		{
		case STATUS_INSUFFICIENT_RESOURCES:
			DBGPRINT("STATUS_INSUFFICIENT_RESOURCES");
			break;
		case STATUS_INVALID_PARAMETER:
			DBGPRINT("STATUS_INVALID_PARAMETER");
			break;
		case STATUS_FLT_NOT_INITIALIZED:
			DBGPRINT("STATUS_FLT_NOT_INITIALIZED");
			break;
		default:
			DBGPRINT_ARG1("UNKNOWN ERROR 0x%x", status);
		}

		DBGPRINT("\n");

		DriverInfoData->TrackerLastPath.MaximumLength = 0;
		DriverInfoData->TrackerLastPath.Length = 0;
		ExFreePool(DriverInfoData->TrackerLastPath.Buffer);

		return status;
	}


	// create communication port
	//
	RtlInitUnicodeString(&uniString, FLPORTNAME);

	status = FltBuildDefaultSecurityDescriptor(&sd, FLT_PORT_ALL_ACCESS);

	if (!NT_SUCCESS(status))
	{
		return status;
	}

	InitializeObjectAttributes( &oa,
		&uniString,
		OBJ_KERNEL_HANDLE | OBJ_CASE_INSENSITIVE,
		NULL,
		sd );

	status = FltCreateCommunicationPort(DriverInfoData->Filter,
		&DriverInfoData->ServerPort,
		&oa,
		NULL,
		FlUserspaceConnect,
		FlUserspaceDisconnect,
		FlUserspaceNotify,
		1 );

	if (!NT_SUCCESS(status)) 
	{
		DBGPRINT("[flmonflt] Port creation failed.");

		DriverInfoData->TrackerLastPath.MaximumLength = 0;
		DriverInfoData->TrackerLastPath.Length = 0;
		ExFreePool(DriverInfoData->TrackerLastPath.Buffer);

		return status;
	}

	FltFreeSecurityDescriptor(sd);

	DBGPRINT("[flmonflt] Port creation finished.\n");

	// kick on filtering activity
	//
	status = FltStartFiltering(DriverInfoData->Filter);

	if (!NT_SUCCESS(status)) 
	{
		DBGPRINT_ARG1("[flmonflt] FltStartFiltering ERROR 0x%x", status);
		FltUnregisterFilter(DriverInfoData->Filter);
		FltCloseCommunicationPort(DriverInfoData->ServerPort);

		DriverInfoData->TrackerLastPath.MaximumLength = 0;
		DriverInfoData->TrackerLastPath.Length = 0;
		ExFreePool(DriverInfoData->TrackerLastPath.Buffer);

		return status;
	}

	// init tracker
	//
	FlTrackerInit(&OcDirectoryTracker);
	DriverInfoData->Tracker = &OcDirectoryTracker;

	DBGPRINT("[flmonflt] Driver initialisation finished.\n");

	return STATUS_SUCCESS;
}



/*! Driver unload routine.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
NTSTATUS DriverUnload (
	IN FLT_FILTER_UNLOAD_FLAGS Flags
	)
{
	UNREFERENCED_PARAMETER(Flags);


	FlTimerThreadStop();

	FltCloseCommunicationPort(DriverInfoData->ServerPort);
	FltUnregisterFilter(DriverInfoData->Filter);


	FlTrackerPrintListDbg(&OcDirectoryTracker);

	FlTrackerClean(&OcDirectoryTracker);
	DirectoriesToWatchClear();


	DriverInfoData->TrackerLastPath.MaximumLength = 0;
	DriverInfoData->TrackerLastPath.Length = 0;
	ExFreePool(DriverInfoData->TrackerLastPath.Buffer);

	ExFreePool(DriverInfoData);

	DBGPRINT("[flmonflt] Driver unloaded.\n");

	return STATUS_SUCCESS;
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

	*/
FLT_PREOP_CALLBACK_STATUS
	FlPreCreate (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID * CompletionContext
	)
{
	NTSTATUS status;
	PFLT_FILE_NAME_INFORMATION nameInfo;


	UNREFERENCED_PARAMETER(CompletionContext);
	UNREFERENCED_PARAMETER(FltObjects);

	PAGED_CODE();



	if (!NT_SUCCESS( Data->IoStatus.Status )) 
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] Error status or reparse_state. Code: 0x%x.\n", 
			Data->IoStatus.Status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}


	status = FltGetFileNameInformation( Data,
		FLT_FILE_NAME_NORMALIZED |
		FLT_FILE_NAME_QUERY_DEFAULT,
		&nameInfo);

	if (!NT_SUCCESS(status))
	{
//		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltGetFileNameInformation failed. Code: 0x%x.\n", status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}


	status = FltParseFileNameInformation(nameInfo);

	if (!NT_SUCCESS(status))
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltParseFileNameInformation failed. Code: 0x%x.\n", status);
		FltReleaseFileNameInformation(nameInfo);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}


	if (DirectoriesToWatchPathExist(&nameInfo->Name))
	{
		FltReleaseFileNameInformation(nameInfo);
		return FLT_PREOP_SUCCESS_WITH_CALLBACK;
	}

	return FLT_PREOP_SUCCESS_NO_CALLBACK;
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
FLT_POSTOP_CALLBACK_STATUS
	FlPostCreate (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__in_opt PVOID CompletionContext,
	__in FLT_POST_OPERATION_FLAGS Flags
	)
{
	NTSTATUS status;
	unsigned int s;
	PFLT_FILE_NAME_INFORMATION nameInfo;
	BOOLEAN isdir = FALSE;
	OcTrackerType type;

	

	UNREFERENCED_PARAMETER(CompletionContext);
	UNREFERENCED_PARAMETER(Data);
	UNREFERENCED_PARAMETER(Flags);

	PAGED_CODE();


	//DBGPRINT("[flmonflt] Entering MJ_CREATE callback.\n");

	if (!NT_SUCCESS(Data->IoStatus.Status)) 
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] Error status or reparse_state. Code: 0x%x.\n", 
			Data->IoStatus.Status);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}


	status = FltGetFileNameInformation(Data,
		FLT_FILE_NAME_NORMALIZED |
		FLT_FILE_NAME_QUERY_DEFAULT,
		&nameInfo);

	if (!NT_SUCCESS(status))
	{
//		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltGetFileNameInformation failed. Code: 0x%x.\n", status);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}


	status = FltParseFileNameInformation(nameInfo);

	if (!NT_SUCCESS(status))
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltParseFileNameInformation failed. Code: 0x%x.\n", status);
		FltReleaseFileNameInformation(nameInfo);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}

	//DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CREATE] %wZ\n", &nameInfo->Name);

	//
	// check, if path is a directory
	//
	status = FltIsDirectory(FltObjects->FileObject, FltObjects->Instance, &isdir);

	if (!NT_SUCCESS(status))
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltIsDirectory failed. Code: 0x%x.\n", status);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}



	if (DirectoriesToWatchPathExist(&nameInfo->Name)) // if the irp for dir is valid
	{

		// is it a directory
		//
		if (isdir)
		{
			type = OCTRACKER_TYPE_DIR;
		}
		else
		{
			type = OCTRACKER_TYPE_FILE;
		}


		s = FlTrackerAddEntry(&OcDirectoryTracker, type, &nameInfo->Name);

		if (s)
		{
			DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FlTrackerAddEntry failed. Code: 0x%x.\n", s);
		}

		KeSetTimer(DriverInfoData->Timer, DriverInfoData->TimerDueTime, NULL); // set wait timer
		KeSetEvent(DriverInfoData->TimerThreadSleepWakeup, 0, FALSE); // unlock main wait

	}

	FltReleaseFileNameInformation(nameInfo);

	return FLT_POSTOP_FINISHED_PROCESSING;
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

	*/
FLT_PREOP_CALLBACK_STATUS
	FlPreCleanup (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID * CompletionContext
	)
{
	NTSTATUS status;
	PFLT_FILE_NAME_INFORMATION nameInfo;


	UNREFERENCED_PARAMETER(CompletionContext);
	UNREFERENCED_PARAMETER(FltObjects);

	PAGED_CODE();

	//DBGPRINT("[flmonflt] Entering MJ_CLEANUP callback.\n");

	if (!NT_SUCCESS( Data->IoStatus.Status )) 
	{
		//DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CLEANUP] Error status. Code: 0x%x.\n", 
		//	Data->IoStatus.Status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}
	
	// use cleanup only for directories
	//
	//status = FltIsDirectory(FltObjects->FileObject, FltObjects->Instance, &isdir);

	//if (!NT_SUCCESS(status))
	//{
	//	DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CLEANUP] FltIsDirectory failed. Code: 0x%x.\n", status);
	//	return FLT_PREOP_SUCCESS_NO_CALLBACK;
	//}

	//if (!isdir)
	//	return FLT_PREOP_SUCCESS_NO_CALLBACK;


	status = FltGetFileNameInformation( Data,
		FLT_FILE_NAME_NORMALIZED |
		FLT_FILE_NAME_QUERY_DEFAULT,
		&nameInfo );

	if (!NT_SUCCESS(status)) 
	{
//		DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CLEANUP] FltGetFileNameInformation failed. Code: 0x%x.\n", status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}


	status = FltParseFileNameInformation(nameInfo);

	if (!NT_SUCCESS(status)) 
	{
		DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CLEANUP] FltParseFileNameInformation failed. Code: 0x%x.\n", status);
		FltReleaseFileNameInformation(nameInfo);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}

	// if the irp for dir is valid
	if (DirectoriesToWatchPathExist(&nameInfo->Name)) 
	{
		FlTrackerRemoveEntry(&OcDirectoryTracker, OCTRACKER_TYPE_DIR, 
			&nameInfo->Name);
	}


	FltReleaseFileNameInformation(nameInfo);
	

	return FLT_PREOP_SUCCESS_NO_CALLBACK;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
FLT_PREOP_CALLBACK_STATUS
	FlPreRead (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID *CompletionContext
	)
{
	NTSTATUS status;
	PFLT_FILE_NAME_INFORMATION nameInfo;


	UNREFERENCED_PARAMETER(CompletionContext);
	//UNREFERENCED_PARAMETER(Data);
	UNREFERENCED_PARAMETER(FltObjects);

	PAGED_CODE();


	if (!NT_SUCCESS(Data->IoStatus.Status)) 
	{
		DBGPRINT_ARG1("[flmonflt:PREOP:MJ_READ] Error status or reparse_state. Code: 0x%x.\n", 
			Data->IoStatus.Status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}

	status = FltGetFileNameInformation(Data,
		FLT_FILE_NAME_NORMALIZED |
		FLT_FILE_NAME_QUERY_DEFAULT,
		&nameInfo);

	if (!NT_SUCCESS(status))
	{
//		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltGetFileNameInformation failed. Code: 0x%x.\n", status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}


	status = FltParseFileNameInformation(nameInfo);

	if (!NT_SUCCESS(status))
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltParseFileNameInformation failed. Code: 0x%x.\n", status);
		FltReleaseFileNameInformation(nameInfo);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}


	if (DirectoriesToWatchPathExist(&nameInfo->Name))
	{
		// if the file is in a watched path
		//
		FlTrackerSetRead(&OcDirectoryTracker, &nameInfo->Name);

		FltReleaseFileNameInformation(nameInfo);
		return FLT_PREOP_SUCCESS_WITH_CALLBACK;
	}


	FltReleaseFileNameInformation(nameInfo);
	return FLT_PREOP_SUCCESS_NO_CALLBACK;
}


//-------------------------------------------------------------------------


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
NTSTATUS
	FlUserspaceConnect(
			__in PFLT_PORT ClientPort,
			__in PVOID ServerPortCookie,
			__in_bcount(SizeOfContext) PVOID ConnectionContext,
			__in ULONG SizeOfContext,
			__deref_out_opt PVOID *ConnectionCookie
	)

{
	DBGPRINT("[flmonflt] Userspace connected.\n");

	UNREFERENCED_PARAMETER(ConnectionCookie);
	UNREFERENCED_PARAMETER(ConnectionContext);
	UNREFERENCED_PARAMETER(ServerPortCookie);
	UNREFERENCED_PARAMETER(SizeOfContext);

	PAGED_CODE();


	if (!DriverInfoData->ClientPort)
	{
		DriverInfoData->ClientPort = ClientPort;
	}
	else
	{
		return STATUS_NOT_SUPPORTED;
	}

	return STATUS_SUCCESS;
}




/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
VOID
	FlUserspaceDisconnect(
			__in_opt PVOID ConnectionCookie
	)
{
	DBGPRINT("[flmonflt] Userspace disconnected.\n");

	UNREFERENCED_PARAMETER(ConnectionCookie);

	PAGED_CODE();


	FltCloseClientPort(DriverInfoData->Filter, &DriverInfoData->ClientPort);
	DriverInfoData->ClientPort = NULL;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
NTSTATUS
	FlUserspaceNotify (
		IN PVOID PortCookie,
		IN PVOID InputBuffer OPTIONAL,
		IN ULONG InputBufferLength,
		OUT PVOID OutputBuffer OPTIONAL,
		IN ULONG OutputBufferLength,
		OUT PULONG ReturnOutputBufferLength
	)
{
	UNREFERENCED_PARAMETER(InputBuffer);
	UNREFERENCED_PARAMETER(OutputBuffer);
	UNREFERENCED_PARAMETER(OutputBufferLength);
	UNREFERENCED_PARAMETER(PortCookie);
	UNREFERENCED_PARAMETER(ReturnOutputBufferLength);

	PAGED_CODE();

	//DBGPRINT("[flmonflt] Userspace notify.\n");
	DBGPRINT_ARG1("[flmonflt] Userspace notify, size=%d.\n", InputBufferLength);


	return STATUS_SUCCESS;
}