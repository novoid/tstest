package org.me.TagStore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is used to write new tagged files to a log.
 * 
 * @author Johannes Anderwald
 * 
 */
public class SyncFileLog {

	/**
	 * log file name
	 */
	public final static String LOG_FILENAME = "store.tgs";

	/**
	 * directory name
	 */
	public final static String LOG_DIRECTORY = ".tagstore";

	/**
	 * stores the tags of the file
	 */
	public final static String TAGS = "tags=";

	/**
	 * stores the time stamp of the file
	 */
	public final static String TIMESTAMP = "timestamp=";

	/**
	 * stores the hash sum of the file
	 */
	public final static String HASHSUM = "hashsum=";

	/**
	 * stores the path separator
	 */
	public final static String PATH_SEPARATOR = "\\";

	/**
       * stores the settings section string
	 */
	public final static String SETTINGS_SECTION ="[settings]";

	/**
	 * stores the config format
	 */
	public final static String CONFIG_FORMAT="config_format=1";

	/**
	 * seperates keys from values
	 */
	public final static String VALUE_SEPERATOR="=";
	
	/**
	 * stores the files section string
	 */
	public final static String FILES_SECTION="[files]";

	/**
	 * file log writer
	 */
	private BufferedWriter m_writer = null;
	
	/**
	 * file log reader
	 */
	private BufferedReader m_reader = null;
	

	/**
	 * returns instance of SyncFileLog
	 * @return SyncFileLog
	 */
	public static SyncFileLog getInstance() {

		//
		// create new instance
		//
		SyncFileLog instance = new SyncFileLog();

		//
		// done
		//
		return instance;
	}

	/**
	 * initializes the file log
	 *  @param read determines if the log should be opened for reading or writing
	 * @return
	 */
	private boolean initialize(boolean read) {

		//
		// get path to be observed (external disk)
		//
		String path = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath();

		//
		// append seperator
		//
		path += File.separator + LOG_DIRECTORY;

		//
		// create file object
		//
		File directory = new File(path);

		//
		// create new directory object
		//
		boolean directory_created = directory.mkdir();

		//
		// informal debug print
		//
		Logger.i("Directory created: " + directory_created);

		//
		// create log file object
		//
		File log_file = new File(path + File.separator + LOG_FILENAME);

		try {
			
			//
			// check if log file exists
			//
			boolean exists = log_file.exists();

			//
			// append / create new log file depending if the file exists
			//
			if (read && exists)
			{
				m_reader = new BufferedReader(new FileReader(log_file));
			}
			else if (!read)
			{
				m_writer = new BufferedWriter(new FileWriter(log_file,
					exists));
			
				if (!exists)
				{
					//
					// prepare file
					//
					m_writer.append(SETTINGS_SECTION);
					m_writer.newLine();
					m_writer.append(CONFIG_FORMAT);
					m_writer.newLine();
					m_writer.newLine();
					m_writer.append(FILES_SECTION);
					m_writer.newLine();

				}
			}
			else
			{
				//
				// read log file but no log file yet exists
				//
				return true;
			}

		} catch (IOException e) {

			//
			// IOException while creating log file
			//
			Logger.e("IOException while creating log file" + log_file.getName());
			return false;
		}

		//
		// completed initialization
		//
		return true;
	}

	public void readLogEntries(ArrayList<SyncLogEntry> entries) {
		
		//
		// initialize sync reader
		//
		boolean init = initialize(true);
		
		//
		// check if it has been initialized
		//
		if (!init || m_reader == null)
			return;
		
		try
		{
			//
			// read first line
			//
			String line = m_reader.readLine();
			if (line.compareTo(SyncFileLog.SETTINGS_SECTION) != 0)
			{
				//
				// file format error
				//
				Logger.e("Error: expected [settings] but got " + line);
				return;
			}
			
			//
			// read config format section
			//
			line = m_reader.readLine();
			if (line.compareTo(SyncFileLog.CONFIG_FORMAT) != 0)
			{
				//
				// file format error
				//
				Logger.e("Error: expected config_format=1 but got " + line);
				return;
			}
			
			//
			// next line is empty, read files section
			//
			m_reader.readLine();
			line = m_reader.readLine();
			if (line.compareTo(SyncFileLog.FILES_SECTION) != 0)
			{
				//
				// file format error
				//
				Logger.e("Error: expected [files] but got " + line);
				return;
			}
			
			//
			// now extract all entries
			//
			readEntries(entries);
			
			//
			// close log
			//
			m_reader.close();
			
		}
		catch(IOException exc)
		{
			exc.printStackTrace();
			return;
		}
		

		
	}
	
	/**
	 * parses the file section
	 * @param entries stores the initialized SyncLogEntry objects
	 */
	private void readEntries(ArrayList<SyncLogEntry> entries) {
		
		//
		// get path to be observed (external disk)
		//
		String path = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath();

		//
		// append seperator
		//
		path += File.separator;
		
		String current_file_name ="";
		SyncLogEntry log_entry = null;
		
		try
		{
			do
			{	
				String line = m_reader.readLine();
				if (line == null)
					break;

				//
				// split line
				//
				String file_name = line.substring(0, line.indexOf(SyncFileLog.PATH_SEPARATOR));
				String keyword = line.substring(line.indexOf(SyncFileLog.PATH_SEPARATOR) + 1, line.indexOf(SyncFileLog.VALUE_SEPERATOR)+1);
				String value = line.substring(line.indexOf(SyncFileLog.VALUE_SEPERATOR)+1);
				if (value.startsWith("\""))
				{
					//
					// remove double quotes
					//
					value = value.substring(1, value.length() - 1);
				}
				
				Logger.e("file_name " + file_name + " keyword: " + keyword + " value: " + value);
				
				if (file_name.compareTo(current_file_name) != 0)
				{
					log_entry = new SyncLogEntry();
					log_entry.m_file_name = path + file_name;
					entries.add(log_entry);
				}
				
				if (keyword.compareTo(SyncFileLog.HASHSUM) == 0) {
					log_entry.m_hash_sum = value;
				}
				else if (keyword.compareTo(SyncFileLog.TIMESTAMP) == 0) {
					log_entry.m_time_stamp = value;
				}
				else if (keyword.compareTo(SyncFileLog.TAGS) == 0)
				{
					log_entry.m_tags = value;
				}
				
			}while(true);
		}
		catch(IOException exc)
		{
			exc.printStackTrace();
		}
	}
	
	
	/**
	 * writes a tagged file to a log
	 * 
	 * @param file_name
	 *            name of the file
	 * @param tags
	 *            tags of the file
	 * @param date
	 *            date of the file
	 * @param hashsum
	 *            hash sum of the file
	 * @return true when successfully written to the log
	 */
	public boolean writeLogEntry(String file_name, String tags, String date,
			String hashsum) {

		
		//
		// initialize log
		//
		boolean result = initialize(false);
		if (!result)
			return result;
		
		
		//
		// get path to be observed (external disk)
		//
		String path = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath();

		//
		// strip that path as this path is not available for the external sync
		// application
		//
		file_name = file_name.substring(path.length() + 1);

		//
		// process log entry
		//
		result = processFile(file_name, tags, date, hashsum);
		
		//
		// close the log
		//
		try {
			m_writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//
		// done
		//
		return result;
	}

	/**
	 * writes the entry to the log file
	 * 
	 * @param file_name
	 *            file name of the entry
	 * @param tags
	 *            tags of the entry
	 * @param date
	 *            date of the entry
	 * @param hashsum
	 *            hash sum of the file
	 * @return true when successful
	 */
	private boolean processFile(String file_name, String tags, String date,
			String hashsum) {

		//
		// format the lines
		//
		String tag_line = file_name + PATH_SEPARATOR + TAGS + "\"" + tags
				+ "\"";
		String timestamp_line = file_name + PATH_SEPARATOR + TIMESTAMP + date;
		String hashsum_line = file_name + PATH_SEPARATOR + HASHSUM + hashsum;

		try {
			//
			// write the tag
			//
			m_writer.append(tag_line);
			m_writer.newLine();

			//
			// write the time stamp
			//
			m_writer.append(timestamp_line);
			m_writer.newLine();

			//
			// write the hashsum
			//
			m_writer.append(hashsum_line);
			m_writer.newLine();

		} catch (IOException e) {
			Logger.e("IOException while writing to log file");
			return false;
		}

		//
		// completed
		//
		return true;
	}
}
