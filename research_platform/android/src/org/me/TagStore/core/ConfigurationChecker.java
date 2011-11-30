package org.me.TagStore.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;


/**
 * checks if all configuration files exists. If not they are created
 * @author Johannes Anderwald
 *
 */
public class ConfigurationChecker implements org.me.TagStore.core.StorageTimerTask.TimerTaskCallback {

	
	/**
	 * stores the context
	 */
	private final Context m_context;
	
	/**
	 * constructor of TagStoreFileChecker
	 */
	public ConfigurationChecker(Context context) {
		m_context = context;
	}
	
	/**
	 * copys the contents of the file
	 * @param reader reader of resource file
	 * @param writer writer of the target file
	 */
	private void copyFile(BufferedReader reader, BufferedWriter writer) {
		
		try
		{
			do
			{
				//
				// read line
				//
				String line = reader.readLine();
				if (line == null)
					break;
			
				//
				// write line
				//
				writer.write(line);
				writer.newLine();
			
			}while(true);
		}
		catch(IOException exc)
		{
			exc.printStackTrace();
		}
	}
	
	/**
	 * copies the resource
	 * @param resource_name name of the resource
	 * @param target_name target file name
	 */
	private void copyResource(String resource_name, File target_name) {
		
		try
		{
			//
			// open input stream
			//
			InputStream raw_stream = m_context.getAssets().open(resource_name);
		
			//
			// create reader
			//
			BufferedReader reader = new BufferedReader(new InputStreamReader(raw_stream, "UTF8"));
		
			//
			// create writer
			//
			BufferedWriter writer = new BufferedWriter(new FileWriter(target_name, false));
		
			//
			// copy file
			//
			copyFile(reader, writer);
			
			//
			// close file objects
			//
			reader.close();
			writer.close();
		}
		catch(IOException exc)
		{
			Logger.e("Exception while copying resource: " + resource_name);
		}
	}
	
	/**
	 * called when the disk is available
	 */
	public void diskAvailable() {
		
		Logger.i("ConfigurationChecker::diskAvailable");
		
		//
		// get sdcard directory
		//
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		//
		// append seperator
		//
		path += File.separator + ConfigurationSettings.LOG_DIRECTORY;
		
		//
		// check if it exists
		//
		File file = new File(path);
		if (!file.exists())
		{
			//
			// create directory
			//
			boolean created = file.mkdir();
			if (!created)
			{
				Logger.e("Error: failed to create directory: " + ConfigurationSettings.LOG_DIRECTORY);
				return;
			}
		}

		//
		// check if cfg file exists
		//
		File cfg_file = new File(path + File.separator + ConfigurationSettings.CFG_FILENAME);
		if (!cfg_file.exists())
		{
			//
			// copy default config
			//
			copyResource(ConfigurationSettings.CFG_FILENAME, cfg_file);
		}
		
		//
		// check if tgs file exists
		//
		File tgs_file = new File(path + File.separator + ConfigurationSettings.LOG_FILENAME);
		if (!tgs_file.exists())
		{
			//
			// copy default config
			//
			copyResource(ConfigurationSettings.LOG_FILENAME, tgs_file);
		}
		
		//
		// now unregister ourselves
		//
		StorageTimerTask task = StorageTimerTask.acquireInstance();
		task.removeCallback(this);
	}

	public void diskNotAvailable() {
		
	}
}
