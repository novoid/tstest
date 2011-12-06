package org.me.TagStore.core;

import java.io.File;

import org.me.TagStore.R;

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
	 * returns the full path of directory residing on the external disk
	 * @param relative_path relative path to be appended to the path of the external disk
	 * @return String
	 */
	private String getFullPath (String relative_path) {
		
		//
		// get sdcard directory
		//
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		//
		// append relative path
		//
		path += File.separator + relative_path;
		
		//
		// done
		//
		return path;
	}
	
	/**
	 * constructs directory if not existing
	 * @param path to be checked
	 */
	private boolean createDirectoryIfNotExists(String path)
	{
		File file = new File(path);
		
		//
		// check if it exists
		//
		if (file.exists())
		{
			if (file.isFile())
			{
				Logger.e("Error: path " + path + " is a file !!!");
				return false;
			}
		}
		else
		{
			//
			// create directory
			//
			boolean created = file.mkdir();
			if (!created)
			{
				Logger.e("Error: failed to create directory: " + ConfigurationSettings.CONFIGURATION_DIRECTORY);
				return false;
			}			
		}
		
		//
		// done
		//
		return true;
	}
	
	/**
	 * copies to resource file to the target location if it does not yet exist
	 * @param resource_name name of resource file
	 */
	private boolean copyResourceIfNotExists(String resource_name) {
		
		//
		// get full path
		//
		String resource_path = getFullPath(ConfigurationSettings.CONFIGURATION_DIRECTORY) + File.separator + resource_name;
		
		//
		// does it exist
		//
		File file = new File(resource_path);
		if (!file.exists())
		{
			//
			// copy default resource from asset
			//
			return IOUtils.copyResource(m_context, ConfigurationSettings.CFG_FILENAME, file);			
		}

		//
		// file already exists
		//
		return true;
	}
	
	/**
	 * initializes the configuration
	 * @return
	 */
	private boolean initConfiguration() {
		
		//
		// construct default directories
		//
		if(!createDirectoryIfNotExists(getFullPath(ConfigurationSettings.TAGSTORE_DIRECTORY)))
				return false;
		if (!createDirectoryIfNotExists(getFullPath(ConfigurationSettings.CONFIGURATION_DIRECTORY)))
				return false;
		if (!createDirectoryIfNotExists(getFullPath(ConfigurationSettings.TAGSTORE_DIRECTORY) + File.separator + m_context.getString(R.string.storage_directory)))
				return false;
		
		//
		// copy default configuration if does not yet exist
		//
		if (!copyResourceIfNotExists(ConfigurationSettings.CFG_FILENAME))
			return false;
		
		if (!copyResourceIfNotExists(ConfigurationSettings.LOG_FILENAME))
			return false;
		
		//
		// done initializing configuration
		//
		return true;
	}
	
	
	/**
	 * called when the disk is available
	 */
	public void diskAvailable() {
		
		Logger.i("ConfigurationChecker::diskAvailable");
		
		//
		// initialize the configuration
		//
		if (!initConfiguration())
		{
			Logger.e("ConfigurationChecker::initConfiguration failed - retrying later");
			return;
		}
		
		//
		// successfully initialized the configuration, lets remove us from the scheduler
		//
		StorageTimerTask task = StorageTimerTask.acquireInstance();
		task.removeCallback(this);
	}

	/**
	 * called when disk is not available
	 */
	public void diskNotAvailable() {
		
		//
		// no op
		//
	}
}
