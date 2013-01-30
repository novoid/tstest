
#ifndef __FlRuleSet_H__
#define __FlRuleSet_H__


#include <string>
#include "SingeltonHolder.h"


using namespace std;




class FlRuleSet
{

	string		m_sLastPathname; // pathname rule
	timeval		m_tLinkOpenTime; // link open time


private:

	bool GetTime(struct timeval *tv);

public:

	FlRuleSet();
	~FlRuleSet();


	bool RulesApply(char type, string path);


	///////////////////////////////////////////////////////////////////////
	// Last Pathname Rule
	///////////////////////////////////////////////////////////////////////
	void SetLastPathName(string path);
	bool EqualsLastPathname(string path);


	///////////////////////////////////////////////////////////////////////
	// File open after link
	///////////////////////////////////////////////////////////////////////
	bool IsLink(char type, string path);
	void SetLinkOpened(char type, string path);
	bool IsLinkOpenInTime();

};





typedef SingeltonHolder<FlRuleSet>	SingeltonRuleSet;

#endif
