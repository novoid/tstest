
/*! \file FlDirectoryList.cpp
 * \brief Defines a list of directories to get watched.
 *
 *
 * \date 2011/08/18
 * \author fpraxmair
 *
 */

#include <ntifs.h>
#include "fldebug.h"
#include "FlMutex.h"
#include "FileMon.h"
#include "FlUcFilename.h"
#include "FlDirectoryList.h"



#define PATH_TO_MONITOR_TEST		L"\\Device\\HarddiskVolume1\\Users\\Administrator\\Desktop\\Neuer Ordner"





//-------------------------------------------------------------------------
BOOLEAN 
	DirectoriesToWatchExists(PUNICODE_STRING dirname);





/*! Initializes the watch directory list.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN 
	DirectoriesToWatchInit(void)
{
	unsigned int i = 0;
	PWCHAR	buf;

#ifdef CHECKED
	UNICODE_STRING teststr;
#endif

	DriverInfoData->PathsToMonitorCount = 0;

	for (i=0; i < MAX_MONITOR_PATH_COUNT; i++)
	{
		buf = ExAllocatePool(NonPagedPool, MAX_PATH_LEN * sizeof(WCHAR));

		if (!buf)
		{
			DirectoriesToWatchClear();
			return FALSE;
		}

		memset(buf, 0, MAX_PATH_LEN * sizeof(WCHAR));

		RtlInitEmptyUnicodeString(&DriverInfoData->PathsToMonitor[i], buf, 
			MAX_PATH_LEN * sizeof(WCHAR)); 
	}


	DBGPRINT("[flmonflt] Directory list initialized.\n");
	DBGPRINT_ARG1("[flmonflt] Directory list count: %d\n", DriverInfoData->PathsToMonitorCount);

#ifdef CHECKED

	RtlInitUnicodeString(&teststr, PATH_TO_MONITOR_TEST);
	DirectoriesToWatchAdd(&teststr);

#endif

	return TRUE;
}


/*! Checks if the directory exists in the list.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN 
	DirectoryToWatchExists(
	PUNICODE_STRING dirname)
{
	unsigned int i;
	BOOLEAN res;

	res = FALSE;


	DBGPRINT_ARG1("[flmonflt] Directory list count: %d\n", DriverInfoData->PathsToMonitorCount);

	

	for (i=0; i < DriverInfoData->PathsToMonitorCount; i++)
	{
		if (!RtlCompareUnicodeString(dirname, 
			&DriverInfoData->PathsToMonitor[i], TRUE))
		{
			res = TRUE;
			break;
		}
	}

	return res;
}


/*! Adds the directory 'dirname' from the list of watched
	directories.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int 
	DirectoriesToWatchAdd(
	PUNICODE_STRING dirname)
{


	if (DriverInfoData->PathsToMonitorCount == MAX_MONITOR_PATH_COUNT-1)
	{
		DBGPRINT("[flmonflt] Directory list full.\n");

		return FLD_LIST_FULL;
	}

	if (DirectoryToWatchExists(dirname))
	{
		DBGPRINT_ARG1("[flmonflt] Directory exists: %wZ\n", dirname);

		return FLD_ALREADY_IN_LIST;
	}

	RtlCopyUnicodeString(
		&DriverInfoData->PathsToMonitor[DriverInfoData->PathsToMonitorCount++],
		dirname);

	DBGPRINT_ARG1("[flmonflt] Directory added: %wZ\n", dirname);
	DBGPRINT_ARG1("[flmonflt] Directory list count: %d\n", DriverInfoData->PathsToMonitorCount);

	return FLD_SUCCESS;
}

/*! Clears the directory list.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
void
	DirectoriesToWatchClear(BOOLEAN freepool)
{
	unsigned int i;


	DBGPRINT("[flmonflt] Clearing directory list.\n");

	if (freepool)
	{
		for (i=0; i < MAX_MONITOR_PATH_COUNT; i++)
		{
			ExFreePool(DriverInfoData->PathsToMonitor[i].Buffer);
		}
	}

	DriverInfoData->PathsToMonitorCount = 0;


	DBGPRINT("[flmonflt] Directory list cleared.\n");
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int
	DirectoriesToWatchPathExistCount(PUNICODE_STRING path)
{
	unsigned int ret = 0;
	unsigned int i = 0;

	for (i=0; i < DriverInfoData->PathsToMonitorCount; i++)
	{

		if (UcFilenameStartsWith(&DriverInfoData->PathsToMonitor[i],
			path))
		{	
			ret++;	
		}
	}

	return ret;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN
	DirectoriesToWatchPathExist(PUNICODE_STRING path)
{
	unsigned int i = 0;


	for (i=0; i < DriverInfoData->PathsToMonitorCount; i++)
	{

		if (UcFilenameStartsWith(&DriverInfoData->PathsToMonitor[i],
			path))
		{	
			return TRUE;
		}
	}

	return FALSE;
}