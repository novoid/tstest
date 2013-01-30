/*++

Copyright (C) ruwido austria gmbH, 2008

Module Name:

    NetworkThread.h {v1.00}

Abstract:

	

Author:

    Florian Praxmair (florian.praxmair@ruwido.com) 04/21/2008

Revision History:

--*/

#ifndef __THREADWRAPPER_H__
#define __THREADWRAPPER_H__



#include <windows.h>



class ThreadWrapper
{

private:

	HANDLE		m_ThreadHandle;
	long long	m_lThreadID;
	int			m_iExitCode;


public:

	ThreadWrapper();
	virtual ~ThreadWrapper();


	long long GetThreadID() { return m_lThreadID; }
	HANDLE GetThreadHandle() { return m_ThreadHandle; }

	void * GetThreadParam() { return m_pThreadParam; }
	void SetThreadParam(void * pParam) { m_pThreadParam = pParam; }

	virtual bool Start();


	void Suspend();
	void Resume();
	void Sleep(long ms);

	void ExitThread();


protected:

	void *		m_pThreadParam;
	int			m_ThreadExitState;

	virtual unsigned long Run(void * arg) = 0;

private:

	static DWORD  WINAPI StartThread(LPVOID  pvThread);


};



#endif