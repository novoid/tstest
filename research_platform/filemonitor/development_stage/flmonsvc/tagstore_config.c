

#include <Windows.h>
#include <stdio.h>
#include <string.h>
#include "tagstore_config.h"




BOOLEAN TagStorePrivStringStartsWith(char * string, char c);
BOOLEAN TagStorePrivGetParts(char * line, PTAGSTORE_STORE_ENTRY entry);
BOOLEAN TagStorePrivGetDir(char * filepath, char * dirpath);
unsigned int TagStoreInstallChangeHandler();
unsigned int TagStoreRemoveChangeHandler();


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN TagStorePrivStringStartsWith(char * string, char c)
{
	if (!string)
		return FALSE;

	if (string[0] == c)
		return TRUE;

	return FALSE;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN TagStorePrivGetParts(char * line, PTAGSTORE_STORE_ENTRY entry)
{

	return TRUE;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
BOOLEAN TagStorePrivGetDir(char * filepath, char * dirpath)
{
	unsigned int last_bs_index = 0;

	// get last index of '\'



	return TRUE;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int TagStoreInstallChangeHandler()
{
	char directory[256];



	FindFirstChangeNotification(directory, FALSE, FILE_NOTIFY_CHANGE_LAST_WRITE);
	return TSC_SUCCESS;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int TagStoreRemoveChangeHandler()
{

}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int TagStoresInit(char * filename, PTAGSTORE_CONFIGFILE cfile)
{
	unsigned int i = 0;

	if (!filename || !cfile)
		return TSC_INVALID_PARAMETER;


	cfile->change_handler = NULL;
	strcpy(cfile->filename, filename);

	for (i=0; i < TCS_MAX_STORES; i++)
	{
		cfile->storelist[i].storename[0] = '\0';
		cfile->storelist[i].storepath[0] = '\0';
	}

	return TSC_SUCCESS;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int TagStoresRead(PTAGSTORE_CONFIGFILE cfile)
{
	char line [TCS_MAX_PATHNAME_LENGTH];
	unsigned int linepos = 0;
	char c;
	FILE * fildes = NULL;
	BOOLEAN sectionbegin = FALSE;
	int count = 0;


	if (!cfile || !cfile->filename || cfile->filename[0] == '\n')
		return TSC_INVALID_PARAMETER;


	fildes  = fopen(cfile->filename, "r");

	if (!fildes)
		return TSC_READFILE_CONFIG_FAILED;	


	do
	{
		c = fgetc(fildes);
		
		if (!c)
			break;

		if (c == '\n')
		{
			line[linepos] = '\0';
			linepos = 0;
		}
		else
		{
			line[linepos++] = c;
		}

		if (linepos == 0) // line has ended
		{
			if (TagStorePrivStringStartsWith(line, '[') && sectionbegin) // section has ended
				sectionbegin = FALSE;

			if (sectionbegin) // we are in the section
			{
				TagStorePrivGetParts(line, &cfile->storelist[count]);
				count++;
			}

			if (!strcmp(line, "[stores]")) // section is starting
			{
				sectionbegin = TRUE;
			}
		}

	}
	while (1);
	
	fclose(fildes);

	return TSC_SUCCESS;
}


