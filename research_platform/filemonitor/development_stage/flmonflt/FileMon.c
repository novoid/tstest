

#include <fltKernel.h>
#include <wdm.h>
#include <Ntstrsafe.h>
#include "Version.h"
#include "FileMon.h"
#include "FlTracker.h"
#include "FlDirectoryList.h"
#include "FlTimerThread.h"
#include "FlUcFilename.h"
#include "FlIpc.h"
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

FLT_POSTOP_CALLBACK_STATUS
	FlPostCleanup (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__in_opt PVOID CompletionContext,
	__in FLT_POST_OPERATION_FLAGS Flags
	);

FLT_PREOP_CALLBACK_STATUS
	FlPreRead (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID *CompletionContext
	);

FLT_POSTOP_CALLBACK_STATUS
	FlPostRead (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__in_opt PVOID CompletionContext,
	__in FLT_POST_OPERATION_FLAGS Flags
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
OCTRACKER	OcTracker;





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
		FlPostCleanup
	},
	
	{ 
		IRP_MJ_READ,
		0,
		FlPreRead,
		FlPostRead
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


/*! Start or stop filtering activity.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
VOID FlStopFiltering() 
{ 
	if (!DriverInfoData->FilterActive)
	{
		DBGPRINT("[flmonflt] Filter already disabled.\n");
		return;
	}

	DriverInfoData->FilterActive = FALSE; 
	DBGPRINT("[flmonflt] Filter disabled.\n");
}

VOID FlStartFiltering() 
{ 
	
	if (DriverInfoData->FilterActive)
	{
		DBGPRINT("[flmonflt] Filter already active.\n");
		return;
	}

	DriverInfoData->FilterActive = TRUE; 
	DBGPRINT("[flmonflt] Filter now active.\n");
}

BOOLEAN FlFilterState()
{
	return DriverInfoData->FilterActive;
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


#ifdef DBG
	
	DBGPRINT("[flmonflt] Checked build.\n");
	KMutexInit(&DebugPrintMutex);

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
	FlStartFiltering();
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
	FlTrackerInit(&OcTracker);
	DriverInfoData->Tracker = &OcTracker;

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


	FlTrackerPrintListDbg(&OcTracker);

	FlTrackerClean(&OcTracker);
	DirectoriesToWatchClear(TRUE);


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


	UNREFERENCED_PARAMETER(FltObjects);

	//PAGED_CODE();


	if (!DriverInfoData->FilterActive)
		return FLT_PREOP_SUCCESS_NO_CALLBACK;

	
	//if (!NT_SUCCESS( Data->IoStatus.Status )) 
	//{
	//	DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CREATE] Error status or reparse_state. Code: 0x%x.\n", 
	//		Data->IoStatus.Status);
	//	return FLT_PREOP_SUCCESS_NO_CALLBACK;
	//}


	status = FltGetFileNameInformation(Data,
		FLT_FILE_NAME_NORMALIZED |
		FLT_FILE_NAME_QUERY_DEFAULT,
		&nameInfo);

	if (!NT_SUCCESS(status))
	{
		//DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltGetFileNameInformation failed. Code: 0x%x.\n", status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}



	*CompletionContext = (PVOID)nameInfo; 

	return FLT_PREOP_SUCCESS_WITH_CALLBACK;
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
	PFLT_FILE_NAME_INFORMATION nameInfo;
	NTSTATUS status;
	unsigned int s;
	
	BOOLEAN isdir = FALSE;
	OcTrackerType type;

	

	UNREFERENCED_PARAMETER(Data);
	UNREFERENCED_PARAMETER(Flags);

	//PAGED_CODE();



	if (!DriverInfoData->FilterActive)
		return FLT_POSTOP_FINISHED_PROCESSING;



	if (!NT_SUCCESS(Data->IoStatus.Status))
	{

		if (Data->IoStatus.Status == STATUS_OBJECT_NAME_NOT_FOUND  
			|| Data->IoStatus.Status == STATUS_OBJECT_NAME_COLLISION
			|| Data->IoStatus.Status == STATUS_OBJECT_PATH_NOT_FOUND)
		{
			//DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] Status not found or name collision, TLIRP: 0x%x.\n", 
			//	IoGetTopLevelIrp());
		}
		else
		{
			//DBGPRINT_ARG2("[flmonflt:POSTOP:MJ_CREATE] Error status or reparse_state. Code: 0x%x, TLIRP: 0x%x.\n", 
			//	Data->IoStatus.Status, IoGetTopLevelIrp());
			return FLT_POSTOP_FINISHED_PROCESSING;
		}
	}

	nameInfo = (PFLT_FILE_NAME_INFORMATION)CompletionContext; 

	if (!nameInfo)
		return FLT_POSTOP_FINISHED_PROCESSING;


	status = FltParseFileNameInformation(nameInfo);

	if (!NT_SUCCESS(status))
	{
		//DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltParseFileNameInformation failed. Code: 0x%x.\n", status);
		FltReleaseFileNameInformation(nameInfo);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}
	

	//DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CREATE] %wZ\n", &nameInfo->Name);





	if (DirectoriesToWatchPathValid(&nameInfo->Name)) // if the irp for dir is valid
	{
		if (Data->IoStatus.Status == STATUS_OBJECT_NAME_NOT_FOUND
			|| Data->IoStatus.Status == STATUS_OBJECT_NAME_COLLISION
			|| Data->IoStatus.Status == STATUS_OBJECT_PATH_NOT_FOUND
			)
		{
			DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] File not found '%wZ'\n", &nameInfo->Name);
			//FlTrackerClean(&OcTracker);
			FltReleaseFileNameInformation(nameInfo);
			return FLT_POSTOP_FINISHED_PROCESSING;
		}


		//
		// check, if path is a directory
		//
		status = FltIsDirectory(FltObjects->FileObject, FltObjects->Instance, &isdir);

		if (!NT_SUCCESS(status))
		{
			DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FltIsDirectory failed. Code: 0x%x.\n", status);
			FltReleaseFileNameInformation(nameInfo);
			return FLT_POSTOP_FINISHED_PROCESSING;
		}

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

		if (FLT_IS_IRP_OPERATION(Data))
		{
			if (Data->Iopb->OperationFlags == SL_OPEN_TARGET_DIRECTORY)
				DBGPRINT("[flmonflt] SL_OPEN_TARGET_DIRECTORY\n");

			if (Data->IoStatus.Information == FILE_OPENED)
			{
				//PrintCallbackData(Data);
				//if ((Data->Iopb->Parameters.Create.Options & 0xFFFFFF) & FILE_DIRECTORY_FILE)
				//	DBGPRINT("FILE_DIRECTORY_FILE\n");


				s = FlTrackerAddEntry(&OcTracker, type, &nameInfo->Name);

				if (s)
				{
					DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CREATE] FlTrackerAddEntry failed. Code: 0x%x.\n", s);
				}

				KeSetTimer(DriverInfoData->Timer, DriverInfoData->TimerDueTime, NULL); // set wait timer
				KeSetEvent(DriverInfoData->TimerThreadSleepWakeup, 0, FALSE); // unlock main wait
			}
		}
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

	UNREFERENCED_PARAMETER(FltObjects);

	//PAGED_CODE();


	if (!DriverInfoData->FilterActive)
		return FLT_PREOP_SUCCESS_NO_CALLBACK;

	//

	//DBGPRINT("[flmonflt] Entering MJ_CLEANUP callback.\n");

	//if (!NT_SUCCESS( Data->IoStatus.Status )) 
	//{
	//	//DBGPRINT_ARG1("[flmonflt:PREOP:MJ_CLEANUP] Error status. Code: 0x%x.\n", 
	//	//	Data->IoStatus.Status);
	//	return FLT_PREOP_SUCCESS_NO_CALLBACK;
	//}


	status = FltGetFileNameInformation( Data,
		FLT_FILE_NAME_NORMALIZED |
		FLT_FILE_NAME_QUERY_DEFAULT,
		&nameInfo );

	if (!NT_SUCCESS(status)) 
	{
		//DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CLEANUP] FltGetFileNameInformation failed. Code: 0x%x.\n", status);
		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}

	*CompletionContext = (PVOID)nameInfo; 
	

	return FLT_PREOP_SUCCESS_WITH_CALLBACK;
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
	FlPostCleanup (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__in_opt PVOID CompletionContext,
	__in FLT_POST_OPERATION_FLAGS Flags
	)
{
	NTSTATUS status;
	PFLT_FILE_NAME_INFORMATION nameInfo;


	UNREFERENCED_PARAMETER(FltObjects);
	UNREFERENCED_PARAMETER(Flags);



	if (!DriverInfoData->FilterActive)
		return FLT_POSTOP_FINISHED_PROCESSING;

	
	if (!NT_SUCCESS( Data->IoStatus.Status )) 
	{
		//DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CLEANUP] Error status. Code: 0x%x.\n", 
		//	Data->IoStatus.Status);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}


	nameInfo = (PFLT_FILE_NAME_INFORMATION)CompletionContext; 

	if (!nameInfo)
		return FLT_POSTOP_FINISHED_PROCESSING;

	status = FltParseFileNameInformation(nameInfo);

	if (!NT_SUCCESS(status)) 
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_CLEANUP] FltParseFileNameInformation failed. Code: 0x%x.\n", status);
		FltReleaseFileNameInformation(nameInfo);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}

	// if the irp for dir is valid
	if (DirectoriesToWatchPathValid(&nameInfo->Name)) 
	{
		FlTrackerRemoveEntry(&OcTracker, OCTRACKER_TYPE_DIR, 
			&nameInfo->Name);
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
	FlPreRead (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__deref_out_opt PVOID *CompletionContext
	)
{
	NTSTATUS status;
	PFLT_FILE_NAME_INFORMATION nameInfo;

	//UNREFERENCED_PARAMETER(CompletionContext);
	//UNREFERENCED_PARAMETER(Data);
	UNREFERENCED_PARAMETER(FltObjects);

	//PAGED_CODE();



	if (!DriverInfoData->FilterActive)
		return FLT_PREOP_SUCCESS_NO_CALLBACK;



	//if (!NT_SUCCESS(Data->IoStatus.Status))
	//{
	//	DBGPRINT_ARG1("[flmonflt:PREOP:MJ_READ] Error status or reparse_state. Code: 0x%x.\n", 
	//		Data->IoStatus.Status);
	//	return FLT_PREOP_SUCCESS_NO_CALLBACK;
	//}

	status = FltGetFileNameInformation(Data,
		FLT_FILE_NAME_NORMALIZED |
		FLT_FILE_NAME_QUERY_DEFAULT,
		&nameInfo);

	if (!NT_SUCCESS(status))
	{
		//DBGPRINT_ARG2("[flmonflt:POSTOP:MJ_READ] FltGetFileNameInformation failed. Code: 0x%x, TLIRP: 0x%x.\n", 
		//	status, IoGetTopLevelIrp());

		return FLT_PREOP_SUCCESS_NO_CALLBACK;
	}

	*CompletionContext = (PVOID)nameInfo; 


	return FLT_PREOP_SUCCESS_WITH_CALLBACK;
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
	FlPostRead (
	__inout PFLT_CALLBACK_DATA Data,
	__in PCFLT_RELATED_OBJECTS FltObjects,
	__in_opt PVOID CompletionContext,
	__in FLT_POST_OPERATION_FLAGS Flags
	)
{
	NTSTATUS status;
	PFLT_FILE_NAME_INFORMATION nameInfo;


	UNREFERENCED_PARAMETER(CompletionContext);
	UNREFERENCED_PARAMETER(Flags);
	UNREFERENCED_PARAMETER(FltObjects);


	if (!DriverInfoData->FilterActive)
		return FLT_POSTOP_FINISHED_PROCESSING;


	if (!NT_SUCCESS(Data->IoStatus.Status)) 
	{
		//DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_READ] Error status or reparse_state. Code: 0x%x.\n", 
		//	Data->IoStatus.Status);

		return FLT_POSTOP_FINISHED_PROCESSING;
	}

	
	nameInfo = (PFLT_FILE_NAME_INFORMATION)CompletionContext; 

	if (!nameInfo)
		return FLT_POSTOP_FINISHED_PROCESSING;


	status = FltParseFileNameInformation(nameInfo);

	if (!NT_SUCCESS(status))
	{
		DBGPRINT_ARG1("[flmonflt:POSTOP:MJ_READ] FltParseFileNameInformation failed. Code: 0x%x.\n", status);
		FltReleaseFileNameInformation(nameInfo);

		return FLT_POSTOP_FINISHED_PROCESSING;
	}


	
	if (DirectoriesToWatchPathValid(&nameInfo->Name))
	{
		//DBGPRINT("[flmonflt] R Path '%wZ'.\n", &nameInfo->Name);

		// if the file is in a watched path
		//
		FlTrackerSetRead(&OcTracker, &nameInfo->Name);

		FltReleaseFileNameInformation(nameInfo);
		return FLT_POSTOP_FINISHED_PROCESSING;
	}
	

	FltReleaseFileNameInformation(nameInfo);
	return FLT_POSTOP_FINISHED_PROCESSING;
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
	unsigned char * InputBufferC = InputBuffer;
	UNICODE_STRING Path;

	//UNREFERENCED_PARAMETER(InputBuffer);
	UNREFERENCED_PARAMETER(OutputBuffer);
	UNREFERENCED_PARAMETER(OutputBufferLength);
	UNREFERENCED_PARAMETER(PortCookie);
	UNREFERENCED_PARAMETER(ReturnOutputBufferLength);

	PAGED_CODE();

	
	if (InputBufferLength < 2)
	{
		DBGPRINT("[flmonflt] Userspace notify, Invalid message format, Buffer < 2.\n");
		return STATUS_INVALID_PARAMETER;
	}

	if (InputBufferC == NULL)
	{
		DBGPRINT("[flmonflt] Userspace notify, Invalid message format, InputBufferC == NULL.\n");
		return STATUS_INVALID_PARAMETER;
	}

	if (InputBufferC[0] != MAGIC_MESSAGE_PREFIX)
	{
		DBGPRINT("[flmonflt] Userspace notify, Invalid message format, Prefix invalid.\n");
		return STATUS_INVALID_PARAMETER;
	}

	//DBGPRINT("[flmonflt] Userspace notify.\n");
	DBGPRINT_ARG2("[flmonflt] Userspace notify, size=%d, CMD='%c'.\n", InputBufferLength, (char)InputBufferC[1]);


	try
	{

		if (InputBufferC[1] == MESSAGE_CMD_ADD_WATCHDIR)
		{
			Path.Length = (USHORT)InputBufferLength-2;
			Path.MaximumLength = (USHORT)InputBufferLength-2;
			Path.Buffer = (PWCH)(InputBufferC+2);


			if (Path.Length == 0)
			{
				DBGPRINT_ARG1("[flmonflt] Userspace notify, Invalid path length. LEN=%d\n", Path.Length);
				return STATUS_INVALID_PARAMETER;
			}

			if (!DirectoriesToWatchAdd(&Path))
			{
				DBGPRINT_ARG1("[flmonflt] Userspace notify, Directory added: 0x%x\n", STATUS_SUCCESS);
				return STATUS_SUCCESS;
			}
			else
			{
				DBGPRINT_ARG1("[flmonflt] Userspace notify, Directory exists: 0x%x\n", STATUS_OBJECTID_EXISTS);
				return STATUS_OBJECTID_EXISTS;
			}

		}
		else if (InputBufferC[1] == MESSAGE_CMD_CLEAR_WATCHDIR)
		{
			DirectoriesToWatchClear(FALSE);
		}
		else if (InputBufferC[1] == MESSAGE_CMD_START_FILTER)
		{
			FlStartFiltering();
		}
		else if (InputBufferC[1] == MESSAGE_CMD_STOP_FILTER)
		{
			FlStopFiltering();
		}
		else if (InputBufferC[1] == MESSAGE_CMD_FILTER_STATE)
		{
			DBGPRINT_ARG1("[flmonflt] Filter state: %d.\n", FlFilterState());

			if (OutputBufferLength >= 1)
			{
				((char*)OutputBuffer)[0] = FlFilterState();
				*ReturnOutputBufferLength = 1;

			
			}
			else
			{
				*ReturnOutputBufferLength = 0;
			}
		}
		else
		{
			DBGPRINT("[flmonflt] Userspace notify, Invalid command format.\n");
			return STATUS_INVALID_PARAMETER;
		}

	} 
	except( EXCEPTION_EXECUTE_HANDLER ) {

		return GetExceptionCode();
	}

	return STATUS_SUCCESS;
}