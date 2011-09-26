

#ifndef __FLMONITOR_H__
#define __FLMONITOR_H__

#include <qthread.h>
#include "FlMonIPC.h"



class FlMonitor : public QThread
{
	Q_OBJECT

private:

	FlMonIPC&	m_FlIPC;
		

public:

	FlMonitor(FlMonIPC& ipc);
	~FlMonitor();

	virtual void run();
};

#endif
