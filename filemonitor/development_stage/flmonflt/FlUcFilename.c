
#include "fldebug.h"
#include "FlUcFilename.h"



#define	FNSTRING_MAXLENGTH			1024



/*! Inits a string by allocating a nonpaged memory pool and copying the
	sourcestring into this pool.
	If SourceString is NULL, the first characted is a zero termination.
	The DestinationString is always allocated with maximum size.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
NTSTATUS UcFilenameInit(
	__out     PUNICODE_STRING String
	)
{
	PWSTR  Buffer;


	Buffer = ExAllocatePool(NonPagedPool, FNSTRING_MAXLENGTH*sizeof(WCHAR));

	if (!Buffer)
	{
		DBGPRINT("[flmonflt] UcFilenameInit: STATUS_NO_MEMORY.\n");
		return STATUS_NO_MEMORY;
	}

	Buffer[0] = Buffer[1] = 0;
	String->Length = 0;
	String->MaximumLength = FNSTRING_MAXLENGTH*sizeof(WCHAR);


	RtlInitEmptyUnicodeString(String, Buffer, FNSTRING_MAXLENGTH*sizeof(WCHAR));

	return STATUS_SUCCESS;
}

/*! Frees a string allocated with UnicodeStringMInit.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
VOID UcFilenameFree(
	__out     PUNICODE_STRING String
	)
{
	if (String->Buffer == NULL ||
		String->MaximumLength == 0)
	{
		DBGPRINT("[flmonflt] UcFilenameFree: STATUS_INVALID_PARAMETER.\n");
		return;
	}

	ExFreePool(String->Buffer);

	String->Length = 0;
	String->Buffer = NULL;
	String->MaximumLength = 0;
}


void UcFilenameCopy(
	__in    PUNICODE_STRING SrcString,
	__out	PUNICODE_STRING DstString
	)
{
	if (!SrcString && !DstString)
	{
		DBGPRINT("[flmonflt] UcFilenameCopy: (!SrcString && !DstString).\n");
		return;
	}

	RtlCopyUnicodeString(DstString, SrcString);
}

BOOLEAN UcFilenameEqual(
	__in    PUNICODE_STRING String1,
	__in	PUNICODE_STRING String2
	)
{
	if (!String1 && !String2)
	{
		DBGPRINT("[flmonflt] UcFilenameEqual: (!SrcString && !DstString).\n");
		return FALSE;
	}

	//if (String1->Length != String2->Length)
	//	return FALSE;

	if (!RtlCompareUnicodeString(String1, String2, TRUE))
		return TRUE;

	return FALSE;
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN UcFilenameStartsWith(
	__in	PUNICODE_STRING MonitorString,
	__in	PUNICODE_STRING InString)
{
	if (!MonitorString && !InString)
	{
		DBGPRINT("[flmonflt] UcFilenameStartsWith: (!SrcString && !DstString).\n");
		return FALSE;
	}

	return RtlPrefixUnicodeString(MonitorString, 
		InString, TRUE);
}