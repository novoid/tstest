
#ifndef __LOGFILE_H__
#define __LOGFILE_H__

#include <windows.h>
#include <stdio.h>


#define FSL_MAX_FILENAME_LENGTH			1024
#define FSL_MAX_LINE_LENGTH				2048

#define FSL_SUCCESS						0
#define FSL_INVALID_PARAMETER			1
#define FSL_FILE_ALREADY_OPENED			2
#define FSL_FILE_UNABLE_TO_OPEN			3



typedef struct _FSLOGFILE
{
	char filename[FSL_MAX_FILENAME_LENGTH];
	FILE * fildes;
}
FSLOGFILE, *PFSLOGFILE;


unsigned int FsLogfileInit(PFSLOGFILE file, char * path);
unsigned int FsLogfileOpen(PFSLOGFILE file);
unsigned int FsLogfileClose(PFSLOGFILE file);
unsigned int FsLogfileWrite(PFSLOGFILE file, char * text, BOOLEAN addtime, 
	const char * separator);


#endif

