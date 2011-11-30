package org.me.TagStore.core;

import org.me.TagStore.R;

public class ConfigurationSettings {

	/**
	 * name of configuration settings
	 */
	public final static String TAGSTORE_PREFERENCES_NAME = "TagStore";

	/**
	 * num items per row for default list view
	 */
	public final static String NUMBER_OF_ITEMS_PER_ROW = "num_item_row";

	/**
	 * default num item per row
	 */
	public final static int DEFAULT_ITEMS_PER_ROW = 3;

	/**
	 * current list view class
	 */
	public final static String CURRENT_LIST_VIEW_CLASS = "current_view_class";

	/**
	 * default list view class
	 */
	public final static String DEFAULT_LIST_VIEW_CLASS = "org.me.TagStore.TagStoreGridViewActivity";

	/**
	 * icon list view class
	 */
	public final static String ICON_LIST_VIEW_CLASS = "org.me.TagStore.TagStoreGridViewActivity";

	/**
	 * cloud view class
	 */
	public final static String CLOUD_VIEW_CLASS = "org.me.TagStore.TagStoreCloudViewActivity";

	/**
	 * current list view sort mode
	 */
	public final static String CURRENT_LIST_VIEW_SORT_MODE = "current_list_view_sort_mode";

	/**
	 * default sort mode
	 */
	public final static String DEFAULT_LIST_VIEW_SORT_MODE = "alphabetic";

	/**
	 * alphabetic sort mode
	 */
	public final static String LIST_VIEW_SORT_MODE_ALPHABETIC = "alphabetic";

	/**
	 * sort tags by popularity
	 */
	public final static String LIST_VIEW_SORT_MODE_POPULAR = "popular";

	/**
	 * sort tags by recently used
	 */
	public final static String LIST_VIEW_SORT_MODE_RECENT = "recent";

	/**
	 * current list item icon
	 */
	public final static String CURRENT_LIST_ITEM_ICON = "current_list_item_icon";

	/**
	 * default list item icon
	 */
	public final static int DEFAULT_LIST_ITEM_ICON = R.drawable.file;

	/**
	 * current list item icon
	 */
	public final static String CURRENT_LIST_TAG_ICON = "current_list_tag_icon";

	/**
	 * default list item icon
	 */
	public final static int DEFAULT_LIST_TAG_ICON = R.drawable.tag;

	/**
	 * notification message id
	 */
	public final static int NOTIFICATION_ID = 1;

	/**
	 * gets the current file which was started to be tagged
	 */
	public static final String CURRENT_FILE_TO_TAG = "current_file_to_tag";

	/**
	 * gets the tag line which was used before the user interrupted the tagging
	 * process
	 */
	public static final String CURRENT_TAG_LINE = "current_tag_line";

	/**
	 * current hash algorithm
	 */
	public static final String CURRENT_HASH_SUM_ALGORITHM = "current_hash_algorithm";

	/**
	 * default hash sum algorithm
	 */
	public static final String DEFAULT_HASH_SUM_ALGORITHM = "SHA1";

	/**
	 * stores the synchronization history
	 */
	public static final String SYNCHRONIZATION_HISTORY = "synch_date";

	/**
	 * boolean setting if tool bar notifications should be enabled
	 */
	public static final String SHOW_TOOLBAR_NOTIFICATIONS = "toolbar_notifications";
	
	/**
	 * log directory name
	 */
	public final static String LOG_DIRECTORY = ".tagstore";

	/**
	 * log file name
	 */
	public final static String LOG_FILENAME = "store.tgs";
	
	
	/**
	 * storage task timer interval
	 */
	public static final long STORAGE_TASK_TIMER_INTERVAL = 15000;
	
	/**
	 * delimiter for tags
	 */
	public static final String TAG_DELIMITER=",";

	/**
	 * name of configuration file
	 */
	public static final String CFG_FILENAME = "store.cfg";
}
