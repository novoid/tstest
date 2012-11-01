package org.me.TagStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.DBManager;
import org.me.TagStore.core.EventDispatcher;
import org.me.TagStore.core.FileTagUtility;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.interfaces.BackKeyCallback;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.ItemViewClickListener;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.interfaces.RetagDialogCallback;
import org.me.TagStore.interfaces.TagEventNotification;
import org.me.TagStore.interfaces.TagStackUIButtonCallback;
import org.me.TagStore.ui.CommonDialogFragment;
import org.me.TagStore.ui.DialogIds;
import org.me.TagStore.ui.DialogItemOperations;
import org.me.TagStore.ui.FileDialogBuilder;
import org.me.TagStore.ui.IconViewItemBuilder;
import org.me.TagStore.ui.IconViewListAdapter;
import org.me.TagStore.ui.IconViewScrollListener;
import org.me.TagStore.ui.MainPageAdapter;
import org.me.TagStore.ui.TagStackUIButtonAdapter;
import org.me.TagStore.ui.ToastManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;
import android.widget.GridView;



public class TagStoreGridViewActivity extends DialogFragment implements GeneralDialogCallback, 
																  RenameDialogCallback, 
																  TagStackUIButtonCallback, 
																  ItemViewClickListener, 
																  RetagDialogCallback,
																  BackKeyCallback,
																  TagEventNotification
{
	/**
	 * list view contents
	 */
	private ArrayList<HashMap<String, Object>> m_list_map;

	/**
	 * stores the tag navigation button adapter
	 */
	private TagStackUIButtonAdapter m_tag_stack_adapter;
	
	/**
	 * stores the common dialog operations object
	 */
	private DialogItemOperations m_dialog_operations;
	
	/**
	 * scroll listener of view
	 */
	private IconViewScrollListener m_scroll_listener;

	/**
	 * common dialog fragment
	 */
	private CommonDialogFragment m_fragment;
	
	/**
	 * current selected item
	 */
	private String m_current_tag;
	
	/**
	 * is current item a tag
	 */
	private boolean m_is_tag;

	@Override
	public void onPause() {

		//
		// call super implementation
		//
		super.onPause();

		Logger.i("TagStoreListViewActivity::onPause");

		//
		// cancel any active toast
		//
		ToastManager toast = ToastManager.getInstance();
		if (toast != null)
		{
			//
			// cancel toast
			//
			toast.cancelToast();
		}
		
		//
		// unregister us with event dispatcher
		//
		unregisterEvents(getEvents());	
		
	}

	
	public void onStart() { 
	    super.onStart(); 
	    
	    //
	    // find grid view
	    //
	    GridView grid_view = (GridView)getView().findViewById(R.id.grid_list_view);
	    if (grid_view != null)
	    {
	    	//
	    	// get layout animation
	    	//
	    	LayoutAnimationController controller = grid_view.getLayoutAnimation();
	    	if (controller != null)
	    	{
	    		//
	    		// start animation
	    		//
	    		controller.start();
	    	}
	    }
	    Logger.i("onStart");
		//
		// register us with event dispatcher
		//
		registerEvents(getEvents());	
	    
	}	
	
	@Override
	public void onResume() {

		//
		// call super implementation
		//
		super.onResume();

		Logger.i("TagStoreGridViewActivity::onResume");

		//
		// register us with event dispatcher
		//
		registerEvents(getEvents());	

		//
		// refresh view
		//
		refreshView();
	}

	/**
	 * unregisters the events from the event dispatcher
	 * @param events
	 */
	private void unregisterEvents(final ArrayList<EventDispatcher.EventId> events) {
		
		for(EventDispatcher.EventId event : events)
		{
			//
			// unregister event
			//
			EventDispatcher.getInstance().unregisterEvent(event, this);
		}
	}

	private void registerEvents(final ArrayList<EventDispatcher.EventId> events) {
		
		for(EventDispatcher.EventId event : events)
		{
			//
			// unregister event
			//
			EventDispatcher.getInstance().registerEvent(event, this);
		}		
	}
	
	private ArrayList<EventDispatcher.EventId> getEvents() {
		
		ArrayList<EventDispatcher.EventId> events = new ArrayList<EventDispatcher.EventId>();
		
		//
		// add interested events
		//
		events.add(EventDispatcher.EventId.TAG_STACK_BUTTON_EVENT);
		events.add(EventDispatcher.EventId.TAG_STACK_LONG_BUTTON_EVENT);
		events.add(EventDispatcher.EventId.BACK_KEY_EVENT);
		events.add(EventDispatcher.EventId.ITEM_CLICK_EVENT);
		events.add(EventDispatcher.EventId.ITEM_LONG_CLICK_EVENT);
		events.add(EventDispatcher.EventId.FILE_TAGGED_EVENT);		
		events.add(EventDispatcher.EventId.FILE_RENAMED_EVENT);
		events.add(EventDispatcher.EventId.TAG_RENAMED_EVENT);
		events.add(EventDispatcher.EventId.FILE_RETAG_EVENT);
		events.add(EventDispatcher.EventId.ITEM_MENU_EVENT);
		events.add(EventDispatcher.EventId.ITEM_CONFLICT_EVENT);
		return events;
	}

	public void onStop() {
		
		//
		// call base method
		//
		super.onStop();
		
		Logger.i("onStop");
		
		//
		// unregister us with event dispatcher
		//
		unregisterEvents(getEvents());
	}
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);
		
		
		//
		// informal debug message
		//
		Logger.d("TagStoreGridViewActivity::onCreate");

		//
		// construct list map
		//
		m_list_map = new ArrayList<HashMap<String, Object>>();

		//
		// create dialog operations object
		//
		m_dialog_operations = new DialogItemOperations(getActivity(), getFragmentManager());
		
		//
		// register us with event dispatcher
		//
		registerEvents(getEvents());	
	}

	/**
	 * refresh current view
	 */
	private void refreshView() {

		if (m_list_map != null)
		{
			//
			// clear the map
			//
			m_list_map.clear();
		}
		
		//
		// acquire tag stack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();

		//
		// verify the tag stack
		//
		tag_stack.verifyTags();
		
		if (tag_stack.isEmpty())
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
			buildListView(getView());
			
			//
			// done
			//
			return;
		
		}
			
		//
		// fill list map with this tag
		//
		fillListMapWithTagStack();

		//
		// now rebuild list
		//
		buildListView(getView());
	}

	/**
	 * refreshes view and displays all associated tags and files in respect to the given tag
	 * @param tag 
	 */
	public void tagButtonClicked(String tag) {
		
		Logger.i("tagButtonClicked: " + tag);
		
		//
		// get tag stack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
		
		//
		// clear tag stack
		//
		tag_stack.clearTags();
		
		//
		// add the tag
		//
		tag_stack.addTag(tag);
		
		//
		// refresh view
		//
		refreshView();
	}
	
	
	public boolean tagButtonLongClicked(String tag) {
		
		Logger.i("tagButtonLongClicked: " + tag);
		
		//
		// save current tag
		//
		m_current_tag = tag;
		m_is_tag = true;
		
		
		FragmentManager fm = getFragmentManager();
		if (fm == null)
			Logger.e("fm is null");
		
		if (getActivity() == null)
			Logger.e("activity is null");
		
		
		//
		// construct new common dialog fragment which will show the dialog
		//
		m_fragment = CommonDialogFragment.newInstance(getActivity(), tag, true, DialogIds.DIALOG_GENERAL_TAG_MENU);
		m_fragment.setDialogItemOperation(m_dialog_operations);
		m_fragment.show(getFragmentManager(), "FIXME");
		
		//
		// long click was handled
		//
		return true;
	}

	
	
	public void renamedFile(String old_file_name, String new_file) {
		
		//
		// refresh view
		//
		refreshView();
	}
	
	
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		//
		// get tag stack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
	
		//
		// check if not back button was pressed
		//
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			//
			// pass on to default handler
			//
			return false;
		}

		Logger.i("TagStoreListViewActivity::onKeyDown empty: " + tag_stack.isEmpty());

		//
		// check if object stack is empty
		//
		if (tag_stack.isEmpty()) {
			//
			// close application
			//
			return true;
		}

		//
		// pop last tag from stack if available
		//
		tag_stack.removeLastTag();

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
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// tag list
		//
		ArrayList<String> tag_list = null;
		
			
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

		if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_ALPHABETIC) == 0) {
			//
			// get tags sorted by alphabet
			//
			tag_list = db_man.getAlphabeticTags();
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_POPULAR) == 0) {
			//
			// get tags sorted by popularity
			//
			tag_list = db_man.getPopularTags();
		} else if (sort_mode
				.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_RECENT) == 0) {
			//
			// TODO: get tags sorted by recently used
			//
			Logger.e("Error: TagListViewActivity::fillListMap get recent tags is not implemented yet");
		}

		if (tag_list == null) {
			//
			// no tag list
			//
			Logger.e("sort_mode:" + sort_mode);
			return false;
		}

		//
		// now add the tags to the list
		//
		for (String current_tag : tag_list) {
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

	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			
			//
			// construct layout
			//
			View view = inflater.inflate(R.layout.tagstore_grid_view, null);

			//
			// find tag stack button
			//
			m_tag_stack_adapter = (TagStackUIButtonAdapter)view.findViewById(R.id.button_adapter);
			
			return view;
	}

	/**
	 * builds the tag tree for the user interface element and updates it
	 */
	protected void buildTagTreeForUI() {

		//
		// set empty tag list
		//
		if (m_tag_stack_adapter != null) {
			m_tag_stack_adapter.refresh();
		}
	}

	/**
	 * build the list view
	 */
	protected void buildListView(View view) {

		//
		// acquire shared settings
		//
		SharedPreferences settings = getActivity().getSharedPreferences(
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
		// lets get the grid view
		//
		GridView grid_view = (GridView)view.findViewById(R.id.grid_list_view);
		if (grid_view == null)
		{
			Logger.e("Error: no grid view found");
			return;
		}
		
		
		//
		// construct list adapter
		//
		IconViewListAdapter list_adapter = new IconViewListAdapter(m_list_map, getActivity().getApplicationContext());

		//
		// load animation
		//
		Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
		
		//
		// now construct the grid layout animation controller
		//
		GridLayoutAnimationController animation_controller = new GridLayoutAnimationController(animation, 0.2f, 0.2f);
		
		//
		// set the animation controller
		//
		grid_view.setLayoutAnimation(animation_controller);
		
		//
		// set icon adapter to grid view
		//
		grid_view.setAdapter(list_adapter);
		
		//
		// set number of items per row
		//
		grid_view.setNumColumns(num_items_per_row);
		
		//
		// construct new list view scroll listener
		//
		m_scroll_listener = new IconViewScrollListener(list_adapter, getActivity().getApplicationContext());

		//
		// set new scroll listener
		//
		grid_view.setOnScrollListener(m_scroll_listener);
	}

	/**
	 * launches an item
	 * 
	 * @param item_path position of item
	 */
	private void launchItem(String item_path) {

		if (m_dialog_operations != null)
		{
			//
			// let dialog operations handle it
			//
			m_dialog_operations.performDialogItemOperation(item_path, false, FileDialogBuilder.MENU_ITEM_ENUM.MENU_ITEM_OPEN);
		}
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
		// get instance of database manager
		//
		DBManager db_man = DBManager.getInstance();		
		
		//
		// get tag reference counts
		//
		final HashMap<String, Integer> ref_count = db_man.getTagReferenceCount();
		
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

				
				public int compare(String tag1, String tag2) {

					//
					// get the tag reference counts
					//
					Integer tag1_ref_count = ref_count.get(tag1);
					Integer tag2_ref_count = ref_count.get(tag2);

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
		HashMap<String, Object> map_entry = IconViewItemBuilder.buildIconViewItem(getActivity().getApplicationContext(), item_name, is_tag);
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
	 */
	protected void fillListMapWithTagStack() {

		//
		// get instance of tagstack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
		
		//
		// get associated tags
		//
		ArrayList<String> linked_tags = FileTagUtility.getLinkedTags();

		//
		// apply sorting
		//
		linked_tags = applySortMethodToTagList(linked_tags);

		//
		// add them to list
		//
		for (String linked_tag : linked_tags) {
			
			//
			// check if the tag is already in visted link
			//
			if (tag_stack.containsTag(linked_tag))
			{
				//
				// this should never happen
				//
				Logger.e("Error: DB query bug detected in TagStoreGridViewActivity::fillListMapWithTag duplicate tag:" + linked_tag);
				continue;
			}

			//
			// add item
			//
			addItem(linked_tag, true);
		}

		//
		// get linked files
		//
		ArrayList<String> linked_files = FileTagUtility.getLinkedFiles();
		
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
		// check if the view is current visible
		//
		if (MainPageAdapter.getInstance().getCurrentItem() != 0)
		{
			//
			// HACK: ViewPageAdapter does not call onPause when switching to next fragment
			//
			return;
		}

		//
		// defer control to common dialog operations object
		//
		boolean result = m_dialog_operations.performDialogItemOperation(m_current_tag, m_is_tag, action_id);
		
		if (result)
		{
			//
			// refresh the view
			//
			refreshView();
		}
	}

	public void onLongListItemClick(String item_name, boolean is_tag) {

		Logger.d("onLongListItemClick: " + item_name + " is_tag: " + is_tag);
		Logger.d("FragmentManager:" + getFragmentManager() + " Activity: " + getActivity());
		Logger.d("ThreadID: " + Thread.currentThread().getId());
		
		
		
		//
		// store event details
		//
		m_current_tag = item_name;
		m_is_tag = is_tag;
		
		//
		// check if this is an tag item
		//
		if (m_is_tag) {
			
			//
			// launch general tag menu
			//
			m_fragment = CommonDialogFragment.newInstance(getActivity(), m_current_tag, true, DialogIds.DIALOG_GENERAL_TAG_MENU);
			m_fragment.setDialogItemOperation(m_dialog_operations);
			m_fragment.show(getFragmentManager(), "FIXME");			
		}
		else
		{
			//
			// request new dialog to be shown
			//
			m_fragment = CommonDialogFragment.newInstance(getActivity(), m_current_tag, false, DialogIds.DIALOG_GENERAL_FILE_MENU);
			m_fragment.setDialogItemOperation(m_dialog_operations);			
			m_fragment.show(getFragmentManager(), "FIXME");				
		}
	}

	public void onListItemClick(String item_name, boolean is_tag) {

		Logger.d("onListItemClick: item" + item_name + " tag: " + is_tag + " was clicked");

		if (getActivity() == null)
		{
			Logger.e("Error: no activity attached");
			return;
		}
		
		
		if (!is_tag)
		{
			//
			// launch item
			//
			launchItem(item_name);

			//
			// done
			//
			return;
		}

		//
		// get tag stack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
		
		//
		// add item to visited tag stack
		//
		tag_stack.addTag(item_name);

		//
		// now clear the list map
		//
		m_list_map.clear();

		//
		// fill list map with this tag
		//
		fillListMapWithTagStack();
	
		//
		// now rebuild list
		//
		buildListView(getView());
	}
	
	
	public void backKeyPressed() {
		
		Logger.i("backKeyPressed");
		
		Activity activity = getActivity();
		if (activity == null)
		{
			Logger.e("Error: Fragment has no activity!");
			return;
		}
		
		refreshView();
	}


	
	public void notifyFileTagged(String file_name) {

		Logger.i("notifyFileTagged: " + file_name);
		//
		// refresh view
		//
		refreshView();
		
	}
}
