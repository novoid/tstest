
#include <windows.h>
#include <stdio.h>
#include <string>
#include <sstream>
#include "../flmon/FlMonIPC.h"
#include "fslogfile.h"
#include "TagstoreConfig.h"
#include "RegistryWindows.h"
#include "filewriter.h"




#define DEFAULT_LOGPATH			"C:\\twservice.log"
#define DEFAULT_EVENTLOGPATH	"C:\\"
#define SERVICE_NAME			L"FileMonitorWatcher"
#define SLEEP_TIME				100


#define FLSV_SUCCESS						0
#define FLSV_INIT_FAILED					0x100
#define FLSV_OPEN_FAILED					0x101
#define FLSV_OPEN_LOG_FAILED				0x102
#define FLSV_REGISTRYKEY_OPEN_FAILED		0x103



SERVICE_STATUS          ServiceStatus;
SERVICE_STATUS_HANDLE   hStatus;

FLMONIPC				mIPC;
RegistryWindows			wRegistry;



void  ServiceMain(int argc, char** argv); 
DWORD InitService();
DWORD StopService();
void  ControlHandler(DWORD request); 
void SetServiceState(DWORD state, DWORD exitcode);
int ServiceRoutine();



void main() 
{ 
	
	SERVICE_TABLE_ENTRY ServiceTable[2];
	ServiceTable[0].lpServiceName = (LPSTR)SERVICE_NAME;
	ServiceTable[0].lpServiceProc = (LPSERVICE_MAIN_FUNCTION)ServiceMain;

	ServiceTable[1].lpServiceName = NULL;
	ServiceTable[1].lpServiceProc = NULL;

	// Start the control dispatcher thread for our service
	StartServiceCtrlDispatcher(ServiceTable);  

}


void SetServiceState(DWORD state, DWORD exitcode)
{
	ServiceStatus.dwCurrentState = state; 
	ServiceStatus.dwWin32ExitCode = exitcode; 
	SetServiceStatus(hStatus, &ServiceStatus); 
}


DWORD InitService()
{
	SingeltonLogfile::Instance()->Write("Initializing service.\n");

	if (!FlMonIPCInit(&mIPC))
	{
		SingeltonLogfile::Instance()->Write("Unable to initialize driver communication module.\n");
		return FLSV_INIT_FAILED;
	}

	if (!FlMonIPCOpen(&mIPC))
	{
		SingeltonLogfile::Instance()->Write("Unable to open driver communication, maybe driver not loaded.\n");
		return FLSV_OPEN_FAILED;
	}


	SingeltonTSConfig::Instance()->SetIPC(&mIPC);
	SingeltonTSConfig::Instance()->UpdateDriverStores();
	//SingeltonTSConfig::Instance()->RestartDeleteWatchers();
	

	

	return FLSV_SUCCESS;
}

DWORD StopService()
{
	SingeltonLogfile::Instance()->Write("Cleanup in progress.\n");

	FlMonIPCClose(&mIPC);


	delete SingeltonTSConfig::Instance();
	delete SingeltonFileWriter::Instance();

	return 0;
}



void ServiceMain(int argc, char** argv)
{
	DWORD r = 0;



	ServiceStatus.dwServiceType = 
		SERVICE_WIN32; 
	ServiceStatus.dwCurrentState = 
		SERVICE_STOPPED; 
	ServiceStatus.dwControlsAccepted   = 
		SERVICE_ACCEPT_STOP | 
		SERVICE_ACCEPT_SHUTDOWN;
	ServiceStatus.dwWin32ExitCode = 0; 
	ServiceStatus.dwServiceSpecificExitCode = 0; 
	ServiceStatus.dwCheckPoint = 0; 
	ServiceStatus.dwWaitHint = 0; 

	hStatus = RegisterServiceCtrlHandler(
		(LPCSTR)SERVICE_NAME, 
		(LPHANDLER_FUNCTION)ControlHandler); 



	if (hStatus == (SERVICE_STATUS_HANDLE)0) 
	{ 
		// Registering Control Handler failed
		SetServiceState(SERVICE_STOPPED, 1);

		SingeltonLogfile::Instance()->Open(DEFAULT_LOGPATH);
		SingeltonLogfile::Instance()->Write("Service stopped, service handler registration failed.\n");
		SingeltonLogfile::Instance()->Close();

		delete SingeltonLogfile::Instance();

		return; 
	} 



	string tscfname;
	string logfname = DEFAULT_LOGPATH;
	string logbasepath = DEFAULT_EVENTLOGPATH;
	

	if (wRegistry.Open())
	{
		SingeltonLogfile::Instance()->Open(DEFAULT_LOGPATH);
		SingeltonLogfile::Instance()->Write("Exiting, unable to open registry path.\n");
		SingeltonLogfile::Instance()->Close();

		delete SingeltonLogfile::Instance();

		ServiceStatus.dwWin32ExitCode = FLSV_REGISTRYKEY_OPEN_FAILED;
		SetServiceState(SERVICE_STOPPED, FLSV_REGISTRYKEY_OPEN_FAILED);

		return;
	}
	

	if (wRegistry.Read(FREG_EVENTLOGFILE, logfname))
	{		
		SingeltonLogfile::Instance()->Open(DEFAULT_LOGPATH);
		SingeltonLogfile::Instance()->Write("Exiting, unable to read eventlogfile path.\n");
		SingeltonLogfile::Instance()->Close();

		delete SingeltonLogfile::Instance();

		ServiceStatus.dwWin32ExitCode = FLSV_REGISTRYKEY_OPEN_FAILED;
		SetServiceState(SERVICE_STOPPED, FLSV_REGISTRYKEY_OPEN_FAILED);

		return;
	}

	if (wRegistry.Read(FREG_EVENTLOGPATH, logbasepath))
	{		
		SingeltonLogfile::Instance()->Open(DEFAULT_LOGPATH);
		SingeltonLogfile::Instance()->Write("Exiting, unable to read eventlogBASEpath.\n");
		SingeltonLogfile::Instance()->Close();

		delete SingeltonLogfile::Instance();

		ServiceStatus.dwWin32ExitCode = FLSV_REGISTRYKEY_OPEN_FAILED;
		SetServiceState(SERVICE_STOPPED, FLSV_REGISTRYKEY_OPEN_FAILED);

		return;
	}

	
	if (wRegistry.Read(FREG_TS_CONFIGFILE, tscfname))
	{
		SingeltonLogfile::Instance()->Open(DEFAULT_LOGPATH);
		SingeltonLogfile::Instance()->Write("Exiting, unable to read config file path.\n");
		SingeltonLogfile::Instance()->Close();

		delete SingeltonLogfile::Instance();

		ServiceStatus.dwWin32ExitCode = FLSV_REGISTRYKEY_OPEN_FAILED;
		SetServiceState(SERVICE_STOPPED, FLSV_REGISTRYKEY_OPEN_FAILED);
		return;
	}
	
	wRegistry.Close();


	if (SingeltonLogfile::Instance()->Open(logfname))
	{
		SingeltonLogfile::Instance()->Write("Exiting, unable to open logfile.\n");
		SingeltonLogfile::Instance()->Close();

		delete SingeltonLogfile::Instance();

		ServiceStatus.dwWin32ExitCode = FLSV_OPEN_LOG_FAILED;
		SetServiceState(SERVICE_STOPPED, FLSV_OPEN_LOG_FAILED);
		return;
	}




	SingeltonLogfile::Instance()->Write("Starting Tagstore Filesystem Watcher Service.\n");
	SingeltonLogfile::Instance()->WriteS("Setting base logging path to: ", logbasepath);
	SingeltonLogfile::Instance()->Write("---------------------------------------------\n");
	FileWriter::SetBaseDirectory(logbasepath);
	SingeltonTSConfig::Instance()->SetFilename(tscfname);

	r = InitService();

	if (!r)
	{
		ServiceStatus.dwWin32ExitCode = 0;
		SetServiceState(SERVICE_RUNNING, 0);
		SingeltonLogfile::Instance()->Write("Service started.\n");
	}
	else
	{
		ServiceStatus.dwWin32ExitCode = r;
		SetServiceState(SERVICE_STOPPED, r);
		SingeltonLogfile::Instance()->Write("Service stopped.\n");
		SingeltonLogfile::Instance()->Close();
		return;
	}


	ServiceRoutine();


	SetServiceState(SERVICE_STOPPED, r);

}

void ControlHandler(DWORD request)
{
	DWORD r = 0;

	switch(request)
	{ 

	case SERVICE_CONTROL_STOP: 
		
		SetServiceState(SERVICE_STOPPED, 0);

		StopService();

		SingeltonLogfile::Instance()->Write("Service stopped on SERVICE_CONTROL_STOP.\n");
		SingeltonLogfile::Instance()->Close();
		delete SingeltonLogfile::Instance();

		break;
	
	case SERVICE_CONTROL_SHUTDOWN: 

		SetServiceState(SERVICE_STOPPED, 0);	
		StopService();

		SingeltonLogfile::Instance()->Write("Service stopped on SERVICE_CONTROL_SHUTDOWN.\n");
		SingeltonLogfile::Instance()->Close();
		delete SingeltonLogfile::Instance();

		break;
	
	default:
		break;
	} 

	// Report current status
	SetServiceStatus (hStatus, &ServiceStatus);
}


int ServiceRoutine()
{
	unsigned char type = 0;
	char message[FLMESSAGE_MAXSIZE];
	unsigned int msg_size = 0;
	unsigned int r = 0;
	unsigned int lastr = 0;
	unsigned int errcnt = 1;


	message[0] = '\0';
	


	while (ServiceStatus.dwCurrentState == SERVICE_RUNNING)
	{
		SingeltonLogfile::Instance()->Write("Waiting for message...\n");

		r = FlMonIPCReadMessage(&mIPC, &type, message, FLMESSAGE_MAXSIZE, &msg_size);

		if (r == FLTIPC_PORT_CLOSED)
		{
			FlMonIPCClose(&mIPC);
			SingeltonLogfile::Instance()->Write("Driver communication closed.\n");
			break;
		}
		else if (r && r != lastr) // error
		{
			SingeltonLogfile::Instance()->Write1(
				"Error reading driver data, error may be occured more often. Code: ", r);

			SingeltonLogfile::Instance()->Write1(
				"Last error occurence: ", errcnt);

			errcnt = 1;
		}
		else if (!r)
		{
			string strmessage(message);
			SingeltonFileWriter::Instance()->Write(type, strmessage);
			errcnt = 1;
		}
		else // r == lastr
		{
			errcnt++;
		}

		lastr = r;

		Sleep(SLEEP_TIME);
	}


	SingeltonLogfile::Instance()->Write("Exiting main service routine.\n");

	return FLSV_SUCCESS;
}

