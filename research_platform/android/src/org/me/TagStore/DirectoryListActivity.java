package org.me.TagStore;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.ArrayList;

public class DirectoryListActivity extends ListActivity {

	/**
	 * stores mapping between list view and list view row
	 */
	ArrayList<HashMap<String, Object>> m_ListViewMap;

	/**
	 * directory name key
	 */
	private static final String DIRECTORY_NAME = "DIRECTORY_NAME";

	/**
	 * stores directory image
	 */
	private static final String DIRECTORY_IMAGE = "DIRECTORY_IMAGE";

	/**
	 * key to user interface elements
	 * 
	 */
	private static final String DIRECTORY_UI_ELEMENT = "DIRECTORY_UI_ELEMENT";

	/**
	 * indicates if directory is from database
	 */
	private static final String DIRECTORY_DATABASE = "DIRECTORY_DATABASE";

	/**
	 * indicates if entry has been deleted
	 */
	private static final String DIRECTORY_DELETED = "DIRECTORY_DELETED";

	/**
	 * directory request code
	 */
	public static int DIRECTORY_LIST_REQUEST_CODE = 20;

	/**
	 * hack required as startActivityForResult is not working with nested
	 * activities
	 */
	public static DirectoryListActivity s_Instance;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ConfigurationActivityGroup.s_Instance.back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Logger.d("RequestCode " + requestCode + "resultCode " + resultCode
				+ "data : " + data);

		if (requestCode != DIRECTORY_LIST_REQUEST_CODE) {
			//
			// WTF?
			//
			return;
		}

		if (resultCode != RESULT_OK) {
			//
			// operation did not succeed
			//
			return;
		}

		//
		// get path
		//
		String new_directory = data.getExtras().getString(
				FileDialogBrowser.SELECTED_PATH_PARAMETER);

		//
		// check if the directory is not already present
		//
		int directory_index = getDirectoryIndex(new_directory);

		Logger.d("DirectoryListActivity::onActivityResult> index "
				+ directory_index);

		if (directory_index == -1) {
			//
			// path not present
			//
			addItem(new_directory, true, false);

			//
			// done
			//
			return;
		}

		//
		// get the existing entry
		//
		HashMap<String, Object> map_entry = m_ListViewMap.get(directory_index);

		//
		// lets check if the entry was deleted before
		//
		Boolean is_deleted = (Boolean) map_entry.get(DIRECTORY_DELETED);

		if (is_deleted.booleanValue() == false) {
			//
			// inform user that the entry was already present
			// FIXME non-nls compatible
			//
			Toast toast = Toast.makeText(getApplicationContext(),
					"Directory already in list", Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();
		} else {
			//
			// add item for user convenience on the end of list
			//
			addItem(new_directory, true, false);

			// FIXME
			// scroll if required
			//
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("DirectoryListActivity::onCreate");

		//
		// HACK: for retreiving result
		//
		s_Instance = this;

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// lets sets our own design
		//
		setContentView(R.layout.directory_list);

		//
		// construct new list map
		//
		m_ListViewMap = new ArrayList<HashMap<String, Object>>();

		//
		// get cancel button
		//
		Button CancelButton = (Button) findViewById(R.id.button_cancel);

		//
		// add cancel button click selector
		//
		CancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ConfigurationActivityGroup.s_Instance.back();
			}

		});

		//
		// get cancel button
		//
		Button DoneButton = (Button) findViewById(R.id.button_done);

		//
		// add cancel button click selector
		//
		DoneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				//
				// save changes
				//
				onSaveChanges();
				ConfigurationActivityGroup.s_Instance.back();
			}

		});

		//
		// TODO: non-nls
		//
		addItem("Add directory...", false, false);

		//
		// read directories from database
		//
		addDirectoriesFromDatabase();
	}

	/**
	 * returns the index of an directory
	 * 
	 * @param directory_name
	 *            path of directory to be searched
	 * @return -1 if not found, otherwise zero based index
	 */
	protected int getDirectoryIndex(String directory_name) {

		//
		// go through directory list
		//
		int Index = 0;
		for (HashMap<String, Object> map_entry : m_ListViewMap) {
			//
			// get directory path
			//
			String current_directory = (String) map_entry.get(DIRECTORY_NAME);

			if (current_directory.compareTo(directory_name) == 0) {
				//
				// directory already exists
				//
				return Index;
			}

			//
			// increment index
			//
			Index++;
		}

		return -1;
	}

	/**
	 * adds all observed directories to the database
	 */
	protected void addDirectoriesFromDatabase() {

		//
		// acquire instance of database
		//
		DBManager db_man = DBManager.getInstance();

		//
		// read listed directories
		//
		ArrayList<String> directory_list = db_man.getDirectories();

		if (directory_list == null) {
			//
			// no directories
			//
			return;
		}

		//
		// now add them to the database
		//
		for (String directory_path : directory_list) {
			//
			// add item
			//
			addItem(directory_path, true, true);
		}
	}

	/**
	 * this function is called when done button is pressed It will then collect
	 * all directories and add them to the database
	 */
	protected void onSaveChanges() {

		//
		// get instance of database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// remove first entry, it is the add directory entry
		//
		m_ListViewMap.remove(0);

		//
		// go through the list and add remove directories to the database
		//
		for (HashMap<String, Object> map_entry : m_ListViewMap) {
			//
			// check if the entry is database entry
			//
			Boolean db_entry = (Boolean) map_entry.get(DIRECTORY_DATABASE);

			//
			// check if entry was deleted
			//
			Boolean is_deleted = (Boolean) map_entry.get(DIRECTORY_DELETED);

			//
			// get directory value
			//
			String directory_path = (String) map_entry.get(DIRECTORY_NAME);

			if (db_entry.booleanValue()) {
				//
				// entry is from database -> check if it was deleted
				//
				if (is_deleted.booleanValue()) {
					//
					// entry was deleted from database
					//
					db_man.removeDirectory(directory_path);

					//
					// FIXME remove pending items
					//
				}
			} else if (is_deleted.booleanValue() == false) {
				//
				// entry is new and not deleted (again) by user
				//
				db_man.addDirectory(directory_path);
			}
		}
	}

	/**
	 * adds an item to the list view
	 * 
	 * @param item_name
	 *            name of the item
	 * @param is_directory
	 *            indicates if the item is a directory
	 */
	protected void addItem(String item_name, boolean is_directory,
			boolean db_entry) {

		//
		// construct new entry
		//
		HashMap<String, Object> map_entry = new HashMap<String, Object>();

		//
		// store directory name in it
		//
		map_entry.put(DIRECTORY_NAME, item_name);

		//
		// set entry active
		//
		map_entry.put(DIRECTORY_DELETED, new Boolean(false));

		//
		// set indicator if it is an database entry
		//
		map_entry.put(DIRECTORY_DATABASE, new Boolean(db_entry));

		if (is_directory) {
			//
			// TODO: add remove button
			//
			map_entry.put(DIRECTORY_IMAGE,
					new Integer(R.drawable.remove_button));
		} else {
			//
			// TODO: add "add directory" button
			//
			map_entry.put(DIRECTORY_IMAGE,
					new Integer(R.drawable.add_directory));
		}

		//
		// add map entry to list view map
		//
		m_ListViewMap.add(map_entry);

		//
		// construct new list adapter
		//
		DirectoryListAdapter list_adapter = new DirectoryListAdapter(
				m_ListViewMap, this);

		//
		// set list adapter to list view
		//
		setListAdapter(list_adapter);

	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {

		Logger.d("Item " + id + " was clicked");

		//
		// first item is add directory item
		//
		if (id != 0) {
			//
			// get hash map entry
			//
			HashMap<String, Object> map_entry = m_ListViewMap.get((int) id);

			//
			// mark entry as removed
			//
			map_entry.put(DIRECTORY_DELETED, new Boolean(true));

			//
			// re-create list adapter, how inefficient ;)
			//
			DirectoryListAdapter list_adapter = new DirectoryListAdapter(
					m_ListViewMap, this);

			//
			// set list adapter to list view
			//
			setListAdapter(list_adapter);

			//
			// done
			//
			return;
		}

		//
		// time to start new activity
		//
		Intent new_intent = new Intent(getBaseContext(),
				FileDialogBrowser.class);

		//
		// get SD card directory
		//
		String sd_card_directory = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath();

		//
		// add start directory
		//
		new_intent.putExtra(FileDialogBrowser.START_PATH_PARAMETER,
				sd_card_directory);

		//
		// now create view
		//

		View new_view = ConfigurationActivityGroup.s_Instance
				.getLocalActivityManager()
				.startActivity("FileDialogBrowser",
						new_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
				.getDecorView();

		//
		// now replace the view
		//
		ConfigurationActivityGroup.s_Instance.replaceView(new_view);
	}

	/**
	 * This class is used to implement a custom list view implementation
	 * 
	 * @author Johannes Anderwald
	 * 
	 */
	private class DirectoryListAdapter extends BaseAdapter {

		/**
		 * stores the content
		 */
		private ArrayList<HashMap<String, Object>> m_Content;

		/*
		 * layout builder
		 */
		private LayoutInflater m_LayoutInflater;

		/**
		 * reference to parent, used for issuing call backs
		 */
		DirectoryListActivity m_Activity;

		/**
		 * 
		 * @param content
		 * @param context
		 */

		public DirectoryListAdapter(ArrayList<HashMap<String, Object>> content,
				DirectoryListActivity activity) {

			//
			// store content
			//
			m_Content = content;

			//
			// store activity
			//
			m_Activity = activity;

			//
			// construct layout inflater from context
			//
			m_LayoutInflater = LayoutInflater.from(activity);
		}

		@Override
		public int getCount() {

			//
			// go through the list and only count these items which are not
			// marked as deleted
			//
			int Count = 0;
			for (HashMap<String, Object> map_entry : m_Content) {
				//
				// get entry deletion status
				//
				Boolean is_deleted = (Boolean) map_entry.get(DIRECTORY_DELETED);

				if (is_deleted.booleanValue() == false) {
					//
					// entry is not deleted
					//
					Count++;
				}
			}

			//
			// return item count
			//
			return Count;
		}

		@Override
		public Object getItem(int index) {

			//
			// go through the list and return entry at index which is not
			// deleted
			//
			int Count = 0;
			for (HashMap<String, Object> map_entry : m_Content) {
				//
				// get entry deletion status
				//
				Boolean is_deleted = (Boolean) map_entry.get(DIRECTORY_DELETED);

				if (is_deleted.booleanValue() == false) {
					//
					// entry is not deleted
					//
					if (Count == index) {
						//
						// found matching index
						//
						return (Object) map_entry;
					}

					//
					// increment object count
					//
					Count++;
				}
			}

			//
			// return object
			//
			Logger.e("Error: DirectoryListAdapter::getItem> index out of bounds");
			return null;
		}

		@Override
		public long getItemId(int position) {

			//
			// go through the list and return index of entry which was requested
			//
			int Count = 0;
			long Index = 0;
			for (HashMap<String, Object> map_entry : m_Content) {
				//
				// get entry deletion status
				//
				Boolean is_deleted = (Boolean) map_entry.get(DIRECTORY_DELETED);

				if (is_deleted.booleanValue() == false) {
					//
					// entry is not deleted
					//
					if (Count == position) {
						//
						// found matching index
						//
						return Index;
					}

					//
					// increment object count
					//
					Count++;
				}

				//
				// increment index
				//
				Index++;
			}

			//
			// bug
			//
			Logger.e("Error: DirectoryListAdapter::getItemId out of bounds");
			return -1;
		}

		@SuppressWarnings("unchecked")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			//
			// first get the map entry
			//
			HashMap<String, Object> map_entry = (HashMap<String, Object>) getItem(position);

			ListItemUIElements element;

			//
			// check if converted view is null
			//
			if (convertView == null) {
				//
				// construct a view which will be re-used for the specific item
				//
				convertView = m_LayoutInflater.inflate(
						R.layout.directory_list_row, null);

				//
				// create new element
				//
				element = new ListItemUIElements();

				//
				// get item view
				//
				element.m_ItemName = (TextView) convertView
						.findViewById(R.id.directory_name_text);

				//
				// get directory button
				//
				element.m_ItemButton = (ImageButton) convertView
						.findViewById(R.id.directory_button);

				Logger.d("m_ItemButton" + element.m_ItemButton);

				//
				// add key click listener
				//
				element.m_ItemButton
						.setOnClickListener(new ListItemClickListener(
								m_Activity, getItemId(position)));

				//
				// store element item in map entry
				//
				map_entry.put(DIRECTORY_UI_ELEMENT, element);
			} else {
				//
				// get ui element
				//
				element = (ListItemUIElements) map_entry
						.get(DIRECTORY_UI_ELEMENT);
			}

			//
			// get item text
			//
			String item_text = (String) map_entry
					.get(DirectoryListActivity.DIRECTORY_NAME);

			//
			// get button image
			//
			Integer resource_id = (Integer) map_entry
					.get(DirectoryListActivity.DIRECTORY_IMAGE);

			if (element.m_ItemName != null && item_text != null) {
				//
				// set item text
				//
				element.m_ItemName.setText(item_text);
			}

			if (element.m_ItemButton != null) {
				//
				// set item button
				//
				element.m_ItemButton.setImageResource(resource_id.intValue());
			}

			//
			// done
			//
			return convertView;
		}

		/**
		 * holds user interface information
		 * 
		 * @author Johannes Anderwald
		 * 
		 */
		private class ListItemUIElements {

			/**
			 * stores text view for re-used view
			 */
			public TextView m_ItemName;

			/**
			 * stores button for the re-used view
			 */
			public ImageButton m_ItemButton;

		};

		/**
		 * click listener for handling clicks on the list item button
		 * 
		 * @author Johannes Anderwald
		 * 
		 */
		private class ListItemClickListener implements OnClickListener {

			long m_Index;
			DirectoryListActivity m_Activity;

			/**
			 * constructor of ListItemClickListener
			 * 
			 * @param activity
			 *            activity callback to parent main frame
			 * @param index
			 *            of item
			 */

			public ListItemClickListener(DirectoryListActivity activity,
					long index) {

				//
				// store members
				//
				m_Activity = activity;
				m_Index = index;

			}

			@Override
			public void onClick(View v) {

				//
				// issue callback
				//
				m_Activity.onListItemClick(null, v, -1, m_Index);
			}
		}
	}

}
