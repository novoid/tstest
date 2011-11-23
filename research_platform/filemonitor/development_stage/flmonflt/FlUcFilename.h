
#ifndef __FLUCFILENAME_H__
#define __FLUCFILENAME_H__



#include <ntifs.h>



NTSTATUS UcFilenameInit(
	__out     PUNICODE_STRING String
	);


VOID UcFilenameFree(
	__out     PUNICODE_STRING String
	);


void UcFilenameCopy(
	__in    PUNICODE_STRING SrcString,
	__out	PUNICODE_STRING DstString
	);

BOOLEAN UcFilenameEqual(
	__in    PUNICODE_STRING SrcString,
	__in	PUNICODE_STRING DstString
	);

BOOLEAN UcFilenameStartsWith(
	__in	PUNICODE_STRING MonitorString,
	__in	PUNICODE_STRING InString);


#endif