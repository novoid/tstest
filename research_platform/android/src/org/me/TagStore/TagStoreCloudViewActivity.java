package org.me.TagStore;

import org.me.TagStore.core.Logger;
import org.me.TagStore.core.TagStackManager;
import org.me.TagStore.interfaces.CloudViewClickListener;
import org.me.TagStore.interfaces.GeneralDialogCallback;
import org.me.TagStore.interfaces.BackKeyCallback;
import org.me.TagStore.interfaces.RenameDialogCallback;
import org.me.TagStore.interfaces.RetagDialogCallback;
import org.me.TagStore.ui.CloudViewSurfaceAdapter;
import org.me.TagStore.ui.CloudViewTouchListener;
import org.me.TagStore.ui.CommonDialogFragment;
import org.me.TagStore.ui.DialogIds;
import org.me.TagStore.ui.DialogItemOperations;
import org.me.TagStore.ui.FileDialogBuilder.MENU_ITEM_ENUM;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TagStoreCloudViewActivity extends Fragment implements GeneralDialogCallback, 
																   		 RenameDialogCallback, 
																   		 BackKeyCallback,																   
																   		 CloudViewClickListener,
																   		 RetagDialogCallback																   		 
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
	private TagStackManager m_tag_stack;

	/**
	 * stores the common dialog operations object
	 */
	DialogItemOperations m_dialog_operations;
	
	private String m_current_tag;
	private boolean m_is_tag;
	
	UIDialogFragmentActivator m_activator;
	
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
			refreshView();
		}
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
		m_dialog_operations = new DialogItemOperations(this, getActivity(), getFragmentManager());
		
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
		
		Logger.i("CloudView::refreshView()");
		
		if (m_tag_stack != null)
		{
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
		
		
	}
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			
		 //
		 // construct view
		 //
		 initView();

		 //
		 // done
		 //
		 return m_view;
	 }
	
	/**
	 * initializes the view
	 */
	private void initView() {
		
		Logger.i("CloudView::initView()");
				
		
		//
		// construct cloud view
		//
		m_view = new CloudViewSurfaceAdapter(this, this, getActivity(), m_tag_stack);
		
		//
		// initialize view
		//		
		//m_view.initializeView();

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
		
	}

	public void onBackPressed() {
		Logger.i("TagStoreCloudViewActivity::onBackPressed tag stack size: "
				+ m_tag_stack.getSize());
	}

	@Override
	public void processMenuFileSelection(MENU_ITEM_ENUM action_id) {

		//
		// let common operations object handle it
		//
		m_dialog_operations.performDialogItemOperation(m_current_tag, m_is_tag, action_id);

		//
		// dismiss fragment
		//
		m_activator.m_fragment.dismiss();

		
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

	private class UIDialogFragmentActivator implements Runnable {

		private final String m_item_name;
		private final boolean m_is_tag;
		private final int m_dialog_id;
		private final DialogItemOperations m_dialog_op;
		
		public CommonDialogFragment m_fragment;
		
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
			m_fragment = CommonDialogFragment.newInstance(m_item_name, m_is_tag, m_dialog_id);
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
}
