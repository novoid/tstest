package org.me.TagStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.DialogIds;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.IconViewClickListenerCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.interfaces.RetagDialogCallback;
import org.me.TagStore.interfaces.TagStackUIButtonCallback;
import org.me.TagStore.ui.DialogItemOperations;
import org.me.TagStore.ui.FileDialogBuilder;
import org.me.TagStore.ui.IconViewItemBuilder;
import org.me.TagStore.ui.IconViewListAdapter;
import org.me.TagStore.ui.IconViewScrollListener;
import org.me.TagStore.ui.TagStackUIButtonAdapter;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ListView;

public class TagStoreListViewActivity extends ListActivity implements GeneralDialogCallback, 
																	  RenameDialogCallback, 
																	  TagStackUIButtonCallback, 
																	  IconViewClickListenerCallback, 
																	  RetagDialogCallback
{

	/**
	 * reference to database manager
	 */
	DBManager m_db;

	/**
	 * stores stack of the visited items
	 */
	TagStackManager m_tag_stack;


	/**
	 * list view contents
	 */
	ArrayList<HashMap<String, Object>> m_list_map;

	/**
	 * stores the tag list sorting order
	 */
	ArrayList<String> m_tag_list;

	/**
	 * stores the tag navigation button adapter
	 */
	TagStackUIButtonAdapter m_tag_list_view;
	
	/**
	 * stores the tag reference count
	 */
	HashMap<String, Integer> m_tag_reference_count;

	/**
	 * stores the common dialog operations object
	 */
	DialogItemOperations m_dialog_operations;
	
	/**
	 * current active file item
	 */
	int m_selected_item_index;

	/**
	 * scroll listener of view
	 */
	IconViewScrollListener m_scroll_listener;


	
	@Override
	public void onPause() {

		//
		// call super implementation
		//
		super.onPause();

		Logger.e("TagStoreListViewActivity::onPause");

		//
		// is there a scroll listener
		//
		if (m_scroll_listener != null) {
			//
			// deactivate toast
			//
			m_scroll_listener.cancelToast();
		}

	}

	@Override
	public void onResume() {

		//
		// call super implementation
		//
		super.onResume();

		Logger.i("TagStoreListViewActivity::onResume");

		//
		// refresh view
		//
		refreshView();
	}

	protected void onPrepareDialog (int id, Dialog dialog) {
		
		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_list_map
				.get(m_selected_item_index);

		//
		// file name
		//
		String file_name;		

		//
		// check if tag
		//
		boolean is_tag = !map_entry.containsKey(IconViewItemBuilder.ITEM_PATH);
		
		if (is_tag)
		{
			//
			// item name
			//
			file_name = (String) map_entry.get(IconViewItemBuilder.ITEM_NAME);
		}
		else
		{
			file_name = (String) map_entry.get(IconViewItemBuilder.ITEM_PATH);
		}
		
		//
		// let common dialog operations handle it
		//
		m_dialog_operations.prepareDialog(id, dialog, file_name, is_tag);
	}
	
	/**
	 * called when there is a need to construct a dialog
	 */
	protected Dialog onCreateDialog(int id) {

		Logger.i("TagStoreListViewActivity::onCreateDialog dialog id: " + id);

		//
		// let common dialog operations handle it
		//
		return m_dialog_operations.createDialog(id);
	}

	protected void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("TagStoreListViewActivity::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// lets sets our own design
		//
		setContentView(R.layout.tagstore_list_view);

		//
		// acquire instance of tag stack
		//
		m_tag_stack = TagStackManager.getInstance();

		//
		// construct list map
		//
		m_list_map = new ArrayList<HashMap<String, Object>>();

		//
		// initialize configuration tab
		//
		initialize();

	}

	/**
	 * refresh current view
	 */
	private void refreshView() {

		//
		// clear the map
		//
		m_list_map.clear();
		
		//
		// verify the tag stack
		//
		m_tag_stack.verifyTags();
		
		
		if (m_tag_stack.isEmpty())
		{
			//
			// refresh root view
			//
			if (!fillListMap()) {
				//
				// failed to fill list map
				//
				Logger.e("Error: TagStoreListActivity::onResume failed to fill list map");
				
				//
				// update tag tree
				//
				buildTagTreeForUI();				
				return;
			}

			//
			// now build the list the view
			//
			buildListView();
			
			//
			// done
			//
			return;
		}			

		
		//
		// this is a bit retarded ;)
		//
		String last_element = m_tag_stack.getLastTag();

		assert(last_element != null);
		
		//
		// fill list map with this tag
		//
		fillListMapWithTag(last_element);

		//
		// now rebuild list
		//
		buildListView();
	}

	/**
	 * refreshes view and displays all associated tags and files in respect to the given tag
	 * @param tag 
	 */
	public void tagButtonClicked(String tag) {
		
		//
		// clear tag stack
		//
		m_tag_stack.clearTags();
		
		//
		// add the tag
		//
		m_tag_stack.addTag(tag);
		
		//
		// refresh view
		//
		refreshView();
	}
	
	
	/**
	 * removes the last tag from the tag stack
	 */
	private void removeLastTagFromTagStack() {

		if (!m_tag_stack.isEmpty())
		{
			//
			// this is a bit retarded ;)
			//
			String last_element = m_tag_stack.getLastTag();

			//
			// remove last element
			//
			m_tag_stack.removeTag(last_element);
		}
	}


	public void renamedFile(String old_file_name, String new_file) {
		
		//
		// refresh view
		//
		refreshView();
	}
	
	@Override
	public void renamedTag(String old_tag_name, String new_tag_name) {
		//
		// refresh view
		//
		refreshView();
		
	}
	
	public void retaggedFile(String file_name, String tag_text) {

		//
		// refresh view
		//
		refreshView();		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		//
		// check if not back button was pressed
		//
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			//
			// pass on to default handler
			//
			return super.onKeyDown(keyCode, event);
		}

		Logger.i("TagStoreListViewActivity::onKeyDown empty: " + m_tag_stack.isEmpty());

		//
		// check if object stack is empty
		//
		if (m_tag_stack.isEmpty()) {
			//
			// exit application
			//
			TagActivityGroup.s_Instance.back();
			return true;
		}

		//
		// pop last tag from stack if available
		//
		removeLastTagFromTagStack();

		//
		// now refresh view
		//
		refreshView();

		//
		// mission accomplished
		//
		return true;
	}



	/**
	 * fills the list map with contents
	 * 
	 * @return
	 */
	private boolean fillListMap() {

		//
		// acquire shared settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get current sort mode
		//
		String sort_mode = settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_SORT_MODE,
				ConfigurationSettings.DEFAULT_LIST_VIEW_SORT_MODE);

		Logger.i("TagStoreListViewActivity::fillListMap sort_mode " + sort_mode);

		if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_ALPHABETIC) == 0) {
			//
			// get tags sorted by alphabet
			//
			m_tag_list = m_db.getAlphabeticTags();
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_POPULAR) == 0) {
			//
			// get tags sorted by popularity
			//
			m_tag_list = m_db.getPopularTags();
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_RECENT) == 0) {
			//
			// TODO: get tags sorted by recently used
			//
			Logger.e("Error: TagListViewActivity::fillListMap get recent tags is not implemented yet");
		}

		if (m_tag_list == null) {
			//
			// no tag list
			//
			return false;
		}

		//
		// now add the tags to the list
		//
		for (String current_tag : m_tag_list) {
			//
			// add item to list
			//
			addItem(current_tag, true);
		}

		//
		// done
		//
		return true;
	}

	/**
	 * initializes the list view
	 */
	private void initialize() {

		//
		// acquire database manager instance
		//
		m_db = DBManager.getInstance();

		
		//
		// create dialog operations object
		//
		m_dialog_operations = new DialogItemOperations(this, TagActivityGroup.s_Instance);
		
		//
		// create button array
		//
		Button[] buttons = new Button[5];
		buttons[0] = (Button) findViewById(R.id.navigation_button_one);
		buttons[1] = (Button) findViewById(R.id.navigation_button_two);
		buttons[2] = (Button) findViewById(R.id.navigation_button_three);
		buttons[3] = (Button) findViewById(R.id.navigation_button_four);
		buttons[4] = (Button) findViewById(R.id.navigation_button_five);
		
		
		//
		// acquire tag text list element
		//
		m_tag_list_view = new TagStackUIButtonAdapter(this, buttons);
				

		if (!fillListMap()) {
			//
			// failed to fill list map
			//
			Logger.e("Error: TagStoreListActivity::initialize failed to fill list map");
			return;
		}

		//
		// now build the list the view
		//
		buildListView();

	}

	/**
	 * builds the tag tree for the user interface element and updates it
	 */
	protected void buildTagTreeForUI() {

		//
		// set empty tag list
		//
		if (m_tag_list_view != null) {
			m_tag_list_view.refresh();
		}
	}

	/**
	 * build the list view
	 */
	protected void buildListView() {

		//
		// acquire shared settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get number per row
		//
		int num_items_per_row = settings.getInt(
				ConfigurationSettings.NUMBER_OF_ITEMS_PER_ROW,
				ConfigurationSettings.DEFAULT_ITEMS_PER_ROW);

		//
		// update tag tree
		//
		buildTagTreeForUI();

		//
		// construct list adapter
		//
		IconViewListAdapter list_adapter = new IconViewListAdapter(
				num_items_per_row, m_list_map, this, this);

		//
		// set list adapter to list view
		//
		setListAdapter(list_adapter);

		ListView list_view = getListView();

		//
		// construct new list view scroll listener
		//
		m_scroll_listener = new IconViewScrollListener(list_adapter, this.getApplicationContext());

		//
		// set new scroll listener
		//
		list_view.setOnScrollListener(m_scroll_listener);
	}

	/**
	 * returns true when the selected item is a tag item
	 * 
	 * @param position
	 *            of item
	 * @return boolean
	 */
	private boolean isTagItem(int position) {

		//
		// get hash map entry
		//
		HashMap<String, Object> map_entry = m_list_map.get(position);

		//
		// check if there is a path associated
		//
		return map_entry.containsKey(IconViewItemBuilder.ITEM_PATH) == false;
	}
	
	/**
	 * launches an item
	 * 
	 * @param position
	 */
	private void launchItem(int position) {

		//
		// get hash map entry
		//
		HashMap<String, Object> map_entry = m_list_map.get(position);

		//
		// get item path
		//
		String item_path = (String) map_entry.get(IconViewItemBuilder.ITEM_PATH);
		
		//
		// let dialog operations handle it
		//
		m_dialog_operations.performDialogItemOperation(item_path, false, FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN);
	}

	/**
	 * this method applies sort method to the collected tag list
	 * 
	 * @param tag_list
	 *            tag list to be sorted
	 * @return sorted tag list depending on current sort method
	 */
	private ArrayList<String> applySortMethodToTagList(
			ArrayList<String> tag_list) {

		//
		// acquire shared settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get current sort mode
		//
		String sort_mode = settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_SORT_MODE,
				ConfigurationSettings.DEFAULT_LIST_VIEW_SORT_MODE);

		if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_ALPHABETIC) == 0) {
			//
			// sort array by collections
			//
			Collections.sort(tag_list);
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_POPULAR) == 0) {
			//
			// sort by using comparator
			//
			Collections.sort(tag_list, new Comparator<String>() {

				@Override
				public int compare(String tag1, String tag2) {

					//
					// get the tag reference counts
					//
					Integer tag1_ref_count = m_tag_reference_count.get(tag1);
					Integer tag2_ref_count = m_tag_reference_count.get(tag2);

					//
					// compare if tag1 is has bigger reference count
					//
					return tag1_ref_count.compareTo(tag2_ref_count);
				}
			});
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_RECENT) == 0) {
			//
			// TODO: implement me recent mode
			//
			Logger.e("TagStoreListViewActivity::applySortMethodToTagList recent mode is not implemented yet");
		}

		//
		// return result
		//
		return tag_list;
	}

	/**
	 * adds an item to the list view
	 * @param item_name name of the item
	 * @param is_tag if true the item is a tag
	 */
	protected void addItem(String item_name, boolean is_tag) {
		
		//
		// construct entry
		//
		HashMap<String, Object> map_entry = IconViewItemBuilder.buildIconViewItem(getApplicationContext(), item_name, is_tag);
		if (map_entry != null)
		{
			//
			// add to list
			//
			m_list_map.add(map_entry);
		}
	}
	
	/**
	 * fills the list map with tags related to item name and associated files
	 * @param item_name name of the tag
	 */
	protected void fillListMapWithTag(String item_name) {

		//
		// get tag reference count
		//
		m_tag_reference_count = m_db.getTagReferenceCount();


		//
		// get tag stack
		//
		ArrayList<String> tag_stack = m_tag_stack.toArray(new String[1]);
		
		
		//
		// get associated tags
		//
		ArrayList<String> linked_tags = m_db.getLinkedTags(tag_stack);

		if (linked_tags != null) {
			//
			// apply sorting
			//
			linked_tags = applySortMethodToTagList(linked_tags);

			//
			// add them to list
			//
			for (String linked_tag : linked_tags) {
				//
				// check if tag has already been visited
				//
				if (m_tag_stack.containsTag(linked_tag)) {
					//
					// tag already present in stack
					//
					Logger.i("TagStoreListViewActivity::onListItemClick tag "
							+ linked_tag
							+ " not added to list as it already visited");
					continue;
				}

				//
				// add item
				//
				addItem(linked_tag, true);
			}
		}

		//
		// get associated files
		// m_tag_stack.toArray(new String[1])
		ArrayList<String> linked_files = m_db.getLinkedFiles(tag_stack);

		if (linked_files != null) {
			//
			// add linked files
			//
			for (String linked_file : linked_files) {
				addItem(linked_file, false);
			}
		}
	}

	/**
	 * invokes action based on the selection
	 * 
	 * @param item_path
	 *            path of item
	 * @param action_id
	 *            what the user has selected
	 */
	public void processMenuFileSelection(FileDialogBuilder.MENU_ITEM_ENUM action_id) {

		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_list_map
				.get(m_selected_item_index);

		//
		// get item path
		//
		String item_path = (String) map_entry.get(IconViewItemBuilder.ITEM_PATH);
		
		boolean is_tag = false;
		if (item_path == null)
		{
			//
			// it is a tag
			//
			is_tag = true;
			item_path = (String)map_entry.get(IconViewItemBuilder.ITEM_NAME);
		}
		
		//
		// defer control to common dialog operations object
		//
		boolean result = m_dialog_operations.performDialogItemOperation(item_path, is_tag, action_id);
		
		if (result)
		{
			//
			// refresh the view
			//
			refreshView();
		}
	}

	public boolean onLongListItemClick(int pos) {

		Logger.d("TagStoreListViewActivity::onLongListItemClick id=" + pos);

		if (pos >= m_list_map.size()) {
			//
			// invalid click
			//
			Logger.e("onLongListItemClick> out of bounds index: " + pos
					+ " size: " + m_list_map.size());
			return true;
		}

		//
		// store selected item position
		//
		m_selected_item_index = pos;
		
		
		
		//
		// check if this is an tag item
		//
		if (isTagItem(pos)) {
			
			//
			// launch general tag menu
			//
			showDialog(DialogIds.DIALOG_GENERAL_TAG_MENU);
		}
		else
		{
			//
			// request new dialog to be shown
			//
			showDialog(DialogIds.DIALOG_GENERAL_FILE_MENU);
		}
		
		return true;
	}

	public void onListItemClick(int position) {

		Logger.d("TagStoreListViewActivity::onListItemClick> Item " + position + " was clicked");

		if (position >= m_list_map.size()) {
			//
			// invalid click
			//
			Logger.e("onListItemClick> out of bounds index: " + position
					+ " size: " + m_list_map.size());
			return;
		}

		//
		// check if the clicked item was a file
		//
		if (!isTagItem(position)) {
			//
			// launch item
			//
			launchItem(position);

			//
			// done
			//
			return;
		}

		//
		// get hash map entry
		//
		HashMap<String, Object> map_entry = m_list_map.get(position);

		//
		// get item name
		//
		String item_name = (String) map_entry.get(IconViewItemBuilder.ITEM_NAME);

		//
		// add item to visited tag stack
		//
		m_tag_stack.addTag(item_name);

		//
		// now clear the list map
		//
		m_list_map.clear();

		//
		// fill list map with this tag
		//
		fillListMapWithTag(item_name);

		//
		// now rebuild list
		//
		buildListView();
	}
}
