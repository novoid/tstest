package org.me.TagStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TagStoreListViewActivity extends ListActivity {

	/**
	 * reference to database manager
	 */
	DBManager m_db;

	/**
	 * hash map key for name of the item
	 */
	protected static final String ITEM_NAME = "item_name";

	/**
	 * hash map key for icon of the item
	 */
	protected static final String ITEM_ICON = "item_icon";

	/**
	 * hash map key for path of a file item
	 */
	protected final static String ITEM_PATH = "item_path";

	private enum MENU_ITEM_ENUM {
		MENU_ITEM_DETAILS, MENU_ITEM_SEND, MENU_ITEM_DELETE
	};

	/**
	 * stores stack of the visited items
	 */
	LinkedHashSet<String> m_tag_stack;

	/**
	 * list view contents
	 */
	ArrayList<HashMap<String, Object>> m_list_map;

	/**
	 * stores the tag list sorting order
	 */
	ArrayList<String> m_tag_list;

	/**
	 * stores tag list ui element
	 */
	TextView m_tag_list_view;

	/**
	 * stores the tag reference count
	 */
	HashMap<String, Integer> m_tag_reference_count;

	/**
	 * current active file item
	 */
	int m_selected_item_index;

	/**
	 * scroll listener of view
	 */
	ListViewScrollListener m_scroll_listener;

	/**
	 * launches default menu dialog
	 */
	private static final int DIALOG_GENERAL_FILE_MENU = 1;

	/**
	 * launches the details view
	 */
	private static final int DIALOG_DETAILS = 2;

	/**
	 * launches the audio dialog
	 */
	private static final int DIALOG_AUDIO = 4;

	/**
	 * launches the video dialog
	 */
	private static final int DIALOG_VIDEO = 5;

	/**
	 * launches the image dialog
	 */
	private static final int DIALOG_IMAGE = 6;

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

		Logger.i("TagStoreListViewActivity::onResume tag stacksize "
				+ m_tag_stack.size());

		//
		// check if tag stack is empty
		//
		if (m_tag_stack.size() > 0) {
			//
			// refresh view
			//
			refreshView();
		} else {
			//
			// clear the map
			//
			m_list_map.clear();

			//
			// refresh root view
			//
			if (!fillListMap()) {
				//
				// failed to fill list map
				//
				Logger.e("Error: TagStoreListActivity::onResume failed to fill list map");
				return;
			}

			//
			// now build the list the view
			//
			buildListView();
		}
	}

	/**
	 * called when there is a need to construct a dialog
	 */
	protected Dialog onCreateDialog(int id) {

		Logger.i("TagStoreListViewActivity::onCreateDialog dialog id: " + id);

		//
		// construct dialog
		//
		Dialog dialog = null;

		//
		// check which dialog is requested
		//
		switch (id) {

		case DIALOG_GENERAL_FILE_MENU:
			dialog = buildGeneralDialogFile(m_selected_item_index);
			break;
		case DIALOG_DETAILS:
			dialog = buildDetailDialogFile(m_selected_item_index);
			break;
		case DIALOG_AUDIO:
			dialog = buildAudioDialogFile(m_selected_item_index);
			break;
		case DIALOG_VIDEO:
			dialog = buildVideoDialogFile(m_selected_item_index);
			break;
		case DIALOG_IMAGE:
			dialog = buildImageDialogFile(m_selected_item_index);
			break;
		}

		return dialog;
	}

	private Dialog buildImageDialogFile(int m_selected_item_index) {
		// TODO Auto-generated method stub
		return null;
	}

	private Dialog buildVideoDialogFile(int m_selected_item_index) {
		// TODO Auto-generated method stub
		return null;
	}

	private Dialog buildAudioDialogFile(int m_selected_item_index) {
		// TODO Auto-generated method stub
		return null;
	}

	private Dialog buildDetailDialogFile(int selected_item_index) {

		//
		// get layout inflater service
		//
		LayoutInflater inflater = (LayoutInflater) TagActivityGroup.s_Instance
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		//
		// construct view
		//
		View layout = inflater.inflate(R.layout.details_dialog,
				(ViewGroup) findViewById(R.id.layout_root));

		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_list_map.get(selected_item_index);

		//
		// get item name
		//
		String item_name = (String) map_entry.get(ITEM_NAME);

		//
		// get text field for name
		//
		TextView text = (TextView) layout.findViewById(R.id.file_name_value);
		if (text != null) {
			//
			// set file name
			//
			text.setText(item_name);
		}

		//
		// get item directory
		//
		String item_path = (String) map_entry.get(ITEM_PATH);

		//
		// create file object
		//
		File file = new File(item_path);

		//
		// remove file name part
		//
		String item_directory = file.getParent();

		//
		// get text field for folder
		//
		text = (TextView) layout.findViewById(R.id.file_folder_value);
		if (text != null) {
			//
			// set file name
			//
			text.setText(item_directory);
		}

		//
		// get file size
		//
		long length = file.length();

		//
		// get text field for size
		//
		text = (TextView) layout.findViewById(R.id.file_size_value);
		if (text != null) {
			//
			// set file name
			// FIXME: non-nls
			//
			text.setText(Long.toString(length) + " Bytes");
		}

		//
		// get mime type map instance
		//
		MimeTypeMap mime_map = MimeTypeMap.getSingleton();

		//
		// get file extension
		//
		String file_extension = item_name
				.substring(item_name.lastIndexOf(".") + 1);

		//
		// guess mime from extension extension
		//
		String mime_type = mime_map.getMimeTypeFromExtension(file_extension);
		//
		// get text field for size
		//
		text = (TextView) layout.findViewById(R.id.file_type_value);
		if (text != null) {
			//
			// set file name
			// FIXME: non-nls
			//
			text.setText(mime_type);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(
				TagActivityGroup.s_Instance);
		builder.setView(layout);
		AlertDialog alertDialog = builder.create();

		return alertDialog;
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
		// construct tag stack
		//
		m_tag_stack = new LinkedHashSet<String>();

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
	public void refreshView() {

		//
		// get all objects stored on the stack
		//
		String[] object_stack = m_tag_stack.toArray(new String[1]);

		//
		// this is a bit retarded ;)
		//
		String last_element = object_stack[object_stack.length - 1];

		//
		// clear the map
		//
		m_list_map.clear();

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
	 * removes the last tag from the tag stack
	 */
	private void removeLastTagFromTagStack() {

		//
		// get all objects stored on the stack
		//
		String[] object_stack = m_tag_stack.toArray(new String[1]);

		//
		// this is a bit retarded ;)
		//
		String last_element = object_stack[object_stack.length - 1];

		//
		// remove last element
		//
		m_tag_stack.remove(last_element);

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

		Logger.i("TagStoreListViewActivity::onKeyDown tag stack size: "
				+ m_tag_stack.size());

		//
		// check if object stack is empty
		//
		if (m_tag_stack.size() == 0) {
			//
			// exit application
			//
			TagActivityGroup.s_Instance.back();
			return true;
		}

		if (m_tag_stack.size() == 1) {
			//
			// back to the roots
			//
			m_list_map.clear();

			//
			// clear tag stack
			//
			m_tag_stack.clear();

			//
			// fill list map
			//
			fillListMap();

			//
			// build list adapter
			//
			buildListView();

			//
			// done
			//
			return true;
		}

		//
		// pop last tag from tag stack
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
	 * adds an item to the list map
	 * 
	 * @param item_name
	 *            name of the item
	 * @param item_icon
	 *            icon of the item
	 */
	private void addItem(String item_name, boolean is_tag) {

		//
		// create map entry
		//
		HashMap<String, Object> map_entry = new HashMap<String, Object>();

		//
		// acquire shared settings
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		Integer default_icon;

		if (is_tag) {
			//
			// get default tag icon
			//
			default_icon = settings.getInt(
					ConfigurationSettings.CURRENT_LIST_TAG_ICON,
					ConfigurationSettings.DEFAULT_LIST_TAG_ICON);

			//
			// add item name
			//
			map_entry.put(ITEM_NAME, item_name);
		} else {
			//
			// fixme: get icon from file type
			//
			default_icon = settings.getInt(
					ConfigurationSettings.CURRENT_LIST_ITEM_ICON,
					ConfigurationSettings.DEFAULT_LIST_ITEM_ICON);

			//
			// add item name
			//
			map_entry.put(ITEM_PATH, item_name);

			//
			// remove path from item name
			//
			String name = item_name.substring(item_name.lastIndexOf("/") + 1);

			//
			// add item name
			//
			map_entry.put(ITEM_NAME, name);
		}

		//
		// add item icon
		//
		map_entry.put(ITEM_ICON, default_icon);

		//
		// add to list map
		//
		m_list_map.add(map_entry);
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
		// acquire tag text list element
		//
		m_tag_list_view = (TextView) findViewById(R.id.navigation_tag_list);

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
		// check if it is empty
		//
		if (m_tag_stack.size() == 0) {
			//
			// set empty tag list
			//
			if (m_tag_list_view != null) {
				m_tag_list_view.setText("");
			}
			return;
		}

		//
		// get all tags in an array
		//
		String[] tag_list = m_tag_stack.toArray(new String[1]);

		String tag_tree = "";

		for (String current_tag : tag_list) {
			//
			// append to line
			//
			tag_tree += current_tag + " ";
		}

		//
		// set empty tag list
		//
		if (m_tag_list_view != null) {
			m_tag_list_view.setText(tag_tree);
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
		TagStoreListAdapter list_adapter = new TagStoreListAdapter(
				num_items_per_row, m_list_map, this);

		//
		// set list adapter to list view
		//
		setListAdapter(list_adapter);

		ListView list_view = getListView();

		//
		// construct new list view scroll listener
		//
		m_scroll_listener = new ListViewScrollListener();

		//
		// set new scroll listener
		//
		list_view.setOnScrollListener(m_scroll_listener);
	}

	private class ListViewScrollListener implements OnScrollListener {

		/**
		 * stores the toast
		 */
		Toast m_toast;

		/**
		 * old toast position
		 */
		int m_item_offset;

		/**
		 * cancels the active toast if any
		 */
		public void cancelToast() {

			if (m_toast != null) {
				//
				// cancel the active toast
				//
				Logger.i("Canceling toast");
				m_toast.cancel();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

			Logger.i("onScroll: item " + firstVisibleItem);

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
			// get offset
			//
			int offset = num_items_per_row * firstVisibleItem;

			//
			// sanity check
			//
			if (offset >= m_list_map.size()) {
				//
				// index out of bounds
				//
				Logger.e("OnScrollListener::onScroll firstVisibleItem "
						+ firstVisibleItem + " item per row "
						+ num_items_per_row + " map size: " + num_items_per_row
						+ " out of bounds");
				return;
			}

			if (m_toast != null && m_item_offset == offset) {
				//
				// superflous scroll event
				//
				return;
			}

			//
			// get map entry
			//
			HashMap<String, Object> map_entry = m_list_map.get(offset);
			if (map_entry == null) {
				Logger.e("no map entry");
				return;
			}

			//
			// get item name
			//
			String item_name = (String) map_entry.get(ITEM_NAME);

			//
			// trim name
			//
			item_name = item_name.substring(0, 1);

			//
			// and to upper case
			//
			item_name = item_name.toUpperCase();

			if (m_toast == null) {

				//
				// create toast
				//
				m_toast = Toast.makeText(getApplicationContext(), item_name,
						Toast.LENGTH_SHORT);

			} else {
				//
				// update text
				//
				m_toast.setText(item_name);
			}

			//
			// set toast position
			//
			m_toast.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL, 10, 0);

			//
			// store item offset
			//
			m_item_offset = offset;

			//
			// display toast
			//
			m_toast.show();
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}
	};

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
		return map_entry.containsKey(ITEM_PATH) == false;
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
		String item_path = (String) map_entry.get(ITEM_PATH);

		//
		// construct file object
		//
		File file = new File(item_path);

		if (file.exists() == false) {
			//
			// FIXME: non-nls compatible
			//
			Toast toast = Toast.makeText(getApplicationContext(),
					"Error: file " + item_path + " no longer exists",
					Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// FIXME: refresh perhaps?
			//
			return;
		}

		//
		// create new intent
		//
		Intent intent = new Intent();

		//
		// set intent action
		//
		intent.setAction(android.content.Intent.ACTION_VIEW);

		//
		// create uri from file
		//
		Uri uri_file = Uri.fromFile(file);

		//
		// get mime type map instance
		//
		MimeTypeMap mime_map = MimeTypeMap.getSingleton();

		//
		// get file extension
		//
		String file_extension = item_path
				.substring(item_path.lastIndexOf(".") + 1);

		//
		// guess file extension
		//
		String mime_type = mime_map.getMimeTypeFromExtension(file_extension);

		//
		// set intent data type
		//
		intent.setDataAndType(uri_file, mime_type);

		//
		// start activity
		//
		TagActivityGroup.s_Instance.startActivity(intent);
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
			// FIXME: implement me
			//
			Logger.e("TagStoreListViewActivity::applySortMethodToTagList recent mode is not implemented yet");
		}

		//
		// return result
		//
		return tag_list;
	}

	protected void fillListMapWithTag(String item_name) {

		//
		// get tag reference count
		//
		m_tag_reference_count = m_db.getTagReferenceCount();

		//
		// get associated tags
		//
		ArrayList<String> linked_tags = m_db.getLinkedTags(item_name);

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
				if (m_tag_stack.contains(linked_tag)) {
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
		//
		ArrayList<String> linked_files = m_db.getLinkedFiles(item_name);

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
	protected void processMenuFileSelection(MENU_ITEM_ENUM action_id) {

		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_list_map
				.get(m_selected_item_index);

		//
		// get item path
		//
		String item_path = (String) map_entry.get(ITEM_PATH);

		Logger.i("TagStoreListViewActivity::processMenuFileSelection item_path "
				+ item_path + " action: " + action_id);

		if (action_id == MENU_ITEM_ENUM.MENU_ITEM_DETAILS) {
			//
			// launch details view
			//
			showDialog(DIALOG_DETAILS);
		} else if (action_id == MENU_ITEM_ENUM.MENU_ITEM_DELETE) {
			//
			// FIXME: should display confirmation dialog
			//
			File file = new File(item_path);

			//
			// lets delete the file from the database
			//
			DBManager db_man = DBManager.getInstance();

			//
			// FIXME: this should be the job of file watch dog service...
			//
			db_man.removeFile(item_path, false, true);

			//
			// delete file
			//
			boolean file_deleted = file.delete();

			//
			// check for success
			//
			String msg;
			if (file_deleted) {
				//
				// FIXME: non-nls
				//
				msg = "Deleted " + item_path;
			} else {
				//
				// FIXME: non-nls
				//
				msg = "Error: failed to delete " + item_path;
			}

			//
			// create toast
			//
			Toast toast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			Logger.i("file_deleted " + file_deleted + " list map size "
					+ m_list_map.size() + " tag stack: " + m_tag_stack);

			//
			// check if file was deleted
			//
			if (file_deleted) {
				//
				// was this the only entry
				//
				if (m_list_map.size() == 1) {
					//
					// the only entry was deleted
					// pop off last item from tag stack and refresh view
					//
					removeLastTagFromTagStack();
				}
			}

			if (m_tag_stack.size() > 0) {
				//
				// refresh view
				//
				refreshView();
			} else {
				//
				// clear the map
				//
				m_list_map.clear();

				//
				// refresh root view
				//
				if (!fillListMap()) {
					//
					// failed to fill list map
					//
					Logger.e("Error: TagStoreListActivity::processMenuFileSelection failed to fill list map");
				}

				//
				// now build the list the view
				//
				buildListView();
			}
		} else if (action_id == MENU_ITEM_ENUM.MENU_ITEM_SEND) {
			File file = new File(item_path);

			if (file.exists() == false) {
				//
				// FIXME: non-nls compatible
				//
				Toast toast = Toast.makeText(getApplicationContext(),
						"Error: file " + item_path + " no longer exists",
						Toast.LENGTH_SHORT);

				//
				// display toast
				//
				toast.show();

				//
				// refresh view
				//
				refreshView();
				return;
			}

			//
			// create new intent
			//
			Intent intent = new Intent();

			//
			// set intent action
			//
			intent.setAction(android.content.Intent.ACTION_SEND);

			//
			// create uri from file
			//
			Uri uri_file = Uri.fromFile(file);

			//
			// get mime type map instance
			//
			MimeTypeMap mime_map = MimeTypeMap.getSingleton();

			//
			// get file extension
			//
			String file_extension = item_path.substring(item_path
					.lastIndexOf(".") + 1);

			//
			// guess file extension
			//
			String mime_type = mime_map
					.getMimeTypeFromExtension(file_extension);

			//
			// set intent data type
			//
			intent.setDataAndType(uri_file, mime_type);

			//
			// put file contents
			//
			intent.putExtra(Intent.EXTRA_STREAM, uri_file);

			//
			// start activity
			//
			TagActivityGroup.s_Instance.startActivity(intent);
		}

	}

	/**
	 * constructs a menu for a file item
	 * 
	 * @param pos
	 *            position of file in list map
	 */
	protected Dialog buildGeneralDialogFile(int pos) {

		//
		// construct new alert builder
		//
		AlertDialog.Builder builder = new AlertDialog.Builder(
				TagActivityGroup.s_Instance);

		//
		// FIXME: non-nls compatible
		//
		builder.setTitle("Options");

		//
		// construct array list for options
		//
		ArrayList<String> menu_options = new ArrayList<String>();
		ArrayList<MENU_ITEM_ENUM> action_ids = new ArrayList<MENU_ITEM_ENUM>();

		//
		// add default menu items
		// FIXME: non-nls compatible
		//
		menu_options.add("Details");
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_DETAILS);

		menu_options.add("Delete");
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_DELETE);

		menu_options.add("Send");
		action_ids.add(MENU_ITEM_ENUM.MENU_ITEM_SEND);

		//
		// FIXME: implement mime options
		//
		Logger.i("Should TagStoreListViewActivity::processMenuForFile process mime types options for file ");

		//
		// convert to string array
		//
		String[] menu_option_list = menu_options.toArray(new String[1]);

		//
		// set menu options and click handler
		//
		builder.setItems(menu_option_list,
				new FileMenuClickListener(action_ids));

		//
		// create dialog
		//
		Dialog dialog = builder.create();

		dialog.setOwnerActivity(this);

		return dialog;
	}

	private class FileMenuClickListener implements
			DialogInterface.OnClickListener {

		/**
		 * stores the menu item actions
		 */
		private ArrayList<MENU_ITEM_ENUM> m_action_ids;

		FileMenuClickListener(ArrayList<MENU_ITEM_ENUM> action_ids) {

			//
			// store members
			//
			m_action_ids = action_ids;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			//
			// perform callback
			//

			processMenuFileSelection(m_action_ids.get(which));
		}
	};

	protected boolean onLongListItemClick(View v, int pos, long id) {

		Logger.d("TagStoreListViewActivity::onLongListItemClick id=" + id);

		if (pos >= m_list_map.size()) {
			//
			// invalid click
			//
			Logger.e("onLongListItemClick> out of bounds index: " + pos
					+ " size: " + m_list_map.size());
			return true;
		}

		//
		// check if this is an tag item
		//
		if (isTagItem(pos)) {
			//
			// FIXME: support options for tags
			//
			return true;
		}

		//
		// store selected item position
		//
		m_selected_item_index = pos;

		//
		// request new dialog to be shown
		//
		showDialog(DIALOG_GENERAL_FILE_MENU);

		return true;
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {

		Logger.d("TagStoreListViewActivity::onListItemClick> Item " + id
				+ " was clicked");

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
		String item_name = (String) map_entry.get(ITEM_NAME);

		//
		// add item to visited tag stack
		//
		m_tag_stack.add(item_name);

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

	/**
	 * this class implements the list adapter which is used to display the items
	 * in the list view
	 * 
	 * @author Johannes Anderwald
	 */
	private class TagStoreListAdapter extends BaseAdapter {

		/**
		 * number of items to display per row
		 */
		private int m_num_items_per_row;

		/**
		 * stores the item data to display
		 */
		private ArrayList<HashMap<String, Object>> m_list_map;

		/*
		 * layout builder
		 */
		private LayoutInflater m_LayoutInflater;

		/**
		 * stores reference to listview activity, used for performing callbacks
		 */
		TagStoreListViewActivity m_Activity;

		/**
		 * constructor of class TagStoreListAdapter
		 * 
		 * @param num_items_per_row
		 *            number of items per row
		 * @param list_map
		 *            stores the details to display
		 */

		public TagStoreListAdapter(int num_items_per_row,
				ArrayList<HashMap<String, Object>> list_map,
				TagStoreListViewActivity activity) {

			//
			// initialize members
			//
			m_num_items_per_row = num_items_per_row;
			m_list_map = list_map;
			m_Activity = activity;

			//
			// construct layout inflater from context
			//
			m_LayoutInflater = LayoutInflater.from(activity);
		}

		@Override
		public int getCount() {

			//
			// return number of list elements divided by number of items
			//
			int list_size = m_list_map.size();

			if (list_size == 0) {
				//
				// no items
				//
				return 0;
			}

			//
			// get number of items
			//
			int count = list_size / m_num_items_per_row;

			if (list_size % m_num_items_per_row != 0) {
				//
				// there is unfinished row
				//
				count++;
			}

			return count;
		}

		@Override
		public Object getItem(int position) {

			//
			// return object
			//
			int offset = position * m_num_items_per_row;

			//
			// return object at that position
			//
			return m_list_map.get(offset);
		}

		@Override
		public long getItemId(int position) {

			//
			// position is id
			//
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ListItemUIElements element = null;

			//
			// calculate number of elements present in this row
			//
			int offset = position * m_num_items_per_row;
			;

			//
			// calculate item length
			//
			int num_items = Math.min(m_list_map.size() - offset,
					m_num_items_per_row);

			if (convertView == null) {

				int[] tag_list_view_rows = new int[] {
						R.layout.tagstore_list_row_one,
						R.layout.tagstore_list_row_two,
						R.layout.tagstore_list_row_three,
						R.layout.tagstore_list_row_four };

				//
				// construct a view which will be re-used for the specific item
				//
				convertView = m_LayoutInflater.inflate(
						tag_list_view_rows[m_num_items_per_row - 1], null);

				//
				// create new element
				//
				element = new ListItemUIElements(num_items);

				//
				// initialize element
				//
				element.initializeWithView(m_Activity, offset, convertView,
						m_num_items_per_row);

				//
				// store element in view
				//
				convertView.setTag(element);
			} else {
				//
				// get ui element
				//
				element = (ListItemUIElements) convertView.getTag();
			}

			//
			// initialize view
			//
			for (int index = 0; index < num_items; index++) {
				//
				// get map entry
				//
				HashMap<String, Object> map_entry = m_list_map.get(offset
						+ index);

				//
				// get item name
				//
				String item_name = (String) map_entry
						.get(TagStoreListViewActivity.ITEM_NAME);

				//
				// get item icon
				//
				Integer item_image = (Integer) map_entry
						.get(TagStoreListViewActivity.ITEM_ICON);

				//
				// set element item
				//
				element.setItemNameAndImage(index, item_name, item_image);
			}

			//
			// done
			//
			return convertView;
		}

		/**
		 * implements long click listener
		 * 
		 * @author Johannes Anderwald
		 * 
		 */
		private class ListItemLongClickListener implements OnLongClickListener {

			/**
			 * stores callback
			 */
			private TagStoreListViewActivity m_Activity;

			/**
			 * stores item index
			 */
			private int m_item_index;

			public ListItemLongClickListener(TagStoreListViewActivity activity,
					int item_index) {

				//
				// store members
				//
				m_Activity = activity;
				m_item_index = item_index;
			}

			@Override
			public boolean onLongClick(View v) {

				//
				// initiate call back
				//
				return m_Activity.onLongListItemClick(v, m_item_index,
						m_item_index);
			}

		}

		/**
		 * implements click listener
		 * 
		 * @author Johannes Anderwald
		 * 
		 */
		private class ListItemClickListener implements OnClickListener {

			/**
			 * stores callback
			 */
			private TagStoreListViewActivity m_Activity;

			/**
			 * stores item index
			 */
			private int m_item_index;

			public ListItemClickListener(TagStoreListViewActivity activity,
					int item_index) {

				//
				// store members
				//
				m_Activity = activity;
				m_item_index = item_index;
			}

			@Override
			public void onClick(View v) {

				//
				// initiate call back
				//
				m_Activity.onListItemClick(null, v, m_item_index, m_item_index);
			}
		}

		/**
		 * stores the user interface elements
		 * 
		 * @author Johannes Anderwald
		 * 
		 */
		private class ListItemUIElements {

			/**
			 * stores user interface elements
			 */
			public TextView m_ItemName1;
			public TextView m_ItemName2;
			public TextView m_ItemName3;
			public TextView m_ItemName4;

			public ImageButton m_ItemButton1;
			public ImageButton m_ItemButton2;
			public ImageButton m_ItemButton3;
			public ImageButton m_ItemButton4;

			public int m_num_elements;

			ListItemUIElements(int num_elements) {

				//
				// store number of elements available
				//
				m_num_elements = num_elements;
			}

			/**
			 * sets the item text and image
			 * 
			 * @param index
			 *            of item to set
			 * @param item_name
			 *            text of the item
			 * @param item_image
			 *            image of the item
			 */
			public void setItemNameAndImage(int index, String item_name,
					Integer item_image) {

				if (index == 0) {
					if (m_ItemName1 != null) {
						m_ItemName1.setText(item_name);
					}

					if (m_ItemButton1 != null) {
						m_ItemButton1.setImageResource(item_image.intValue());
					}
				} else if (index == 1) {
					if (m_ItemName2 != null) {
						m_ItemName2.setText(item_name);
					}

					if (m_ItemButton2 != null) {
						m_ItemButton2.setImageResource(item_image.intValue());
					}
				} else if (index == 2) {
					if (m_ItemName3 != null) {
						m_ItemName3.setText(item_name);
					}

					if (m_ItemButton3 != null) {
						m_ItemButton3.setImageResource(item_image.intValue());
					}
				} else if (index == 3) {
					if (m_ItemName4 != null) {
						m_ItemName4.setText(item_name);
					}

					if (m_ItemButton4 != null) {
						m_ItemButton4.setImageResource(item_image.intValue());
					}
				}
			}

			/**
			 * initializes all view elements
			 * 
			 * @param convertView
			 *            elements which are retrieved from this view
			 * @param num_elements_per_row
			 *            num elements per row
			 */
			public void initializeWithView(TagStoreListViewActivity activity,
					int item_offset, View convertView, int num_elements_per_row) {

				//
				// initialize first element
				//
				m_ItemName1 = (TextView) convertView
						.findViewById(R.id.tag_name_one);
				m_ItemButton1 = (ImageButton) convertView
						.findViewById(R.id.tag_image_one);

				if (m_ItemButton1 != null) {
					m_ItemButton1.setOnClickListener(new ListItemClickListener(
							activity, item_offset));
					m_ItemButton1
							.setOnLongClickListener(new ListItemLongClickListener(
									activity, item_offset));
				}

				if (m_ItemName1 != null) {
					m_ItemName1.setOnClickListener(new ListItemClickListener(
							activity, item_offset));
					m_ItemName1
							.setOnLongClickListener(new ListItemLongClickListener(
									activity, item_offset));
				}

				if (m_num_elements >= 2) {
					m_ItemName2 = (TextView) convertView
							.findViewById(R.id.tag_name_two);
					m_ItemButton2 = (ImageButton) convertView
							.findViewById(R.id.tag_image_two);
					if (m_ItemButton2 != null) {
						m_ItemButton2
								.setOnClickListener(new ListItemClickListener(
										activity, item_offset + 1));
						m_ItemButton2
								.setOnLongClickListener(new ListItemLongClickListener(
										activity, item_offset + 1));
					}

					if (m_ItemName2 != null) {
						m_ItemName2
								.setOnClickListener(new ListItemClickListener(
										activity, item_offset));
						m_ItemName2
								.setOnLongClickListener(new ListItemLongClickListener(
										activity, item_offset));
					}
				} else if (num_elements_per_row >= 2) {
					//
					// element has at least two element, only one is present
					//
					m_ItemName2 = (TextView) convertView
							.findViewById(R.id.tag_name_two);
					m_ItemButton2 = (ImageButton) convertView
							.findViewById(R.id.tag_image_two);

					//
					// hide elements
					//
					if (m_ItemName2 != null) {
						m_ItemName2.setVisibility(View.INVISIBLE);
					}

					if (m_ItemButton2 != null) {
						m_ItemButton2.setVisibility(View.INVISIBLE);
					}
				}

				if (m_num_elements >= 3) {
					m_ItemName3 = (TextView) convertView
							.findViewById(R.id.tag_name_three);
					m_ItemButton3 = (ImageButton) convertView
							.findViewById(R.id.tag_image_three);

					if (m_ItemButton3 != null) {
						m_ItemButton3
								.setOnClickListener(new ListItemClickListener(
										activity, item_offset + 2));
						m_ItemButton3
								.setOnLongClickListener(new ListItemLongClickListener(
										activity, item_offset + 2));
					}

					if (m_ItemName3 != null) {
						m_ItemName3
								.setOnClickListener(new ListItemClickListener(
										activity, item_offset));
						m_ItemName3
								.setOnLongClickListener(new ListItemLongClickListener(
										activity, item_offset));
					}

				} else if (num_elements_per_row >= 3) {
					//
					// element has at least three element, less are present
					//
					m_ItemName3 = (TextView) convertView
							.findViewById(R.id.tag_name_three);
					m_ItemButton3 = (ImageButton) convertView
							.findViewById(R.id.tag_image_three);

					//
					// hide elements
					//
					if (m_ItemName3 != null) {
						m_ItemName3.setVisibility(View.INVISIBLE);
					}

					if (m_ItemButton3 != null) {
						m_ItemButton3.setVisibility(View.INVISIBLE);
					}
				}

				if (m_num_elements == 4) {
					m_ItemName4 = (TextView) convertView
							.findViewById(R.id.tag_name_four);
					m_ItemButton4 = (ImageButton) convertView
							.findViewById(R.id.tag_image_four);
					if (m_ItemButton4 != null) {
						m_ItemButton4
								.setOnClickListener(new ListItemClickListener(
										activity, item_offset + 3));
						m_ItemButton4
								.setOnLongClickListener(new ListItemLongClickListener(
										activity, item_offset + 3));
					}

					if (m_ItemName4 != null) {
						m_ItemName4
								.setOnClickListener(new ListItemClickListener(
										activity, item_offset));
						m_ItemName4
								.setOnLongClickListener(new ListItemLongClickListener(
										activity, item_offset));
					}
				} else if (num_elements_per_row == 4) {
					//
					// element has at least three element, less are present
					//
					m_ItemName4 = (TextView) convertView
							.findViewById(R.id.tag_name_four);
					m_ItemButton4 = (ImageButton) convertView
							.findViewById(R.id.tag_image_four);

					//
					// hide elements
					//
					if (m_ItemName4 != null) {
						m_ItemName4.setVisibility(View.INVISIBLE);
					}

					if (m_ItemButton4 != null) {
						m_ItemButton4.setVisibility(View.INVISIBLE);
					}
				}
			}
		}
	};
}
