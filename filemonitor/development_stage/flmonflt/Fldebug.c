#pragma once

#include <ntifs.h>
#include "FlMutex.h"
#include "fldebug.h"



#ifdef DBG

KMUTEX		DebugPrintMutex;	


/*! Prints the callback data for debugging.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
void 
	PrintCallbackData(
	PFLT_CALLBACK_DATA Data
	)
{

	unsigned int disp;


	if (!Data)
	{
		DBGPRINT("Data for callback is NULL.\n");
		return;
	}


	KMutexAquire(&DebugPrintMutex);

	DBGPRINT("[flmonflt] IRP callback data information:\n");



	DBGPRINT("-----------------------------------------\n");
	DBGPRINT_ARG1("Thread: %i\n", Data->Thread);

	DBGPRINT_ARG1("Flags: %i\n", Data->Flags);

	DBGPRINT_ARG1("Requestor mode: %i\n", Data->RequestorMode);

	DBGPRINT_ARG1("Iopb->IrpFlags: %i\n", Data->Iopb->IrpFlags);
	DBGPRINT_ARG1("Iopb->MajorFunction: %i\n", Data->Iopb->MajorFunction);
	DBGPRINT_ARG1("Iopb->MinorFunction: %i\n", Data->Iopb->MinorFunction);


	DBGPRINT_ARG1("Iopb->OperationFlags: %i\n", Data->Iopb->OperationFlags);

	if (Data->Iopb->OperationFlags & SL_CASE_SENSITIVE)
		DBGPRINT("SL_CASE_SENSITIVE\n");
	if (Data->Iopb->OperationFlags & SL_EXCLUSIVE_LOCK)
		DBGPRINT("SL_EXCLUSIVE_LOCK\n");
	if (Data->Iopb->OperationFlags & SL_FAIL_IMMEDIATELY)
		DBGPRINT("SL_FAIL_IMMEDIATELY\n");
	if (Data->Iopb->OperationFlags & SL_FORCE_ACCESS_CHECK)
		DBGPRINT("SL_FORCE_ACCESS_CHECK\n");
	if (Data->Iopb->OperationFlags & SL_FORCE_DIRECT_WRITE)
		DBGPRINT("SL_FORCE_DIRECT_WRITE\n");
	if (Data->Iopb->OperationFlags & SL_INDEX_SPECIFIED)
		DBGPRINT("SL_INDEX_SPECIFIED\n");
	if (Data->Iopb->OperationFlags & SL_OPEN_PAGING_FILE)
		DBGPRINT("SL_OPEN_PAGING_FILE\n");
	if (Data->Iopb->OperationFlags & SL_OPEN_TARGET_DIRECTORY)
		DBGPRINT("SL_OPEN_TARGET_DIRECTORY\n");
	if (Data->Iopb->OperationFlags & SL_OVERRIDE_VERIFY_VOLUME)
		DBGPRINT("SL_OVERRIDE_VERIFY_VOLUME\n");
	if (Data->Iopb->OperationFlags & SL_RESTART_SCAN)
		DBGPRINT("SL_RESTART_SCAN\n");
	if (Data->Iopb->OperationFlags & SL_RETURN_SINGLE_ENTRY)
		DBGPRINT("SL_RETURN_SINGLE_ENTRY\n");
	if (Data->Iopb->OperationFlags & SL_WATCH_TREE)
		DBGPRINT("SL_WATCH_TREE\n");
	if (Data->Iopb->OperationFlags & SL_WRITE_THROUGH)
		DBGPRINT("SL_WRITE_THROUGH\n");


	if (Data->Iopb->MajorFunction == IRP_MJ_CREATE)
	{
		DBGPRINT_ARG1("Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess: %i\n", 
			Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess);

		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & DELETE)
			DBGPRINT("DELETE\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_READ_DATA)
			DBGPRINT("FILE_READ_DATA\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_READ_ATTRIBUTES)
			DBGPRINT("FILE_READ_ATTRIBUTES\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_READ_EA)
			DBGPRINT("FILE_READ_EA\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & READ_CONTROL)
			DBGPRINT("READ_CONTROL\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_WRITE_DATA)
			DBGPRINT("FILE_WRITE_DATA\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_WRITE_ATTRIBUTES)
			DBGPRINT("FILE_WRITE_ATTRIBUTES\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_WRITE_EA )
			DBGPRINT("FILE_WRITE_EA \n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_APPEND_DATA)
			DBGPRINT("FILE_APPEND_DATA\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & WRITE_DAC )
			DBGPRINT("WRITE_DAC \n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & WRITE_OWNER )
			DBGPRINT("WRITE_OWNER \n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & SYNCHRONIZE)
			DBGPRINT("SYNCHRONIZE\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_EXECUTE)
			DBGPRINT("FILE_EXECUTE\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_LIST_DIRECTORY)
			DBGPRINT("FILE_LIST_DIRECTORY\n");
		if (Data->Iopb->Parameters.Create.SecurityContext->DesiredAccess & FILE_TRAVERSE)
			DBGPRINT("FILE_TRAVERSE\n");


		DBGPRINT_ARG1("Data->Iopb->Parameters.Create.FileAttributes: %i\n", Data->Iopb->Parameters.Create.FileAttributes);

		if (Data->Iopb->Parameters.Create.FileAttributes & FILE_ATTRIBUTE_NORMAL)
			DBGPRINT("FILE_ATTRIBUTE_NORMAL\n");
		if (Data->Iopb->Parameters.Create.FileAttributes & FILE_ATTRIBUTE_READONLY)
			DBGPRINT("FILE_ATTRIBUTE_READONLY\n");
		if (Data->Iopb->Parameters.Create.FileAttributes & FILE_ATTRIBUTE_HIDDEN)
			DBGPRINT("FILE_ATTRIBUTE_HIDDEN\n");
		if (Data->Iopb->Parameters.Create.FileAttributes & FILE_ATTRIBUTE_SYSTEM)
			DBGPRINT("FILE_ATTRIBUTE_SYSTEM\n");
		if (Data->Iopb->Parameters.Create.FileAttributes & FILE_ATTRIBUTE_ARCHIVE)
			DBGPRINT("FILE_ATTRIBUTE_ARCHIVE\n");
		if (Data->Iopb->Parameters.Create.FileAttributes & FILE_ATTRIBUTE_TEMPORARY)
			DBGPRINT("FILE_ATTRIBUTE_TEMPORARY\n");


		DBGPRINT_ARG1("Data->Iopb->Parameters.Create.Options(CreateOptions): %i\n", 
			(Data->Iopb->Parameters.Create.Options & 0xFFFFFF));

		if (Data->Iopb->Parameters.Create.Options & FILE_DIRECTORY_FILE)
			DBGPRINT("FILE_DIRECTORY_FILE\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_NON_DIRECTORY_FILE)
			DBGPRINT("FILE_NON_DIRECTORY_FILE\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_WRITE_THROUGH)
			DBGPRINT("FILE_WRITE_THROUGH\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_SEQUENTIAL_ONLY)
			DBGPRINT("FILE_SEQUENTIAL_ONLY\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_RANDOM_ACCESS)
			DBGPRINT("FILE_RANDOM_ACCESS\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_NO_INTERMEDIATE_BUFFERING)
			DBGPRINT("FILE_NO_INTERMEDIATE_BUFFERING\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_SYNCHRONOUS_IO_ALERT)
			DBGPRINT("FILE_SYNCHRONOUS_IO_ALERT\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_SYNCHRONOUS_IO_NONALERT)
			DBGPRINT("FILE_SYNCHRONOUS_IO_NONALERT\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_CREATE_TREE_CONNECTION)
			DBGPRINT("FILE_CREATE_TREE_CONNECTION\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_OPEN_REPARSE_POINT)
			DBGPRINT("FILE_OPEN_REPARSE_POINT\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_DELETE_ON_CLOSE)
			DBGPRINT("FILE_DELETE_ON_CLOSE\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_OPEN_BY_FILE_ID)
			DBGPRINT("FILE_OPEN_BY_FILE_ID\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_OPEN_FOR_BACKUP_INTENT)
			DBGPRINT("FILE_OPEN_FOR_BACKUP_INTENT\n");
		if (Data->Iopb->Parameters.Create.Options & FILE_RESERVE_OPFILTER )
			DBGPRINT("FILE_RESERVE_OPFILTER \n");

//		if (Data->Iopb->Parameters.Create.Options & FILE_OPEN_REQUIRING_OPLOCK)
//			DBGPRINT("FILE_OPEN_REQUIRING_OPLOCK\n");


		if (Data->Iopb->Parameters.Create.Options & FILE_COMPLETE_IF_OPLOCKED)
			DBGPRINT("FILE_COMPLETE_IF_OPLOCKED\n");


		disp = (Data->Iopb->Parameters.Create.Options >> 24) & 0xFF;
		DBGPRINT_ARG1("Data->Iopb->Parameters.Create.Options(CreateDisposition): %i\n", 
			(disp));

		if (disp & FILE_SUPERSEDE)
			DBGPRINT("FILE_SUPERSEDE\n");
		if (disp & FILE_CREATE)
			DBGPRINT("FILE_CREATE\n");
		if (disp & FILE_OPEN)
			DBGPRINT("FILE_OPEN\n");
		if (disp & FILE_OPEN_IF)
			DBGPRINT("FILE_OPEN_IF\n");
		if (disp & FILE_OVERWRITE)
			DBGPRINT("FILE_OVERWRITE\n");
		if (disp & FILE_OVERWRITE_IF)
			DBGPRINT("FILE_OVERWRITE_IF\n");


		DBGPRINT_ARG1("Data->Iopb->Parameters.Create.ShareAccess: %i\n", 
			Data->Iopb->Parameters.Create.ShareAccess);

		if (Data->Iopb->Parameters.Create.ShareAccess & FILE_SHARE_READ)
			DBGPRINT("FILE_SHARE_READ\n");
		if (Data->Iopb->Parameters.Create.ShareAccess & FILE_SHARE_WRITE)
			DBGPRINT("FILE_SHARE_WRITE\n");
		if (Data->Iopb->Parameters.Create.ShareAccess & FILE_SHARE_DELETE)
			DBGPRINT("FILE_SHARE_DELETE\n");

		DBGPRINT_ARG1("Data->Iopb->Parameters.Create.EaLength: %i\n", 
			Data->Iopb->Parameters.Create.EaLength);
	}
	else if (Data->Iopb->MajorFunction == IRP_MJ_QUERY_INFORMATION)
	{
		DBGPRINT_ARG1("Data->Iopb->Parameters.QueryFileInformation.FileInformationClass: %i\n",
			Data->Iopb->Parameters.QueryFileInformation.FileInformationClass);

		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileAllInformation)
			DBGPRINT("FileAllInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileAttributeTagInformation)
			DBGPRINT("FileAttributeTagInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileBasicInformation)
			DBGPRINT("FileBasicInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileCompressionInformation)
			DBGPRINT("FileCompressionInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileEaInformation)
			DBGPRINT("FileEaInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileInternalInformation)
			DBGPRINT("FileInternalInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileMoveClusterInformation)
			DBGPRINT("FileMoveClusterInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileNameInformation)
			DBGPRINT("FileNameInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileNetworkOpenInformation)
			DBGPRINT("FileNetworkOpenInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FilePositionInformation)
			DBGPRINT("FilePositionInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileStandardInformation)
			DBGPRINT("FileStandardInformation\n");
		if (Data->Iopb->Parameters.QueryFileInformation.FileInformationClass == 
			FileStreamInformation)
			DBGPRINT("FileStreamInformation\n");
	}


	



	DBGPRINT_ARG1("IoStatus.Status: %i\n", Data->IoStatus.Status);
	DBGPRINT_ARG1("IoStatus.Information: %i\n", Data->IoStatus.Information);

	if (Data->IoStatus.Information == FILE_CREATED)
		DBGPRINT("FILE_CREATED\n");
	if (Data->IoStatus.Information == FILE_OPENED)
		DBGPRINT("FILE_OPENED\n");
	if (Data->IoStatus.Information == FILE_OVERWRITTEN)
		DBGPRINT("FILE_OVERWRITTEN\n");
	if (Data->IoStatus.Information == FILE_SUPERSEDED)
		DBGPRINT("FILE_SUPERSEDED\n");
	if (Data->IoStatus.Information == FILE_EXISTS)
		DBGPRINT("FILE_EXISTS\n");
	if (Data->IoStatus.Information == FILE_DOES_NOT_EXIST)
		DBGPRINT("FILE_DOES_NOT_EXIST\n");


	DBGPRINT("-----------------------------------------\n");


	KMutexRelease(&DebugPrintMutex);
}

#else


void 
	PrintCallbackData(PFLT_CALLBACK_DATA Data) { UNREFERENCED_PARAMETER(Data); }



#endif