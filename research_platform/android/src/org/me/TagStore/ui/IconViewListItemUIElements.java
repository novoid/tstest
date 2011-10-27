package org.me.TagStore.ui;


import org.me.TagStore.R;
import org.me.TagStore.interfaces.IconViewClickListenerCallback;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * stores the user interface elements
 * 
 * @author Johannes Anderwald
 * 
 */
public class IconViewListItemUIElements {

	/**
	 * stores user interface elements
	 */
	public TextView m_ItemName1;
	public TextView m_ItemName2;
	public TextView m_ItemName3;
	public TextView m_ItemName4;

	public ImageButton m_ItemButton1;
	public ImageButton m_ItemButton2;
	public ImageButton m_ItemButton3;
	public ImageButton m_ItemButton4;

	public int m_num_elements;

	IconViewListItemUIElements(int num_elements) {

		//
		// store number of elements available
		//
		m_num_elements = num_elements;
	}

	/**
	 * sets the item text and image
	 * 
	 * @param index
	 *            of item to set
	 * @param item_name
	 *            text of the item
	 * @param item_image
	 *            image of the item
	 */
	public void setItemNameAndImage(int index, String item_name,
			Integer item_image) {

		if (index == 0) {
			if (m_ItemName1 != null) {
				m_ItemName1.setText(item_name);
			}

			if (m_ItemButton1 != null) {
				m_ItemButton1.setImageResource(item_image.intValue());
			}
		} else if (index == 1) {
			if (m_ItemName2 != null) {
				m_ItemName2.setText(item_name);
			}

			if (m_ItemButton2 != null) {
				m_ItemButton2.setImageResource(item_image.intValue());
			}
		} else if (index == 2) {
			if (m_ItemName3 != null) {
				m_ItemName3.setText(item_name);
			}

			if (m_ItemButton3 != null) {
				m_ItemButton3.setImageResource(item_image.intValue());
			}
		} else if (index == 3) {
			if (m_ItemName4 != null) {
				m_ItemName4.setText(item_name);
			}

			if (m_ItemButton4 != null) {
				m_ItemButton4.setImageResource(item_image.intValue());
			}
		}
	}

	/**
	 * initializes all view elements
	 * 
	 * @param convertView
	 *            elements which are retrieved from this view
	 * @param num_elements_per_row
	 *            num elements per row
	 */
	public void initializeWithView(IconViewClickListenerCallback callback,
			int item_offset, View convertView, int num_elements_per_row) {

		//
		// initialize first element
		//
		m_ItemName1 = (TextView) convertView
				.findViewById(R.id.tag_name_one);
		m_ItemButton1 = (ImageButton) convertView
				.findViewById(R.id.tag_image_one);

		if (m_ItemButton1 != null) {
			m_ItemButton1.setOnClickListener(new IconViewListItemClickListener(
					callback, item_offset));
			m_ItemButton1
					.setOnLongClickListener(new IconViewListItemLongClickListener(
							callback, item_offset));
		}

		if (m_ItemName1 != null) {
			m_ItemName1.setOnClickListener(new IconViewListItemClickListener(
					callback, item_offset));
			m_ItemName1
					.setOnLongClickListener(new IconViewListItemLongClickListener(
							callback, item_offset));
		}

		if (m_num_elements >= 2) {
			m_ItemName2 = (TextView) convertView
					.findViewById(R.id.tag_name_two);
			m_ItemButton2 = (ImageButton) convertView
					.findViewById(R.id.tag_image_two);
			if (m_ItemButton2 != null) {
				m_ItemButton2
						.setOnClickListener(new IconViewListItemClickListener(
								callback, item_offset + 1));
				m_ItemButton2
						.setOnLongClickListener(new IconViewListItemLongClickListener(
								callback, item_offset + 1));
			}

			if (m_ItemName2 != null) {
				m_ItemName2
						.setOnClickListener(new IconViewListItemClickListener(
								callback, item_offset));
				m_ItemName2
						.setOnLongClickListener(new IconViewListItemLongClickListener(
								callback, item_offset));
			}
		} else if (num_elements_per_row >= 2) {
			//
			// element has at least two element, only one is present
			//
			m_ItemName2 = (TextView) convertView
					.findViewById(R.id.tag_name_two);
			m_ItemButton2 = (ImageButton) convertView
					.findViewById(R.id.tag_image_two);

			//
			// hide elements
			//
			if (m_ItemName2 != null) {
				m_ItemName2.setVisibility(View.INVISIBLE);
			}

			if (m_ItemButton2 != null) {
				m_ItemButton2.setVisibility(View.INVISIBLE);
			}
		}

		if (m_num_elements >= 3) {
			m_ItemName3 = (TextView) convertView
					.findViewById(R.id.tag_name_three);
			m_ItemButton3 = (ImageButton) convertView
					.findViewById(R.id.tag_image_three);

			if (m_ItemButton3 != null) {
				m_ItemButton3
						.setOnClickListener(new IconViewListItemClickListener(
								callback, item_offset + 2));
				m_ItemButton3
						.setOnLongClickListener(new IconViewListItemLongClickListener(
								callback, item_offset + 2));
			}

			if (m_ItemName3 != null) {
				m_ItemName3
						.setOnClickListener(new IconViewListItemClickListener(
								callback, item_offset));
				m_ItemName3
						.setOnLongClickListener(new IconViewListItemLongClickListener(
								callback, item_offset));
			}

		} else if (num_elements_per_row >= 3) {
			//
			// element has at least three element, less are present
			//
			m_ItemName3 = (TextView) convertView
					.findViewById(R.id.tag_name_three);
			m_ItemButton3 = (ImageButton) convertView
					.findViewById(R.id.tag_image_three);

			//
			// hide elements
			//
			if (m_ItemName3 != null) {
				m_ItemName3.setVisibility(View.INVISIBLE);
			}

			if (m_ItemButton3 != null) {
				m_ItemButton3.setVisibility(View.INVISIBLE);
			}
		}

		if (m_num_elements == 4) {
			m_ItemName4 = (TextView) convertView
					.findViewById(R.id.tag_name_four);
			m_ItemButton4 = (ImageButton) convertView
					.findViewById(R.id.tag_image_four);
			if (m_ItemButton4 != null) {
				m_ItemButton4
						.setOnClickListener(new IconViewListItemClickListener(
								callback, item_offset + 3));
				m_ItemButton4
						.setOnLongClickListener(new IconViewListItemLongClickListener(
								callback, item_offset + 3));
			}

			if (m_ItemName4 != null) {
				m_ItemName4
						.setOnClickListener(new IconViewListItemClickListener(
								callback, item_offset));
				m_ItemName4
						.setOnLongClickListener(new IconViewListItemLongClickListener(
								callback, item_offset));
			}
		} else if (num_elements_per_row == 4) {
			//
			// element has at least three element, less are present
			//
			m_ItemName4 = (TextView) convertView
					.findViewById(R.id.tag_name_four);
			m_ItemButton4 = (ImageButton) convertView
					.findViewById(R.id.tag_image_four);

			//
			// hide elements
			//
			if (m_ItemName4 != null) {
				m_ItemName4.setVisibility(View.INVISIBLE);
			}

			if (m_ItemButton4 != null) {
				m_ItemButton4.setVisibility(View.INVISIBLE);
			}
		}
	}
}