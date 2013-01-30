package org.me.tagstore.ui;

import org.me.tagstore.core.Logger;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

public class CommonDialogFragment extends DialogFragment {

	/**
	 * key for storing item name
	 */
	public final String PARAM_ITEM_NAME = "item_name";

	/**
	 * key for storing if it is a tag
	 */
	public final String PARAM_IS_TAG = "item_tag";

	/**
	 * key for storing dialog id
	 */
	public final String PARAM_DIALOG_ID = "dialog_item_id";

	/**
	 * stores the item name
	 */
	public String m_item_name;

	/**
	 * stores if it is a tag or an file
	 */
	public boolean m_is_tag;

	/**
	 * requested dialog id
	 */
	public int m_dialog_id;

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
	public static CommonDialogFragment newInstance(Context context,
			String item_name, boolean is_tag, int dialog_id) {

		if (context == null || item_name == null)
			return null;

		//
		// create bundle for storing parameters
		//
		Bundle args = new Bundle();
		if (args == null)
			return null;

		//
		// store parameters
		//
		args.putString("item_name", item_name);
		args.putBoolean("item_tag", is_tag);
		args.putInt("dialog_item_id", dialog_id);

		//
		// construct fragment
		//
		CommonDialogFragment fragment = (CommonDialogFragment) Fragment
				.instantiate(context, CommonDialogFragment.class.getName(),
						args);

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
		if (args == null)
			Logger.e("args null!!");

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
