

#include <Windows.h>
#include <time.h>
#include <string>
#include "../flmon/FlMonIPC.h"
#include "FlRuleSet.h"
#include "filewriter.h"





using namespace std;


string FileWriter::m_BaseDirectory = "";


FileWriter::FileWriter()
{
	m_Registry.Open();	

}

FileWriter::~FileWriter()
{
	m_Registry.Close();
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
string FileWriter::CurrentFilename()
{
	time_t rawtime;
	struct tm * timeinfo;
	char timepart[256];
	string filename;


	time (&rawtime);
	timeinfo = localtime(&rawtime);

	strftime(timepart, 256, "%Y-%m-%d", timeinfo);
	filename = filename + string(timepart);
	filename = filename + string("_tswatch.log");
	filename = m_BaseDirectory + filename;

	//filename = m_BaseDirectory + "test.log";

	return filename;
}




unsigned int FileWriter::Write(char type, string path)
{
	DWORD r = FW_SUCCESS;


	// check if rules apply
	if (SingeltonRuleSet::Instance()->RulesApply(type, path))
	{
		return FW_SUCCESS;
	}



	string fn = CurrentFilename();
	SingeltonRuleSet::Instance()->SetLinkOpened(type, path);


	if (NeedNewFile())
	{
		
		SingeltonLogfile::Instance()->Write("Creating new filesystem logging file.\n");

		Close();
		

		if (Open(fn))
		{
			
			SingeltonLogfile::Instance()->Write("Error opening filesystem logging file.\n");
			return FW_WRITE_FAILED;
		}
		
		UpdateRegLastFilename(fn);
	}
	else
	{
		if (Open(fn))
		{

			SingeltonLogfile::Instance()->Write("Error opening filesystem logging file.\n");
			return FW_WRITE_FAILED;
		}
	}


	string t = "ERR";

	if (type == MESSAGE_NOTIFY_DIR_OPEN)
		t = "DIR_OPEN";
	else if (type == MESSAGE_NOTIFY_FILE_READ)
		t = "FIL_OPEN";
	else if (type == MESSAGE_NOTIFY_FILE_DELETE)
		t = "FIL_DELE";

	path = t + ";" + path + "\n";
	r = FsLogFile::Write(path, TRUE, ";");


	if (r)
		return FW_WRITE_FAILED;
	

	return FW_SUCCESS;
}




unsigned int FileWriter::UpdateRegLastFilename(string filename)
{
	return m_Registry.Update(FREG_LASTFILENAME, filename);
}

unsigned int FileWriter::GetRegLastFilename(string& filename)
{
	return m_Registry.Read(FREG_LASTFILENAME, filename);
}

bool FileWriter::NeedNewFile()
{
	string regfilename;
	string currfilename;


	GetRegLastFilename(regfilename);
	currfilename = CurrentFilename();

	if (regfilename == currfilename)
		return false;

	return true;
}
