

//#include <Windows.h>

#include <stdio.h>
#include "FlTest.h"
#include "FlMonIPC.h"





int main()
{
	
	FlMonIPC ipc;

	if (!ipc.Open())
	{
		printf("Open failed.\n");
		return 0;
	}

	char buf[2];
	buf[0] = 'a';
	buf[1] = '\0';


	//ipc.SendMessage(buf, 2);
	ipc.ReadMessage();

	ipc.Close();

	
	/*
	FlTest test;
	test.OpenDirectory(L"C:\\\Users\\Administrator\\Desktop\\Neuer Ordner", false);
	*/

	getchar();


	return 0;
}