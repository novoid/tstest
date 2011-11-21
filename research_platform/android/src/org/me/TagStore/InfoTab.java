package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.Logger;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InfoTab extends Fragment {

	/**
	 * notification manager
	 */
	NotificationManager m_notification_manager;

	int m_index;
	
	public void onCreate(Bundle savedInstanceState) {
		
		//
		// construct base class
		//
		super.onCreate(savedInstanceState);
		

		
		m_index = 2; //savedInstanceState.getInt(ConfigurationSettings.FRAGMENT_INDEX);
		
	}

	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			
		 //
		 // construct layout
		 //
		 View view = inflater.inflate(R.layout.infotab, null);

		 //
		 // done
		 //
		 return view;
	 }
	 
	 public void onPause() {
		 
		 //
		 // call super method
		 //
		 super.onPause();
		 
		 Logger.e("InfoTab::onPause");
	 }
	 
}
