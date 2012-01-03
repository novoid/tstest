
#include <string>
#include "FileWriter.h"
#include "FileDeletedWatcher.h"


using namespace std;


FileDeletedWatcher::FileDeletedWatcher(string dir)
	: DirectoryWatcher(dir)
{
	SetNotifyFilter(FILE_NOTIFY_CHANGE_FILE_NAME);
}


FileDeletedWatcher::~FileDeletedWatcher()
{
}


void FileDeletedWatcher::ProcessResult(PFILE_NOTIFY_INFORMATION fni)
{
	if (fni->Action == FILE_ACTION_REMOVED)
	{
		string filename;
		LPSTR str = new CHAR[fni->FileNameLength+1];

		int r = WideCharToMultiByte(CP_ACP, 0, fni->FileName, fni->FileNameLength,
			str,
			fni->FileNameLength+1, 0, 0);

		if (!r)
			SingeltonLogfile::Instance()->Write("Convert of DELETED filename failed.\n");
		else
			SingeltonFileWriter::Instance()->Write(MESSAGE_NOTIFY_FILE_DELETE, filename);
		

		delete str;
	}
}
