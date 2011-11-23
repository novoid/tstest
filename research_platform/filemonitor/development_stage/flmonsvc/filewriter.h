

#ifndef __FILEWRITER_H__
#define __FILEWRITER_H__


#include <time.h>
#include "logfile.h"


#define FW_MAX_FILENAME_LENGTH			1024

#define FW_SUCCESS						0
#define FW_INVALID_PARAMETER			1
#define FW_WRITE_FAILED					2


unsigned int FwGetCurrentFilename(char * filename);
unsigned int FwWriteToCurrentLogfile(PFSLOGFILE file, char * text);


#endif
