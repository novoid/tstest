
#include <string.h>
#include <time.h>
#include "logfile.h"


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FsLogfileInit(PFSLOGFILE file, char * path)
{
	if (!file || !path || path[0] == '\0')
		return FSL_INVALID_PARAMETER;

	file->fildes = NULL;

	strcpy_s(file->filename, strlen(path)+1, path);

	return FSL_SUCCESS;
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FsLogfileOpen(PFSLOGFILE file)
{
	if (!file)
		return FSL_INVALID_PARAMETER;

	if (file->fildes)
		return FSL_FILE_ALREADY_OPENED;


	file->fildes = fopen((const char*)file->filename, "a+");

	if (!file->fildes)
		return FSL_FILE_UNABLE_TO_OPEN;

	return FSL_SUCCESS;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FsLogfileClose(PFSLOGFILE file)
{
	if (!file)
		return FSL_INVALID_PARAMETER;

	fclose(file->fildes);

	return FSL_SUCCESS;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FsLogfileWrite(PFSLOGFILE file, char * text, BOOLEAN addtime,
	const char * separator)
{
	char complete_text[FSL_MAX_LINE_LENGTH];
	time_t rawtime;
	struct tm * timeinfo;

	if (!file || !text)
		return FSL_INVALID_PARAMETER;


	complete_text[0] = '\0';

	if (addtime)
	{
		time (&rawtime);
		timeinfo = localtime ( &rawtime );

		strftime(complete_text, FSL_MAX_LINE_LENGTH, "%d.%m.%Y %H:%M:%S", timeinfo);

		if (separator)
		{
			strcat(complete_text, separator);
		}
		else
		{
			strcat(complete_text, " - ");
		}
	}

	strcat(complete_text, text);

	fwrite(complete_text, strlen(complete_text), 1, file->fildes);

	return FSL_SUCCESS;
}
