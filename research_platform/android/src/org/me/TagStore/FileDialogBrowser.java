package org.me.TagStore;

import java.io.File;
import java.io.FileFilter;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.Logger;

/**
 * This class is used selecting directories to be monitored
 * 
 * @author Johannes Anderwald
 * 
 */
public class FileDialogBrowser extends ListActivity {

	/**
	 * select button of dialog
	 */
	private Button m_SelectButton = null;

	/**
	 * create button of dialog
	 */
	private Button m_CancelButton = null;

	/**
	 * stores mapping for list view
	 */
	private ArrayList<HashMap<String, Object>> m_ListViewMap;

	/**
	 * stores currently selected path
	 */
	private TextView m_PathTextView = null;

	/**
	 * currently selected path
	 */
	private String m_CurrentPath = null;

	/**
	 * starting path
	 */
	private String m_StartPath = null;

	/**
	 * directory name key
	 */
	private static final String DIRECTORY_NAME = "DIRECTORY_NAME";

	/**
	 * full directory path
	 */
	private static final String DIRECTORY_PATH = "DIRECTORY_PATH";

	/**
	 * stores directory image
	 */
	private static final String DIRECTORY_IMAGE = "DIRECTORY_IMAGE";

	/**
	 * parameter name to pass starting path
	 */
	public static final String START_PATH_PARAMETER = "BROWSE_PATH";

	/**
	 * parameter name which stores the selected path (result of the dialog)
	 */
	public static final String SELECTED_PATH_PARAMETER = "SELECTED_PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//
		//
		//
		Logger.d("FileDialogBrowser::onCreate");

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// lets sets our own design
		//
		setContentView(R.layout.file_dialog_browser);

		//
		// store select button
		//
		m_SelectButton = (Button) findViewById(R.id.button_select);

		//
		// add select button click listener
		//
		m_SelectButton.setOnClickListener(new OnClickListener() {


			public void onClick(View v) {
				if (m_CurrentPath != null) {

					//
					// store result in intent
					//
					getIntent()
							.putExtra(SELECTED_PATH_PARAMETER, m_CurrentPath);

					//
					// set result code
					//
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		//
		// store cancel button
		//
		m_CancelButton = (Button) findViewById(R.id.button_cancel);

		//
		// add cancel button click selector
		//
		m_CancelButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}

		});

		//
		// get text view
		//
		m_PathTextView = (TextView) findViewById(R.id.text_view_path);

		//
		// get starting path
		//
		m_StartPath = getIntent().getStringExtra(START_PATH_PARAMETER);

		//
		// update current path
		//
		m_PathTextView.setText(m_StartPath);

		//
		// disable select button until a path has been selected
		//
		m_SelectButton.setEnabled(false);

		//
		// default state is canceled
		//
		setResult(RESULT_CANCELED, getIntent());

		//
		// fill image view
		//
		onPathChange(m_StartPath);
	}

	protected void onPathChange(String current_path) {

		//
		// construct new file
		//
		File cur_file = new File(current_path);

		//
		// sanity check
		//
		assert (cur_file.isDirectory());

		//
		// construct file map
		//
		m_ListViewMap = new ArrayList<HashMap<String, Object>>();

		if (current_path.compareTo(m_StartPath) != 0) {
			//
			// add backward link "../"
			//
			HashMap<String, Object> map_entry = new HashMap<String, Object>();
			map_entry.put(DIRECTORY_NAME, "..");
			map_entry.put(DIRECTORY_PATH, cur_file.getParent());
			map_entry.put(DIRECTORY_IMAGE, R.drawable.folder);

			//
			// add map entry to list view map
			//
			m_ListViewMap.add(map_entry);
		}

		//
		// now construct adapter for list view
		//
		SimpleAdapter adapter = new SimpleAdapter(this, m_ListViewMap,
				R.layout.file_dialog_browser_row, new String[] {
						DIRECTORY_NAME, DIRECTORY_IMAGE }, new int[] {
						R.id.directory_name_text, R.id.directory_image });		
		
		//
		// construct file filter
		//
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		    	//
		    	// only accept directories but don't include the tagstore directory
		    	//
		        return file.isDirectory() && (!file.getName().equals(ConfigurationSettings.TAGSTORE_DIRECTORY));
		    }
		};
		
		//
		// collect list of directories
		//
		File[] cur_files = cur_file.listFiles(fileFilter);
		
		if (cur_files != null)
		{
			//
			// now sort the directories
			//
			Arrays.sort(cur_files, new Comparator<File>() {

				public int compare(File arg0, File arg1) {
					return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
				}
			});
		

			//
			// enumerate all directories
			//
			for (File file : cur_files) {
				//
				// get directory name
				//
				String dir_name = file.getName();

				//
				// construct new hash map entry
				//
				HashMap<String, Object> map_entry = new HashMap<String, Object>();

				//
				// store directory name and path in there
				//
				map_entry.put(DIRECTORY_NAME, dir_name);
				map_entry.put(DIRECTORY_PATH, file.getAbsolutePath());
				map_entry.put(DIRECTORY_IMAGE, R.drawable.folder);

				//
				// add map entry to list view map
				//
				m_ListViewMap.add(map_entry);
			}
		}
			
		//
		// notify data changed
		//
		adapter.notifyDataSetChanged();

		//
		// set list adapter to list view
		//
		setListAdapter(adapter);

	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {

		//
		// get map entry
		//
		HashMap<String, Object> map_entry = m_ListViewMap.get(position);

		//
		// get current directory
		//
		String current_directory = (String) map_entry.get(DIRECTORY_PATH);

		//
		// log current directory
		//
		Logger.d("FileDialogBrowser::onListItemClick> new directory "
				+ current_directory);

		//
		// update current directory
		//
		m_CurrentPath = current_directory;

		//
		// update text view
		//
		m_PathTextView.setText(m_CurrentPath);

		//
		// check if this is root directory
		//
		if (m_CurrentPath.compareTo(m_StartPath) == 0) {
			//
			// don't enable selecting from root path
			//
			m_SelectButton.setEnabled(false);
		} else {
			//
			// enable select button
			//
			m_SelectButton.setEnabled(true);
		}

		//
		// refresh list
		//
		onPathChange(current_directory);
	}
}
