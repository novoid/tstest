
#ifndef __FILEDELETEDWATCHER_H__
#define __FILEDELETEDWATCHER_H__


#include <string>
#include "DirectoryWatcher.h"


using namespace std;



class FileDeletedWatcher : public DirectoryWatcher
{

private:

	virtual void ProcessResult(PFILE_NOTIFY_INFORMATION fni);

public:

	FileDeletedWatcher(string dir);
	~FileDeletedWatcher();
};


#endif
