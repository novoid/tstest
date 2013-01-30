

#ifndef __FLDEBUG_H__
#define __FLDEBUG_H__



#include <Fltkernel.h>



#define DBGPRINT(_str_) \
	DbgPrintEx(DPFLTR_IHVDRIVER_ID, DPFLTR_ERROR_LEVEL, _str_)

#define DBGPRINT_ARG1(_str_, _arg1_) \
	DbgPrintEx(DPFLTR_IHVDRIVER_ID, DPFLTR_ERROR_LEVEL, _str_, _arg1_)

#define DBGPRINT_ARG2(_str_, _arg1_, _arg2_) \
	DbgPrintEx(DPFLTR_IHVDRIVER_ID, DPFLTR_ERROR_LEVEL, _str_, _arg1_, _arg2_)

#define DBGPRINT_ARG3(_str_, _arg1_, _arg2_, _arg3_) \
	DbgPrintEx(DPFLTR_IHVDRIVER_ID, DPFLTR_ERROR_LEVEL, _str_, _arg1_, _arg2_, _arg3_)


#define DBGBREAK	DbgBreakPoint()


extern KMUTEX		DebugPrintMutex;	


void 
	PrintCallbackData(
	PFLT_CALLBACK_DATA Data
	);


#endif
