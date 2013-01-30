
#include <Windows.h>
#include <string>
#include "FsLogFile.h"
#include "RegistryWindows.h"


#define FW_REGSUBKEY			"SOFTWARE\\TUGRAZ\\FileSystemWatcher"


using namespace std;


RegistryWindows::RegistryWindows()
{
	mRegKeyService = 0;
}


RegistryWindows::~RegistryWindows()
{
	Close();
}


unsigned int RegistryWindows::Open()
{
	LONG status = ERROR_SUCCESS;


	status = RegCreateKey(HKEY_LOCAL_MACHINE, //HKEY_CURRENT_USER,
		FW_REGSUBKEY,
		&mRegKeyService);

	if (status != ERROR_SUCCESS)
	{
		return FREG_OPEN_FAILED;
	}


	return FREG_SUCCESS;
}

void RegistryWindows::Close()
{
	RegCloseKey(mRegKeyService);
}

unsigned int RegistryWindows::Read(string name, string& val)
{
	LONG status = ERROR_SUCCESS;
	DWORD size = 0;
	char regval[2048];
	DWORD s = 2048;


	status = RegQueryValueEx(mRegKeyService,
		name.c_str(),
		0,
		0,
		(LPBYTE)regval,
		&s);

	if (status != ERROR_SUCCESS)
	{
		SingeltonLogfile::Instance()->Write1("Error reading registry key. Code: ", status);
		return FREG_GET_FAILED;
	}

	val = string(regval);

	return FREG_SUCCESS;
}

unsigned int RegistryWindows::Update(string name, string val)
{
	LONG status = ERROR_SUCCESS;
	DWORD size = 0;
	char regval[2048];


	size = val.size();
	memcpy(regval, val.c_str(), size);
	regval[size] = '\0';

	SingeltonLogfile::Instance()->Write("Updating regvalue.\n");

	status = RegSetValueEx(mRegKeyService,
		name.c_str(),
		0,
		REG_SZ,
		(BYTE*)regval,
		size);

	if (status != ERROR_SUCCESS)
	{
		SingeltonLogfile::Instance()->Write1("Error writing registry key. Code: ", status);
		return FREG_SET_FAILED;
	}


	return FREG_SUCCESS;
}

