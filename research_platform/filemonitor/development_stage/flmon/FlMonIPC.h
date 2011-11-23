

#ifndef __FLMONIPC_H__
#define __FLMONIPC_H__

#ifdef __cplusplus
extern "C" {
#endif


#include <windows.h>
#include "../flmonflt/FlIpc.h"



#define FLMESSAGE_MAXSIZE			1024


#define FLTIPC_SUCCESS						0
#define FLTIPC_ERROR						1
#define FLTIPC_PORT_CLOSED					2
#define FLTIPC_MESSAGESIZE_EXCEEDED			3



typedef struct _FLMONIPC
{
	HANDLE Port;

} FLMONIPC, *PFLMONIPC;


BOOLEAN __stdcall 
	FlMonIPCInit(PFLMONIPC ipc);

BOOLEAN __stdcall 
	FlMonIPCOpen(PFLMONIPC ipc);

void __stdcall 
	FlMonIPCClose(PFLMONIPC ipc);

BOOLEAN __stdcall 
	FlMonIPCOpened(PFLMONIPC ipc);

unsigned int __stdcall 
	FlMonIPCReadMessage(PFLMONIPC ipc, 
	unsigned char * type, char * msg, unsigned int msg_maxsize, 
	unsigned int * msg_size);

unsigned int __stdcall FlMonIPCSendMessage(PFLMONIPC ipc, 
	const char * send_message, DWORD send_size,
	const char * reply_message, DWORD reply_size);


unsigned int __stdcall 
	FlMonIPCSendAddWatchDir(PFLMONIPC ipc, 
	WCHAR * str, unsigned int size);

unsigned int __stdcall 
	FlMonIPCSendClearWatchDir(PFLMONIPC ipc);

unsigned int __stdcall 
	FlMonIPCSendStartFiltering(PFLMONIPC ipc, 
	BOOLEAN state);

unsigned int __stdcall 
	FlMonIPCGetFilteringState(PFLMONIPC ipc, 
	BOOLEAN * state);



#ifdef __cplusplus 
}
#endif


#endif

