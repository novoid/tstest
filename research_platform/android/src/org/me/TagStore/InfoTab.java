package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.Logger;

import android.app.Activity;
import android.os.Bundle;

public class InfoTab extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		
		//
		// construct base class
		//
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.infotab);
		
	}
	
	 public void onPause() {
		 
		 //
		 // call super method
		 //
		 super.onPause();
		 Logger.i("InfoTab::onPause");
	 }
	 
}
