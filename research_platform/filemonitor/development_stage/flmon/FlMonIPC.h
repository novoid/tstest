

#ifndef __FLMONIPC_H__
#define __FLMONIPC_H__

#include "stdafx.h"


#include <Fltuser.h>

#define FLMESSAGE_MAXSIZE			1024


#define FLTIPC_SUCCESS				0
#define FLTIPC_ERROR				1
#define FLTIPC_PORT_CLOSED			2



struct FLIPC_MESSAGE
{

	FILTER_MESSAGE_HEADER	MessageHeader;
	char 					Message[FLMESSAGE_MAXSIZE];

};


class FlMonIPC
{

private:

	HANDLE m_pPort;


public:

	FlMonIPC();
	~FlMonIPC();


	bool Open();
	void Close();
	bool Opened() { return m_pPort != NULL; }

	unsigned int ReadMessage();
	unsigned int SendMessage(const char * message, DWORD size);
};


#endif

