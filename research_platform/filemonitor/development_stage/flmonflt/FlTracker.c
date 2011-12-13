

#include <fltKernel.h>
#include "FileMon.h"
#include "FlIpc.h"
#include "fldebug.h"
#include "FlMutex.h"
#include "FlUcFilename.h"
#include "FlTracker.h"



#define MIN(a,b) ((a) > (b) ? (b) : (a))
#define FLMESSAGE_MAXSIZE			1024


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
		//DBGPRINT_ARG1("[flmonflt] Tracker adderr, invalid param ('%wZ').\n", FilePath);

		return DIRTRACKER_WRONG_PARAMETER;
	}




	KMutexAquire(&tracker->Mutex);
	string_match = FALSE;
	last = FlTrackerGetLastEntry(tracker, FilePath, &string_match);

	if (string_match) // entry is already in list
	{
		//DBGPRINT_ARG1("[flmonflt] Tracker add, entry already in list ('%wZ').\n", 
		//	FilePath);

		DBGPRINT_ARG1("[flmonflt] ++ '%wZ'\n", FilePath);

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
		//DBGPRINT_ARG1("[flmonflt] Tracker add, tracker empty adding first in list ('%wZ').\n", 
		//	FilePath);

		tracker->FirstEntry = entry;
		tracker->Count = 1;
	}
	else // append to list
	{
		//DBGPRINT_ARG1("[flmonflt] Tracker add to list ('%wZ').\n", 
		//	FilePath);

		last->NextEntry = entry;
		tracker->Count++;
	}


	DBGPRINT_ARG1("[flmonflt] -> '%wZ'\n", FilePath);

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

				DBGPRINT_ARG1("[flmonflt] -- '%wZ'\n", FilePath);


				KMutexRelease(&tracker->Mutex);

				//DBGPRINT_ARG1("[flmonflt] Tracker decremented ('%wZ').\n", 
				//	FilePath);

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


			//DBGPRINT_ARG1("[flmonflt] Tracker removed ('%wZ').\n", 
			//	FilePath);

			DBGPRINT_ARG1("[flmonflt] <- '%wZ'\n", FilePath);

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

	*string_match = FALSE;

	if (!tracker->FirstEntry)
	{
		//DBGPRINT("[flmonflt] FlDirTrackerGetLastEntry empty.\n");
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
void 
	FlTrackerEntryPrint(POCTRACKER_ENTRY t)
{
	if (!t)
	{
		DBGPRINT("[ftmonflt] Trackerentry NULL.\n");
		return;
	}

	DBGPRINT("[ftmonflt] ");

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
	FlTrackerInfoToUserspace(PFLDRIVERDATA driver, BOOLEAN onlybestone)
{
	POCTRACKER_ENTRY t;
	unsigned int i = 0;
	NTSTATUS status;
	ANSI_STRING AnsiBuffer;
	LARGE_INTEGER Timeout;
	char message[FLMESSAGE_MAXSIZE];
	char abuf[FLMESSAGE_MAXSIZE-1];

	
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

	if (onlybestone)
		t = FlTrackerGetBestEntry(driver->Tracker);
	else
		t = driver->Tracker->FirstEntry;

	while(t)
	{
		DBGPRINT("[flmonflt] Sending message.\n");

		AnsiBuffer.Length = 0;
		AnsiBuffer.MaximumLength = FLMESSAGE_MAXSIZE-1;
		AnsiBuffer.Buffer = abuf;
		AnsiBuffer.Buffer[0] = '\0';
		RtlUnicodeStringToAnsiString(&AnsiBuffer, &t->FilePath, FALSE);


		DBGPRINT_ARG2("[flmonflt] Converted to ansi(%d): '%s'\n", 
			AnsiBuffer.Length, AnsiBuffer.Buffer);

		// build message
		//
		if (t->Type == OCTRACKER_TYPE_DIR)
			message[0] = MESSAGE_NOTIFY_DIR;
		else if (t->Type == OCTRACKER_TYPE_FILE)
			message[0] = MESSAGE_NOTIFY_FILE;
		else
			message[0] = '\0';

		memcpy(message+1, AnsiBuffer.Buffer, 
			MIN(AnsiBuffer.Length+1, FLMESSAGE_MAXSIZE));
		message[AnsiBuffer.Length+2] = '\0'; // zero termination

		DBGPRINT_ARG2("[flmonflt] Memcopy(%d): '%s'\n", 
			MIN(AnsiBuffer.Length+1, FLMESSAGE_MAXSIZE), message+1);

		DBGPRINT_ARG1("[flmonflt] Notify Type: '%c'\n", message[0]); 

		// send message to userspace
		//
		status = FltSendMessage(
			driver->Filter,
			&driver->ClientPort,
			message,
			MIN(AnsiBuffer.Length+2, FLMESSAGE_MAXSIZE),
			NULL,
			0,
			&Timeout);

		

		if (status == STATUS_SUCCESS)
			DBGPRINT_ARG2("[flmonflt] Send OK: len=%d, '%s'\n", AnsiBuffer.Length+1, message+1);
		else if (status == STATUS_TIMEOUT)
			DBGPRINT("[flmonflt] Send timed out.\n");
		else 
			DBGPRINT_ARG1("[flmonflt] Send error %d.\n", status);
	

		if (onlybestone)
		{
			i = 1;
			break;
		}

		t = t->NextEntry;
		i++;
	}


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
			DBGPRINT_ARG1("[flmonflt] R %wZ\n", FilePath);
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

	if (!driver->Tracker->Count)
		return;

	// set last added entry
	UcFilenameCopy(&driver->Tracker->FirstEntry->FilePath, 
		&DriverInfoData->TrackerLastPath);

}


POCTRACKER_ENTRY
	FlTrackerEntryWithHighestCount(POCTRACKER tracker)
{

	POCTRACKER_ENTRY t = NULL;
	POCTRACKER_ENTRY r = NULL;
	int last_count = -1;

	KMutexAquire(&tracker->Mutex);


	if (!tracker)
	{
		KMutexRelease(&tracker->Mutex);
		return NULL;
	}

	if (!tracker->FirstEntry)
	{
		KMutexRelease(&tracker->Mutex);
		return NULL;
	}


	r = t = tracker->FirstEntry;
	last_count = t->Count;

	while(t->NextEntry)
	{
		if (t->Count > last_count)
		{
			last_count = t->Count;
			r = t;
		}

		t = t->NextEntry;
	}

	KMutexRelease(&tracker->Mutex);

	return r;
}


BOOLEAN 
	FlTrackerHasValidFiles(POCTRACKER tracker)
{
	POCTRACKER_ENTRY t;


	KMutexAquire(&tracker->Mutex);

	t = tracker->FirstEntry;

	while(t)
	{
		if (t->Type == OCTRACKER_TYPE_FILE
			&& t->WasRead >= 1)
		{
			KMutexRelease(&tracker->Mutex);
			return TRUE;
		}

		t = t->NextEntry;
	}

	KMutexRelease(&tracker->Mutex);
	return FALSE;
}



/*! Routine selects the best entry out of the result list after timeout.
	It parses the list entries and tracks the count of access trys for 
	the two types.
	If a file type exists it is returned, otherwise the directory with the 
	highest count is returned.

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
POCTRACKER_ENTRY
	FlTrackerGetBestEntry(POCTRACKER tracker)
{

	POCTRACKER_ENTRY t = NULL;
	POCTRACKER_ENTRY dir = NULL;
	POCTRACKER_ENTRY file = NULL;
	int last_dcount = -1;
	int last_fcount = -1;

	KMutexAquire(&tracker->Mutex);


	if (!tracker)
	{
		KMutexRelease(&tracker->Mutex);
		return NULL;
	}

	if (!tracker->FirstEntry)
	{
		KMutexRelease(&tracker->Mutex);
		return NULL;
	}


	dir = file = t = tracker->FirstEntry;

	if (t->Type == OCTRACKER_TYPE_DIR)
		last_dcount = t->Count;
	else
		last_fcount = t->Count;

	while(t->NextEntry)
	{
		if (t->Type == OCTRACKER_TYPE_DIR)
		{
			if (t->Count > last_dcount)
			{
				last_dcount = t->Count;
				dir = t;
			}
		}
		else if (t->Type == OCTRACKER_TYPE_FILE)
		{
			if (t->WasRead && t->Count > last_fcount)
			{
				file = t;
				last_fcount = t->Count;
			}
		}

		t = t->NextEntry;
	}

	KMutexRelease(&tracker->Mutex);

	if (file)
		return file;
	else
		return dir;
}
