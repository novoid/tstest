
#include <windows.h>
#include <stdio.h>
#include "../flmon/FlMonIPC.h"
#include "logfile.h"
#include "filewriter.h"


//#define COMPILE_AS_SERVICE

#define SERVICE_NAME		L"FileMonitorWatcher"
#define SLEEP_TIME			100


SERVICE_STATUS          ServiceStatus; 
SERVICE_STATUS_HANDLE   hStatus; 

void  ServiceMain(int argc, char** argv); 
DWORD InitService();
DWORD StopService();
void  ControlHandler(DWORD request); 
void SetServiceState(DWORD state, DWORD exitcode);
int ServiceRoutine();



void main() 
{ 

	DWORD r = 0;

#ifdef _DEBUG

	FSLOGFILE logfile;
//	char * logfilename;
	char tswfilename[FW_MAX_FILENAME_LENGTH];

#endif


#ifdef COMPILE_AS_SERVICE

	SERVICE_TABLE_ENTRY ServiceTable[2];
	ServiceTable[0].lpServiceName = SERVICE_NAME;
	ServiceTable[0].lpServiceProc = (LPSERVICE_MAIN_FUNCTION)ServiceMain;

	ServiceTable[1].lpServiceName = NULL;
	ServiceTable[1].lpServiceProc = NULL;

	// Start the control dispatcher thread for our service
	StartServiceCtrlDispatcher(ServiceTable);  

#else

#ifdef _DEBUG
	/*
	logfilename = "tswservice.log";
	FsLogfileInit(&logfile, logfilename);

	r = FsLogfileOpen(&logfile);

	if (r)
	{
		printf("FAILED (%d) to open logfile '%s'.\n", r, logfilename);
		return;
	}

	r = FsLogfileWrite(&logfile, "Das ist ein Test\n", TRUE, " * ");

	if (r)
	{
		printf("FAILED (%d) to write to logfile '%s'.\n", r, logfilename);
		return;
	}

	FsLogfileClose(&logfile);
	*/

	tswfilename[0] = '\0';
	FwGetCurrentFilename(tswfilename);

#endif


	InitService();
	r = ServiceRoutine();

	if (!r)
	{
		printf("Tagstore Filemonitoring Watcher Service started in command line mode.\n");
	}
	else
	{
		printf("FAILED(%d) to start Tagstore Filemonitoring Watcher Service in command line mode.\n",
		r);
	}


#endif

}


void SetServiceState(DWORD state, DWORD exitcode)
{
	ServiceStatus.dwCurrentState = state; 
	ServiceStatus.dwWin32ExitCode = exitcode; 
	SetServiceStatus(hStatus, &ServiceStatus); 
}


DWORD InitService()
{
	
	return 0;
}

DWORD StopService()
{

	return 0;
}



void ServiceMain(int argc, char** argv)
{
	DWORD r = 0;

	ServiceStatus.dwServiceType = 
		SERVICE_WIN32; 
	ServiceStatus.dwCurrentState = 
		SERVICE_START_PENDING; 
	ServiceStatus.dwControlsAccepted   = 
		SERVICE_ACCEPT_STOP | 
		SERVICE_ACCEPT_SHUTDOWN;
	ServiceStatus.dwWin32ExitCode = 0; 
	ServiceStatus.dwServiceSpecificExitCode = 0; 
	ServiceStatus.dwCheckPoint = 0; 
	ServiceStatus.dwWaitHint = 0; 

	hStatus = RegisterServiceCtrlHandler(
		SERVICE_NAME, 
		(LPHANDLER_FUNCTION)ControlHandler); 

	if (hStatus == (SERVICE_STATUS_HANDLE)0) 
	{ 
		// Registering Control Handler failed
		return; 
	} 



	r = InitService();

	if (!r)
	{
		SetServiceState(SERVICE_RUNNING, r);
	}
	else
	{
		SetServiceState(SERVICE_STOPPED, r);
		return;
	}


	r = ServiceRoutine();

	SetServiceState(SERVICE_STOPPED, r);
}

void ControlHandler(DWORD request)
{
	DWORD r = 0;

	switch(request) 
	{ 

	case SERVICE_CONTROL_STOP: 
		
		r = StopService();

		if (!r)
			SetServiceState(SERVICE_STOPPED, 0);
		else
			SetServiceState(SERVICE_RUNNING, r);


	case SERVICE_CONTROL_SHUTDOWN: 

		r = StopService();

		if (!r)
			SetServiceState(SERVICE_STOPPED, 0);
		else
			SetServiceState(SERVICE_RUNNING, r);

		return; 

	default:
		break;
	} 

	// Report current status
	SetServiceStatus (hStatus, &ServiceStatus);
}


int ServiceRoutine()
{
	while (ServiceStatus.dwCurrentState == SERVICE_RUNNING)
	{
		
		Sleep(SLEEP_TIME);
	}

	return 0;
}

