
#include <ntifs.h>
#include "FlMutex.h"


void 
	KMutexInit(KMUTEX * mutex)
{
	KeInitializeMutex(mutex, 0);
}


BOOLEAN 
	KMutexAquire(KMUTEX * mutex)
{
	NTSTATUS s = STATUS_SUCCESS;

	s = KeWaitForMutexObject(
		mutex,
		Executive,
		KernelMode,
		FALSE,
		NULL);

	if (NT_SUCCESS(s))
		return TRUE;
	return FALSE;
}


BOOLEAN 
	KMutexRelease(KMUTEX * mutex)
{
	if (!KeReleaseMutex(mutex, FALSE))
		return TRUE;
	return FALSE;
}
