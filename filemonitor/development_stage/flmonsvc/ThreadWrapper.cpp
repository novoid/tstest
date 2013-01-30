/*++

Copyright (C) ruwido austria gmbH, 2008

Module Name:

    NetworkThread.cpp {v1.00}

Abstract:

	

Author:

    Florian Praxmair (florian.praxmair@ruwido.com) 04/21/2008

Revision History:

--*/


#include <iostream>
#include "ThreadWrapper.h"


using namespace std;


ThreadWrapper::ThreadWrapper()
{
	m_lThreadID = -1;
	m_ThreadHandle = NULL;;
	m_pThreadParam = NULL;
}

ThreadWrapper::~ThreadWrapper()
{
	ExitThread();
}



bool ThreadWrapper::Start()
{
	m_ThreadHandle = CreateThread(NULL, NULL, &ThreadWrapper::StartThread, (LPVOID)this, NULL, (LPDWORD) &m_lThreadID);

	return (m_ThreadHandle != INVALID_HANDLE_VALUE);
}

void ThreadWrapper::ExitThread()
{
	if (m_ThreadHandle)
		CloseHandle(m_ThreadHandle);

	m_ThreadHandle = NULL;
}

void ThreadWrapper::Sleep(long ms)
{
	::Sleep(ms);
}

void ThreadWrapper::Suspend()
{
	::SuspendThread(m_ThreadHandle);
}

void ThreadWrapper::Resume()
{
	::ResumeThread(m_ThreadHandle);
}

DWORD WINAPI ThreadWrapper::StartThread(LPVOID pArgs)
{
	ThreadWrapper * pNwStart = (ThreadWrapper*)pArgs;

	unsigned long lRes = pNwStart->Run(pNwStart->m_pThreadParam);

	// don't write anything to memory here because thread may already 
	// be ended and it's data and members already deleted

	return lRes;
}
