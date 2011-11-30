package org.me.TagStore;

import org.me.TagStore.R;
import org.me.TagStore.core.ConfigurationSettings;
import org.me.TagStore.core.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ListViewSettingsActivity extends Fragment {

	/**
	 * list view radio button
	 */
	private RadioButton m_list_view_button;

	/**
	 * cloud view radio button
	 */
	private RadioButton m_cloud_view_button;

	/**
	 * list view seek bar
	 */
	private SeekBar m_seek_bar_list_view;

	/**
	 * alphabetic sort mode
	 */
	private RadioButton m_alphabetic_sort_mode;

	/**
	 * popular sort mode
	 */
	private RadioButton m_popular_sort_mode;

	/**
	 * columns list view
	 */
	private TextView m_list_view_column;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//
		// pass onto lower classes
		//
		super.onCreate(savedInstanceState);

		//
		// informal debug message
		//
		Logger.d("ListViewSettingsActivity::onCreate");
	}

	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
			
		 //
		 // construct layout
		 //
		 View view = inflater.inflate(R.layout.list_view_settings, null);


		//
		// acquire shared settings
		//
		SharedPreferences settings = getActivity().getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get current list view
		//
		String current_view_class = settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_CLASS,
				ConfigurationSettings.DEFAULT_LIST_VIEW_CLASS);

		//
		// get number per row
		//
		int num_items_per_row = settings.getInt(
				ConfigurationSettings.NUMBER_OF_ITEMS_PER_ROW,
				ConfigurationSettings.DEFAULT_ITEMS_PER_ROW);

		//
		// get current sort mode
		//
		String sort_mode = settings.getString(
				ConfigurationSettings.CURRENT_LIST_VIEW_SORT_MODE,
				ConfigurationSettings.DEFAULT_LIST_VIEW_SORT_MODE);

		//
		// get ui elements
		//
		m_list_view_button = (RadioButton) view.findViewById(R.id.button_icon_list_view);
		m_cloud_view_button = (RadioButton) view.findViewById(R.id.button_cloud_list_view);
		m_seek_bar_list_view = (SeekBar) view.findViewById(R.id.seekbar_list_view);
		m_alphabetic_sort_mode = (RadioButton) view.findViewById(R.id.button_alphabetic_sort_mode);
		m_popular_sort_mode = (RadioButton) view.findViewById(R.id.button_popular_sort_mode);
		m_list_view_column = (TextView) view.findViewById(R.id.text_num_rows);

		if (m_seek_bar_list_view != null) {

			//
			// progress it between 0 - column items - 1
			//
			m_seek_bar_list_view.setMax(3);

			//
			// one item increment
			//
			m_seek_bar_list_view.setKeyProgressIncrement(1);

			//
			// set current item
			//
			m_seek_bar_list_view.setProgress(num_items_per_row - 1);

			//
			// set interface listener
			//
			m_seek_bar_list_view
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							//
							// check if list view column is available
							//
							if (m_list_view_column != null) {

								//
								// update change
								//
								m_list_view_column.setText(Integer
										.toString(progress + 1));
								
								//
								// save settings
								//
								saveSettings();
								
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}

					});

		}

		//
		// add key listener
		//
		if (m_alphabetic_sort_mode != null) {

			if (sort_mode
					.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_ALPHABETIC) == 0) {
				//
				// current sort mode is alphabetic
				//
				m_alphabetic_sort_mode.setChecked(true);
				
			}

			m_alphabetic_sort_mode.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (m_popular_sort_mode != null) {

						//
						// set popular mode unchecked
						//
						m_popular_sort_mode.setChecked(false);
						
						//
						// save settings
						//
						saveSettings();						
					}

				}

			});
		}

		//
		// add key listener for popular mode
		//
		if (m_popular_sort_mode != null) {

			if (sort_mode
					.compareTo(ConfigurationSettings.LIST_VIEW_SORT_MODE_POPULAR) == 0) {
				//
				// current sort mode is alphabetic
				//
				m_popular_sort_mode.setChecked(true);
				
			}

			m_popular_sort_mode.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (m_alphabetic_sort_mode != null) {

						//
						// set popular mode unchecked
						//
						m_alphabetic_sort_mode.setChecked(false);
						
						//
						// save settings
						//
						saveSettings();
					}

				}

			});
		}

		//
		// add key listener for icon list mode
		//
		if (m_list_view_button != null) {

			m_list_view_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					//
					// enable icon view
					//
					toggleListViewUIElements(true);
					
					//
					// save settings
					//
					saveSettings();
				}

			});
		}

		//
		// add key listener for cloud list mode
		//
		if (m_cloud_view_button != null) {

			m_cloud_view_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					//
					// enable icon view
					//
					toggleListViewUIElements(false);
					
					//
					// save settings
					//
					saveSettings();
				}
			});
		}

		if (m_list_view_column != null) {
			//
			// update column count
			//
			m_list_view_column.setText(Integer.toString(num_items_per_row));
		}

		if (current_view_class
				.compareTo(ConfigurationSettings.ICON_LIST_VIEW_CLASS) == 0) {
			//
			// list view mode
			//
			toggleListViewUIElements(true);
		} else {
			//
			// cloud view mode
			//
			toggleListViewUIElements(false);
		}
		
		//
		// done
		//
		return view;
	}
	 
	protected void saveSettings() {

		//
		// acquire shared settings
		//
		SharedPreferences settings = getActivity().getSharedPreferences(
				ConfigurationSettings.TAGSTORE_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		//
		// get editor
		//
		SharedPreferences.Editor editor = settings.edit();

		if (m_seek_bar_list_view != null) {

			//
			// seek bar has zero based index
			//
			int progress = m_seek_bar_list_view.getProgress() + 1;

			//
			// store number of rows
			//
			editor.putInt(ConfigurationSettings.NUMBER_OF_ITEMS_PER_ROW,
					progress);
		}

		if (m_list_view_button != null) {

			//
			// see if it is checked
			//
			boolean checked = m_list_view_button.isChecked();

			if (checked) {
				//
				// set list view mode
				//
				editor.putString(ConfigurationSettings.CURRENT_LIST_VIEW_CLASS,
						ConfigurationSettings.ICON_LIST_VIEW_CLASS);
			} else {
				//
				// set cloud view mode
				//
				editor.putString(ConfigurationSettings.CURRENT_LIST_VIEW_CLASS,
						ConfigurationSettings.CLOUD_VIEW_CLASS);
			}
		}

		if (m_alphabetic_sort_mode != null) {

			//
			// see if it is checked
			//
			boolean checked = m_alphabetic_sort_mode.isChecked();

			if (checked) {
				//
				// store sort mode
				//
				editor.putString(
						ConfigurationSettings.CURRENT_LIST_VIEW_SORT_MODE,
						ConfigurationSettings.LIST_VIEW_SORT_MODE_ALPHABETIC);
			}
		}

		if (m_popular_sort_mode != null) {

			//
			// see if it is checked
			//
			boolean checked = m_popular_sort_mode.isChecked();

			if (checked) {
				//
				// store sort mode
				//
				editor.putString(
						ConfigurationSettings.CURRENT_LIST_VIEW_SORT_MODE,
						ConfigurationSettings.LIST_VIEW_SORT_MODE_POPULAR);
			}
		}

		//
		// now commit changes
		//
		editor.commit();
	}

	/**
	 * toggles view list mode
	 * 
	 * @param enable
	 *            if true, list view mode is enabled, otherwise cloud view mode
	 *            is enabled
	 */
	private void toggleListViewUIElements(boolean enable) {

		Logger.i("toggleListViewUIElements enable" + enable);

		if (enable) {
			if (m_cloud_view_button != null) {
				//
				// unselect cloud view
				//
				m_cloud_view_button.setChecked(false);
			}

			if (m_list_view_button != null) {
				//
				// select list view
				//
				m_list_view_button.setChecked(true);
				Logger.d("toggleListViewUIElements true");
			}

			if (m_seek_bar_list_view != null) {
				//
				// enable seek bar
				//
				m_seek_bar_list_view.setEnabled(true);
			}

			if (m_alphabetic_sort_mode != null) {
				//
				// enable alphabetic sort mode
				//
				m_alphabetic_sort_mode.setEnabled(true);
			}

			if (m_popular_sort_mode != null) {
				//
				// enable popular sort mode
				//
				m_popular_sort_mode.setEnabled(true);
			}

			if (m_popular_sort_mode != null && m_alphabetic_sort_mode != null) {
				//
				// if both are unchecked enable popular sort mode
				//
				if (m_popular_sort_mode.isChecked() == false
						&& m_alphabetic_sort_mode.isChecked() == false) {
					//
					// enable popular mode
					//
					m_popular_sort_mode.setChecked(true);
				}
			}

		} else {

			if (m_cloud_view_button != null) {
				//
				// enable cloud view
				//
				m_cloud_view_button.setChecked(true);
			}

			if (m_list_view_button != null) {
				//
				// disable list view
				//
				m_list_view_button.setChecked(false);
			}

			if (m_seek_bar_list_view != null) {
				//
				// disable seek bar
				//
				m_seek_bar_list_view.setEnabled(false);
			}

			if (m_alphabetic_sort_mode != null) {
				//
				// enable alphabetic sort mode
				//
				m_alphabetic_sort_mode.setEnabled(false);
			}

			if (m_popular_sort_mode != null) {
				//
				// enable popular sort mode
				//
				m_popular_sort_mode.setEnabled(false);
			}

		}

	}
}
