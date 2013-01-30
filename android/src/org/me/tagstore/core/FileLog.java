package org.me.tagstore.core;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This class is used to write new tagged files to a log.
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
	public final static String SHARED_TAG = "android";

	/**
	 * stores the path separator
	 */
	public final static String PATH_SEPARATOR = "\\";

	/**
	 * stores the settings section string
	 */
	public final static String SETTINGS_SECTION = "[settings]";

	/**
	 * stores the config format
	 */
	public final static String CONFIG_FORMAT = "config_format=1";

	/**
	 * config format
	 */
	public final static String CONFIG_FORMAT_STMT = "config_format";

	/**
	 * seperates keys from values
	 */
	public final static String VALUE_SEPERATOR = "=";

	/**
	 * stores the files section string
	 */
	public final static String FILES_SECTION = "[files]";

	/**
	 * supported config format version
	 */
	private static final int CONFIG_FORMAT_TYPE = 1;

	/**
	 * writes the an empty header
	 * 
	 * @param writer
	 * @return
	 */
	public boolean clearEntries(BufferedWriter writer) {

		try {
			writer.append(SETTINGS_SECTION);
			writer.newLine();
			writer.append(CONFIG_FORMAT);
			writer.newLine();
			writer.newLine();
			writer.append(FILES_SECTION);
			writer.newLine();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * This function creates an empty log file. All previous entries are
	 * truncated
	 */
	public boolean clearLogEntries(String path) {

		BufferedWriter writer = null;

		// construct file writer
		try {
			writer = new BufferedWriter(new FileWriter(path, false));
		} catch (IOException exc) {
			return false;
		}

		// clear entries
		boolean clear_entries = clearEntries(writer);

		try {
			// close log
			writer.close();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		return clear_entries;
	}

	public boolean readLogEntries(BufferedReader reader,
			ArrayList<SyncLogEntry> entries, String item_path_prefix, boolean remove_shared_tag) {

		try {
			boolean settings_section_found = false;
			boolean config_format_found = false;
			String line;

			do {
				// fetch line
				line = reader.readLine();
				if (line == null)
					return false;

				if (line.length() == 0)
					continue;

				if (line.compareTo(FileLog.SETTINGS_SECTION) == 0) {
					if (settings_section_found) {
						// invalid config file
						Logger.e("duplication settings section found");
						return false;
					}

					// found settings section
					settings_section_found = true;

					// skip line
					continue;
				}

				if (line.startsWith(FileLog.CONFIG_FORMAT_STMT)) {
					// extract value
					if (!line.contains("=")) {
						// invalid definition
						Logger.e("invalid definition: " + line);
						return false;
					}

					// extract value
					String value = line.substring(line.indexOf("=") + 1);

					// trim white spaces
					value = value.trim();

					// extract value
					try {
						int config_type = Integer.parseInt(value);
						if (config_type != FileLog.CONFIG_FORMAT_TYPE) {
							Logger.e("unsupported config type:" + config_type);
							return false;
						}
					} catch (NumberFormatException exc) {
						// invalid definition
						Logger.e("invalid config type: " + value);
						return false;
					}

					if (config_format_found) {
						// multiple config format definitions
						Logger.e("multiple config_format definitions");
						return false;
					}

					// found config format
					config_format_found = true;
					continue;
				}

				if (line.compareTo(FileLog.FILES_SECTION) == 0) {
					if (!settings_section_found || !config_format_found) {
						// missing config format section
						Logger.e("missing config format section");
						return false;
					}

					// now extract all entries
					return readEntries(reader, entries, item_path_prefix, remove_shared_tag);
				}

			} while (line != null);

			// Java BUG
			return false;
		} catch (IOException exc) {
			exc.printStackTrace();
			return false;
		}
	}

	/**
	 * reads all sync log entries into the provided arraylist
	 * 
	 * @param entries
	 *            populated list of sync log entries after reading the log
	 */
	public boolean readLogEntries(String path, String item_path_prefix,
			ArrayList<SyncLogEntry> entries, boolean remove_shared_tag) {

		//
		// initialize sync reader
		//
		BufferedReader reader = null;

		try {
			//reader = new BufferedReader(new FileReader(path));
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "ISO-8859-15"));
			
		} catch (IOException e) {
			return false;
		}

		//
		// read entries
		//
		boolean result = readLogEntries(reader, entries, item_path_prefix, remove_shared_tag);

		try {
			reader.close();
		} catch (IOException exc) {

		}
		return result;
	}

	/**
	 * parses the file section
	 * 
	 * @param entries
	 *            stores the initialized SyncLogEntry objects
	 */
	@SuppressLint("NewApi")
	private boolean readEntries(BufferedReader reader,
			ArrayList<SyncLogEntry> entries, String path, boolean remove_shared_tag) {

		// append seperator
		path += File.separator;

		String current_file_name = "";
		SyncLogEntry log_entry = null;

		try {
			do {
				String line = reader.readLine();
				if (line == null)
					break;
				
				// check for white spaces and empty lines
				line.trim();
				if (line.length() == 0)
					continue;

				//
				// split line
				//
				if (line.indexOf(FileLog.PATH_SEPARATOR) == -1
						|| line.indexOf(FileLog.VALUE_SEPERATOR) == -1) {

					Logger.e("missing path or value separator");
					return false;
				}

				String file_name = URLDecoder.decode(line.substring(0,
						line.indexOf(FileLog.PATH_SEPARATOR)));
				String keyword = line.substring(
						line.indexOf(FileLog.PATH_SEPARATOR) + 1,
						line.indexOf(FileLog.VALUE_SEPERATOR) + 1);
				String value = line.substring(line
						.indexOf(FileLog.VALUE_SEPERATOR) + 1);

				if (value.startsWith("\"")) {
					//
					// remove double quotes
					//
					value = value.substring(1, value.length() - 1);
				}

				//
				// replace '\' with '/' because PythonQt has problems with keys
				// which expose a hierarchy
				//
				file_name = file_name.replace("\\", "/");

				Logger.i(line);
				Logger.i("path: " + path + "file_name: " + file_name
						+ " keyword: " + keyword + " value: " + value);

				if (file_name.compareTo(current_file_name) != 0) {
					log_entry = new SyncLogEntry();
					log_entry.m_file_name = path + file_name;
					current_file_name = file_name;
					entries.add(log_entry);
				}

				if (keyword.compareTo(FileLog.TIMESTAMP) == 0) {
					log_entry.m_time_stamp = value;
				} else if (keyword.compareTo(FileLog.TAGS) == 0) {
					
					if (remove_shared_tag)
						log_entry.m_tags = value.replace(SHARED_TAG, "");
					else
						log_entry.m_tags = value;
				}

			} while (true);
		} catch (IOException exc) {
			exc.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean writeLogEntries(BufferedWriter writer,
			String strip_path_prefix, ArrayList<SyncLogEntry> entries, boolean append_shared_tag) {

		try {
			writer.append(SETTINGS_SECTION);
			writer.newLine();
			writer.append(CONFIG_FORMAT);
			writer.newLine();
			writer.newLine();
			writer.append(FILES_SECTION);
			writer.newLine();
		} catch (IOException e) {
			return false;
		}

		for (SyncLogEntry entry : entries) {

			String file_name = entry.m_file_name;

			if (entry.m_file_name.startsWith(strip_path_prefix)
					&& strip_path_prefix.length() > 0) {
				//
				// strip that path as this path is not available for the
				// external sync
				// application
				//
				file_name = entry.m_file_name.substring(strip_path_prefix
						.length() + 1);
			}

			String tags = entry.m_tags;
			if (append_shared_tag)
				tags += "," + SHARED_TAG;
			
			
			//
			// add log entry
			//
			if (!processFile(writer, file_name,
					tags, entry.m_time_stamp,
					entry.m_hash_sum))
				return false;
		}
		return true;
	}

	/**
	 * writes the entries of the array into the log
	 * 
	 * @param entries
	 *            to be written
	 * @return true on success
	 */
	public boolean writeLogEntries(String filepath, String strip_path_prefix,
			ArrayList<SyncLogEntry> entries, boolean append_shared_tag) {

		// construct file writer
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "ISO-8859-15"));
					
		} catch (IOException exc) {
			return false;
		}

		// write entries
		boolean result = writeLogEntries(writer, strip_path_prefix, entries, append_shared_tag);

		//
		// close the log
		//
		try {
			writer.close();
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
	private boolean processFile(BufferedWriter writer, String file_name,
			String tags, String date, String hashsum) {

		//
		// Python Qt requires does not support slashes(\/) in the settings key
		// name
		// a hack arround that is to convert all forward slashes to backward and
		// escape the whole file name
		//
		file_name = file_name.replace("/", "\\");

		//
		// escape the file name
		//
		file_name = URLEncoder.encode(file_name);

		//
		// Python Qt does not support decoding plus signs as it is a legal
		// character
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
			writer.append(tag_line);
			writer.newLine();

			//
			// write the time stamp
			//
			writer.append(timestamp_line);
			writer.newLine();
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
