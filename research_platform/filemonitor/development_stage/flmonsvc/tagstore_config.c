

#include <Windows.h>
#include <stdio.h>
#include <string.h>
#include "tagstore_config.h"




BOOLEAN TagStorePrivStringStartsWith(char * string, char c);
BOOLEAN TagStorePrivGetParts(char * line, PTAGSTORE_STORE_ENTRY entry);



BOOLEAN TagStorePrivStringStartsWith(char * string, char c)
{
	if (!string)
		return FALSE;

	if (string[0] == c)
		return TRUE;

	return FALSE;
}

BOOLEAN TagStorePrivGetParts(char * line, PTAGSTORE_STORE_ENTRY entry)
{

	return TRUE;
}


unsigned int TagStoresInit(PTAGSTORE_STORES stores)
{
	unsigned int i = 0;

	if (!stores)
		return TSC_INVALID_PARAMETER;


	for (i=0; i < TCS_MAX_STORES; i++)
	{
		stores->stores[i].storename[0] = '\0';
		stores->stores[i].storepath[0] = '\0';
	}

	return TSC_SUCCESS;
}


unsigned int TagStoresRead(char * config_file, PTAGSTORE_STORES stores)
{
	char line [TCS_MAX_PATHNAME_LENGTH];
	unsigned int linepos = 0;
	char c;
	FILE * fildes = NULL;
	BOOLEAN sectionbegin = FALSE;
	char sectionname [TCS_MAX_PATHNAME_LENGTH];
	int count = 0;


	if (!config_file || !stores)
		return TSC_INVALID_PARAMETER;


	TagStoresInit(stores);

	fildes  = fopen(config_file, "r");

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
				TagStorePrivGetParts(line, &stores->stores[count]);
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

