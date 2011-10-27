package org.me.TagStore;

import java.util.ArrayList;

import org.me.TagStore.core.Logger;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PendingFileActivityGroup extends ActivityGroup {
	/**
	 * instance of configuration tab
	 */
	public static PendingFileActivityGroup s_Instance = null;

	/**
	 * stores the activity stack
	 */
	protected ArrayList<View> m_ActivityStack;

	/**
	 * name of class to be launched
	 */
	public static String CLASS_NAME = "org.me.TagStore.AddFileTagActivity";

	protected void onResume() {

		super.onResume();

		Logger.i("PendingFileActivityGroup::onResume");

		//
		// get view on top
		//
		View view = m_ActivityStack.get(m_ActivityStack.size() - 1);

		//
		// get context of view
		//
		Context ctx = view.getContext();

		if (ctx instanceof AddFileTagActivity) {
			//
			// instance of TagStoreListViewActivity
			//
			AddFileTagActivity activity = (AddFileTagActivity) ctx;

			//
			// notify it has resumed
			//
			activity.onResume();
		}
	}

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass call onto sub class
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.d("PendingFileActivityGroup::onCreate");

		//
		// store instance member
		//
		s_Instance = this;

		//
		// allocate activity stack
		//
		m_ActivityStack = new ArrayList<View>();

		//
		// now get class object
		//
		Class<Activity> class_object = null;

		try {
			//
			// get class name
			//
			class_object = (Class<Activity>) Class.forName(CLASS_NAME);
		} catch (ClassNotFoundException e) {
			Logger.d("PendingFileActivityGroup::onCreate Failed to construct Class "
					+ CLASS_NAME);
		}

		//
		// start the activity
		//
		View view = getLocalActivityManager().startActivity(
				CLASS_NAME,
				new Intent(this, class_object)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
				.getDecorView();

		//
		// add view to activity stack
		//
		m_ActivityStack.add(view);

		//
		// set active view
		//
		setContentView(view);
		Logger.d("PendingFileActivityGroup::onCreate() StackSize "
				+ m_ActivityStack.size());
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
		} else {

			//
			// the last view has been removed, time to finish
			//
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
