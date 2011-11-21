package org.me.TagStore.ui;

import org.me.TagStore.core.Logger;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CommonDialogFragment extends DialogFragment {

	/**
	 * key for storing item name
	 */
	public final static String PARAM_ITEM_NAME = "item_name";

	/**
	 * key for storing if it is a tag
	 */
	public final static String PARAM_IS_TAG = "item_tag";

	/**
	 * key for storing dialog id
	 */
	public final static String PARAM_DIALOG_ID = "dialog_item_id";

	/**
	 * stores the item name
	 */
	private String m_item_name;
	
	/**
	 * stores if it is a tag or an file
	 */
	private boolean m_is_tag;
	
	/**
	 * requested dialog id
	 */
	private int m_dialog_id;

	/**
	 * helper object for creating the dialogs
	 */
	private DialogItemOperations m_common_dialog_operations;

	/**
	 * constructs a new common dialog fragment
	 * 
	 * @param item_name
	 *            name of the item
	 * @param is_tag
	 *            if it is a tag
	 * @param dialog_id
	 *            requested dialog id
	 * @return
	 */
	public static CommonDialogFragment newInstance(String item_name,
			boolean is_tag, int dialog_id) {

		//
		// construct fragment
		//
		CommonDialogFragment fragment = new CommonDialogFragment();

		//
		// create bundle for storing parameters
		//
		Bundle args = new Bundle();

		//
		// store parameters
		//
		args.putString(PARAM_ITEM_NAME, item_name);
		args.putBoolean(PARAM_IS_TAG, is_tag);
		args.putInt(PARAM_DIALOG_ID, dialog_id);

		//
		// set args
		//
		fragment.setArguments(args);

		//
		// done
		//
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// call super method
		//
		super.onCreate(savedInstanceState);

		//
		// get parameters
		//
		Bundle args = getArguments();

		//
		// extract parameters
		//
		m_item_name = args.getString(PARAM_ITEM_NAME);
		m_is_tag = args.getBoolean(PARAM_IS_TAG);
		m_dialog_id = args.getInt(PARAM_DIALOG_ID);
	}

	/**
	 * constructs the dialog
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		if (m_common_dialog_operations != null) {
			//
			// construct dialog
			//
			return m_common_dialog_operations.createDialog(m_dialog_id,
					m_item_name, m_is_tag, this);
		}

		Logger.e("BUG: common dialog operations not set !!");
		return null;
	}

	/**
	 * sets the common dialog operations
	 * 
	 * @param dialog_operations
	 */
	public void setDialogItemOperation(DialogItemOperations dialog_operations) {

		m_common_dialog_operations = dialog_operations;

	}

}
