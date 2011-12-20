
#include <string.h>
#include <time.h>
#include <iostream>
#include <sstream>
#include "fslogfile.h"



using namespace std;

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
FsLogFile::FsLogFile()
{
	m_Fildes = NULL;
	
}

FsLogFile::~FsLogFile()
{
	Close();
}


/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FsLogFile::Open(string path)
{

	if (m_Fildes)
		return FSL_FILE_ALREADY_OPENED;

	m_Filename = path;
	m_Fildes = fopen(m_Filename.c_str(), "a+");

	if (!m_Fildes)
		return FSL_FILE_UNABLE_TO_OPEN;

	return FSL_SUCCESS;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FsLogFile::Close()
{
	if (!m_Fildes)
		return FSL_INVALID_PARAMETER;

	fclose(m_Fildes);
	m_Fildes = NULL;

	return FSL_SUCCESS;
}

/*! .

	\param [in]
	\param [out]
	\param [in,out]

	\pre
	\post
	\return

*/
unsigned int FsLogFile::Write(string text, BOOLEAN addtime,
	string separator)
{
	string complete_text;
	char strtime[512];
	time_t rawtime;
	struct tm * timeinfo;


#ifdef _DEBUG

	Open(m_Filename);

#endif

	if (!m_Fildes)
		return FSL_FILE_UNABLE_TO_OPEN;


	if (addtime)
	{
		time (&rawtime);
		timeinfo = localtime ( &rawtime );

		strftime(strtime, FSL_MAX_LINE_LENGTH, "%d.%m.%Y %H:%M:%S", timeinfo);

		complete_text = strtime + separator;
	}

	complete_text = complete_text + text;

	fwrite(complete_text.c_str(), complete_text.size(), 1, m_Fildes);

	fflush(m_Fildes);

	cout << complete_text;

#ifdef _DEBUG

	Close();

#endif

	return FSL_SUCCESS;
}

unsigned int FsLogFile::Write(string text)
{
	return Write(text, true, " - ");
}

unsigned int FsLogFile::Write1(string text, int errcode)
{
	stringstream ss;

	ss << text << errcode << endl;

	return Write(ss.str());
}

unsigned int FsLogFile::WriteS(string text, string text2)
{
	stringstream ss;

	ss << text << " '" << text2 << "'" << endl;

	return Write(ss.str());
}