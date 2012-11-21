package org.me.tagstore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.me.tagstore.R;
import org.me.tagstore.core.ConfigurationSettings;
import org.me.tagstore.core.DBManager;
import org.me.tagstore.core.EventDispatcher;
import org.me.tagstore.core.FileTagUtility;
import org.me.tagstore.core.Logger;
import org.me.tagstore.core.PendingFileChecker;
import org.me.tagstore.core.TagValidator;
import org.me.tagstore.core.VocabularyManager;
import org.me.tagstore.interfaces.GeneralDialogCallback;
import org.me.tagstore.interfaces.OptionsDialogCallback;
import org.me.tagstore.interfaces.RenameDialogCallback;
import org.me.tagstore.ui.CommonDialogFragment;
import org.me.tagstore.ui.DialogIds;
import org.me.tagstore.ui.DialogItemOperations;
import org.me.tagstore.ui.FileDialogBuilder;
import org.me.tagstore.ui.FileDialogBuilder.MENU_ITEM_ENUM;
import org.me.tagstore.ui.MainPageAdapter;
import org.me.tagstore.ui.StatusBarNotification;
import org.me.tagstore.ui.ToastManager;
import org.me.tagstore.ui.UIEditorActionListener;
import org.me.tagstore.ui.UITagTextWatcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

public class AddFileTagActivity extends Fragment implements
		OptionsDialogCallback, RenameDialogCallback, GeneralDialogCallback {

	/**
	 * stores the file name of the currently tagged file
	 */
	private String m_file_name = "";

	/**
	 * stores the file path of the file which has the same file name as the
	 * current file name and which is already present in the tag store
	 */
	private String m_duplicate_file_name = "";

	/**
	 * stores the file path of the file which is being re-named
	 */
	private String m_rename_file_name = "";

	/**
	 * stores the last tags used
	 */
	private String m_last_settings = "";

	/**
	 * common dialog operations object
	 */
	private DialogItemOperations m_dialog_operations;

	/**
	 * stores the button ids
	 */
	static int[] s_tag_button_ids = new int[] { R.id.button_tag_one,
			R.id.button_tag_two, R.id.button_tag_three, R.id.button_tag_four,
			R.id.button_tag_five, R.id.button_tag_six };

	/**
	 * stores popular tags
	 */
	private LinkedHashSet<String> m_popular_tags;

	/**
	 * reference to auto complete field
	 */
	private AutoCompleteTextView m_text_view;

	/**
	 * number of buttons which are assigned to popular tags
	 */
	private final static int NUMBER_POPULAR_TAGS = s_tag_button_ids.length;

	/**
	 * status bar notification
	 */
	private StatusBarNotification m_status_bar;

	/**
	 * dialog fragment
	 */
	private CommonDialogFragment m_fragment = null;

	/**
	 * indicator if the tagging should be performed automatically
	 */
	private boolean m_perform_tag = false;

	public void renamedFile(String old_file_name, String new_file) {

		Logger.i("renamedFile: " + old_file_name + " new file_name: "
				+ new_file);

		if (old_file_name.compareTo(m_file_name) == 0) {

			//
			// set file name
			//
			m_file_name = new_file;

			//
			// get file name text field
			//
			TextView file_name_text_view = (TextView) getView().findViewById(
					R.id.pending_file_list);

			if (file_name_text_view != null) {

				//
				// update file name in the text view
				//
				SpannableString content = new SpannableString(m_file_name);
				content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
				file_name_text_view.setText(content);
			}

			//
			// perform tag
			//
			if (m_perform_tag) {
				performTag();
			}
		} else if (old_file_name.compareTo(m_rename_file_name) == 0) {
			if (m_perform_tag) {
				//
				// the existing file in the tagstore got renamed
				//
				performTag();
			}
		}

		//
		// a file got renamed which does not interest us at the moment
		//
	}

	public void renamedTag(String old_tag_name, String new_tag_name) {

		Logger.i("renamedTag: old_tag_name: " + old_tag_name + " new_tag_name:"
				+ new_tag_name);

		//
		// first scan the active buttons if the old tag name is present
		//
		for (int button_id : s_tag_button_ids) {
			//
			// find button
			//
			Button button = (Button) getView().findViewById(button_id);

			//
			// get button text
			//
			String button_text = (String) button.getText();
			if (button_text.compareTo(old_tag_name) == 0) {
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
		if (m_popular_tags.contains(old_tag_name)) {
			//
			// remove old tag name
			//
			m_popular_tags.remove(old_tag_name);

			//
			// add new tag name
			//
			m_popular_tags.add(new_tag_name);
		}

		//
		// FIXME: should the current tag line be updated too?
		//
	}

	public void processOptionsDialogCommand(String file_name, boolean ignore) {

		Logger.i("processOptionsDialogCommand: file_name " + file_name
				+ " ignore: " + ignore);

		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		if (ignore) {
			if (file_name.compareTo(m_duplicate_file_name) == 0) {

				//
				// remove the file from the tag store
				//
				db_man.removeFile(file_name);

				//
				// tag the file
				//
				performTag();

			} else if (file_name.compareTo(m_file_name) == 0) {
				//
				// remove file from pending list
				//
				db_man.removePendingFile(file_name);

				//
				// update GUI
				//
				initialize(getView(), false);
			}
		} else {
			//
			// store the name of the file to be renamed
			//
			m_rename_file_name = file_name;

			//
			// display dialog
			//
			CommonDialogFragment dialog_fragment = CommonDialogFragment
					.newInstance(getActivity(), m_rename_file_name, false,
							DialogIds.DIALOG_RENAME);
			dialog_fragment.setDialogItemOperation(m_dialog_operations);
			dialog_fragment.show(getFragmentManager(), "FIXME");
		}
	}

	public void onPause() {

		//
		// call super method
		//
		super.onPause();

		Logger.i("AddFile::onPause");

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

		//
		// cancel any on-going toast when switching the view
		//
		ToastManager.getInstance().cancelToast();
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
		// initialize view
		//
		initialize(getView(), true);
	}

	public void onStop() {

		//
		// inform base class
		//
		super.onStop();

		//
		// unregister us from event dispatcher
		//
		EventDispatcher.getInstance().unregisterEvent(
				EventDispatcher.EventId.TAG_RENAMED_EVENT, this);
		EventDispatcher.getInstance().unregisterEvent(
				EventDispatcher.EventId.FILE_RENAMED_EVENT, this);
		EventDispatcher.getInstance().unregisterEvent(
				EventDispatcher.EventId.ITEM_CONFLICT_EVENT, this);
		EventDispatcher.getInstance().unregisterEvent(
				EventDispatcher.EventId.ITEM_MENU_EVENT, this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// let base class initialize
		//
		super.onCreate(savedInstanceState);

		//
		// construct on create dialog operations object
		//
		m_dialog_operations = new DialogItemOperations();
		FileTagUtility utility = new FileTagUtility();
		utility.initializeFileTagUtility();

		FileDialogBuilder builder = new FileDialogBuilder();
		builder.initializeFileDialogBuilder(DBManager.getInstance(), utility);

		m_dialog_operations.initDialogItemOperations(getActivity(),
				getFragmentManager(), utility, ToastManager.getInstance(),
				builder);

		//
		// create status bar
		//
		m_status_bar = new StatusBarNotification();
		m_status_bar.initializeStatusBarNotification(getActivity()
				.getApplicationContext());

		//
		// register us with the event dispatcher
		//
		EventDispatcher.getInstance().registerEvent(
				EventDispatcher.EventId.TAG_RENAMED_EVENT, this);
		EventDispatcher.getInstance().registerEvent(
				EventDispatcher.EventId.FILE_RENAMED_EVENT, this);
		EventDispatcher.getInstance().registerEvent(
				EventDispatcher.EventId.ITEM_CONFLICT_EVENT, this);
		EventDispatcher.getInstance().registerEvent(
				EventDispatcher.EventId.ITEM_MENU_EVENT, this);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle saved) {

		//
		// construct layout
		//
		View view = inflater.inflate(R.layout.addfiletag, null);

		//
		// done
		//
		return view;
	}

	/**
	 * collapses the screen keyboard
	 */
	private void collapseScreenKeyboard() {

		//
		// get activity
		//
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			if (imm != null && m_text_view != null) {
				imm.hideSoftInputFromWindow(m_text_view.getWindowToken(), 0);
			}
		}
	}

	private void initialize(View view, boolean check_settings) {

		//
		// check parameters
		//
		if (!getParameters(check_settings)) {

			//
			// remove notification
			//
			removeNotification();

			//
			// collapse on screen keyboard
			//
			collapseScreenKeyboard();

			//
			// set current tag line
			//
			setCurrentPreferenceTagLine("");

			//
			// no more files
			//
			MainPageAdapter adapter = MainPageAdapter.getInstance();

			//
			// remove add file tag fragment
			//
			adapter.removeAddFileTagFragment();
			return;
		}

		//
		// initialize basic user interface components
		//
		if (!initializeUIComponents(view))
			return;

		if (!initializeTagButtons(view))
			return;
	}

	/**
	 * returns true when the file name is unique (no other file named liked this
	 * is present in the tagstore)
	 * 
	 * @param file_name
	 *            file name to be checked
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
		if (files == null || files.isEmpty()) {
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
			SharedPreferences settings = getActivity().getSharedPreferences(
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
		// construct pending file checker
		//
		PendingFileChecker file_checker = new PendingFileChecker();
		FileTagUtility utility = new FileTagUtility();
		utility.initializeFileTagUtility();
		file_checker.initializePendingFileChecker(DBManager.getInstance(),
				utility);

		//
		// are there pending files
		//
		if (!file_checker.hasPendingFiles()) {
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
		m_file_name = file_checker.getPendingFiles().get(0);

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
	private boolean initializeUIComponents(View view) {
		//
		// get file name text field
		//
		TextView file_name_text_view = (TextView) view
				.findViewById(R.id.pending_file_list);

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
		SpannableString content = new SpannableString(m_file_name);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		file_name_text_view.setText(content);

		//
		// set file name click listener
		//
		file_name_text_view.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				//
				// launch file
				//
				m_dialog_operations.performDialogItemOperation(m_file_name,
						false, FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN);
			}
		});

		//
		// add long item click listener
		//
		file_name_text_view.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {

				//
				// don't tag when the dialog is finished
				//
				m_perform_tag = false;

				//
				// launch file options dialog
				//
				m_fragment = CommonDialogFragment.newInstance(getActivity(),
						m_file_name, false, DialogIds.DIALOG_GENERAL_FILE_MENU);
				m_fragment.setDialogItemOperation(m_dialog_operations);
				m_fragment.show(getFragmentManager(), "FIXME");
				Logger.i("onLongClick: " + m_file_name + "fragment: "
						+ m_fragment);

				//
				// handled
				//
				return true;
			}
		});

		//
		// get reference to auto complete field
		//
		m_text_view = (AutoCompleteTextView) view.findViewById(R.id.tag_field);
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
		// construct text watcher
		//
		UITagTextWatcher watcher = new UITagTextWatcher();
		watcher.initializeUITagTextWatcher(true, true);

		//
		// add text changed listener
		//
		m_text_view.addTextChangedListener(watcher);

		//
		// get the tag me button
		//
		Button button = (Button) view.findViewById(R.id.button_tag_done);

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
	 * 
	 * @param tags
	 */
	private void initializeTagField(String[] tags) {

		//
		// construct array adapter holding the popular entries
		//
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
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

	private void initTags() {

		//
		// new tags list
		//
		m_popular_tags = new LinkedHashSet<String>();

		//
		// acquire database manager instance
		//
		DBManager db_man = DBManager.getInstance();

		//
		// acquire shared settings
		//
		SharedPreferences settings = getActivity().getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get current sort mode
		//
		String sort_mode = settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_SORT_MODE,
				ConfigurationSettings.DEFAULT_LIST_VIEW_SORT_MODE);

		Logger.i("TagStoreListViewActivity::fillListMap sort_mode " + sort_mode);

		ArrayList<String> tags = null;

		if (VocabularyManager.getInstance().getControlledVocabularyState()) {
			//
			// get controlled vocabulary
			//
			HashSet<String> vocabulary = VocabularyManager.getInstance()
					.getVocabulary();

			if (vocabulary != null) {
				//
				// add all vocabulary entries
				//
				m_popular_tags.addAll(vocabulary);
			}

			//
			// done for now
			//
			return;
		}

		if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_ALPHABETIC) == 0) {
			//
			// get tags sorted by alphabet
			//
			tags = db_man.getAlphabeticTags();
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_POPULAR) == 0) {
			//
			// get tags sorted by popularity
			//
			tags = db_man.getPopularTags();
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_RECENT) == 0) {
			//
			// TODO: get tags sorted by recently used
			//
			tags = null;
			Logger.e("Error: AddFileTagActivity::initTags get recent tags is not implemented yet");
		}

		if (tags != null) {
			//
			// add all tags
			//
			m_popular_tags.addAll(tags);
		}
	}

	/**
	 * initializes the tag buttons
	 * 
	 * @return
	 */
	private boolean initializeTagButtons(View view) {

		//
		// init tags
		//
		initTags();

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
				Button button = (Button) view.findViewById(button_id);

				if (button != null) {
					//
					// hide button
					//
					button.setVisibility(View.INVISIBLE);
				}
			}

			//
			// restore tag field contents
			//
			if (m_text_view != null) {
				//
				// set line
				//
				m_text_view.setText(m_last_settings);

				//
				// move cursor to last position
				//
				m_text_view.setSelection(m_text_view.length());
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

		int index = 0;
		for (String popular_tag : m_popular_tags.toArray(new String[1])) {
			//
			// initialize popular button
			//
			initializeTagButton(view, s_tag_button_ids[index], popular_tag,
					true);

			//
			// remove tag
			//
			m_popular_tags.remove(popular_tag);

			//
			// update index
			//
			index++;
			if (index >= number_tags)
				break;
		}

		if (number_tags < NUMBER_POPULAR_TAGS) {
			//
			// hide tag buttons
			//
			for (index = number_tags; index < NUMBER_POPULAR_TAGS; index++) {
				//
				// find button
				//
				Button button = (Button) view
						.findViewById(s_tag_button_ids[index]);

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
	protected void initializeTagButton(View view, int button_id,
			String button_text, boolean popular) {

		//
		// first get the button from the view
		//
		Button button = (Button) view.findViewById(button_id);

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
				m_text_view.setText(button_text
						+ ConfigurationSettings.TAG_DELIMITER);
			} else {

				//
				// get button text
				//
				String tag_text = m_text_view.getText().toString();

				//
				// is there a delimiter at the end
				//
				if (tag_text.endsWith(ConfigurationSettings.TAG_DELIMITER)) {
					//
					// no need to put a separator before the tag is appended
					//
					m_text_view.append(button_text
							+ ConfigurationSettings.TAG_DELIMITER);
				} else {
					//
					// add delimiter before appending the tag
					//
					m_text_view
							.append(ConfigurationSettings.TAG_DELIMITER
									+ button_text
									+ ConfigurationSettings.TAG_DELIMITER);
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
			Button button = (Button) getView().findViewById(button_id);

			//
			// check if there is a unused tag left
			//
			if (m_popular_tags.size() > 0) {
				//
				// grab first tag
				//
				String new_tag = m_popular_tags.toArray(new String[1])[0];

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
	 * gets an editor for common preferences
	 * 
	 * @return
	 */
	private SharedPreferences.Editor getEditor() {

		//
		// get activity
		//
		Activity activity = getActivity();
		if (activity != null) {
			//
			// get settings
			//
			SharedPreferences settings = activity.getSharedPreferences(
					ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
					Context.MODE_PRIVATE);

			//
			// get settings editor
			//
			return settings.edit();
		}

		//
		// no activity!
		//
		return null;
	}

	/**
	 * updates the current file in the shared settings. If file name is empty,
	 * 
	 * @param file_name
	 *            new current file
	 */
	private void setCurrentPreferenceFile(String file_name) {

		//
		// get editor
		//
		SharedPreferences.Editor editor = getEditor();
		if (editor != null) {
			//
			// set current file
			//
			editor.putString(ConfigurationSettings.CURRENT_FILE_TO_TAG,
					file_name);

			Logger.e("AddFileTagActivity::setCurrentPreferenceFile file_name "
					+ file_name);

			//
			// and commit the changes
			//
			editor.commit();
		}
	}

	private void setCurrentPreferenceTagLine(String tag_line) {

		//
		// get editor
		//
		SharedPreferences.Editor editor = getEditor();
		if (editor != null) {
			//
			// all files have been tagged, clear current tag line
			//
			editor.putString(ConfigurationSettings.CURRENT_TAG_LINE, tag_line);

			//
			// commit changes
			//
			editor.commit();
		}
	}

	/**
	 * removes notification from notification manager
	 */
	protected void removeNotification() {

		//
		// remove status bar notification when enabled
		//
		if (m_status_bar.isStatusBarNotificationEnabled()) {
			//
			// cancel it
			//
			m_status_bar.removeStatusBarNotification();
		}
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
		// create file tag utility
		//
		FileTagUtility utility = new FileTagUtility();
		utility.initializeFileTagUtility();

		//
		// first validate the tags
		//
		if (!utility.validateTags(tag_text, m_text_view)) {

			//
			// done for now
			//
			return;
		}

		//
		// check if it is unique
		//
		if (!isFileNameUnique(m_file_name)) {

			//
			// enable auto-tag mode
			//
			m_perform_tag = true;

			//
			// show options dialog
			//
			CommonDialogFragment dialog_fragment = CommonDialogFragment
					.newInstance(getActivity(), m_file_name, false,
							DialogIds.DIALOG_OPTIONS);
			dialog_fragment.setDialogItemOperation(m_dialog_operations);
			dialog_fragment.show(getFragmentManager(), "FIXME");
			return;
		}

		//
		// extract file name from path
		//
		String file_name = new File(m_file_name).getName();

		//
		// construct tag validator
		//
		TagValidator validator = new TagValidator();

		//
		// check if the file name contains invalid characters
		//
		if (validator.containsReservedCharacters(file_name)) {
			//
			// display toast
			//
			ToastManager.getInstance().displayToastWithString(
					R.string.reserved_character_file_name);
			Logger.e("invalid character found in " + file_name);

			//
			// enable auto-tag mode
			//
			m_perform_tag = true;

			//
			// launch rename dialog
			//
			CommonDialogFragment dialog_fragment = CommonDialogFragment
					.newInstance(getActivity(), m_file_name, false,
							DialogIds.DIALOG_RENAME);
			dialog_fragment.setDialogItemOperation(m_dialog_operations);
			dialog_fragment.show(getFragmentManager(), "FIXME");
			return;
		}

		//
		// check if the file name is a reserved keyword
		//
		if (validator.isReservedKeyword(file_name)) {
			//
			// not allowed
			//
			ToastManager.getInstance().displayToastWithString(
					R.string.reserved_keyword_file_name);

			//
			// enable auto-tag mode
			//
			m_perform_tag = true;

			//
			// launch rename dialog
			//
			CommonDialogFragment dialog_fragment = CommonDialogFragment
					.newInstance(getActivity(), m_file_name, false,
							DialogIds.DIALOG_RENAME);
			dialog_fragment.setDialogItemOperation(m_dialog_operations);
			dialog_fragment.show(getFragmentManager(), "FIXME");
			return;
		}

		//
		// add file
		//
		if (utility.tagFile(m_file_name, tag_text, true) == false) {

			//
			// failed to tag
			//
			return;
		}

		//
		// dispatch tagged event
		//
		EventDispatcher.getInstance().signalEvent(
				EventDispatcher.EventId.FILE_TAGGED_EVENT,
				new Object[] { m_file_name });

		Logger.i("AddFileTag::performTag file : " + m_file_name
				+ " added to database");

		//
		// done now
		// re-initialize
		//
		initialize(getView(), false);
	}

	/**
	 * This class serves as callback adapter. It will call the onTagButtonClick
	 * when a tag button is clicked
	 * 
	 * @author Johannes Anderwald
	 * 
	 */
	public class ButtonTagClickListener implements OnClickListener {

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

		public void onClick(View v) {

			//
			// get button
			//
			Button button = (Button) getView().findViewById(m_button_id);

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

	public void processMenuFileSelection(MENU_ITEM_ENUM selection) {

		//
		// check if the view is current visible
		//
		if (MainPageAdapter.getInstance().getCurrentItem() != 1) {
			//
			// HACK: ViewPageAdapter does not call onPause when switching to
			// next fragment
			//
			return;
		}

		//
		// defer control to common dialog operations object
		//
		m_dialog_operations.performDialogItemOperation(m_file_name, false,
				selection);

		//
		// check if the operation was a file deletion action
		//
		if (selection == MENU_ITEM_ENUM.MENU_ITEM_DELETE) {
			//
			// reinit view
			//
			initialize(getView(), false);
		}
	}
}
