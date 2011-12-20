

#ifndef __FILEWRITER_H__
#define __FILEWRITER_H__

#include <string>
#include <time.h>
#include "SingeltonHolder.h"
#include "RegistryWindows.h"
#include "fslogfile.h"



#define FW_SUCCESS							0
#define FW_INVALID_PARAMETER				1
#define FW_WRITE_FAILED						2
#define FW_FILE_NOT_OPENED					3
#define FW_REGOPEN_LASTFN_FAILED			4
#define FW_REGGET_LASTFN_FAILED				5
#define FW_REGUPDATE_LASTFN_FAILED			6

using namespace std;


class FileWriter : public FsLogFile
{

private:

	RegistryWindows	m_Registry;

	static string m_BaseDirectory;

public:


	FileWriter();
	~FileWriter();

	static void SetBaseDirectory(string dirname) { m_BaseDirectory = dirname; }

	string CurrentFilename();
	unsigned int Write(char type, string path);
	bool RulesApply(char type, string& path);


	bool NeedNewFile();

	unsigned int UpdateRegLastFilename(string filename);
	unsigned int GetRegLastFilename(string& filename);

};


typedef SingeltonHolder<FileWriter> SingeltonFileWriter;

#endif
