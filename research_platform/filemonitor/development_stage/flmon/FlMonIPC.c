



#include <windows.h>
#include <Fltuser.h>
#include <stdio.h>
#include "../flmonflt/FlIpc.h"
#include "FlMonIPC.h"



#define MIN(a,b) ((a) > (b) ? (b) : (a))
#define FLPORTNAME		L"\\FlFileMonitor"




typedef struct _FLIPC_MESSAGE
{

	FILTER_MESSAGE_HEADER	MessageHeader;
	char 					Message[FLMESSAGE_MAXSIZE];

} FLIPC_MESSAGE, *PFLIPC_MESSAGE;





BOOLEAN __stdcall FlMonIPCInit(PFLMONIPC ipc)
{
	if (!ipc)
		return FALSE;

	ipc->Port = INVALID_HANDLE_VALUE;

	return TRUE;
}

BOOLEAN __stdcall FlMonIPCOpen(PFLMONIPC ipc)
{
	HRESULT hResult = S_OK;

	if (!ipc)
		return FALSE;

	hResult = FilterConnectCommunicationPort(FLPORTNAME, 0, NULL, 0, NULL, &ipc->Port);

	if (!SUCCEEDED(hResult)) 
	{
		if (hResult == HRESULT_FROM_WIN32(ERROR_INVALID_HANDLE)) 
		{
			printf("Port is disconnected.\n");
			return FALSE;
		} 
		else
		{
			printf("Error occured. Code = 0x%X\n", hResult);
			return FALSE;
		}
	}

	return TRUE;
}

void __stdcall FlMonIPCClose(PFLMONIPC ipc)
{
	if (!ipc || ipc->Port == INVALID_HANDLE_VALUE)
		return;

	printf("Closing port: 0x%X\n", ipc->Port);
	CloseHandle(ipc->Port);
	ipc->Port = INVALID_HANDLE_VALUE;
}

BOOLEAN __stdcall FlMonIPCOpened(PFLMONIPC ipc)
{
	if (!ipc)
		return FALSE;

	
	if (ipc->Port != INVALID_HANDLE_VALUE)
		return TRUE;

	return FALSE;
}

unsigned int __stdcall FlMonIPCReadMessage(PFLMONIPC ipc, 
	unsigned char * type, char * msg, unsigned int msg_maxsize, 
	unsigned int * msg_size)
{
	HRESULT hResult = S_OK;
	FLIPC_MESSAGE message;


	if (!ipc || !msg_size)
	{
		printf("FlMonIPCReadMessage: Invalid parameter.\n");
		return FLTIPC_ERROR;
	}


	printf("Port reading message.\n");

	hResult = FilterGetMessage(
		ipc->Port,
		(PFILTER_MESSAGE_HEADER)&message,
		sizeof(FLIPC_MESSAGE),
		NULL);

	//printf("FilterGetMessage finished.\n" );

	if (!SUCCEEDED(hResult)) 
	{
		if (hResult == HRESULT_FROM_WIN32(ERROR_INVALID_HANDLE)) 
		{
			printf("Port is disconnected.\n");
			return FLTIPC_PORT_CLOSED;
		} 
		else
		{
			//printf("Unknown error occured. Error = 0x%X\n", hResult);
			return FLTIPC_ERROR;
		}

		msg[0] = '\0';
	}
	else
	{
		memcpy(msg, message.Message+1, FLMESSAGE_MAXSIZE-1);
		*type = message.Message[0];

		printf("Message OK, Type=%c, Message='%s'.\n", *type, msg);

		return FLTIPC_SUCCESS;
	}
}



unsigned int __stdcall FlMonIPCSendMessage(PFLMONIPC ipc, 
	const char * send_message, DWORD send_size,
	const char * reply_message, DWORD reply_size)
{
	HRESULT hr;
	DWORD res_size = 0;


	if (!ipc)
		return 0;

	if (!send_message)
		return 0;

	printf("Trying to send %d bytes.\n", send_size);


	hr = FilterSendMessage(
		ipc->Port,
		(LPVOID)send_message,
		send_size,
		reply_message,
		reply_size,
		&res_size);

	if (!SUCCEEDED(hr)) 
	{
		printf("Send failed.\n");
	}
	else
	{
		printf("Send OK, return count: %d.\n", res_size);
	}

	printf("Send result size = %d.\n", res_size);

	return res_size;
}


unsigned int __stdcall FlMonIPCSendAddWatchDir(PFLMONIPC ipc, WCHAR * str, unsigned int size)
{
	char message[FLMESSAGE_MAXSIZE];
	unsigned int r = 0;


	if (!ipc)
		return FLTIPC_ERROR;


	if (size * sizeof(WCHAR) + 2 > FLMESSAGE_MAXSIZE)
		return FLTIPC_MESSAGESIZE_EXCEEDED;

	message[0] = MAGIC_MESSAGE_PREFIX;
	message[1] = MESSAGE_CMD_ADD_WATCHDIR;

	memcpy(message, str, size * sizeof(WCHAR));

	r = FlMonIPCSendMessage(ipc, message, size * sizeof(WCHAR) + 2, NULL, 0);

	if (r)
		return FLTIPC_SUCCESS;
	else
		return FLTIPC_ERROR;
}

unsigned int __stdcall FlMonIPCSendClearWatchDir(PFLMONIPC ipc)
{
	char message[2];
	unsigned int r = 0;


	if (!ipc)
		return FLTIPC_ERROR;


	message[0] = MAGIC_MESSAGE_PREFIX;
	message[1] = MESSAGE_CMD_CLEAR_WATCHDIR;


	r = FlMonIPCSendMessage(ipc, message, 2, NULL, 0);

	if (!r)
		return FLTIPC_SUCCESS;
	else
		return FLTIPC_ERROR;
}


unsigned int __stdcall FlMonIPCSendStartFiltering(PFLMONIPC ipc, BOOLEAN state)
{
	char message[2];
	unsigned int r = 0;


	if (!ipc)
		return FLTIPC_ERROR;


	message[0] = MAGIC_MESSAGE_PREFIX;


	if (state)
		message[1] = MESSAGE_CMD_START_FILTER;
	else
		message[1] = MESSAGE_CMD_STOP_FILTER;


	r = FlMonIPCSendMessage(ipc, message, 2, NULL, 0);

	if (!r)
		return FLTIPC_SUCCESS;
	else
		return FLTIPC_ERROR;
}


unsigned int __stdcall 
	FlMonIPCGetFilteringState(PFLMONIPC ipc, 
	BOOLEAN * state)
{
	char send_message[2];
	char reply_message[1];
	unsigned int r = 0;


	if (!ipc || !state)
		return FLTIPC_ERROR;


	send_message[0] = MAGIC_MESSAGE_PREFIX;
	send_message[1] = MESSAGE_CMD_FILTER_STATE;


	r = FlMonIPCSendMessage(ipc, send_message, 2, reply_message, 1);

	if (r == 1)
	{
		*state = (reply_message[0]) ? 1 : 0;
		return FLTIPC_SUCCESS;
	}
	else
	{
		*state = 0;
		return FLTIPC_ERROR;
	}
}
