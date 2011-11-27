

#ifndef __TAGSTORE_CONFIG_H__
#define __TAGSTORE_CONFIG_H__


#include <Windows.h>



#define TSC_MAX_STORENAME_LENGTH				255
#define TCS_MAX_PATHNAME_LENGTH					1024
#define TCS_MAX_STORES							255



#define TSC_SUCCESS								0
#define TSC_INVALID_PARAMETER					1
#define TSC_READFILE_CONFIG_FAILED				2


typedef struct _TAGSTORE_STORE_ENTRY
{
	char storename[TSC_MAX_STORENAME_LENGTH];
	char storepath[TCS_MAX_PATHNAME_LENGTH];

} TAGSTORE_STORE_ENTRY, *PTAGSTORE_STORE_ENTRY;


typedef struct _TAGSTORE_CONFIGFILE
{
	HANDLE	change_handler;
	char	filename[TCS_MAX_PATHNAME_LENGTH];
	TAGSTORE_STORE_ENTRY storelist[TCS_MAX_STORES];

} TAGSTORE_CONFIGFILE, *PTAGSTORE_CONFIGFILE;



unsigned int TagStoresInit(char * filename, PTAGSTORE_CONFIGFILE cfile);
unsigned int TagStoresRead(PTAGSTORE_CONFIGFILE cfile);






#endif
