
#include "FsLogFile.h"
#include "FileDeletedWatcher.h"
#include "FileWriter.h"
#include "TsDeletedWatcher.h"



TsDeletedWatcher::TsDeletedWatcher()
{
}


TsDeletedWatcher::~TsDeletedWatcher()
{
	for (int i=0; i < m_Watchers.size(); i++)
	{
		FileDeletedWatcher * d = m_Watchers[i];
		d->Stop();
		delete d;
	}

	m_Watchers.clear();
}

bool TsDeletedWatcher::AddDirectory(string dir)
{
	FileDeletedWatcher * fdw = new FileDeletedWatcher(dir);
	
	
	bool b = true;
	
	
	SingeltonLogfile::Instance()->Write("Starting directory watcher.\n");
	b = fdw->StartThread();

	if (b)
	{
		m_Watchers.push_back(fdw);
		SingeltonLogfile::Instance()->WriteS("Started directory watcher: ", dir);
	}
	else
	{
		SingeltonLogfile::Instance()->WriteS("Error starting directory watcher: ", dir);
	}

	

	return b;
	
}
