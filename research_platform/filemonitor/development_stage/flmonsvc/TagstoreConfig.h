

#ifndef __TAGSTORE_CONFIG_H__
#define __TAGSTORE_CONFIG_H__

#include <Windows.h>
#include <string>
#include <vector>
#include "../flmon/FlMonIPC.h"
#include "SingeltonHolder.h"


#define TSC_SUCCESS							 0
#define TSC_FILE_NOT_FOUND					-1
#define TSC_FILE_READ_TIMEOUT				-2
#define TSC_IPC_NOT_OPEN					-3
#define TSC_FILE_UNABLE_TO_DELETE_DIRS		-4
#define TSC_FILE_UNABLE_TO_ADD_DIR			-5


using namespace std;


struct Tagstore
{
	string Name;
	string Path;

	string ToString();
};


class TagstoreConfig
{

private:

	string m_ConfigFilename;
	vector<Tagstore> m_Stores;
	vector<string> m_Subpaths;
	HANDLE m_ConfigReadLock;

	// Config file change watcher
	HANDLE		m_ThreadHandle;
	long long	m_lThreadID;
	bool		m_bRunThread;
	HANDLE		m_ThreadStopWait;
	
	PFLMONIPC	m_IPC;

private:

	string Trim(string str);
	bool Split(string line, string& left, string& right);


public:

	HANDLE		m_DirectoryHandle;

	TagstoreConfig();
	TagstoreConfig(string filename);
	~TagstoreConfig();

	void SetSubpaths();

	void SetIPC(PFLMONIPC ipc) { m_IPC = ipc; }
	void SetFilename(string filename) { m_ConfigFilename = filename; Read(); }
	
	string GetDirFromFilepath(string path);
	string GetFilenameFromFilepath(string path);
	bool EndsWithTagstoreConfigfilename(WCHAR * filename, unsigned int size);

	string GetDosDevicename(string path);
	string GetDevicenameFromFilepath(string path);
	string ConvertToDosDevicename(string path);

	bool ThreadState() { return m_bRunThread; }
	void StopThread() { m_bRunThread = false; }
	HANDLE GetThreadStopWait() { return m_ThreadStopWait; }

	int Read();
	string GetConfigFilename() { return m_ConfigFilename; }

	unsigned int StoreCount() { return m_Stores.size(); }
	Tagstore operator[](int index) { return m_Stores[index]; }

	int UpdateDriverStores();

	bool InstallChangeHandler();
	void UninstallChangeHandler();

	

};


typedef SingeltonHolder<TagstoreConfig> SingeltonTSConfig;


#endif
