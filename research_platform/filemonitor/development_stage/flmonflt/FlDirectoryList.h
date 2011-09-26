

#ifndef __FLDIRECTORYLIST_H__
#define __FLDIRECTORYLIST_H__



#define FLD_SUCCESS						0
#define FLD_ALREADY_IN_LIST				1
#define FLD_LIST_FULL					2



BOOLEAN 
	DirectoriesToWatchInit(void);

unsigned int 
	DirectoriesToWatchAdd(PUNICODE_STRING dirname);

void 
	DirectoriesToWatchClear(void);

unsigned int
	DirectoriesToWatchPathExistCount(PUNICODE_STRING path);

BOOLEAN
	DirectoriesToWatchPathExist(PUNICODE_STRING path);

#endif

