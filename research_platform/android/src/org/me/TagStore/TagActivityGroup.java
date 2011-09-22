package org.me.TagStore;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.View;

public class TagActivityGroup extends ActivityGroup {

	/**
	 * instance of tag tab group
	 */
	public static TagActivityGroup s_Instance = null;

	/**
	 * stores the activity stack
	 */
	protected ArrayList<View> m_ActivityStack;

	/**
	 * stores the current visible view
	 */
	protected String m_current_view_class;

	protected void onResume() {

		super.onResume();
		Logger.i("TagActivityGroup::onResume");

		//
		// get current view class
		//
		String current_view_class = getCurrentViewClass();

		if (current_view_class.compareTo(m_current_view_class) != 0) {
			//
			// replace view
			//
			m_current_view_class = current_view_class;

			//
			// initialize view
			//
			initializeView(current_view_class);

			//
			// done
			//
			return;
		}

		//
		// initialize view
		//

		//
		// get view on top
		//
		View view = m_ActivityStack.get(m_ActivityStack.size() - 1);

		//
		// get context of view
		//
		Context ctx = view.getContext();

		if (ctx instanceof TagStoreListViewActivity) {
			//
			// instance of TagStoreListViewActivity
			//
			TagStoreListViewActivity activity = (TagStoreListViewActivity) ctx;

			//
			// notify it has resumed
			//
			activity.onResume();
		} else if (ctx instanceof TagStoreCloudViewActivity) {
			//
			// instance of TagStoreListViewActivity
			//
			TagStoreCloudViewActivity activity = (TagStoreCloudViewActivity) ctx;

			//
			// notify it has resumed
			//
			activity.onResume();
		}
	}

	protected String getCurrentViewClass() {

		//
		// acquire shared preference settings instance
		//
		SharedPreferences settings = getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get current list view class
		//
		return settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_CLASS,
				ConfigurationSettings.DEFAULT_LIST_VIEW_CLASS);
	}

	@SuppressWarnings("unchecked")
	protected void initializeView(String class_name) {

		//
		// create class name
		//
		Class<Activity> class_object = null;
		try {
			//
			// get class object
			//
			class_object = (Class<Activity>) Class.forName(class_name);
		} catch (ClassNotFoundException exc) {
			//
			// log error
			//
			Logger.e("Error TagActivityGroup::onCreate class " + class_name
					+ " not found");
			return;
		}

		//
		// start the activity
		//
		View view = getLocalActivityManager().startActivity(
				class_name,
				new Intent(this, class_object)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
				.getDecorView();

		//
		// create activity stack
		//
		m_ActivityStack = new ArrayList<View>();

		//
		// add view to activity stack
		//
		m_ActivityStack.add(view);

		//
		// set active view
		//
		setContentView(view);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// initialize base class
		//
		super.onCreate(savedInstanceState);

		//
		// store instance member
		//
		s_Instance = this;

		//
		// get current view class
		//
		m_current_view_class = getCurrentViewClass();

		//
		// initialize view
		//
		initializeView(m_current_view_class);
	}

	/**
	 * this function gets called when another sub activity wants to become
	 * visible
	 * 
	 * @param view
	 *            which should replace the old view
	 */
	public void replaceView(View view) {

		//
		// add view to activity stack
		//
		m_ActivityStack.add(view);

		Logger.d("StackSize " + m_ActivityStack.size());

		//
		// set active view
		//
		setContentView(view);
	}

	/**
	 * this function is called when the sub activity exists
	 */
	public void back() {

		//
		// check if there are activities on the stack
		//
		if (m_ActivityStack.size() > 1) {
			//
			// remove last activity (current)
			//
			m_ActivityStack.remove(m_ActivityStack.size() - 1);

			//
			// let the parent view come visible again
			//
			setContentView(m_ActivityStack.get(m_ActivityStack.size() - 1));
			Logger.d("TagActivityGroup::back()> New StackSize "
					+ m_ActivityStack.size());
		} else {

			//
			// the last view has been removed, time to finish
			//
			Logger.d("TagActivityGroup::back()> New StackSize "
					+ m_ActivityStack.size());
			finish();
		}
	}

	@Override
	public void onBackPressed() {

		//
		// call object method to do processing
		//
		s_Instance.back();
		return;
	}
}