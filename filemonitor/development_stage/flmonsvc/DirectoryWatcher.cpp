
#include "windows.h"
#include <string>
#include <iostream>
#include "FsLogFile.h"
#include "FileWriter.h"
#include "DirectoryWatcher.h"


#define MAX_ERR			8


using namespace std;


DirectoryWatcher::DirectoryWatcher(string dir)
{
	m_DirectoryHandle = CreateFile(
		dir.c_str(), 
		FILE_LIST_DIRECTORY,
		FILE_SHARE_READ|FILE_SHARE_DELETE|FILE_SHARE_WRITE, 
		NULL, 
		OPEN_EXISTING,
		FILE_FLAG_BACKUP_SEMANTICS,
		NULL 
		);


	m_pBuffer = new char[2048];
	memset(m_pBuffer, 0, 2048);

	m_ThreadEndEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
}

DirectoryWatcher::~DirectoryWatcher()
{
	Stop();

	CloseHandle(m_ThreadEndEvent);
	delete m_pBuffer;
}


unsigned long DirectoryWatcher::Run(void * arg)
{

	if (m_DirectoryHandle == INVALID_HANDLE_VALUE)
	{

		return 1;
	}
	
	DirectoryWatcher * dw = (DirectoryWatcher*)arg;
	DWORD r = 0;
	unsigned int err = 0;


	while(1)
	{

		BOOL b = ReadDirectoryChangesW(dw->m_DirectoryHandle,
			dw->m_pBuffer,
			2048,
			FALSE,
			m_NotifyFilter,
			&r,
			NULL,
			NULL);

		if (!b)
		{
			err++;

			if (err == MAX_ERR)
			{
				err = 0;
				break;
			}
		}
		else
		{
			PFILE_NOTIFY_INFORMATION fni = (PFILE_NOTIFY_INFORMATION)m_pBuffer;
			dw->ProcessResult(fni);
			memset(dw->m_pBuffer, 0, 2048);
		}

	}

	SetEvent(dw->m_ThreadEndEvent);


	return 0;
}

void DirectoryWatcher::Stop()
{
	DWORD r;

	CancelSynchronousIo(GetThreadHandle());

	r = WaitForSingleObject(m_ThreadEndEvent, 3000);

	if (r == WAIT_TIMEOUT)
	{
		cout << "Timeout." << endl;
	}


	if (m_DirectoryHandle != INVALID_HANDLE_VALUE)
	{
		CloseHandle(m_DirectoryHandle);
		m_DirectoryHandle = INVALID_HANDLE_VALUE;
	}

	ExitThread();
}



