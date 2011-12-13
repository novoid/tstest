

#include "fldebug.h"
#include "FileMon.h"
#include "FlTracker.h"
#include "FlTimerThread.h"






static BOOLEAN	TimerThreadStopExec = FALSE;
KEVENT			ThreadSleepWakeup;
KTIMER			Timer;


void
	TimerThreadExecRoutine(
	__in  PVOID StartContext
	);



/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
NTSTATUS
	FlTimerThreadCreate()
{
	NTSTATUS status = STATUS_SUCCESS;
	HANDLE handle;
	OBJECT_ATTRIBUTES objectAttribs;


	TimerThreadStopExec = FALSE;

	InitializeObjectAttributes(
		&objectAttribs, NULL, OBJ_KERNEL_HANDLE, NULL, NULL);

	KeInitializeEvent(&ThreadSleepWakeup, SynchronizationEvent, FALSE);
	DriverInfoData->TimerThreadSleepWakeup = &ThreadSleepWakeup;

	KeInitializeTimer(&Timer);
	DriverInfoData->Timer = &Timer;



	status = PsCreateSystemThread(&handle, 
		THREAD_ALL_ACCESS,
		&objectAttribs, 
		NULL, 
		NULL, 
		(PKSTART_ROUTINE)TimerThreadExecRoutine, 
		NULL);

	if (!NT_SUCCESS(status)) 
	{
		KeCancelTimer(&Timer);
		DriverInfoData->Timer = NULL;
		DriverInfoData->TimerThreadSleepWakeup = NULL;

		DBGPRINT_ARG1("[flmonflt] PsCreateSystemThread failed: 0x%X\n", status);
	} 
	else 
	{
		ObReferenceObjectByHandle(handle, THREAD_ALL_ACCESS, NULL,
			KernelMode, &DriverInfoData->TimerThread, NULL);

		ZwClose(handle);

		DBGPRINT("[flmonflt] PsCreateSystemThread succeeded.\n");
	}

	return status;
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
NTSTATUS
	FlTimerThreadStop()
{
	LARGE_INTEGER tend_timeout;


	DBGPRINT("[flmonflt] Stopping timer thread.\n");

	tend_timeout.QuadPart = RELATIVE(SECONDS(8));
	TimerThreadStopExec = TRUE;


	KeCancelTimer(DriverInfoData->Timer);
	KeSetEvent(DriverInfoData->TimerThreadSleepWakeup, 0, FALSE);

	DBGPRINT("[flmonflt] Waiting for thread to exit.\n");
	KeWaitForSingleObject(DriverInfoData->TimerThread, Executive, KernelMode, TRUE, &tend_timeout);

	DBGPRINT("[flmonflt] Thread exited, dereferencing object.\n");
	ObDereferenceObject(DriverInfoData->TimerThread);

	DBGPRINT("[flmonflt] Thread dereferenced.\n");

	return STATUS_SUCCESS;
}





/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
void
	TimerThreadExecRoutine(
	__in  PVOID StartContext
	)
{ 
	UNREFERENCED_PARAMETER(StartContext);


	DBGPRINT("[flmonflt] Starting timer thread.\n");

	DriverInfoData->TimerDueTime.QuadPart = RELATIVE(MILLISECONDS(TIMER_THREAD_LIST_TIMEOUT_MS));


	while(!TimerThreadStopExec)
	{
		// wait for event, thread will sleep until its set.
		KeWaitForSingleObject(DriverInfoData->TimerThreadSleepWakeup, 
			Executive, KernelMode, FALSE, NULL);

		if (TimerThreadStopExec)
		{
			DBGPRINT("[flmonflt] Terminating timer thread with TimerThreadSleepWakeup.\n");
			break;
		}

		

		
		DBGPRINT("[flmonflt] Waiting for timer to elapse.\n");

		//DriverInfoData->TrackerLastEntryValid = FALSE;
		KeSetTimer(DriverInfoData->Timer, DriverInfoData->TimerDueTime, NULL);
		KeWaitForSingleObject(DriverInfoData->Timer, Executive, KernelMode, FALSE, NULL);


		DBGPRINT("[flmonflt] Timer elapsed.\n");

		FlTrackerSetLastEntry(DriverInfoData);
		//DriverInfoData->TrackerLastEntryValid = TRUE;

		

		DBGPRINT("[flmonflt] *************** Timer elapsed. ***************\n");
		FlTrackerEntryPrint(FlTrackerGetBestEntry(DriverInfoData->Tracker));
		DBGPRINT("[flmonflt] **********************************************\n");
		FlTrackerPrintListDbg(DriverInfoData->Tracker);
		DBGPRINT("[flmonflt] ----------------------------------------------\n");


		FlTrackerInfoToUserspace(DriverInfoData, TRUE);

		FlTrackerClean(DriverInfoData->Tracker);
		

		// clear event to nonsignaled
		KeClearEvent(DriverInfoData->TimerThreadSleepWakeup);
	}

	DBGPRINT_ARG1("[flmonflt] Terminating timer thread, Stop=%d.\n", TimerThreadStopExec);
	PsTerminateSystemThread(STATUS_SUCCESS);
}

