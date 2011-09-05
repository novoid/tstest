package org.me.TagStore;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DatabaseSettingsActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.i("DatabaseSettingsActivity::onCreate");

		//
		// lets sets our own design
		//
		setContentView(R.layout.database_settings);

		//
		// initialize configuration tab
		//
		initialize();
	}

	/**
	 * initialize user interface
	 */
	private void initialize() {

		//
		// get back button
		//
		Button back_button = (Button) findViewById(R.id.button_back);
		if (back_button != null) {
			//
			// add click listener
			//
			back_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//
					// notify that we are done
					//
					ConfigurationActivityGroup.s_Instance.back();
				}
			});
		}

		//
		// get reset button
		//
		Button reset_button = (Button) findViewById(R.id.button_reset);
		if (reset_button != null) {

			//
			// add click listener
			//
			reset_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//
					// call reset database method
					//
					resetDatabase();
				}
			});
		}

	}

	/**
	 * resets the database
	 */
	protected void resetDatabase() {

		//
		// acquire database manager instance
		//
		DBManager db_man = DBManager.getInstance();

		//
		// reset the database
		//
		boolean reset_database = db_man.resetDatabase();

		Toast toast;

		if (reset_database) {
			//
			// successfully deleted database
			//
			toast = Toast.makeText(getApplicationContext(),
					"Successfully reset database", Toast.LENGTH_SHORT);
		} else {
			//
			// failed to reset database
			//
			toast = Toast.makeText(getApplicationContext(),
					"Failed to reset database", Toast.LENGTH_SHORT);
		}

		//
		// display toast
		//
		toast.show();
	}
}
