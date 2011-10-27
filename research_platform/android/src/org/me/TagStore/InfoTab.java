package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.Logger;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

public class InfoTab extends Activity {

	/**
	 * notification manager
	 */
	NotificationManager m_notification_manager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//
		// set view
		//
		setContentView(R.layout.infotab);

		//
		// get notification service name
		//
		String ns_name = Context.NOTIFICATION_SERVICE;

		//
		// get notification service manager instance
		//
		m_notification_manager = (NotificationManager) getSystemService(ns_name);

		Logger.d("InfoTab::onCreate");

	}

}
