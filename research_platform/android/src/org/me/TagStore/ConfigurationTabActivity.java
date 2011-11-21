package org.me.TagStore;

import java.util.ArrayList;
import java.util.HashMap;

import org.me.TagStore.R;
import org.me.TagStore.core.Logger;
import org.me.TagStore.ui.MainPageAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * This class displays various configuration settings and launches the
 * associated dialogs
 * 
 * @author Johannes Anderwald
 * 
 */
public class ConfigurationTabActivity extends ListFragment {

	/**
	 * key name for item name
	 */
	private static final String ITEM_NAME = "ITEM_NAME";

	/**
	 * key name for class to be launched
	 */
	private static final String ITEM_CLASS_NAME = "ITEM_CLASS_NAME";

	/**
	 * key for item image
	 */
	private static final String ITEM_IMAGE = "ITEM_IMAGE";

	/**
	 * holds list map
	 */
	protected ArrayList<HashMap<String, Object>> m_ListMap;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("ConfigurationTab::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// lets sets our own design
		//
		//setContentView(R.layout.configuration_tab);

		//
		// initialize configuration tab
		//
		initialize();

	}
	
	public void onResume() {
		
		//
		// resume
		//
		super.onResume();
		
		Logger.i("ConfigurationTabActivity::onResume");
	}
	
	public void onPause() {
		
		super.onPause();
		
		Logger.i("ConfigurationTabActivity::onPause");
		
	}
	
	
	/**
	 * adds the directory settings to configuration item
	 */
	private void addDirectoryConfigurationItem() {

		//
		// construct map entries
		//
		HashMap<String, Object> directory_map_entry = new HashMap<String, Object>();

		//
		// get localized string
		//
		String item_name = super.getActivity().getApplicationContext().getString(R.string.tagstore_directories);
		
		
		//
		// set name
		//
		directory_map_entry.put(ITEM_NAME, item_name);

		//
		// TODO: make image
		//
		directory_map_entry.put(ITEM_IMAGE, R.drawable.options_selected);

		//
		// FIXME: hard coded class name
		//
		directory_map_entry.put(ITEM_CLASS_NAME,
				"org.me.TagStore.DirectoryListActivity");

		//
		// add entry
		//
		m_ListMap.add(directory_map_entry);
	}

	/**
	 * adds the synchronization configuration item to the configuration list
	 */
	private void addSynchronizeConfigurationItem() {

		//
		// construct synchronize setting
		//
		HashMap<String, Object> synchronize_map_entry = new HashMap<String, Object>();

		//
		// get localized string
		//
		String item_name = super.getActivity().getApplicationContext().getString(R.string.sync_tagstore);
		
		//
		// set name
		//
		synchronize_map_entry.put(ITEM_NAME, item_name);

		//
		// FIXME: hard coded class name
		//
		synchronize_map_entry.put(ITEM_CLASS_NAME,
				"org.me.TagStore.SynchronizeTagStoreActivity");

		//
		// TODO: make image
		//
		synchronize_map_entry.put(ITEM_IMAGE, R.drawable.refresh);

		//
		// add entry
		//
		m_ListMap.add(synchronize_map_entry);
	}

	private void addInfoConfigurationItem() {

		//
		// construct synchronize setting
		//
		HashMap<String, Object> info_map_entry = new HashMap<String, Object>();

		//
		// get localized string
		//
		String item_name = super.getActivity().getApplicationContext().getString(R.string.about_tagstore);
		
		//
		// set name
		//
		info_map_entry.put(ITEM_NAME, item_name);
		
		//
		// FIXME: hard coded class name
		//
		info_map_entry.put(ITEM_CLASS_NAME, "org.me.TagStore.InfoTab");

		//
		// TODO: make image
		//
		info_map_entry.put(ITEM_IMAGE, R.drawable.info);

		//
		// add entry
		//
		m_ListMap.add(info_map_entry);
	}

	private void addListViewConfigurationItem() {

		//
		// construct synchronize setting
		//
		HashMap<String, Object> info_map_entry = new HashMap<String, Object>();

		
		//
		// get localized string
		//
		String item_name = super.getActivity().getApplicationContext().getString(R.string.listview_settings);
		
		//
		// set name
		//
		info_map_entry.put(ITEM_NAME, item_name);
		
		//
		// FIXME: hard coded class name
		//
		info_map_entry.put(ITEM_CLASS_NAME,
				"org.me.TagStore.ListViewSettingsActivity");

		//
		// TODO: make image
		//
		info_map_entry.put(ITEM_IMAGE, R.drawable.options_selected);

		//
		// add entry
		//
		m_ListMap.add(info_map_entry);
	}

	/**
	 * adds database configuration item
	 */
	private void addDatabaseConfigurationItem() {

		//
		// construct synchronize setting
		//
		HashMap<String, Object> info_map_entry = new HashMap<String, Object>();

		//
		// get localized string
		//
		String item_name = super.getActivity().getApplicationContext().getString(R.string.database_options);
		
		//
		// set name
		//
		info_map_entry.put(ITEM_NAME, item_name);
		
		//
		// FIXME: hard coded class name
		//
		info_map_entry.put(ITEM_CLASS_NAME,
				"org.me.TagStore.DatabaseSettingsActivity");

		//
		// TODO: make image
		//
		info_map_entry.put(ITEM_IMAGE, R.drawable.options_selected);

		//
		// add entry
		//
		m_ListMap.add(info_map_entry);
	}

	/**
	 * adds database configuration item
	 */
	private void addNotificationConfigurationItem() {

		//
		// construct synchronize setting
		//
		HashMap<String, Object> info_map_entry = new HashMap<String, Object>();

		//
		// get localized string
		//
		String item_name = super.getActivity().getApplicationContext().getString(R.string.notification_settings);
		
		//
		// set name
		//
		info_map_entry.put(ITEM_NAME, item_name);
		
		//
		// FIXME: hard coded class name
		//
		info_map_entry.put(ITEM_CLASS_NAME,
				"org.me.TagStore.NotificationSettingsActivity");

		//
		// TODO: make image
		//
		info_map_entry.put(ITEM_IMAGE, R.drawable.options_selected);

		//
		// add entry
		//
		m_ListMap.add(info_map_entry);
	}
	
	/**
	 * called to initialize the list view
	 */
	private void initialize() {

		//
		// construct hash map
		//
		m_ListMap = new ArrayList<HashMap<String, Object>>();

		//
		// add directory configuration item
		//
		addDirectoryConfigurationItem();

		//
		// add list view configuration item
		//
		addListViewConfigurationItem();

		//
		// add database configuration item
		//
		addDatabaseConfigurationItem();

		//
		// add notification configuration item
		//
		addNotificationConfigurationItem();
		
		//
		// add synchronization configuration item
		//
		addSynchronizeConfigurationItem();

		//
		// add info configuration item
		//
		addInfoConfigurationItem();

		
		//
		// now create simple adapter to display the entries
		//
		SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), m_ListMap,
				R.layout.configuration_tab_row, new String[] { ITEM_NAME,
						ITEM_IMAGE }, new int[] { R.id.configuration_item,
						R.id.configuration_item_image });
		//
		// set list adapter
		//
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {

		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_ListMap.get(position);

		//
		// get the associated item class name
		//
		String class_name = (String) map_entry.get(ITEM_CLASS_NAME);

		//
		// get associated item name
		//
		String item_name = (String) map_entry.get(ITEM_NAME);
		
		//
		// check if it is supported
		//
		if (class_name == null || class_name.length() == 0) {
			
			//
			// BUG detected:
			//
			Logger.e("Error: no class name for item: " + item_name);
			return;
		}

		//
		// get main page adapter
		//
		MainPageAdapter adapter = MainPageAdapter.getInstance();
		
		//
		// HACK: when the add file fragment is present
		// remove add file fragment
		// change tab to trigger change
		// add configuration item
		// change tab
		// add add file fragment
		//
		
		boolean add_file_present = adapter.isAddFileTagFragmentActive();
		if (add_file_present)
		{
			adapter.removeAddFileTagFragment();
			adapter.setCurrentFragmentByIndex(0);
		}
		
		adapter.addFragmentByIndex(class_name, item_name, 2);
		adapter.setCurrentFragmentByIndex(2);
		
		if (add_file_present)
		{
			//
			// get localized new file
			//
			String new_file = getString(
					R.string.new_file);
			
			//
			// rebuild tabs
			//
			adapter.addFragment(AddFileTagActivity.class.getName(),
					new_file);	
		}
		
	}
}
