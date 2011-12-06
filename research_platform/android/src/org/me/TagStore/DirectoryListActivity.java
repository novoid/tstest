package org.me.TagStore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.DirectoryChangeWorker;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.MainServiceConnection;
import org.me.TagStore.core.PendingFileChecker;
import org.me.TagStore.core.ServiceLaunchRunnable;
import org.me.TagStore.ui.ToastManager;

public class DirectoryListActivity extends ListFragment {

	/**
	 * stores mapping between list view and list view row
	 */
	private ArrayList<HashMap<String, Object>> m_ListViewMap;

	/**
	 * directory name key
	 */
	private static final String DIRECTORY_NAME = "DIRECTORY_NAME";

	
	/**
	 * if the entry is a directory
	 */
	private static final String DIRECTORY_ENTRY = "DIRECTORY_ENTRY";
	
	/**
	 * stores directory image key
	 */
	private static final String DIRECTORY_IMAGE = "DIRECTORY_IMAGE";

	/**
	 * key to user interface elements
	 * 
	 */
	private static final String DIRECTORY_UI_ELEMENT = "DIRECTORY_UI_ELEMENT";

	/**
	 * directory request code
	 */
	public static int DIRECTORY_LIST_REQUEST_CODE = 20;

	
	/**
	 * stores the connection handle to the watch dog service
	 */
	private MainServiceConnection m_connection;
	
	/**
	 * stores the path to the tagstore storage directory
	 */
	private String m_tagstore_directory;
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Logger.d("RequestCode " + requestCode + "resultCode " + resultCode
				+ "data : " + data);

		if (requestCode != DIRECTORY_LIST_REQUEST_CODE) {
			//
			// WTF?
			//
			return;
		}

		//
		// FIXME fragment
		//
		if (resultCode != Activity.RESULT_OK) {
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
			addItem(new_directory, true);

			//
			// construct new list adapter
			//
			DirectoryListAdapter list_adapter = new DirectoryListAdapter(this);

			//
			// set list adapter to list view
			//
			setListAdapter(list_adapter);		
						
			
			return;
		}

		//
		// inform user that the entry was already present
		//
		ToastManager.getInstance().displayToastWithString(R.string.directory_present);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);
		
		//
		// initialize tagstore directory
		//
		m_tagstore_directory = Environment.getExternalStorageDirectory().getAbsolutePath();
		m_tagstore_directory += File.separator + ConfigurationSettings.TAGSTORE_DIRECTORY;
		m_tagstore_directory += File.separator + getString(R.string.storage_directory);
		
		//
		// informal debug message
		//
		Logger.d("DirectoryListActivity::onCreate");
	}
	
	
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			
		 View view = inflater.inflate(R.layout.directory_list, null);

		//
		// construct new list map
		//
		m_ListViewMap = new ArrayList<HashMap<String, Object>>();
		m_ListViewMap.clear();

		//
		// get localized directory
		//
		String add_directory_string = getActivity().getString(R.string.add_directory);
		
		
		//
		// add directory button
		//
		addItem(add_directory_string, false);

		//
		// read directories from database
		//
		addDirectoriesFromDatabase();
		
		//
		// construct new list adapter
		//
		DirectoryListAdapter list_adapter = new DirectoryListAdapter(this);

		//
		// set list adapter to list view
		//
		setListAdapter(list_adapter);		
		
		//
		// build main service connection
		//
		m_connection = new MainServiceConnection();
		
		//
		// construct service launcher
		//
		ServiceLaunchRunnable launcher = new ServiceLaunchRunnable(getActivity().getApplicationContext(), m_connection);
		
		//
		// create a thread which will start the service
		//
		Thread launcher_thread = new Thread(launcher);
		
		//
		// now start the thread
		//
		launcher_thread.start();
		
		//
		// done
		//
		return view;
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

		//
		// now add them to the database
		//
		for (String directory_path : directory_list) {
			//
			// add item
			//
			addItem(directory_path, true);
		}
	}

	/**
	 * called when the activity is hidden
	 */
	public void onPause() {

		//
		// call super method
		//
		super.onPause();
		
		Logger.i("DirectoryList::onPause");
		
		//
		// get instance of database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get list of directories
		//
		ArrayList<String> db_directories = db_man.getDirectories();
		if (db_directories == null)
			db_directories = new ArrayList<String>();
		
		//
		// compute list of new directories
		//
		ArrayList<String> current_directories = new ArrayList<String>();
		
		for(HashMap<String, Object> map_entry : m_ListViewMap)
		{
			//
			// get directory image
			//
			Integer dir_image = (Integer)map_entry.get(DIRECTORY_IMAGE);
			if (dir_image == R.drawable.add_directory)
			{
				//
				// skip add directory entry
				//
				continue;
			}
			
			//
			// get directory value
			//
			String directory_path = (String) map_entry.get(DIRECTORY_NAME);
			
			//
			// store in list
			//
			current_directories.add(directory_path);
		}
		
		//
		// compute list of deleted directories
		//
		ArrayList<String> deleted_directories = new ArrayList<String>(db_directories);
		deleted_directories.removeAll(current_directories);
		
		//
		// remove deleted directories
		//
		for(String directory : deleted_directories)
		{
			//
			// remove path from database
			//
			db_man.removeDirectory(directory);

			//
			// remove path from watch dog service
			//
			m_connection.unregisterDirectory(directory);
			
			//
			// remove pending items
			//
			removePendingFilesfromDirectory(directory);			
		}
		
		//
		// compute list of new added directories
		//
		current_directories.removeAll(db_directories);
		
		//
		// now add them
		//
		for(String directory : current_directories)
		{
			Logger.e("adding: " + directory);
			
			//
			// add path to directory
			//
			db_man.addDirectory(directory);
			
			//
			// add path to observed list
			//
			m_connection.registerDirectory(directory);

			//
			// add new files from that directory
			//
			queryAddNewDirectories(directory);			
		}
		
		//
		// cancel any on-going toast when switching the view
		//
		ToastManager.getInstance().cancelToast();		
	}

	/**
	 * adds all files from the that directory to the pending file list
	 * @param directory_path
	 */
	private void queryAddNewDirectories(String directory_path) {
		
		//
		// construct directory change worker
		//
		DirectoryChangeWorker change_worker = new DirectoryChangeWorker();
		
		//
		// get instance of database manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// get new files from directory
		//
		ArrayList<String> files = change_worker.getNewFilesFromDirectory(directory_path);
		
		for(String file : files)
		{
			Logger.i("new file in directory: " + file);
			db_man.addPendingFile(file);
		}
	}
	
	
	
	
	/**
	 * removes all pending files which are placed in that directory
	 * @param directory_path
	 */
	private void removePendingFilesfromDirectory(String directory_path) {
		
		//
		// get pending file checker
		//
		PendingFileChecker file_checker = new PendingFileChecker();
		
		//
		// get pending files
		//
		ArrayList<String> pending_files = file_checker.getPendingFiles();

		//
		// loop all files and remove pending files
		//
		for(String current_file : pending_files)
		{
			File cfile = new File(current_file);
			if (cfile.getParent().compareTo(directory_path) == 0)
			{
				//
				// file is directory which got removed
				//
				FileTagUtility.removePendingFile(current_file);
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
	protected void addItem(String item_name, boolean is_directory) {

		//s
		// construct new entry
		//
		HashMap<String, Object> map_entry = new HashMap<String, Object>();

		//
		// store directory name in it
		//
		map_entry.put(DIRECTORY_NAME, item_name);

		if (is_directory) {
			
			if (!item_name.equals(m_tagstore_directory))
			{
				//
				// add remove button
				//
				map_entry.put(DIRECTORY_IMAGE,
						new Integer(R.drawable.remove_button));
			}
			else
			{
				//
				// default tagstore storage directory
				//
				map_entry.put(DIRECTORY_IMAGE,
						new Integer(R.drawable.storage_button));
			}
			
			//
			// it is a directory entry
			//
			map_entry.put(DIRECTORY_ENTRY,
					new Boolean(true));
			
		} else {
			//
			// add "add directory" button
			//
			map_entry.put(DIRECTORY_IMAGE,
					new Integer(R.drawable.add_directory));
			
			//
			// not a directory entry
			//
			map_entry.put(DIRECTORY_ENTRY,
					new Boolean(false));
		}

		//
		// add map entry to list view map
		//
		m_ListViewMap.add(map_entry);
	}
	
	/**
	 * launches the file dialog browser
	 */
	private void launchFileDialogBrowser() {
		
		//
		// get current storage state
		//
		String storage_state = Environment.getExternalStorageState();
		if (!storage_state.equals(Environment.MEDIA_MOUNTED) && !storage_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
		{
			Logger.e("Error: storage_state is : " + storage_state);
			
			//
			// the media is currently not accessible
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_media_not_mounted);
			
			//
			// done
			//
			return;
		}
		
		//
		// time to start new activity
		//
		Intent new_intent = new Intent(getActivity(),
				FileDialogBrowser.class);

		//
		// get SD card directory
		//
		String sd_card_directory = Environment.getExternalStorageDirectory().getAbsolutePath();

		//
		// add start directory
		//
		new_intent.putExtra(FileDialogBrowser.START_PATH_PARAMETER,
				sd_card_directory);

		//
		// now create view
		//

		startActivityForResult(new_intent, DirectoryListActivity.DIRECTORY_LIST_REQUEST_CODE);
	}
	
	
	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {

		Logger.d("Item " + id + " was clicked");

		//
		// get entry
		//
		HashMap<String, Object> map_entry = m_ListViewMap.get((int)id);
		
		//
		// is it a directory
		//
		boolean directory_entry = ((Boolean)map_entry.get(DIRECTORY_ENTRY)).booleanValue();
		if (!directory_entry)
		{
			//
			// launch browser
			//
			launchFileDialogBrowser();
			return;
		}
		
		//
		// get directory path
		//
		String path = (String)map_entry.get(DIRECTORY_NAME);
		
		//
		// is it the default tagstore directory
		//
		if (m_tagstore_directory.equals(path))
		{
			//
			// the tagstore directory storage directory can not be removed
			//
			ToastManager.getInstance().displayToastWithString(R.string.error_tagstore_directory);
			return;
		}		
		
		//
		// remove item
		//
		m_ListViewMap.remove((int)id);

		//
		// re-create list adapter, how inefficient ;)
		//
		DirectoryListAdapter list_adapter = new DirectoryListAdapter(this);

		//
		// set list adapter to list view
		//
		setListAdapter(list_adapter);

		//
		// done
		//
		return;
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
		//private ArrayList<HashMap<String, Object>> m_Content;

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

		public DirectoryListAdapter(DirectoryListActivity activity) {

			//
			// store activity
			//
			m_Activity = activity;

			//
			// construct layout inflater from context
			//
			m_LayoutInflater = LayoutInflater.from(activity.getActivity());
		}

		@Override
		public int getCount() {

			//
			// return count
			//
			return m_ListViewMap.size();
		}

		@Override
		public Object getItem(int index) {

			//
			// return index
			//
			return m_ListViewMap.get(index);
		}

		@Override
		public long getItemId(int position) {

			//
			// position is index
			//
			return position;
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
