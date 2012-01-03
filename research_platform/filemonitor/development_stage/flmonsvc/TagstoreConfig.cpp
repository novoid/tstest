
#include <Windows.h>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include "TsDeletedWatcher.h"
#include "fslogfile.h"
#include "tagstoreconfig.h"



string Tagstore::ToString()
{
	stringstream ss;

	ss << Name << " = " << Path << "\n";
	return ss.str();
}


///////////////////////////////////////////////////////////////////////////

TagstoreConfig::TagstoreConfig(string filename)
{
	m_ConfigFilename = filename;
	m_ThreadHandle = INVALID_HANDLE_VALUE;
	m_lThreadID = -1;
	m_ConfigReadLock = CreateMutex(0, FALSE, NULL);

	SetSubpathsReadWatch();
	SetSubpathsDeleteWatch();
}

TagstoreConfig::TagstoreConfig()
{
	m_ThreadHandle = INVALID_HANDLE_VALUE;
	m_lThreadID = -1;
	m_ConfigReadLock = CreateMutex(0, FALSE, NULL);
	m_pDeleteWatcher = NULL;

	SetSubpathsReadWatch();
	SetSubpathsDeleteWatch();
}

TagstoreConfig::~TagstoreConfig()
{
	delete m_pDeleteWatcher;
}


void TagstoreConfig::SetSubpathsReadWatch()
{
	m_SubpathsReadWatch.push_back("Ablage");
	m_SubpathsReadWatch.push_back("Beschreibungen");
	m_SubpathsReadWatch.push_back("Kategorien");
}

void TagstoreConfig::SetSubpathsDeleteWatch()
{
	m_SubpathsDeleteWatch.push_back("Ablage");
}

string TagstoreConfig::GetDirFromFilepath(string path)
{
	int pos = 0;

	pos = path.find_last_of("\\");

	if (pos == -1)
		return string("");

	return path.substr(0, pos+1);
}

string TagstoreConfig::GetFilenameFromFilepath(string path)
{
	int pos = 0;

	pos = path.find_last_of("\\");

	if (pos == -1)
		return string("");

	return path.substr(pos+1, path.size()-pos-1);
}

string TagstoreConfig::ConvertToDosDevicename(string path)
{
	int pos = 0;
	pos = path.find_first_of("\\");

	if (pos == -1)
		return string("");

	string path_without_drive = path.substr(pos, path.size()-1);
	string device = GetDosDevicename(GetDevicenameFromFilepath(path));
	string result = device + path_without_drive;

	return result;	
}


string TagstoreConfig::Trim(string str)
{
	string::size_type pos = str.find_last_not_of(' ');
	
	if(pos != string::npos) {
		str.erase(pos + 1);
		pos = str.find_first_not_of(' ');
		if(pos != string::npos) str.erase(0, pos);
	}
	else 
	{
		str.erase(str.begin(), str.end());
	}

	return str;
}

bool TagstoreConfig::Split(string line, string& left, string& right)
{
	int pos = 0;

	pos = line.find_first_of("=");

	if (pos == -1)
		return false;
	
	left = line.substr(0, pos);
	right = line.substr(pos+1, line.size()-(pos+1));

	return true;
}

// size is the size in bytes without terminating zero
bool TagstoreConfig::EndsWithTagstoreConfigfilename(WCHAR * filename, unsigned int size)
{
	string sfilename = GetFilenameFromFilepath(m_ConfigFilename);

	if (sfilename.empty())
		return false;

	//convert from wide char to narrow char array
	char ch[MAX_PATH];
	
	if (!WideCharToMultiByte(CP_ACP, 0, filename, size/2, ch, MAX_PATH, NULL, NULL))
		return false;

	//A std:string  using the char* constructor.
	ch[size/2] = '\0';
	std::string ss(ch);
	

	if (ss == sfilename)
		return true;

	return false;
}



string TagstoreConfig::GetDosDevicename(string path)
{
	DWORD r = 0;
	char target[1024];

	target[0] = '\0';

	r = QueryDosDevice(path.c_str(), target, 1024);

	return string(target);
}

string TagstoreConfig::GetDevicenameFromFilepath(string path)
{
	int pos = 0;

	pos = path.find_first_of("\\");

	if (pos == -1)
		return string("");

	return path.substr(0, pos);
}


int TagstoreConfig::Read()
{
	string line;
	bool stores_section = false;
	DWORD rlock;


	rlock = WaitForSingleObject(m_ConfigReadLock, 1000);

	if (rlock != WAIT_OBJECT_0)
	{
		SingeltonLogfile::Instance()->Write("Configfile read timed out.\n");
		return TSC_FILE_READ_TIMEOUT;
	}

	ifstream infile(m_ConfigFilename, std::ios_base::in);

	if (!infile.is_open())
	{
		ReleaseMutex(m_ConfigReadLock);

		SingeltonLogfile::Instance()->Write("Configfile not found.\n");
		return TSC_FILE_NOT_FOUND;
	}

	m_Stores.clear();

	SingeltonLogfile::Instance()->Write("Reading Tagstore configuration.\n");
	
	while (getline(infile, line, '\n'))
	{
		if (Trim(line) == "[Stores]" ||
			Trim(line) == "[stores]")
		{
			stores_section = true;
			continue;
		}
		else if (line.size() > 0 && line[0] == '[' && stores_section)
		{
			stores_section = false;
		}

		if (stores_section)
		{
			string left, right;

			Split(line, left, right);

			// replace all "/" with "\"
			for (int i=0; i < right.size(); i++)
			{
				if (right[i] == '/')
					right[i] = '\\';
			}

			Tagstore store;
			store.Name = Trim(left);
			store.Path = Trim(right);

			
			string tsstr = store.ToString();
			SingeltonLogfile::Instance()->Write(tsstr);

			m_Stores.push_back(store);
		}
	}

	infile.close();


	unsigned int cnt = m_Stores.size();

	ReleaseMutex(m_ConfigReadLock);


	return cnt;
}




DWORD WINAPI StartConfigWatchThread(LPVOID pArgs)
{
	string directory;
	TagstoreConfig * config = (TagstoreConfig*)pArgs;
	string filename = config->GetConfigFilename();
	void *		NotifyBuffer = new char[256];
	FILE_NOTIFY_INFORMATION * notifyInfo;
	DWORD ret = 0;
	BOOL b = FALSE;
	HANDLE mutex;


	config->m_DirectoryHandle = INVALID_HANDLE_VALUE;
	mutex = config->GetThreadStopWait();

	mutex = CreateMutex(0, TRUE, NULL);


	SingeltonLogfile::Instance()->Write("Starting config file watcher thread.\n");
	directory = config->GetDirFromFilepath(filename);


	config->m_DirectoryHandle = CreateFile(directory.c_str(), GENERIC_READ, FILE_SHARE_READ,
		0, OPEN_EXISTING, FILE_FLAG_BACKUP_SEMANTICS , 0);

	if (config->m_DirectoryHandle == INVALID_HANDLE_VALUE)
	{
		SingeltonLogfile::Instance()->Write("Open Tagstore Configfile directory failed.\n");
		return -1;
	}


	notifyInfo = (FILE_NOTIFY_INFORMATION*) new char[sizeof (FILE_NOTIFY_INFORMATION) + sizeof(WCHAR) * MAX_PATH];
	OVERLAPPED ov;




	while(config->ThreadState())
	{

		b = ReadDirectoryChangesW(
			config->m_DirectoryHandle,
			(LPVOID)notifyInfo,
			2048,
			FALSE,
			FILE_NOTIFY_CHANGE_LAST_WRITE,
			&ret,
			NULL,
			NULL);


		SingeltonLogfile::Instance()->Write("ReadDirectoryChangesW continue.\n");

		if (b)
		{
			SingeltonLogfile::Instance()->Write("Directory state changed.\n");

			if (config->EndsWithTagstoreConfigfilename(notifyInfo->FileName, notifyInfo->FileNameLength))
			{
				SingeltonLogfile::Instance()->Write("Config file similar to change value.\n");
				config->Read();

				config->UpdateDriverStores();
			}
		}


	}


	if (!ReleaseMutex(mutex))
		SingeltonLogfile::Instance()->Write("Thread wait release failed.\n");

	delete [] notifyInfo;

	SingeltonLogfile::Instance()->Write("Config file watcher thread exit.\n");

	return 0;
}

int TagstoreConfig::UpdateDriverStores()
{
	unsigned int r = 0;


	if (!FlMonIPCOpened(m_IPC))
	{
		SingeltonLogfile::Instance()->Write("Unable to update driver config, communication not open.\n");
		return TSC_IPC_NOT_OPEN;
	}

	r = FlMonIPCSendClearWatchDir(m_IPC);

	if(r)
	{
		SingeltonLogfile::Instance()->Write("Unable to update driver config, unable to delete dirlist.\n");
		return TSC_FILE_UNABLE_TO_DELETE_DIRS;
	}


	unsigned int psize = 0;
	WCHAR wdir[1024];
	unsigned int subcount = m_SubpathsReadWatch.size();
	bool only_basepath = (subcount == 0);
	
	if (only_basepath)
	{
		SingeltonLogfile::Instance()->Write("Not using subpaths.\n");
	}


	for (int i=0; i < StoreCount(); i++)
	{
		
		string dosdevice = ConvertToDosDevicename(m_Stores[i].Path);



		if (only_basepath)
		{
			r = MultiByteToWideChar(CP_ACP, 0, dosdevice.c_str(), -1, 
				(LPWSTR)wdir, 1024);

			if(!r)
			{
				SingeltonLogfile::Instance()->Write("Unable to update driver config, path convert failed.\n");
				return TSC_FILE_UNABLE_TO_ADD_DIR;
			}
		}

		string strsubpath;


		for (int n = 0; ((only_basepath)? n == 0 : n < subcount); n++)
		{

			if (only_basepath)
			{
				r = FlMonIPCSendAddWatchDir(m_IPC, wdir, dosdevice.size());
			}
			else
			{
				WCHAR wsubdir[2048];
				wsubdir[0] = '\0';
				strsubpath = dosdevice + "\\" + m_SubpathsReadWatch[n];

				//SingeltonLogfile::Instance()->WriteS("Using path: ",
				//	(only_basepath) ? dosdevice : strsubpath);

				r = MultiByteToWideChar(CP_ACP, 0, strsubpath.c_str(), -1, 
					(LPWSTR)wsubdir, 2048);

				if(!r)
				{
					SingeltonLogfile::Instance()->Write("Unable to update driver config, path convert failed.\n");
					return TSC_FILE_UNABLE_TO_ADD_DIR;
				}

				r = FlMonIPCSendAddWatchDir(m_IPC, wsubdir, strsubpath.size());
			}

			if(r)
			{
				if (r == FLTIPC_DIR_EXISTS)
					SingeltonLogfile::Instance()->WriteS("Unable to update driver config, unable to add dir, dir already exists.",
					(only_basepath) ? dosdevice : strsubpath);
				else if (r == FLTIPC_INVALID_PARAMETER)
					SingeltonLogfile::Instance()->WriteS("Unable to update driver config, unable to add dir, invalid driver parameter.",
					(only_basepath) ? dosdevice : strsubpath);
				else
					SingeltonLogfile::Instance()->WriteS("Unable to update driver config, unable to add dir, unknown error.",
					(only_basepath) ? dosdevice : strsubpath);

				return TSC_FILE_UNABLE_TO_ADD_DIR;
			}
			else
			{
				SingeltonLogfile::Instance()->WriteS("Updated driver config, dir added.",
					(only_basepath) ? dosdevice : strsubpath);
			}
		}
	}

	return TSC_SUCCESS;
}

int TagstoreConfig::RestartDeleteWatchers()
{

	SingeltonLogfile::Instance()->Write1("Adding delete watchers: ", StoreCount());
	SingeltonLogfile::Instance()->Write1("Subpaths (del): ", m_SubpathsDeleteWatch.size());
	

	if (m_pDeleteWatcher)
		delete m_pDeleteWatcher;


	m_pDeleteWatcher = new TsDeletedWatcher();
	
	if (!m_pDeleteWatcher)
	{
		SingeltonLogfile::Instance()->Write("Error allocating delete watcher threads.\n");
		return 0;
	}


	for (int i=0; i < StoreCount(); i++)
	{
		string base = m_Stores[i].Path;

		for (int n = 0; n < m_SubpathsDeleteWatch.size(); n++)
		{
			string path = base + string("\\") + m_SubpathsDeleteWatch[n];
			m_pDeleteWatcher->AddDirectory(path);
		}

	}

	return TSC_SUCCESS;
}


