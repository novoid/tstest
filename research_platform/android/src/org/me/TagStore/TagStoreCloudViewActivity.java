package org.me.TagStore;

import org.me.TagStore.core.DialogIds;
import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.interfaces.CloudViewClickListener;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.BackKeyCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.ui.CloudViewSurfaceAdapter;
import org.me.TagStore.ui.CloudViewTouchListener;
import org.me.TagStore.ui.DialogItemOperations;
import org.me.TagStore.ui.FileDialogBuilder.MENU_ITEM_ENUM;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

public class TagStoreCloudViewActivity extends Activity implements GeneralDialogCallback, 
																   RenameDialogCallback, 
																   BackKeyCallback,																   
																   CloudViewClickListener
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
	 * stores all tags
	 */
	TagStackManager m_tag_stack;

	/**
	 * stores the common dialog operations object
	 */
	DialogItemOperations m_dialog_operations;
	
	@Override
	public void onResume() {

		//
		// call base class
		//
		super.onResume();

		if (m_tag_stack != null)
		{
			//
			// verify the tag stack
			//
			m_tag_stack.verifyTags();
			
			//
			// init view
			//
			initView();
		}
	}

	protected void onPrepareDialog (int id, Dialog dialog) {
		
		//
		// get the current tag
		//
		String file_name = m_view.getCurrentItem();	

		//
		// is it a tag
		//
		boolean is_tag = m_view.isCurrentItemTag();
		
		//
		// let common dialog operations object handle it
		//
		m_dialog_operations.prepareDialog(id, dialog, file_name, is_tag);
	}

	
	/**
	 * called when there is a need to construct a dialog
	 */
	protected Dialog onCreateDialog(int id) {

		Logger.i("TagStoreListViewActivity::onCreateDialog dialog id: " + id);
		
		//
		// get the current tag
		//
		String file_name = m_view.getCurrentItem();
		Logger.e("onCreateDialog: " + file_name);

		//
		// let common dialog operations handle it
		//
		return m_dialog_operations.createDialog(id);
	}
	
	
	protected void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("TagStoreCloudViewActivity::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// create common dialog operations object 
		//
		m_dialog_operations = new DialogItemOperations(this, TagActivityGroup.s_Instance);
		
		//
		// construct new tag stack
		//
		m_tag_stack = TagStackManager.getInstance();
	}

	public void renamedFile(String old_file_name, String new_file)
	{
		
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
		
		//
		// verify the tag stack
		//
		m_tag_stack.verifyTags();
		
		//
		// refresh the view
		//
		m_view.initializeView();			
		m_view.invalidate();
		
	}
	
	/**
	 * initializes the view
	 */
	private void initView() {
		
		//
		// construct cloud view
		//
		m_view = new CloudViewSurfaceAdapter(this, this, getApplicationContext(), m_tag_stack);
		
		//
		// initialize view
		//		
		m_view.initializeView();

		//
		// construct cloud touch listener
		//
		m_listener = new CloudViewTouchListener(m_view);

		//
		// add on touch listener
		//
		m_view.setOnTouchListener(m_listener);

		//
		// set content view of activity
		//
		setContentView(m_view);
		
		//
		// http://stackoverflow.com/questions/974680/android-onkeydown-problem
		// we need to be focusable otherwise we don't receive key events
		//
		m_view.requestFocus();
		m_view.setFocusableInTouchMode(true);
		
	}

	@Override
	public void onBackPressed() {
		Logger.i("TagStoreCloudViewActivity::onBackPressed tag stack size: "
				+ m_tag_stack.getSize());
	}

	@Override
	public void processMenuFileSelection(MENU_ITEM_ENUM action_id) {

		//
		// let common operations object handle it
		//
		boolean result = m_dialog_operations.performDialogItemOperation(m_view.getCurrentItem(), m_view.isCurrentItemTag(), action_id);
		if (result)
		{
			//
			// refresh view
			//
			refreshView();
		}
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
	public void onListItemClick(String item_name, boolean is_tag) {
		
		if (!is_tag)
		{
			//
			// launch item
			//
			launchItem(item_name);
		}
		
	}

	@Override
	public void onLongListItemClick(String item_name, boolean is_tag) {

		if (!is_tag)
		{
			//
			// display file item menu dialog
			//
			runOnUiThread( new Runnable() {

				@Override
				public void run() {
					showDialog(DialogIds.DIALOG_GENERAL_FILE_MENU);					
				}
			});			
		}
		else
		{
			//
			// display tag item menu dialog
			//
			runOnUiThread( new Runnable() {

				@Override
				public void run() {
					showDialog(DialogIds.DIALOG_GENERAL_TAG_MENU);					
				}
			});			
		}
	}

	@Override
	public void backKeyPressed() {
		
		//
		// invoke back key
		//
		TagActivityGroup.s_Instance.back();
	}


	
}
