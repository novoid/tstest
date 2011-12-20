
#ifndef __LOGFILE_H__
#define __LOGFILE_H__

#include <windows.h>
#include <string>
#include <stdio.h>
#include "SingeltonHolder.h"


#define FSL_MAX_FILENAME_LENGTH			1024
#define FSL_MAX_LINE_LENGTH				2048

#define FSL_SUCCESS						0
#define FSL_INVALID_PARAMETER			1
#define FSL_FILE_ALREADY_OPENED			2
#define FSL_FILE_UNABLE_TO_OPEN			3


using namespace std;


class FsLogFile
{

	string m_Filename;
	FILE * m_Fildes;


public:

	FsLogFile();
	~FsLogFile();

	unsigned int Open(string path);
	unsigned int Close();
	unsigned int Write(string text, BOOLEAN addtime,
		string separator);
	unsigned int Write(string text);
	unsigned int Write1(string text, int errcode);
	unsigned int WriteS(string text, string text2);
};


typedef SingeltonHolder<FsLogFile> SingeltonLogfile;

#endif

