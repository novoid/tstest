
#ifndef __FLMUTEX_H__
#define __FLMUTEX_H__



#include <ntifs.h>


void 
	KMutexInit(KMUTEX * mutex);

BOOLEAN 
	KMutexAquire(KMUTEX * mutex);

BOOLEAN 
	KMutexRelease(KMUTEX * mutex);

#endif
