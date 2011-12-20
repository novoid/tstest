
#ifndef __REGISTRYWINDOWS_H__
#define __REGISTRYWINDOWS_H__



#include <Windows.h>
#include <string>



#define FREG_SUCCESS					0
#define FREG_GET_FAILED					1
#define FREG_SET_FAILED					2
#define FREG_OPEN_FAILED				3



#define FREG_LASTFILENAME		"LastFilename"
#define FREG_TS_CONFIGFILE		"TagstoreConfigFile"
#define FREG_EVENTLOGFILE		"EventLogfile"
#define FREG_EVENTLOGPATH		"EventLogPath"


using namespace std;


class RegistryWindows
{

private:

	HKEY mRegKeyService;


public:
	RegistryWindows();
	~RegistryWindows();

	unsigned int Open();
	void Close();
	unsigned int Read(string name, string& val);
	unsigned int Update(string name, string val);

};

#endif

