package org.me.TagStore.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This class is used to write new tagged files to a log.
 * 
 * @author Johannes Anderwald
 * 
 */
public class FileLog {

	/**
	 * stores the tags of the file
	 */
	public final static String TAGS = "tags=";

	/**
	 * stores the time stamp of the file
	 */
	public final static String TIMESTAMP = "timestamp=";

	/**
	 * all tags have per default the android tag associated
	 */
	public final static String SHARED_TAG="android";
	
	
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
	 * config format
	 */
	public final static String CONFIG_FORMAT_STMT ="config_format";
	

	/**
	 * seperates keys from values
	 */
	public final static String VALUE_SEPERATOR="=";
	
	/**
	 * stores the files section string
	 */
	public final static String FILES_SECTION="[files]";

	/**
	 * supported config format version
	 */
	private static final int CONFIG_FORMAT_TYPE = 1;

	/**
	 * file log writer
	 */
	private BufferedWriter m_writer = null;
	
	/**
	 * file log reader
	 */
	private BufferedReader m_reader = null;
	

	/**
	 * initializes the file log
	 *  @param read determines if the log should be opened for reading or writing
	 * @return
	 */
	private boolean initialize(String path, boolean read) {

		//
		// create log file object
		//
		File log_file = new File(path);

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
					false));
			
				if (true)
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

	/**
	 * This function creates an empty log file. All previous entries are truncated
	 */
	public void clearLogEntries(String path) {
		
		//
		// initialize sync reader
		//
		boolean init = initialize(path, true);
	
		//
		// check if it has been initialized
		//
		if (!init || m_reader == null)
			return;		
		
		try
		{
			//
			// close log
			//
			m_reader.close();
		}
		catch(IOException exc)
		{
			exc.printStackTrace();
		}
	}
	
	
	/**
	 * reads all sync log entries into the provided arraylist
	 * @param entries populated list of sync log entries after reading the log
	 */
	public void readLogEntries(String path, String item_path_prefix, ArrayList<SyncLogEntry> entries) {
		
		//
		// initialize sync reader
		//
		boolean init = initialize(path, true);
		
		//
		// check if it has been initialized
		//
		if (!init || m_reader == null)
			return;
		
		try
		{
			boolean settings_section_found = false;
			boolean config_format_found = false;
			String line;
			
			do
			{
				// fetch line
				line = m_reader.readLine();
				
				if (line.length() == 0)
					continue;
				
				//Logger.i(line);
				
				if (line.compareTo(FileLog.SETTINGS_SECTION) == 0)
				{
					if (settings_section_found)
					{
						// invalid config file
						Logger.e("duplication settings section found");
						break;
					}
					
					// found settings section
					settings_section_found = true;
					
					// skip line
					continue;
				}
				
				if (line.startsWith(FileLog.CONFIG_FORMAT_STMT))
				{
					// extract value
					if (!line.contains("="))
					{
						// invalid definition
						Logger.e("invalid definition: " + line);
						break;
					}
					
					// extract value
					String value = line.substring(line.indexOf("=") + 1);
					
					// trim white spaces
					value = value.trim();
					
					// extract value
					try
					{
						int config_type	 = Integer.parseInt(value);
						if (config_type != FileLog.CONFIG_FORMAT_TYPE)
						{
							Logger.e("unsupported config type:" + config_type);
							break;
						}
					}
					catch(NumberFormatException exc)
					{
						// invalid definition
						Logger.e("invalid config type: " + value);
						break;
					}
					
					if (config_format_found)
					{
						// multiple config format definitions
						Logger.e("multiple config_format definitions");
						break;
					}
					
					// found config format
					config_format_found = true;
					continue;
				}
				
				if (line.compareTo(FileLog.FILES_SECTION) == 0)
				{
					if (!settings_section_found || !config_format_found)
					{
						// missing config format section
						Logger.e("missing config format section");
						break;
					}
					
					// now extract all entries
					readEntries(entries, item_path_prefix);
					break;
				}
					
			}while(line != null);
			
			// close log
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
	private void readEntries(ArrayList<SyncLogEntry> entries, String path) {
		
		// append seperator
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

				// check for white spaces and empty lines
				line.trim();
				if (line.length() == 0)
					continue;
				
				
				
				//
				// split line
				//
				String file_name = URLDecoder.decode(line.substring(0, line.indexOf(FileLog.PATH_SEPARATOR)));
				String keyword = line.substring(line.indexOf(FileLog.PATH_SEPARATOR) + 1, line.indexOf(FileLog.VALUE_SEPERATOR)+1);
				String value = line.substring(line.indexOf(FileLog.VALUE_SEPERATOR)+1);
				
				if (value.startsWith("\""))
				{
					//
					// remove double quotes
					//
					value = value.substring(1, value.length() - 1);
				}
				
				//
				// replace '\' with '/' because PythonQt has problems with keys which expose a hierarchy
				//
				file_name = file_name.replace("\\", "/");
				
				//Logger.i(line);
				//Logger.i("path: " + path + "file_name: " + file_name + " keyword: " + keyword + " value: " + value);
				
				if (file_name.compareTo(current_file_name) != 0)
				{
					log_entry = new SyncLogEntry();
					log_entry.m_file_name = path + file_name;
					current_file_name = file_name;
					entries.add(log_entry);
				}
				
				if (keyword.compareTo(FileLog.TIMESTAMP) == 0) {
					log_entry.m_time_stamp = value;
				}
				else if (keyword.compareTo(FileLog.TAGS) == 0)
				{
					log_entry.m_tags = value.replace(SHARED_TAG, "");
				}
				
			}while(true);
		}
		catch(IOException exc)
		{
			exc.printStackTrace();
		}
	}
	
	/**
	 * writes the entries of the array into the log
	 * @param entries to be written
	 * @return true on success
	 */
	public boolean writeLogEntries(String filepath, String strip_path_prefix, ArrayList<SyncLogEntry> entries) {
		
		//
		// initialize log
		//
		boolean result = initialize(filepath, false);
		if (!result)
			return result;
		
		for (SyncLogEntry entry : entries)
		{

			String file_name = entry.m_file_name;
			
			if (entry.m_file_name.startsWith(strip_path_prefix))
			{
				//
				// strip that path as this path is not available for the external sync
				// application
				//				
				file_name = entry.m_file_name.substring(strip_path_prefix.length() + 1);
			}
			
			//
			// add log entry
			//
			processFile(file_name, entry.m_tags +  "," + SHARED_TAG, entry.m_time_stamp, entry.m_hash_sum);
		}
		
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
		return true;
		
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
		// Python Qt requires does not support slashes(\/) in the settings key name
		// a hack arround that is to convert all forward slashes to backward and escape the whole file name
		//
		file_name = file_name.replace("/", "\\");

		//
		// escape the file name
		//
		file_name = URLEncoder.encode(file_name);
		
		//
		// Python Qt does not support decoding plus signs as it is a legal character
		//
		file_name = file_name.replace("+", "%20");
		
		//
		// format the lines
		//
		String tag_line = file_name + PATH_SEPARATOR + TAGS + "\"" + tags
				+ "\"";
		String timestamp_line = file_name + PATH_SEPARATOR + TIMESTAMP + date;

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
