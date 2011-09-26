
#ifndef __FLOPENCLOSE_TRACKER_H__
#define __FLOPENCLOSE_TRACKER_H__



#include <fltKernel.h>
#include "FileMon.h"
#include "FlMutex.h"
#include "fldebug.h"



#define FLTMESSAGE_BUFFERSIZE				1024
#define FLTMESSAGE_TIMEOUT					3000 // timeout in ms


#define DIRTRACKER_SUCCESS						0
#define DIRTRACKER_ALLOC_ERROR					1
#define DIRTRACKER_WRONG_PARAMETER				2
#define DIRTRACKER_EMPTY						3
#define DIRTRACKER_ENTRY_NOTFOUND				4







//-------------------------------------------------------------------------


BOOLEAN 
	FlTrackerInit(POCTRACKER tracker);

unsigned int
	FlTrackerAddEntry(	POCTRACKER tracker,
				OcTrackerType type,
				PUNICODE_STRING FilePath);

unsigned int
	FlTrackerRemoveEntry(	POCTRACKER tracker,
					OcTrackerType type,
					PUNICODE_STRING FilePath);



POCTRACKER_ENTRY 
	FlTrackerGetLastEntry(	POCTRACKER tracker,
							PUNICODE_STRING similar_path,
							PBOOLEAN string_match);


unsigned int
	FlTrackerClean(POCTRACKER tracker);

void 
	FlTrackerPrintListDbg(POCTRACKER tracker);

unsigned int 
	FlTrackerCount(POCTRACKER tracker);


unsigned int
	FlTrackerInfoToUserspace(PFLDRIVERDATA driver);

BOOLEAN
	FlTrackerSetRead(	POCTRACKER tracker,
						PUNICODE_STRING FilePath);

BOOLEAN 
	FlTrackerEntryValid(POCTRACKER_ENTRY entry);

VOID
	FlTrackerSetLastEntry(PFLDRIVERDATA driver);

#endif
