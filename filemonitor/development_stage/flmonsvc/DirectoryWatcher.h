
#ifndef __DIRECTORYWATCHER_H__
#define __DIRECTORYWATCHER_H__


#include "windows.h"
#include "ThreadWrapper.h"
#include <string>
#include <vector>

using namespace std;


class DirectoryWatcher : private ThreadWrapper
{

private:

	HANDLE				m_ThreadEndEvent;
	HANDLE				m_DirectoryHandle;
	OVERLAPPED *		m_pOverlapped;
	char *				m_pBuffer;
	DWORD				m_NotifyFilter;


private:

	virtual void ProcessResult(PFILE_NOTIFY_INFORMATION fni) = 0;
	virtual unsigned long Run(void * arg);

public:

	DirectoryWatcher(string dir);
	~DirectoryWatcher();

	void SetNotifyFilter(DWORD nf) { m_NotifyFilter = nf; }
	bool StartThread() { return Start(); }
	void Stop();
};


#endif

