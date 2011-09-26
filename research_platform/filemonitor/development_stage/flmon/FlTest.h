#pragma once

#include <windows.h>

class FlTest
{

public:

	FlTest();
	~FlTest();

	bool OpenDirectory(WCHAR * path, bool closehandle);
};

