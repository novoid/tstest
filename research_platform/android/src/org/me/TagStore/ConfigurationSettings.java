package org.me.TagStore;

public class ConfigurationSettings {

	/**
	 * name of configuration settings
	 */
	protected final static String TAGSTORE_PREFERENCES_NAME = "TagStore";

	/**
	 * num items per row for default list view
	 */
	protected final static String NUMBER_OF_ITEMS_PER_ROW = "num_item_row";

	/**
	 * default num item per row
	 */
	protected final static int DEFAULT_ITEMS_PER_ROW = 3;

	/**
	 * current list view class
	 */
	protected final static String CURRENT_LIST_VIEW_CLASS = "current_view_class";

	/**
	 * default list view class
	 */
	protected final static String DEFAULT_LIST_VIEW_CLASS = "org.me.TagStore.TagStoreListViewActivity";

	/**
	 * icon list view class
	 */
	protected final static String ICON_LIST_VIEW_CLASS = "org.me.TagStore.TagStoreListViewActivity";

	/**
	 * cloud view class
	 */
	protected final static String CLOUD_VIEW_CLASS = "org.me.TagStore.TagStoreCloudViewActivity";

	/**
	 * current list view sort mode
	 */
	protected final static String CURRENT_LIST_VIEW_SORT_MODE = "current_list_view_sort_mode";

	/**
	 * default sort mode
	 */
	protected final static String DEFAULT_LIST_VIEW_SORT_MODE = "alphabetic";

	/**
	 * alphabetic sort mode
	 */
	protected final static String LIST_VIEW_SORT_MODE_ALPHABETIC = "alphabetic";

	/**
	 * sort tags by popularity
	 */
	protected final static String LIST_VIEW_SORT_MODE_POPULAR = "popular";

	/**
	 * sort tags by recently used
	 */
	protected final static String LIST_VIEW_SORT_MODE_RECENT = "recent";

	/**
	 * current list item icon
	 */
	protected final static String CURRENT_LIST_ITEM_ICON = "current_list_item_icon";

	/**
	 * default list item icon
	 */
	protected final static int DEFAULT_LIST_ITEM_ICON = R.drawable.file;

	/**
	 * current list item icon
	 */
	protected final static String CURRENT_LIST_TAG_ICON = "current_list_tag_icon";

	/**
	 * default list item icon
	 */
	protected final static int DEFAULT_LIST_TAG_ICON = R.drawable.tag;

	/**
	 * default list tag icon
	 */

	/**
	 * determines if pending file tag should be displayed
	 */
	protected final static String PENDING_FILE_TAB = "display_pending_file_tab";

	/**
	 * notification message id
	 */
	protected final static int NOTIFICATION_ID = 1;

	/**
	 * gets the current file which was started to be tagged
	 */
	public static final String CURRENT_FILE_TO_TAG = "current_file_to_tag";

	/**
	 * gets the tag line which was used before the user interrupted the tagging
	 * process
	 */
	public static final String CURRENT_TAG_LINE = "current_tag_line";

}
