

#ifndef __FILEMON_H__
#define __FILEMON_H__


#include <fltKernel.h>
#include <ntifs.h>



#define MAX_MONITOR_PATH_COUNT			256
#define FLPORTNAME						L"\\FlFileMonitor"
#define CLOSE_WAIT_TIMEOUT				300 //ms



#define ABSOLUTE(wait) (wait)

#define RELATIVE(wait) (-(wait))

#define NANOSECONDS(nanos)   \
	(((signed __int64)(nanos)) / 100L)

#define MICROSECONDS(micros) \
	(((signed __int64)(micros)) * NANOSECONDS(1000L))

#define MILLISECONDS(milli)  \
	(((signed __int64)(milli)) * MICROSECONDS(1000L))

#define SECONDS(seconds)         \
	(((signed __int64)(seconds)) * MILLISECONDS(1000L))



#define MAX_PATH_LEN						1024

#define OCTRACKER_TYPE_ANY					0
#define OCTRACKER_TYPE_DIR					1
#define OCTRACKER_TYPE_FILE					2


typedef unsigned short OcTrackerType;

//-------------------------------------------------------------------------
// Structures
//-------------------------------------------------------------------------

typedef struct _OCTRACKER_ENTRY
{
	OcTrackerType				Type; // file or dir
	UNICODE_STRING				FilePath; // file or dir path
	unsigned int				Count; // MJ_CREATE count (dirs only)
	unsigned int				WasRead; // if a read happend this is 1 (files only)

	struct _OCTRACKER_ENTRY * 	NextEntry; // pointer to next list entry

} OCTRACKER_ENTRY, *POCTRACKER_ENTRY;


typedef struct _OCTRACKER
{
	POCTRACKER_ENTRY 			FirstEntry;
	unsigned int				Count;
	KMUTEX						Mutex;			

} OCTRACKER, *POCTRACKER;

//-------------------------------------------------------------------------
typedef struct _FLDRIVERDATA
{
	// our driver
	//
	PDRIVER_OBJECT		DriverObject;
	PFLT_FILTER			Filter;

	// paths to monitor
	//
	unsigned int		PathsToMonitorCount;
	UNICODE_STRING		PathsToMonitor[MAX_MONITOR_PATH_COUNT];

	// userspace communication
	//
	PFLT_PORT			ServerPort;
	//PEPROCESS			UserProcess;
	PFLT_PORT			ClientPort;

	// timer thread
	//
	PKTHREAD			TimerThread;
	PKEVENT				TimerThreadSleepWakeup;
	PKTIMER				Timer;
	LARGE_INTEGER		TimerDueTime;

	// tracker
	//
	POCTRACKER			Tracker;
	UNICODE_STRING		TrackerLastPath;
	//BOOLEAN				TrackerLastEntryValid;


} FLDRIVERDATA, *PFLDRIVERDATA;


extern PFLDRIVERDATA DriverInfoData;




#endif
