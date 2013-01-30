
#ifndef __TSDELETEDWATCHER_H__
#define __TSDELETEDWATCHER_H__


#include <string>
#include <vector>


using namespace std;



class FileDeletedWatcher;

class TsDeletedWatcher
{

private:

	vector<FileDeletedWatcher*> m_Watchers;

public:

	TsDeletedWatcher();
	virtual ~TsDeletedWatcher();

	bool AddDirectory(string dir);

};


#endif
