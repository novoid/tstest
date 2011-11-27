
#include <time.h>
#include "filewriter.h"



/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FwGetCurrentFilename(char * filename)
{
	time_t rawtime;
	struct tm * timeinfo;
	char timepart[256];

	if (!filename)
		return FW_INVALID_PARAMETER;


	filename[0] = '\0';
	time (&rawtime);
	timeinfo = localtime(&rawtime);

	strftime(timepart, FW_MAX_FILENAME_LENGTH, "%Y-%m-%d", timeinfo);
	strcat(filename, timepart);
	strcat(filename, "_tswatch.log");

	return FW_SUCCESS;
}


unsigned int FwWriteToCurrentLogfile(PFSLOGFILE file, char * text)
{
	DWORD r = FW_SUCCESS;

	if (!file)
		return FW_INVALID_PARAMETER;

	r = FsLogfileWrite(file, text, TRUE, ";");

	if (r)
		return FW_WRITE_FAILED;

	return FW_SUCCESS;
}

