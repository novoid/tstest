



#include <windows.h>
#include <stdio.h>
#include "FlMonIPC.h"


#define MIN(a,b) ((a) > (b) ? (b) : (a))
#define FLPORTNAME		L"\\FlFileMonitor"





FlMonIPC::FlMonIPC()
{
	m_pPort = INVALID_HANDLE_VALUE;
}


FlMonIPC::~FlMonIPC()
{
	Close();
}


bool FlMonIPC::Open()
{
	HRESULT hResult = S_OK;



	if (m_pPort != INVALID_HANDLE_VALUE)
		return false;

	hResult = FilterConnectCommunicationPort(FLPORTNAME,
		0,
		NULL,
		0,
		NULL,
		&m_pPort);

	if (IS_ERROR(hResult)) {

		//cout << "Could not connect to filter: " << hResult << "\n";
		return false;
	}

	return true;
}

void FlMonIPC::Close()
{
	if (m_pPort != INVALID_HANDLE_VALUE)
	{
		CloseHandle(m_pPort);
		m_pPort = INVALID_HANDLE_VALUE;
	}
}

unsigned int FlMonIPC::ReadMessage()
{
	HRESULT hResult = S_OK;
	FLIPC_MESSAGE message;
	


	printf("Port reading message (%d).\n", sizeof(FLIPC_MESSAGE));

	hResult = FilterGetMessage(
		m_pPort,
		(PFILTER_MESSAGE_HEADER)&message,
		sizeof(FLIPC_MESSAGE),
		NULL);

	printf("FilterGetMessage finished.\n" );

	if (!SUCCEEDED(hResult)) 
	{
		if (hResult == HRESULT_FROM_WIN32(ERROR_INVALID_HANDLE)) 
		{
			printf("Port is disconnected.\n");
			return FLTIPC_PORT_CLOSED;
		} 
		else
		{
			printf("Unknown error occured. Error = 0x%X\n", hResult);
			return FLTIPC_ERROR;
		}
	}
	else
	{
		printf("Message: '%s'", message.Message);
		return FLTIPC_SUCCESS;
	}
}


unsigned int FlMonIPC::SendMessage(const char * message, DWORD size)
{
	HRESULT hr;
	DWORD res_size = 0;


	if (!message)
		return 0;

	printf("Trying to send %d bytes.\n", size);


	hr = FilterSendMessage(
		m_pPort,
		(LPVOID)message,
		size,
		NULL,
		0,
		&res_size);

	if (!SUCCEEDED(hr)) 
	{
		printf("Send failed.\n" );
	}
	else
	{
		printf("Send OK, return count: %d.\n", res_size);
	}

	return res_size;
}