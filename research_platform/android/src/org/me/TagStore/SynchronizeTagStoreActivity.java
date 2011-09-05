package org.me.TagStore;

import android.app.Activity;
import android.os.Bundle;

public class SynchronizeTagStoreActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//
		// informal debug message
		//
		Logger.d("SynchronizeTagStoreActivity::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// lets sets our own design
		//
		setContentView(R.layout.synchronize_tag_store);
	}

}
