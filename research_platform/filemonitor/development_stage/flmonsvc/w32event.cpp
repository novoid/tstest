

#include "w32event.h"


#include <windows.h>
#include <stdio.h>
#include "fleventlog.h"
#include "provider.h"

#pragma comment(lib, "advapi32.lib")

#define PROVIDER_NAME L"MyEventProvider"

// Hardcoded insert string for the event messages.
CONST LPWSTR pBadCommand = L"The command that was not valid";
CONST LPWSTR pNumberOfRetries = L"3";
CONST LPWSTR pSuccessfulRetries = L"0";
CONST LPWSTR pQuarts = L"8";
CONST LPWSTR pGallons = L"2";


void TestEvents()
{
	HANDLE hEventLog = NULL;
	LPWSTR pInsertStrings[2] = {NULL, NULL};
	DWORD dwEventDataSize = 0;

	// The source name (provider) must exist as a subkey of Application.
	hEventLog = RegisterEventSource(NULL, (LPCSTR)PROVIDER_NAME);
	if (NULL == hEventLog)
	{
		wprintf(L"RegisterEventSource failed with 0x%x.\n", GetLastError());
		goto cleanup;
	}


	// This event uses insert strings.
	pInsertStrings[0] = pNumberOfRetries;
	pInsertStrings[1] = pSuccessfulRetries;
	if (!ReportEvent(hEventLog, EVENTLOG_WARNING_TYPE, NETWORK_CATEGORY, MSG_RETRIES, NULL, 2, 0, (LPCWSTR*)pInsertStrings, NULL))
	{
		wprintf(L"ReportEvent failed with 0x%x for event 0x%x.\n", GetLastError(), MSG_RETRIES);
		goto cleanup;
	}

	// This event uses insert strings.
	pInsertStrings[0] = pQuarts;
	pInsertStrings[1] = pGallons;
	if (!ReportEvent(hEventLog, EVENTLOG_INFORMATION_TYPE, UI_CATEGORY, MSG_COMPUTE_CONVERSION, NULL, 2, 0, (LPCWSTR*)pInsertStrings, NULL))
	{
		wprintf(L"ReportEvent failed with 0x%x for event 0x%x.\n", GetLastError(), MSG_COMPUTE_CONVERSION);
		goto cleanup;
	}

	wprintf(L"All events successfully reported.\n");

cleanup:

	if (hEventLog)
		DeregisterEventSource(hEventLog);
}


