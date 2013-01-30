
#include "fludebug.h"
#include "FlMonIPC.h"


int __cdecl main(int argc, char ** argv)
{
	unsigned int r = FLTIPC_SUCCESS;
	FLMONIPC ipc;
	unsigned int msg_size = 0;
	char t;
	char msg[1024];


	r = FlMonIPCInit(&ipc);

	if (!r)
	{
		DBGUPRINT1("Init failed with code: %d.\n", r);
		getchar();
		return r;
	}

	r = FlMonIPCOpen(&ipc);

	if (!r)
	{
		DBGUPRINT1("Open failed with code: %d.\n", r);
		getchar();
		return r;
	}


	r = FlMonIPCSendAddWatchDir(&ipc, L"C:\\Store0", 9);
	//r = FlMonIPCSendClearWatchDir(&ipc);

	if (r)
	{
		DBGUPRINT("Command failed.\n");
		getchar();
		return r;
	}
	else
	{
		DBGUPRINT("Command SUCCESS.\n");
		getchar();
		return r;
	}

	/*
	while (1)
	{

		r = FlMonIPCReadMessage(&ipc, &t, msg, 1024, &msg_size);

		if (r)
		{
			DBGUPRINT1("Readmessage failed with code %d.\n", r);

			if (r == FLTIPC_PORT_CLOSED)
			{
				break;
			}

			getchar();
			continue;
		}
		else
		{
			DBGUPRINT1("Message: '%s'.\n", msg);
			continue;
		}

	}
	*/

	getchar();
	return 0;
}