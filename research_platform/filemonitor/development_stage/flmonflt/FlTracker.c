

#include <fltKernel.h>
#include "FileMon.h"
#include "fldebug.h"
#include "FlMutex.h"
#include "FlUcFilename.h"
#include "FlTracker.h"



/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN 
	FlTrackerInit(POCTRACKER tracker)
{

	if (!tracker)
	{
		DBGPRINT("[flmonflt] Tracker init failed.\n");
		return FALSE;
	}

	tracker->FirstEntry = NULL;
	tracker->Count = 0;
	KMutexInit(&tracker->Mutex);

	DBGPRINT("[flmonflt] Tracker initialized.\n");

	return TRUE;
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
	FlTrackerAddEntry(	POCTRACKER tracker,
						OcTrackerType type,
						PUNICODE_STRING FilePath)
{

	POCTRACKER_ENTRY	entry;
	POCTRACKER_ENTRY	last;
	NTSTATUS			status;
	BOOLEAN				string_match;



	

	if (!tracker)
	{
		DBGPRINT_ARG1("[flmonflt] Tracker adderr, invalid param ('%wZ').\n", FilePath);

		return DIRTRACKER_WRONG_PARAMETER;
	}


	// check if last entry is similar
	//if (DriverInfoData->TrackerLastEntryValid)
	//{
		if (UcFilenameEqual(&DriverInfoData->TrackerLastPath,
			FilePath))
		{
			DBGPRINT_ARG1("[flmonflt] Tracker path was last added: '%wZ'.\n", FilePath);
			return DIRTRACKER_SUCCESS;
		}
	//}



	KMutexAquire(&tracker->Mutex);
	string_match = FALSE;
	last = FlTrackerGetLastEntry(tracker, FilePath, &string_match);

	if (string_match) // entry is already in list
	{
		DBGPRINT_ARG1("[flmonflt] Tracker add, entry already in list ('%wZ').\n", 
			FilePath);

		last->Count++;
		

		KMutexRelease(&tracker->Mutex);

		return DIRTRACKER_SUCCESS;
	}

	// create tracker entry
	//
	entry = ExAllocatePool(NonPagedPool, sizeof(OCTRACKER_ENTRY));

	if (!entry)
	{
		DBGPRINT_ARG1("[flmonflt] Tracker add, entry alloc error ('%wZ').\n", 
			FilePath);
		ExFreePool(entry);

		KMutexRelease(&tracker->Mutex);
		return DIRTRACKER_ALLOC_ERROR;
	}

	entry->NextEntry = NULL;
	entry->WasRead = FALSE;
	entry->Count = 1;
	entry->Type = type;
	status = UcFilenameInit(&entry->FilePath);

	if (!NT_SUCCESS(status))
	{
		DBGPRINT_ARG1("[flmonflt] Tracker add, str init err ('%wZ').\n", FilePath);
		ExFreePool(entry);

		KMutexRelease(&tracker->Mutex);
		return DIRTRACKER_ALLOC_ERROR;
	}

	UcFilenameCopy(FilePath, &entry->FilePath);

	if (!last) // tracker is empty
	{
		DBGPRINT_ARG1("[flmonflt] Tracker add, tracker empty adding first in list ('%wZ').\n", 
			FilePath);

		tracker->FirstEntry = entry;
		tracker->Count = 1;
	}
	else // append to list
	{
		DBGPRINT_ARG1("[flmonflt] Tracker add to list ('%wZ').\n", 
			FilePath);


		last->NextEntry = entry;
		tracker->Count++;
	}


	KMutexRelease(&tracker->Mutex);


	//DBGPRINT_ARG2("[flmonflt] Tracker added (0x%x, '%wZ').\n", ThreadID, FilePath);

	return DIRTRACKER_SUCCESS;
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
	FlTrackerRemoveEntry(	POCTRACKER tracker,
							OcTrackerType type,
							PUNICODE_STRING FilePath)
{
	POCTRACKER_ENTRY current;
	POCTRACKER_ENTRY prev;


	KMutexAquire(&tracker->Mutex);

	if (!tracker)
	{
		DBGPRINT_ARG1("[flmonflt] Tracker remove err, wrong param ('%wZ').\n", FilePath);
		KMutexRelease(&tracker->Mutex);
		return DIRTRACKER_WRONG_PARAMETER;
	}

	if (!tracker->FirstEntry)
	{
		DBGPRINT_ARG1("[flmonflt] Tracker remove err, empty ('%wZ').\n", FilePath);
		KMutexRelease(&tracker->Mutex);

		return DIRTRACKER_EMPTY;
	}

	current = tracker->FirstEntry;
	prev = NULL;

	

	do
	{

		if ((type == OCTRACKER_TYPE_ANY || current->Type == type)
			&& UcFilenameEqual(FilePath, &current->FilePath))
		{
			//
			// entry found, 
			//

			// see if current entry occures twice
			//
			if (current->Count > 1)
			{
				current->Count--;
				KMutexRelease(&tracker->Mutex);

				DBGPRINT_ARG1("[flmonflt] Tracker decremented ('%wZ').\n", 
					FilePath);

				return DIRTRACKER_SUCCESS;
			}

			// current is to delete
			//
			if (!prev) // current is first entry
			{
				tracker->FirstEntry = current->NextEntry;
				tracker->Count--;
			}
			else // current has a previous node
			{
				prev->NextEntry = current->NextEntry;
				tracker->Count--;
			}


			UcFilenameFree(&current->FilePath);
			ExFreePool(current);


			DBGPRINT_ARG1("[flmonflt] Tracker removed ('%wZ').\n", 
				FilePath);

			KMutexRelease(&tracker->Mutex);


			return DIRTRACKER_SUCCESS;
		}

		prev = current;
		current = current->NextEntry;
	}
	while(current);

	KMutexRelease(&tracker->Mutex);


	DBGPRINT_ARG1("[flmonflt] Tracker remove err, not found ('%wZ').\n", FilePath);

	return DIRTRACKER_ENTRY_NOTFOUND;
}




/*! Get the last entry in list.

	\param [in] similar_path If parameter is NULL it is ignored, if a pointer
				to a unicode_string is given the list is searched for the filename
				with that name. If no name is found, the last entry in the list
				is returned.
	\param [out]
	\param [in,out]

	\pre
	\post
	\return NULL if list is empty
			!NULL if entry was found (string_match=1, if string was found)

*/
POCTRACKER_ENTRY 
	FlTrackerGetLastEntry(	POCTRACKER tracker, 
							PUNICODE_STRING similar_path,
							PBOOLEAN string_match)
{

	POCTRACKER_ENTRY t;


	if (!tracker || tracker->Count > 100)
	{
		DBGBREAK;
	}

	*string_match = 0;

	if (!tracker->FirstEntry)
	{
		DBGPRINT("[flmonflt] FlDirTrackerGetLastEntry empty.\n");
		return NULL;
	}
	
	t = tracker->FirstEntry;

	do
	{
		if (similar_path && 
			UcFilenameEqual(&t->FilePath, similar_path))
		{
			*string_match = 1;
			return t;
		}

		if (!t->NextEntry)
			return t;

		t = t->NextEntry;
	}
	while(t);

	return t;
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
	FlTrackerClean(POCTRACKER tracker)
{
	POCTRACKER_ENTRY t;
	POCTRACKER_ENTRY tmp;
	unsigned int i = 0;


	KMutexAquire(&tracker->Mutex);

	if (!tracker->FirstEntry)
	{
		DBGPRINT("[flmonflt] Tracker empty in free.\n");
		tracker->Count = 0;
		KMutexRelease(&tracker->Mutex);
		return 0;
	}

	t = tracker->FirstEntry;

	while(t)
	{
		tmp = t;
		t = t->NextEntry;

		UcFilenameFree(&tmp->FilePath);
		ExFreePool(tmp);
		i++;
	}


	tracker->FirstEntry = NULL;
	tracker->Count = 0;

	KMutexRelease(&tracker->Mutex);

	DBGPRINT_ARG1("[flmonflt] Tracker %d entries removed in global free.\n", i);

	return i;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

	*/
void 
	FlTrackerPrintListDbg(POCTRACKER tracker)
{
	POCTRACKER_ENTRY t;
	unsigned int i = 0;


	KMutexAquire(&tracker->Mutex);
	DBGPRINT_ARG1("[flmonflt] Tracker dbgprint begin (%d entries) -----------\n",
		tracker->Count);

	if (!tracker->FirstEntry)
	{
		;
	}
	else
	{
		t = tracker->FirstEntry;

		while(t)
		{
			DBGPRINT_ARG1("[ftmonflt] %d: ", i);


			if (FlTrackerEntryValid(t))
				DBGPRINT("VALID ");
			else
				DBGPRINT("INVAL ");

			if (t->Type == OCTRACKER_TYPE_DIR)
				DBGPRINT("DIR");
			else
				DBGPRINT("FIL");
			

			DBGPRINT_ARG3(" Path: %wZ, Count: %d, Readstate: %d\n", 
				t->FilePath, t->Count, t->WasRead);

			t = t->NextEntry;
			i++;
		}
	}

	DBGPRINT_ARG1("[flmonflt] Tracker dbgprint end %d entries ------------\n", i);

	if (i > 1)
		DBGPRINT("[flmonflt] ERROR: Tracker had multiple entries ------------\n");

	KMutexRelease(&tracker->Mutex);
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
	FlTrackerCount(POCTRACKER tracker)
{
	unsigned int c = 0;

	KMutexAquire(&tracker->Mutex);
	c = tracker->Count;
	KMutexRelease(&tracker->Mutex);

	return c;
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
	FlTrackerInfoToUserspace(PFLDRIVERDATA driver)
{
	POCTRACKER_ENTRY t;
	unsigned int i = 0;
	NTSTATUS status;
	ANSI_STRING SendBuffer;
	LARGE_INTEGER Timeout;

	
	if (!driver->ClientPort)
	{
		DBGPRINT("[flmonflt::FlTrackerInfoToUserspace] FltSendMessage failed, client not connected.\n");
		FlTrackerClean(driver->Tracker);
		return 0;
	}

	status = STATUS_SUCCESS;
	Timeout.QuadPart = RELATIVE(MILLISECONDS(FLTMESSAGE_TIMEOUT));

	KMutexAquire(&driver->Tracker->Mutex);

	if (!driver->Tracker->FirstEntry)
	{
		DBGPRINT("[flmonflt::FlTrackerInfoToUserspace] Tracker empty in free.\n");
		KMutexRelease(&driver->Tracker->Mutex);
		return 0;
	}

	t = driver->Tracker->FirstEntry;

	while(t)
	{
		DBGPRINT("[flmonflt] Sending message.\n");

		RtlUnicodeStringToAnsiString(&SendBuffer, &t->FilePath, TRUE);


		DBGPRINT_ARG1("[flmonflt] DPC IRQL: 0x%x.\n", KeGetCurrentIrql());

		status = FltSendMessage(
			driver->Filter,
			&driver->ClientPort,
			SendBuffer.Buffer,
			SendBuffer.Length,
			NULL,
			0,
			&Timeout);

		RtlFreeAnsiString(&SendBuffer);

		if (status == STATUS_SUCCESS)
			DBGPRINT("[flmonflt] Send OK.\n");
		else if (status == STATUS_TIMEOUT)
			DBGPRINT("[flmonflt] Send timed out.\n");
		else 
			DBGPRINT_ARG1("[flmonflt] Send error %d.\n", status);
	

		t = t->NextEntry;
		i++;
	}


	//FlTrackerClean(driver->Tracker);

	KMutexRelease(&driver->Tracker->Mutex);
	
	DBGPRINT_ARG1("[flmonflt::FlTrackerInfoToUserspace] Tracker %d entries sent to userspace.\n", i);
	


	return i;
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
	FlTrackerSetRead(	POCTRACKER tracker,
						PUNICODE_STRING FilePath)
{
	POCTRACKER_ENTRY t;


	t = tracker->FirstEntry;

	while(t)
	{
		if (UcFilenameEqual(&t->FilePath, FilePath))
		{
			t->WasRead = TRUE;
			return TRUE;
		}

		t = t->NextEntry;
	}

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
BOOLEAN 
	FlTrackerEntryValid(POCTRACKER_ENTRY entry)
{
	if (!entry)
		return FALSE;

	if (entry->Type == OCTRACKER_TYPE_DIR)
		return TRUE;

	if (entry->Type == OCTRACKER_TYPE_FILE
		&& entry->WasRead)
		return TRUE;

	return FALSE;
}


VOID
	FlTrackerSetLastEntry(PFLDRIVERDATA driver)
{
	DBGPRINT_ARG1("[flmonflt] Copying to last entry cache. Trackercount: %d\n",
		driver->Tracker->Count);

	if (!driver->Tracker->Count)
		return;

	// set last added entry
	UcFilenameCopy(&driver->Tracker->FirstEntry->FilePath, 
		&DriverInfoData->TrackerLastPath);

}
