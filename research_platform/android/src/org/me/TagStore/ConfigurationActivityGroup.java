package org.me.TagStore;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;

public class ConfigurationActivityGroup extends ActivityGroup {

	/**
	 * instance of configuration tab
	 */
	public static ConfigurationActivityGroup s_Instance = null;

	/**
	 * stores the activity stack
	 */
	protected ArrayList<View> m_ActivityStack;

	/**
	 * name of class to be launched
	 */
	public static String CLASS_NAME = "CLASS_NAME";

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass call onto sub class
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.d("ConfigurationActivityGroup::onCreate");

		//
		// store instance member
		//
		s_Instance = this;

		//
		// allocate activity stack
		//
		m_ActivityStack = new ArrayList<View>();

		//
		// get intent
		//
		Intent intent = getIntent();

		//
		// get parameters
		//
		Bundle data = intent.getExtras();

		//
		// get class name
		//
		String class_name = data.getString(CLASS_NAME);

		//
		// now get class object
		//
		Class<Activity> class_object = null;

		try {
			//
			// get class name
			//
			class_object = (Class<Activity>) Class.forName(class_name);
		} catch (ClassNotFoundException e) {
			Logger.d("Failed to construct Class " + class_name);
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
		// add view to activity stack
		//
		m_ActivityStack.add(view);

		//
		// set active view
		//
		setContentView(view);
		Logger.d("ConfigurationActivityGroup::onCreate() StackSize "
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
			Logger.d("back()> New StackSize " + m_ActivityStack.size());
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
