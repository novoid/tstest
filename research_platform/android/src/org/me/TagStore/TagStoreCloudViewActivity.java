package org.me.TagStore;

import java.util.ArrayList;

import org.me.TagStore.core.EventDispatcher;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.interfaces.ItemViewClickListener;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.BackKeyCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.interfaces.RetagDialogCallback;
import org.me.TagStore.interfaces.TagEventNotification;
import org.me.TagStore.interfaces.TagStackUIButtonCallback;
import org.me.TagStore.ui.CloudViewSurfaceAdapter;
import org.me.TagStore.ui.CloudViewTouchListener;
import org.me.TagStore.ui.CommonDialogFragment;
import org.me.TagStore.ui.DialogIds;
import org.me.TagStore.ui.DialogItemOperations;
import org.me.TagStore.ui.TagStackUIButtonAdapter;
import org.me.TagStore.ui.FileDialogBuilder.MENU_ITEM_ENUM;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TagStoreCloudViewActivity extends Fragment implements GeneralDialogCallback, 
																   		 RenameDialogCallback, 
																   		 BackKeyCallback,																   
																   		 ItemViewClickListener,
																   		 RetagDialogCallback, 
																   		 TagStackUIButtonCallback,
                                                                         TagEventNotification																   		 
{
	/**
	 * stores the cloud view
	 */
	private CloudViewSurfaceAdapter m_view;

	/**
	 * stores the cloud touch listener
	 */
	private CloudViewTouchListener m_listener;

	/**
	 * stores the common dialog operations object
	 */
	private DialogItemOperations m_dialog_operations;
	
	/**
	 * stores the current item
	 */
	private String m_current_tag;
	
	/**
	 * is the current selected item a tag
	 */
	private boolean m_is_tag;
	
	/**
	 * stores the tag navigation button adapter
	 */
	private TagStackUIButtonAdapter m_tag_stack_adapter;

	/**
	 * common dialog fragment
	 */
	public CommonDialogFragment m_fragment;

	
	/**
	 * activator for the ui dialogs
	 */
	private UIDialogFragmentActivator m_activator;
	
	@Override
	public void onResume() {

		//
		// call base class
		//
		super.onResume();

		//
		// get tag stack
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
		if (tag_stack != null)
		{
			//
			// verify the tag stack
			//
			tag_stack.verifyTags();
			
			//
			// init view
			//
			refreshView();
		}
	}

	public void onStop() {
		
		//
		// call base method
		//
		super.onStop();
		
		//
		// unregister us with event dispatcher
		//
		unregisterEvents(getEvents());
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
	
	
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);		
		
		//
		// informal debug message
		//
		Logger.i("CloudView::onCreate");

		//
		// create common dialog operations object 
		//
		m_dialog_operations = new DialogItemOperations(getActivity(), getFragmentManager());
		
		//
		// register us with event dispatcher
		//
		registerEvents(getEvents());
	}

	
	
	public void renamedFile(String old_file_name, String new_file)
	{
		Logger.e("old:" + old_file_name + " new_file:" + new_file);
		//
		// refresh view
		//
		refreshView();
	}
	
	@Override
	public void renamedTag(String old_tag_name, String new_tag_name) 
	{
		//
		// refresh view
		//
		refreshView();
	}
	
	
	private void refreshView() {
		
		Logger.i("CloudView::refreshView()");
		
		//
		// get tag stack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();
		if (tag_stack != null)
		{
			//
			// verify the tag stack
			//
			tag_stack.verifyTags();

			//
			// refresh the view
			//
			m_view.initializeView();			
			m_view.invalidate();
		
			buildTagTreeForUI();
		}
		
		
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
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			
		 //
		 // construct view
		 //
		 return initView(inflater, container);
	 }
	
	/**
	 * initializes the view
	 */
	private View initView(LayoutInflater inflater, ViewGroup container) {
		
		Logger.i("CloudView::initView()");
		
		//
		// construct layout
		//
		View view = (View)inflater.inflate(R.layout.tagstore_cloud, null);
		
		//
		// get cloud view from inflated view
		//
		m_view = (CloudViewSurfaceAdapter)view.findViewById(R.id.tagstore_cloud_view); 
		
		//
		// construct cloud touch listener
		//
		m_listener = new CloudViewTouchListener(m_view);

		//
		// add on touch listener
		//
		m_view.setOnTouchListener(m_listener);

		//
		// http://stackoverflow.com/questions/974680/android-onkeydown-problem
		// we need to be focusable otherwise we don't receive key events
		//
		m_view.requestFocus();
		m_view.setFocusableInTouchMode(true);
		
		//
		// find tag stack button
		//
		m_tag_stack_adapter = (TagStackUIButtonAdapter)view.findViewById(R.id.button_adapter);
		if (m_tag_stack_adapter != null)
		{
			//
			// refresh view
			//
			m_tag_stack_adapter.refresh();
		}
		return view;
	}
	
	public void onBackPressed() {
		
		//
		// get tag stack manager
		//
		TagStackManager tag_stack = TagStackManager.getInstance();

		Logger.i("TagStoreCloudViewActivity::onBackPressed tag stack size: "
				+ tag_stack.getSize());
		
		//
		// refresh view
		//
		refreshView();
	}

	@Override
	public void processMenuFileSelection(MENU_ITEM_ENUM action_id) {

		//
		// let common operations object handle it
		//
		m_dialog_operations.performDialogItemOperation(m_current_tag, m_is_tag, action_id);

		if (m_fragment != null)
		{
			//
			// dismiss fragment
			//
			m_fragment.dismiss();
			m_fragment = null;
		}

		//
		// refresh view
		//
		refreshView();
	}

	/**
	 * launches an item
	 * 
	 * @param position
	 */
	private void launchItem(String item_path) {
		
		//
		// let dialog operations handle it
		//
		m_dialog_operations.performDialogItemOperation(item_path, false, MENU_ITEM_ENUM.MENU_ITEM_OPEN);
	}

	@Override
	public void onListItemClick(final String item_name, final boolean is_tag) {
		
		//
		// refresh tag stack on the ui thread
		//
		getActivity().runOnUiThread( new Runnable() {

			@Override
			public void run() {
				
				if (!is_tag)
				{
					//
					// launch item
					//
					launchItem(item_name);
				}
				
				//
				// rebuild tag stack
				//
				buildTagTreeForUI();
			}
		});
		
	}

	@Override
	public void onLongListItemClick(String item_name, boolean is_tag) {

		if (item_name == null)
			return;
		
		
		m_current_tag = item_name;
		m_is_tag = is_tag;
		
		if (is_tag)
		{
			//
			// display file item menu dialog
			//
			
			m_activator = new UIDialogFragmentActivator(m_current_tag, m_is_tag, DialogIds.DIALOG_GENERAL_TAG_MENU, m_dialog_operations);
			getActivity().runOnUiThread(m_activator);			
		}
		else
		{
			//
			// display tag item menu dialog
			//
			m_activator =  new UIDialogFragmentActivator(m_current_tag, m_is_tag, DialogIds.DIALOG_GENERAL_FILE_MENU, m_dialog_operations);
			getActivity().runOnUiThread(m_activator);					
		}
	}

	@Override
	public void backKeyPressed() {
		
		Logger.i("cloud view backKeyPressed");
		//
		// refresh view
		//
		refreshView();
	}

	/**
	 * Implements a class which launches a new dialog when its run method is invoked
	 * @author Johannes Anderwald
	 *
	 */
	private class UIDialogFragmentActivator implements Runnable {

		private final String m_item_name;
		private final boolean m_is_tag;
		private final int m_dialog_id;
		private final DialogItemOperations m_dialog_op;
		
		
		public UIDialogFragmentActivator(String item_name, boolean is_tag, int dialog_id, DialogItemOperations dialog_op) {
			
			//
			// init members
			//
			m_item_name = item_name;
			m_is_tag = is_tag;
			m_dialog_id = dialog_id;
			m_dialog_op = dialog_op;
		}

		@Override
		public void run() {

			//
			// construct new common dialog fragment which will show the dialog
			//
			m_fragment = CommonDialogFragment.newInstance(getActivity(), m_item_name, m_is_tag, m_dialog_id);
			m_fragment.setDialogItemOperation(m_dialog_op);
			m_fragment.show(getFragmentManager(), "FIXME");
		}
	}

	@Override
	public void retaggedFile(String file_name, String tag_text) {

		//
		// refresh view
		//
		Logger.i("reTaggedFile");
		refreshView();		
		
	}

	@Override
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

	@Override
	public boolean tagButtonLongClicked(String tag) {
		
		
		Logger.i("tagButtonLongClicked: " + tag);
		
		//
		// save current tag
		//
		m_current_tag = tag;
		m_is_tag = true;
		
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

	public void notifyFileTagged(String file_name) {

		Logger.i("notifyFileTagged: " + file_name);
		//
		// refresh view
		//
		refreshView();
		
	}	
	
}
