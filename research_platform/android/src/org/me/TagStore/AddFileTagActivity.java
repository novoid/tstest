package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.interfaces.OptionsDialogCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.ui.FileDialogBuilder;
import org.me.TagStore.ui.UIEditorActionListener;
import org.me.TagStore.ui.UITagTextWatcher;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddFileTagActivity extends Activity implements OptionsDialogCallback, RenameDialogCallback{

	/**
	 * stores the dialog id of options dialog
	 */
	private static final int DIALOG_OPTIONS = 1;
	
	/**
	 * stores the dialog id of the rename dialog
	 */
	private static final int DIALOG_RENAME = 2;
	
	
	/**
	 * stores the file name of the new file passed as parameter of intent
	 */
	private String m_file_name = "";
	
	/**
	 * stores the file path of the file which has the same file name and is already present in the tag store
	 */
	private String m_duplicate_file_name ="";
	
	/**
	 * stores the file path of the file which is being re-named
	 */
	private String m_rename_file_name = "";

	/**
	 * stores the last tags used passed as parameter of intent
	 */
	private String m_last_settings = "";

	/**
	 * stores the button ids
	 */
	static int[] s_tag_button_ids = new int[] { R.id.button_tag_one,
			R.id.button_tag_two, R.id.button_tag_three,
			R.id.button_tag_four, R.id.button_tag_five, R.id.button_tag_six };
	
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

	/**
	 * displays toast for user notifications
	 */
	private Toast m_toast;
	
	@Override
	public Dialog onCreateDialog(int dialog_id) {
		
		Dialog dialog = null;
		switch(dialog_id)
		{
		case DIALOG_OPTIONS:
			dialog = FileDialogBuilder.buildOptionsDialogFile(TagActivityGroup.s_Instance);
			break;	
		case DIALOG_RENAME:
			dialog = FileDialogBuilder.buildRenameDialogFile(TagActivityGroup.s_Instance);
			break;
		}
		
		//
		// done
		//
		return dialog;
	}
	
	protected void onPrepareDialog (int id, Dialog dialog) {
		
		//
		// check which dialog is requested
		//
		switch (id) {
			case DIALOG_OPTIONS:
				FileDialogBuilder.updateOptionsDialogFileView(dialog, m_duplicate_file_name, m_file_name, this);
				break;
			case DIALOG_RENAME:
				FileDialogBuilder.updateRenameDialogFile(dialog, m_rename_file_name, false, false, this);
				break;
		}
	}
	

	@Override
	public void renamedFile(String old_file_name, String new_file) {

		Logger.i("renamedFile: " + old_file_name + " new file_name: " + new_file);
		if (old_file_name.compareTo(m_file_name) == 0)
		{
			//
			// update file name in the text view
			//
			m_file_name = new_file;

			//
			// get file name text field
			//
			TextView file_name_text_view = (TextView) findViewById(R.id.pending_file_list);

			if (file_name_text_view != null) {
				//
				// set file name
				//
				file_name_text_view.setText(m_file_name);	
			}
		}
	}
	

	@Override
	public void renamedTag(String old_tag_name, String new_tag_name) {
		//
		// first scan the active buttons if the old tag name is present
		//
		for (int button_id : s_tag_button_ids) 
		{
			//
			// find button
			//
			Button button = (Button) findViewById(button_id);

			//
			// get button text
			//
			String button_text = (String) button.getText();
			if (button_text.compareTo(old_tag_name) == 0)
			{
				//
				// update button tag
				//
				button.setText(new_tag_name);
				return;
			}
		}
		
		//
		// search in popular list
		//
		for(String current_tag : m_popular_tags)
		{
			if (current_tag.compareTo(old_tag_name) == 0)
			{
				//
				// add new tag
				//
				m_popular_tags.add(m_popular_tags.indexOf(current_tag), new_tag_name);
				
				//
				// remove old tag
				//
				m_popular_tags.remove(current_tag);
				return;
			}
		}
		
		//
		// FIXME: should the current tag line be updated too?
		//
	}
	
	@Override
	public void processOptionsDialogCommand(String file_name, boolean ignore) {

		Logger.i("processOptionsDialogCommand: file_name " + file_name + " ignore: " + ignore);
		
		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		if (ignore)
		{
			if (file_name.compareTo(m_duplicate_file_name) == 0)
			{
				//
				// remove the file from the tag store
				//
				db_man.removeFile(file_name, false, true);
			}
			else if (file_name.compareTo(m_file_name) == 0)
			{
				//
				// remove file from pending list
				//
				db_man.removeFile(file_name, true, false);
				
				//
				// update GUI
				//
				initialize(false);
			}
		}
		else
		{
			//
			// store the name of the file to be renamed
			//
			m_rename_file_name = file_name;
			
			//
			// display dialog
			//
			showDialog(DIALOG_RENAME);
		}
	}
	
	public void onPause() {

		//
		// call super method
		//
		super.onPause();

		if (m_text_view != null) {
			
			//
			// get text
			//
			Editable edit = m_text_view.getText();

			//
			// convert to string
			//
			String tag_line = edit.toString();

			//
			// store in editor
			//
			setCurrentPreferenceTagLine(tag_line);
		}

		if (m_toast != null)
		{
			//
			// cancel any on-going toast when switching the view
			//
			m_toast.cancel();
		}
		
	}

	/**
	 * resumes the activity
	 */
	public void onResume() {

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
		// initialize view
		//
		initialize(true);
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
		// initialize view
		//
		initialize(true);
		
	}

	private void initialize(boolean check_settings) {
		
		//
		// check parameters
		//
		if (!getParameters(check_settings))
		{
			//
			// get localized new file
			//
			String new_file = getApplicationContext().getString(R.string.new_file);
			
			//
			// no more files, remove pending file item
			//
			MainActivity.s_Instance.showTab(new_file, false);
					
			//
			// remove notification
			//
			removeNotification();

			
			//
			// set current tag line
			//
			setCurrentPreferenceTagLine("");			
			return;
		}
		//
		// initialize basic user interface components
		//
		if (!initializeUIComponents())
			return;

		if (!initializeTagButtons())
			return;

		if (!isFileNameUnique(m_file_name))
		{
			//
			// show options dialog
			//
			showDialog(DIALOG_OPTIONS);
		}
	
	}
	
	
	/**
	 * returns true when the file name is unique (no other file named liked this is present in the tagstore)
	 * @param file_name file name to be checked
	 * @return boolean
	 */
	private boolean isFileNameUnique(String file_name) {
		
		
		//
		// extract file name
		//
		String file = new File(file_name).getName();
		
		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get all files which have the same name
		//
		ArrayList<String> files = db_man.getSimilarFilePaths(file);
		
		//
		// any file paths which have the same file name but different directory
		//
		if (files == null || files.isEmpty())
		{
			//
			// no duplicates found, great!
			//
			return true;
		}

		//
		// get first duplicate
		//
		m_duplicate_file_name = files.get(0);
		
		//
		// this file name is already present in the tag store
		//
		return false;
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
		m_text_view.setOnEditorActionListener(new UIEditorActionListener());
		
		//
		// add text changed listener
		//
		m_text_view.addTextChangedListener(new UITagTextWatcher(getApplicationContext(), true));		
		
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

	/**
	 * initializes the tag field
	 * @param tags
	 */
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
			for (int button_id : s_tag_button_ids) {
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
			initializeTagButton(s_tag_button_ids[index], popular_tag, true);

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
				Button button = (Button) findViewById(s_tag_button_ids[index]);

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
			
			//
			// show button
			//
			button.setVisibility(View.VISIBLE);
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
				// get button text
				//
				String tag_text = m_text_view.getText().toString();
				
				//
				// is there a delimiter at the end
				//
				if (tag_text.endsWith(DELIMITER))
				{
					//
					// no need to put a separator before the tag is appended
					//
					m_text_view.append(button_text + DELIMITER);
				}
				else
				{
					//
					// add delimiter before appending the tag
					//
					m_text_view.append(DELIMITER + button_text + DELIMITER);
				}
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

		//
		// and commit the changes
		//
		editor.commit();
	}
	
	private void setCurrentPreferenceTagLine(String tag_line) {
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

		//
		// all files have been tagged, clear current tag line
		//
		editor.putString(ConfigurationSettings.CURRENT_TAG_LINE, "");

		//
		// commit changes
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

		//
		// first validate the tags
		//
		if (!FileTagUtility.validateTags(tag_text, this, m_text_view)) {

			//
			// done for now
			//
			return;
		}

		//
		// add file
		//
		if (FileTagUtility.addFile(m_file_name, tag_text, getApplicationContext()) == false)
		{
			//
			// failed to tag
			//
			return;
		}

		Logger.i("AddFileTag::performTag file : " + m_file_name
				+ " added to database");

		//
		// done now
		// re-initialize
		//
		initialize(false);
	}

	/**
	 * This class serves as callback adapter. It will call the onTagButtonClick
	 * when a tag button is clicked
	 * 
	 * @author Johannes Anderwald
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
	}

}
