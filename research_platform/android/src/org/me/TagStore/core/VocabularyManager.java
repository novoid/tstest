package org.me.TagStore.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * This class is responsible for loading vocabularies
 * @author Johannes Anderwald
 */
public class VocabularyManager {

	/**
	 * reference to single instance
	 */
	private static VocabularyManager s_instance = null;

	/**
	 * application context
	 */
	private final Context m_context;
	
	/**
	 * vocabulary list
	 */
	private HashSet<String> m_vocabulary = null;
	
	
	/**
	 * returns the instance to the vocabulary manager
	 * @return
	 */
	public static VocabularyManager getInstance() {
		
		return s_instance;
	}
	
	/**
	 * builds the single instance of the vocabulary manager
	 * @param context application context
	 */
	public static void buildVocabularyManager(Context context) {
		
		s_instance = new VocabularyManager(context);
	}

	/**
	 * application context
	 * @param context
	 */
	private VocabularyManager(Context context) {
		m_context = context;
		
		if (doesVocabularyFileExist())
			loadControlledVocabulary();
	}
	
	/**
	 * enables / disables the vocabulary state
	 * @param enabled
	 */
	public void setControlledVocabularyState(boolean enabled) {
		
		//
		// acquire shared settings
		//
		SharedPreferences settings = m_context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get editor
		//
		SharedPreferences.Editor editor = settings.edit();

		//
		// sets the controlled vocabulary state 
		//
		editor.putBoolean(ConfigurationSettings.CONTROLLED_VOCABULARY_STATE, enabled);
		
		
		//
		// done
		//
		editor.commit();
	}
	
	/**
	 * gets the controlled vocabulary state
	 * @return if true, controlled vocabulary is enabled otherwise disabled
	 */
	public boolean getControlledVocabularyState() {
		
		//
		// acquire shared settings
		//
		SharedPreferences settings = m_context.getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		
		//
		// gets default vocabulary state
		//
		return settings.getBoolean(ConfigurationSettings.CONTROLLED_VOCABULARY_STATE, ConfigurationSettings.DEFAULT_CONTROLLED_VOCABULARY_STATE);
	}
	
	/**
	 * returns true when the vocabulary file exists
	 * @return
	 */
	public boolean doesVocabularyFileExist() {
		
		//
		// get path
		//
		String voc_file = getVocabularyFilePath();
		
		//
		// create file object
		//
		File file = new File(voc_file);
		
		//
		// check if it exists
		//
		return (file.exists() && file.isFile());
	}
	
	
	/**
	 * builds the path to vocabulary file
	 * @return path
	 */
	private String getVocabularyFilePath() {
		
		//
		// get path to external disk
		//
		StringBuilder path = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
		
		//
		// append tagstore directory
		//
		path.append(File.separator);
		path.append(ConfigurationSettings.TAGSTORE_DIRECTORY);
		
		//
		// append configuration directory
		//
		path.append(File.separator);
		path.append(ConfigurationSettings.CONFIGURATION_DIRECTORY);
		
		//
		// append vocabulary file name
		//
		path.append(File.separator);
		path.append(ConfigurationSettings.VOCABULARY_FILENAME);
		
		//
		// done
		//
		return path.toString();
	}
	
	/**
	 * loads the vocabulary into storage
	 * @return true on success
	 */
	private boolean loadControlledVocabulary() {
		
		//
		// get path to vocabulary
		//
		String path = getVocabularyFilePath();
		
		//
		// build result list
		//
		m_vocabulary = new HashSet<String>();
		ArrayList<String> temp_list = new ArrayList<String>();

		//
		// read contents into temporary list
		//
		if (IOUtils.readFile(path, temp_list))
		{
			//
			// add to vocabulary
			//
			m_vocabulary.addAll(temp_list);
			
			//
			// success
			//
			return true;
		}
		
		//
		// failed
		//
		return false;
	}
	
	
	/**
	 * checks if the given tag name is contained in the controlled vocabulary
	 * @param tag_name name of the tag
	 * @return true if it exists
	 */
	public boolean isTagPartOfControlledVocabulary(String tag_name) {
		
		//
		// sanity check
		//
		Assert.assertTrue(m_vocabulary != null);
		
		//
		// check if it is part of vocabulary
		//
		return m_vocabulary.contains(tag_name);
	}

	/**
	 * returns the whole vocabulary
	 * @return
	 */
	public final HashSet<String> getVocabulary() {
		return m_vocabulary;
	}
	
}
