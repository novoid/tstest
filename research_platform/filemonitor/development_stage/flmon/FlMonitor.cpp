
#include <iostream>
#include "FlMonitor.h"


using namespace std;


FlMonitor::FlMonitor(FlMonIPC& ipc)
	: m_FlIPC(ipc)
{
	
}


FlMonitor::~FlMonitor()
{
}

void FlMonitor::run()
{
	unsigned int status;


	if (!m_FlIPC.Opened())
	{
		m_FlIPC.Open();

		cout << "ERROR opening driver ipc connection." << endl;
		quit();
	}

	while(true)
	{	
		status = m_FlIPC.ReadMessage();
	}
}