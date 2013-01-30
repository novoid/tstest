package org.me.tagstore.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import junit.framework.Assert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * This class is responsible for loading vocabularies
 */
public class VocabularyManager {

	/**
	 * application context
	 */
	private Context m_context;

	/**
	 * vocabulary list
	 */
	private LinkedHashSet<String> m_vocabulary = null;

	private IOUtils m_utils;

	/**
	 * builds single instance of the vocabulary manager
	 * 
	 * @param context
	 *            application context
	 * @param utils
	 *            io class
	 * @return
	 */
	public void initializeVocabularyManager(Context context,
			IOUtils utils) {


		// initialize
		initialize(context, utils);
	}

	/**
	 * initialize vocabulary manager
	 * 
	 * @param context
	 *            application context
	 */
	private void initialize(Context context, IOUtils utils) {

		// store context
		m_context = context;
		m_utils = utils;
		if (doesVocabularyFileExist())
			loadControlledVocabulary();
	}

	/**
	 * enables / disables the vocabulary state
	 * 
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
		editor.putBoolean(ConfigurationSettings.CONTROLLED_VOCABULARY_STATE,
				enabled);

		//
		// done
		//
		editor.commit();
	}

	/**
	 * gets the controlled vocabulary state
	 * 
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
		return settings.getBoolean(
				ConfigurationSettings.CONTROLLED_VOCABULARY_STATE,
				ConfigurationSettings.DEFAULT_CONTROLLED_VOCABULARY_STATE);
	}

	/**
	 * returns true when the vocabulary file exists
	 * 
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
	 * 
	 * @return path
	 */
	private String getVocabularyFilePath() {

		//
		// get path to external disk
		//
		StringBuilder path = new StringBuilder(Environment
				.getExternalStorageDirectory().getAbsolutePath());

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
	 * 
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
		m_vocabulary = new LinkedHashSet<String>();

		//
		// read contents into temporary list
		//
		ArrayList<String> temp_list = m_utils.readFile(path);

		//
		// was it successfull
		//
		if (temp_list != null) {
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
	 * 
	 * @param tag_name
	 *            name of the tag
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
	 * 
	 * @return
	 */
	public HashSet<String> getVocabulary() {
		return new LinkedHashSet<String>(m_vocabulary);
	}

	/**
	 * performs reloading of the vocabulary
	 * 
	 * @return
	 */
	public boolean loadVocabulary() {

		if (doesVocabularyFileExist())
			return loadControlledVocabulary();

		return false;
	}

}
