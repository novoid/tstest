
#include <Windows.h>
#include "FlTest.h"



FlTest::FlTest()
{
}


FlTest::~FlTest()
{
}

bool FlTest::OpenDirectory(WCHAR * path, bool closehandle)
{
	HANDLE hFile = INVALID_HANDLE_VALUE;
	
	hFile = CreateFile(
		path, 
		GENERIC_READ, 
		FILE_SHARE_READ,
		0, 
		OPEN_EXISTING, 
		FILE_FLAG_BACKUP_SEMANTICS, 
		0);

	if( hFile != INVALID_HANDLE_VALUE )
	{
		return false;
	}
	else
	{
		if (closehandle)
			CloseHandle(hFile);

		return true;
	}
}