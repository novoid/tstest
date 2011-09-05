package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class AddFileTagActivity extends Activity {

	/**
	 * stores the file name of the new file passed as parameter of intent
	 */
	private String m_file_name = "";

	/**
	 * stores the last tags used passed as parameter of intent
	 */
	private String m_last_settings = "";

	/**
	 * stores popular tags
	 */
	ArrayList<String> m_popular_tags;

	/**
	 * stores the recently used tags
	 */
	ArrayList<String> m_recent_tags;

	/**
	 * stores pending file list
	 */
	ArrayList<String> m_pending_files;

	/**
	 * reference to auto complete field
	 */
	AutoCompleteTextView m_text_view;

	/**
	 * number of buttons which are assigned to popular tags
	 */
	private final static int NUMBER_POPULAR_TAGS = 6;

	/**
	 * key name for parameter last tag
	 */
	public final static String LAST_TAG = "LAST_TAG";

	/**
	 * tag separator
	 */
	public final static String DELIMITER = ",";

	public void onPause() {

		//
		// call super method
		//
		super.onPause();

		//
		// acquire shared settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get editor
		//
		SharedPreferences.Editor editor = settings.edit();

		if (m_text_view != null) {
			//
			// get editable
			//
			Editable edit = m_text_view.getText();

			//
			// convert to string
			//
			String tag_line = edit.toString();

			//
			// store in editor
			//
			editor.putString(ConfigurationSettings.CURRENT_TAG_LINE, tag_line);

		}

		//
		// commit changes
		//
		editor.commit();
	}

	/**
	 * resumes the activity
	 */
	protected void onResume() {

		//
		// call super method
		//
		super.onResume();

		//
		// check parameters
		//
		if (!getParameters(true))
			return;

		//
		// initialize basic user interface components
		//
		if (!initializeUIComponents())
			return;

		if (!initializeTagButtons())
			return;

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//
		// let base class initialize
		//
		super.onCreate(savedInstanceState);

		Logger.i("AddFileTagActivity::onCreate");

		//
		// setup view
		//
		setContentView(R.layout.addfiletag);

		//
		// check parameters
		//
		if (!getParameters(true))
			return;

		//
		// initialize basic user interface components
		//
		if (!initializeUIComponents())
			return;

		if (!initializeTagButtons())
			return;

	}

	/**
	 * gets the parameters from the passed intent
	 * 
	 * @return true when successful
	 */
	private boolean getParameters(boolean check_settings) {

		//
		// if settings should be checked
		//
		if (check_settings) {
			//
			// acquire shared settings
			//
			SharedPreferences settings = getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);

			//
			// get last file which was started to get tagged
			//
			m_file_name = settings.getString(
					ConfigurationSettings.CURRENT_FILE_TO_TAG, "");

			if (m_file_name.length() != 0) {
				//
				// we have an file which was started to be tagged
				// but the user did not complete the tagging process
				// lets get the current tag line which was used
				//
				m_last_settings = settings.getString(
						ConfigurationSettings.CURRENT_TAG_LINE, "");

				//
				// check if the file exists
				//
				File file = new File(m_file_name);
				if (file.exists()) {
					Logger.i("AddFileTagActivity::getParameters file_name "
							+ m_file_name + " Length " + m_file_name.length());

					//
					// done for now
					//
					return true;
				}

				//
				// file was removed in between
				//
			}
		}

		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get pending files from database
		//
		ArrayList<String> pending_files = db_man.getPendingFiles();

		//
		// are there pending files
		//
		if (pending_files == null || pending_files.size() == 0) {
			//
			// no more pending files
			//
			Logger.i("AddFileTagActivity::getParameters> no more pending files");
			setCurrentPreferenceFile("");
			m_file_name = "";
			return false;
		}

		//
		// get first pending file
		//
		m_file_name = pending_files.get(0);

		//
		// update current preferences file
		//
		setCurrentPreferenceFile(m_file_name);

		//
		// done
		//
		return true;
	}

	/**
	 * initialize basic user interface components
	 * 
	 * @return true on success
	 */
	private boolean initializeUIComponents() {
		//
		// get file name text field
		//
		TextView file_name_text_view = (TextView) findViewById(R.id.pending_file_list);

		if (file_name_text_view == null) {
			//
			// failed to get file name field
			//
			Logger.e("Error AddFileTagActivity::initializeUIComponents> failed to find file name field");
			return false;
		}

		//
		// set file name
		//
		file_name_text_view.setText(m_file_name);

		//
		// get reference to auto complete field
		//
		m_text_view = (AutoCompleteTextView) findViewById(R.id.tag_field);
		if (m_text_view == null) {
			//
			// failed to find tag field
			//
			Logger.e("Error: AddFileTagActivity::initializeUIComponents> failed to find tag field");
			return false;
		}

		//
		// add editor action listener
		//
		m_text_view.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {

				if (event != null) {
					if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
						//
						// collapse keyboard
						//
						InputMethodManager imm = (InputMethodManager) v
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

						//
						// event is consumed
						//
						return true;
					}
				}

				//
				// event is not consumed
				//
				return false;
			}
		});

		//
		// get the tag me button
		//
		Button button = (Button) findViewById(R.id.button_tag_done);

		if (button == null) {
			//
			// failed to find tag button
			//
			Logger.e("Error: AddFileTagActivity::initializeUIComponents> failed to find tag done button");
			return false;
		}

		//
		// add click listener
		//
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				// call performTag to do the work
				//
				performTag();
			}
		});

		//
		// done
		//
		return true;
	}

	private void initializeTagField(String[] tags) {

		//
		// construct array adapter holding the popular entries
		//
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, tags);

		if (m_text_view != null) {
			//
			// set auto-completion threshold
			//
			m_text_view.setThreshold(3);

			//
			// set auto completion adapter
			//
			m_text_view.setAdapter(adapter);

			//
			// set line
			//
			m_text_view.setText(m_last_settings);

			//
			// move cursor to last position
			//
			m_text_view.setSelection(m_text_view.length());
		}
	}

	/**
	 * initializes the tag buttons
	 * 
	 * @return
	 */
	private boolean initializeTagButtons() {

		//
		// acquire database manager instance
		//
		DBManager db_man = DBManager.getInstance();

		//
		// build integer array of identifiers
		//
		int[] tag_button_ids = new int[] { R.id.button_tag_one,
				R.id.button_tag_two, R.id.button_tag_three,
				R.id.button_tag_four, R.id.button_tag_five, R.id.button_tag_six };

		//
		// get popular tags
		//
		m_popular_tags = db_man.getPopularTags();

		if (m_popular_tags == null || m_popular_tags.size() == 0) {
			//
			// no tags to add
			//
			Logger.i("AddFileTagActivity::initializeTagButtons no tags found");

			//
			// hide the tag buttons
			//
			for (int button_id : tag_button_ids) {
				//
				// find button
				//
				Button button = (Button) findViewById(button_id);

				if (button != null) {
					//
					// hide button
					//
					button.setVisibility(View.INVISIBLE);
				}
			}
			return true;
		}

		//
		// convert to string array list
		//
		String[] tags = m_popular_tags.toArray(new String[1]);

		//
		// initialize tag text field with auto completion
		//
		initializeTagField(tags);

		//
		// get max tags available
		//
		int number_tags = Math.min(NUMBER_POPULAR_TAGS, m_popular_tags.size());

		for (int index = 0; index < number_tags; index++) {
			//
			// get popular tag
			//
			String popular_tag = m_popular_tags.get(0);

			//
			// initialize popular button
			//
			initializeTagButton(tag_button_ids[index], popular_tag, true);

			//
			// remove tag
			//
			m_popular_tags.remove(0);
		}

		if (number_tags < NUMBER_POPULAR_TAGS) {
			//
			// hide tag buttons
			//
			for (int index = number_tags; index < NUMBER_POPULAR_TAGS; index++) {
				//
				// find button
				//
				Button button = (Button) findViewById(tag_button_ids[index]);

				if (button != null) {
					//
					// hide button
					//
					button.setVisibility(View.INVISIBLE);
				}
			}
		}

		//
		// FIXME: support recent used buttons
		//
		return true;

	}

	/**
	 * initializes the tag button
	 * 
	 * @param button_id
	 *            id of button
	 * @param button_text
	 *            text of button
	 * @param popular
	 *            indicates if it is from popular tag button
	 */
	protected void initializeTagButton(int button_id, String button_text,
			boolean popular) {

		//
		// first get the button from the view
		//
		Button button = (Button) findViewById(button_id);

		if (button != null) {
			//
			// assign button text
			//
			button.setText(button_text);

			//
			// set click listener
			//
			button.setOnClickListener(new ButtonTagClickListener(button_id,
					popular));
		}
	}

	/**
	 * receives call back when a tag button was pressed
	 * 
	 * @param button_id
	 *            id of button which was pressed
	 * @param button_text
	 *            text of the button
	 * @param popular
	 *            indicates if it was one of the popular buttons
	 */
	protected void onTagButtonClick(int button_id, String button_text,
			boolean popular) {

		if (m_text_view != null) {
			//
			// check length of field
			//
			if (m_text_view.length() == 0) {
				//
				// field is empty
				//
				m_text_view.setText(button_text + DELIMITER);
			} else {
				//
				// separate the item
				//
				m_text_view.append(button_text + DELIMITER);
			}

			m_text_view.setSelection(m_text_view.length());
		}

		//
		// check if this was one of the popular tags
		//
		if (popular) {
			//
			// get button with this id
			//
			Button button = (Button) findViewById(button_id);

			//
			// check if there is a unused tag left
			//
			if (m_popular_tags.size() > 0) {
				//
				// grab first tag
				//
				String new_tag = m_popular_tags.get(0);

				//
				// check if the view was found
				//
				if (button != null) {
					//
					// update button text
					//
					button.setText(new_tag);

					//
					// remove consumed tag
					//
					m_popular_tags.remove(0);
				}
			} else {
				//
				// no more tags left, hide button
				//
				if (button != null) {
					//
					// hide button
					//
					button.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	/**
	 * updates the current file in the shared settings. If file name is empty,
	 * it also resets the ConfigurationSettings.PENDING_FILE_TAB to false which
	 * hides the add file tag activity
	 * 
	 * @param file_name
	 *            new current file
	 */
	private void setCurrentPreferenceFile(String file_name) {

		//
		// get settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get settings editor
		//
		SharedPreferences.Editor editor = settings.edit();

		//
		// set current file
		//
		editor.putString(ConfigurationSettings.CURRENT_FILE_TO_TAG, file_name);

		Logger.e("AddFileTagActivity::setCurrentPreferenceFile file_name "
				+ file_name);

		if (file_name.length() == 0) {
			//
			// clear pending flag to display pending file tab
			//
			editor.putBoolean(ConfigurationSettings.PENDING_FILE_TAB, false);
		}

		//
		// and commit the changes
		//
		editor.commit();
	}

	/**
	 * removes notification from notification manager
	 */
	protected void removeNotification() {

		//
		// get notification service name
		//
		String ns_name = Context.NOTIFICATION_SERVICE;

		//
		// get notification service manager instance
		//
		NotificationManager notification_manager = (NotificationManager) getSystemService(ns_name);

		//
		// clear notification
		//
		notification_manager.cancel(ConfigurationSettings.NOTIFICATION_ID);
	}

	/**
	 * performs the tagging
	 */
	protected void performTag() {

		String tag_text = "";

		if (m_text_view != null) {
			//
			// grab the tags
			//
			tag_text = m_text_view.getText().toString();
		}

		if (tag_text.length() == 0) {
			//
			// must be at least one tag
			// FIXME: non-nls
			//
			Toast toast = Toast.makeText(getApplicationContext(),
					"There must be at least one tag for the file",
					Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// done for now
			//
			return;
		}

		//
		// first add the file
		//
		if (!addFileToDB(m_file_name)) {
			//
			// inform user that it failed to add the file
			// FIXME: non-nls compatible
			//
			Toast toast = Toast
					.makeText(getApplicationContext(),
							"Error: failed to add file to database",
							Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// done
			//
			return;
		}

		//
		// now process the tags
		//
		if (!processTags(tag_text)) {
			//
			// inform user that it failed to process tags
			// FIXME: non-nls compatible
			//
			Toast toast = Toast.makeText(getApplicationContext(),
					"Error: failed to process tags", Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// remove file from database store
			//
			DBManager db_man = DBManager.getInstance();

			//
			// remove entry
			//
			db_man.removeFile(m_file_name, false, true);

			//
			// re-add to pending file list
			//
			db_man.addPendingFile(m_file_name);

			//
			// done
			//
			return;
		}

		Logger.i("AddFileTag::performTag file : " + m_file_name
				+ " added to database");

		//
		// done now
		// check if there more pending files
		//
		if (getParameters(false) == false) {
			//
			// no more files, remove pending file item
			//
			MainActivity.s_Instance
					.removeTab(MainActivity.PENDING_FILE_TAB_SPEC_NAME);

			//
			// rebuild tab list
			//
			MainActivity.s_Instance.rebuildTabs();

			//
			// remove notification
			//
			removeNotification();

			//
			// done
			//
			return;
		}

		//
		// now initialize the tag buttons
		//
		if (!initializeTagButtons()) {
			//
			// failed to initialize tag buttons
			//
			return;

		}

	}

	/**
	 * this function processes the associated tags. It will create tag entry if
	 * the tag is new, or update the reference count of existing tags
	 * 
	 * @param tag_text
	 *            string containing the tags
	 * @return
	 */
	private boolean processTags(String tag_text) {

		//
		// get instance of database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get file id
		//
		long file_id = db_man.getFileId(m_file_name);
		if (file_id < 0) {
			Logger.e("Error AddFileTagActivity::processTags failed to get file id for file "
					+ m_file_name);
			return false;
		}

		//
		// split the tag text into unique set of tokens
		//
		Set<String> tag_list = splitTagText(tag_text);

		//
		// get tag reference count
		//
		HashMap<String, Integer> tag_reference = db_man.getTagReferenceCount();

		//
		// iterate tag list
		//
		for (String current_tag : tag_list) {
			//
			// is there a tag list
			//
			if (tag_reference != null) {
				//
				// check is the tag known
				//
				if (tag_reference.containsKey(current_tag) == false) {
					//
					// user entered new tag
					//
					Logger.i("AddFileTagActivity::processTags new tag: "
							+ current_tag);

					//
					// add tag to database
					//
					db_man.addTag(current_tag);
				} else {
					//
					// adds reference to tag
					//
					int reference_count = tag_reference.get(current_tag)
							.intValue() + 1;
					Logger.i("AddFileTagActivity::processTags tag: "
							+ current_tag + " new reference count: "
							+ reference_count);

					db_man.setTagReference(current_tag, reference_count);
				}
			} else {
				//
				// first tag ever
				//
				Logger.i("AddFileTagActivity::processTags first tag: "
						+ current_tag);

				//
				// add tag to database
				//
				db_man.addTag(current_tag);
			}

			//
			// now get tag id
			//
			long tag_id = db_man.getTagId(current_tag);
			if (tag_id < 0) {
				//
				// failed to get tag
				//
				Logger.e("Error AddFileTagActivity::processTags> failed to get tag id");
				return false;
			}

			//
			// add file < - > tag mapping
			//
			if (!db_man.addFileTagMapping(file_id, tag_id))
				return false;
		}

		//
		// done
		//
		return true;
	}

	/**
	 * splitTagText splits the tag text line into unique sets of token
	 * 
	 * @param tag_text
	 *            to be splitted
	 * @return set of token
	 */
	private Set<String> splitTagText(String tag_text) {

		//
		// split the tag_text
		//
		StringTokenizer tokenizer = new StringTokenizer(tag_text, DELIMITER);

		//
		// create hash map to store the tags in order to remove duplicates
		//
		Set<String> tag_list = new HashSet<String>();

		//
		// add tokens to list
		//
		while (tokenizer.hasMoreTokens())
			tag_list.add(tokenizer.nextToken());

		//
		// done
		//
		return tag_list;
	}

	/**
	 * adds a file to the database
	 */
	private boolean addFileToDB(String file_name) {

		//
		// get instance of database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// first get the creation date of the file
		//
		String create_date = db_man.getPendingFileDate(file_name);
		if (create_date == null) {
			//
			// error: no file date
			//
			Logger.e("Error: failed to retrieve create date from database");
			return false;
		}

		String mime_type = "";

		//
		// get file extension
		//
		int index = file_name.lastIndexOf('.');
		if (index > 0) {
			String extension = file_name.substring(index + 1);
			if (extension.length() != 0) {
				//
				// get mime type map instance
				//
				MimeTypeMap mime_map = MimeTypeMap.getSingleton();

				//
				// guess file extension
				//
				mime_type = mime_map.getMimeTypeFromExtension(extension);
			}
		}

		//
		// informal debug prints
		//
		Logger.i("AddFileTagActivity::addFileToDB> file_name: " + file_name);
		Logger.i("AddFileTagActivity::addFileToDB> create_date: " + create_date);
		Logger.i("AddFileTagActivity::addFileToDB> mime_type: " + mime_type);

		//
		// add to the database
		//
		long result = db_man.addFile(file_name, mime_type, create_date);

		//
		// result
		//
		Logger.i("AddFileTagActivity::addFileToDB> result: " + result);

		//
		// now remove pending file
		//
		db_man.removeFile(m_file_name, true, false);

		//
		// done
		//
		return true;
	}

	/**
	 * This class serves as callback adapter. It will call the onTagButtonClick
	 * when a tag button is clicked
	 * 
	 * @author Johnseyii
	 * 
	 */
	private class ButtonTagClickListener implements OnClickListener {

		/**
		 * indicates that one of the popular button were pressed
		 */
		private boolean m_popular_button;

		/**
		 * id of button
		 */
		private int m_button_id;

		/**
		 * constructor of class ButtonTagClickListener
		 * 
		 * @param button_id
		 *            id of the button
		 * @param button_text
		 *            text of the button
		 * @param popular_button
		 *            if it is one of the popular buttons
		 */
		ButtonTagClickListener(int button_id, boolean popular_button) {

			//
			// store settings
			//
			m_button_id = button_id;
			m_popular_button = popular_button;
		}

		@Override
		public void onClick(View v) {

			//
			// get button
			//
			Button button = (Button) findViewById(m_button_id);

			//
			// get button text
			//
			CharSequence button_text = button.getText();

			//
			// perform call back
			//
			onTagButtonClick(m_button_id, button_text.toString(),
					m_popular_button);
		}
	};

}
