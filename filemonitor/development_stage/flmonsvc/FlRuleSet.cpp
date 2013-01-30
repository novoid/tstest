
#include <time.h>
#include "../flmonflt/FlIpc.h"
#include "FsLogFile.h"
#include "FlRuleSet.h"



#if defined(_MSC_VER) || defined(_MSC_EXTENSIONS)
#define DELTA_EPOCH_IN_MICROSECS  11644473600000000Ui64
#else
#define DELTA_EPOCH_IN_MICROSECS  11644473600000000ULL
#endif

///////////////////////////////////////////////////////////////////////////

#define DEFAULT_LINK_TIMEOUT_MS			300




FlRuleSet::FlRuleSet()
{
}


FlRuleSet::~FlRuleSet()
{
}



bool FlRuleSet::GetTime(struct timeval *tv)
{
	FILETIME ft;
	unsigned __int64 tmpres = 0;
	static int tzflag;


	if (NULL != tv)
	{
		GetSystemTimeAsFileTime(&ft);

		tmpres |= ft.dwHighDateTime;
		tmpres <<= 32;
		tmpres |= ft.dwLowDateTime;

		/*converting file time to unix epoch*/
		tmpres -= DELTA_EPOCH_IN_MICROSECS; 
		tmpres /= 10;  /*convert into microseconds*/
		tv->tv_sec = (long)(tmpres / 1000000UL);
		tv->tv_usec = (long)(tmpres % 1000000UL);

		return true;
	}

	return false;
}

bool FlRuleSet::RulesApply(char type, string path)
{
	if (EqualsLastPathname(path))
	{
		SingeltonLogfile::Instance()->WriteS("Ruleset 'EqualsLastPathname' applied to: ", path);
		return true;
	}

	if (IsLinkOpenInTime())
	{
		SingeltonLogfile::Instance()->WriteS("Ruleset 'LinkOpenInTime' applied to: ", path);
		return true;
	}


	return false;
}

///////////////////////////////////////////////////////////////////////////
// Last Pathname Rule
///////////////////////////////////////////////////////////////////////////
void FlRuleSet::SetLastPathName(string path)
{
	m_sLastPathname = path;
}

bool FlRuleSet::EqualsLastPathname(string path)
{
	bool b = (m_sLastPathname == path);
	SetLastPathName(path);
	return b;
}


///////////////////////////////////////////////////////////////////////
// File open after link
///////////////////////////////////////////////////////////////////////
bool FlRuleSet::IsLink(char type, string path)
{
	if (type != MESSAGE_NOTIFY_FILE_READ)
		return false;

	int size = path.size();

	if (size < 4)
		return false;

	if (path[size-1] == 'k'
		&& path[size-2] == 'n'
		&& path[size-3] == 'l'
		&& path[size-4] == '.'
		)
	{
		return true;
	}

	return false;
}

void FlRuleSet::SetLinkOpened(char type, string path)
{
	bool b = IsLink(type, path);

	if (!GetTime(&m_tLinkOpenTime) && !b)
	{
		m_tLinkOpenTime.tv_sec = 0;
		m_tLinkOpenTime.tv_usec = 0;
	}
}

bool FlRuleSet::IsLinkOpenInTime()
{
	if (!m_tLinkOpenTime.tv_sec && !m_tLinkOpenTime.tv_usec)
		return false;

	timeval now;

	GetTime(&now);

	unsigned int diff = (now.tv_sec-m_tLinkOpenTime.tv_sec)*1000 +
		(now.tv_usec-m_tLinkOpenTime.tv_usec)/1000;

	if (diff < 0)
		return false;

	if (diff < DEFAULT_LINK_TIMEOUT_MS)
		return true;

	return false;
}