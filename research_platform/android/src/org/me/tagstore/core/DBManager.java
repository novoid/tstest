package org.me.tagstore.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * manages access to sqlite engine
 * 
 */
public class DBManager {

	/**
	 * instance of the SQLite database
	 */
	private SQLiteDatabase m_db = null;

	/**
	 * sql database utility helper class
	 */
	private OpenHelper m_db_helper = null;

	/**
	 * stores the layout of the database
	 */
	private ArrayList<SQLTableLayout> m_layouts = null;

	/**
	 * instance of database manager
	 */
	private static DBManager instance = null;

	/**
	 * name of the database
	 */
	private final static String DATABASE_NAME = "tagstore.db";

	/**
	 * version of the database
	 */
	private final static int DATABASE_VERSION = 13;

	/**
	 * directory field id
	 */
	private static final String DIRECTORY_FIELD_ID = "did";

	/**
	 * table name for the observed directory table
	 */
	private final static String DIRECTORY_TABLE_NAME = "directory";

	/**
	 * stores the path of the observed directories
	 */
	private final static String DIRECTORY_FIELD_PATH = "directory_path";

	/**
	 * name of table for the pending files
	 */
	private final static String PENDING_FILE_TABLE_NAME = "pending_file";

	/**
	 * name of field for pending file paths
	 */
	private final static String PENDING_FIELD_PATH = "pending_file_path";

	/**
	 * pending file primary key field name
	 */
	private static final String PENDING_FIELD_ID = "pid";

	/**
	 * name of table for stored files
	 */
	private final static String FILE_TABLE_NAME = "file";

	/**
	 * file id for stored file name
	 */
	private final static String FILE_FIELD_ID = "fid";

	/**
	 * name of field for stored file paths
	 */
	private final static String FILE_FIELD_PATH = "file_path";

	/**
	 * stores the MIME type of the stored file
	 */
	private final static String FILE_FIELD_TYPE = "file_type";

	/**
	 * stores the creation date of the file
	 */
	private final static String FILE_FIELD_CREATE_DATE = "file_creation_date";

	/**
	 * stores the hash sum of the file
	 */
	private final static String FILE_FIELD_HASH_SUM = "file_hash_sum";

	/**
	 * name of table tag
	 */
	private final static String TAG_TABLE_NAME = "tag";

	/**
	 * name of field of tag
	 */
	private final static String TAG_FIELD_NAME = "tag_name";

	/**
	 * tag create date
	 */
	private final static String TAG_FIELD_CREATE_DATE = "tag_create_date";

	/**
	 * tag usage
	 */
	private final static String TAG_FIELD_USAGE = "tag_usage";

	/**
	 * tag id
	 */
	private final static String TAG_FIELD_ID = "tid";

	/**
	 * map table name
	 */
	private final static String MAP_TABLE_NAME = "map";

	/**
	 * mapping id
	 */
	private final static String MAP_FIELD_ID = "mid";

	/**
	 * tag field map id
	 */
	private final static String MAP_FIELD_TAG = "tid";

	/**
	 * file filed map id
	 */
	private final static String MAP_FIELD_FILE = "fid";

	private final static String SYNC_TABLE_PREFIX = "sync_";

	/**
	 * sync table name
	 */
	private final static String SYNC_TABLE_NAME = "sync";

	/**
	 * sync id
	 */
	private final static String SYNC_FIELD_ID = "sid";

	/**
	 * sync file path
	 */
	private final static String SYNC_FIELD_PATH = "sync_path";

	/**
	 * sync date
	 */
	private final static String SYNC_FIELD_DATE = "sync_date";

	/**
	 * sync tags
	 */
	private final static String SYNC_FIELD_TAGS = "sync_tags";

	/**
	 * sync hash sum
	 */
	private final static String SYNC_FIELD_HASH_SUM = "sync_hash_sum";

	/**
	 * sync revision
	 */
	private final static String SYNC_FIELD_REV = "sync_rev";

	/**
	 * gets an instance of the database manager
	 * 
	 * @return
	 */
	public static DBManager getInstance() {

		if (instance == null) {
			instance = new DBManager();
		}

		return instance;
	}

	@SuppressWarnings("unused")
	private void constructTestDatabase() {

		addDirectory("/mnt/sdcard/download");
		// return;

		//
		// construct date format
		//
		SimpleDateFormat date_format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		//
		// format date
		//
		String date = date_format.format(new Date());

		//
		// add 3 files
		//
		addFile("/mnt/sdcard/file1", "text/plain", date, "no hash");
		addFile("/mnt/sdcard/file2", "text/plain", date, "no hash");
		addFile("/mnt/sdcard/file3", "text/plain", date, "no hash");
		addFile("/mnt/sdcard/file4", "text/plain", date, "no hash");

		//
		// add 3 tags
		//
		addTag("tag1");
		addTag("tag2");
		addTag("tag3");

		char[] current_tag = { 'A' };
		for (int index = 4; index < 27 * 10; index++) {

			if (index % 10 == 0)
				current_tag[0]++;

			String tag = new String(current_tag);

			addTag(tag + Integer.toString(index % 10));
		}

		long tag1_id = getTagId("tag1");
		long tag2_id = getTagId("tag2");
		long tag3_id = getTagId("tag3");

		long file1_id = getFileId("/mnt/sdcard/file1");
		long file2_id = getFileId("/mnt/sdcard/file2");
		long file3_id = getFileId("/mnt/sdcard/file3");
		long file4_id = getFileId("/mnt/sdcard/file4");

		addFileTagMapping(file1_id, tag1_id);
		addFileTagMapping(file1_id, tag2_id);
		addFileTagMapping(file2_id, tag2_id);
		addFileTagMapping(file2_id, tag3_id);
		addFileTagMapping(file3_id, tag1_id);
		addFileTagMapping(file3_id, tag3_id);
		addFileTagMapping(file4_id, tag3_id);

		setTagReference("tag1", 2);
		setTagReference("tag2", 2);
		setTagReference("tag3", 3);

	}

	/**
	 * stores the result of the cursor in the array list
	 * 
	 * @param cursor
	 *            to collect the results from
	 * @param result_list
	 *            contains the result list
	 */
	private void collectResultSet(Cursor cursor, ArrayList<String> result_list) {

		//
		// are there any data sets
		//
		if (!cursor.moveToFirst())
			return;

		//
		// add the result
		//
		do {
			result_list.add(cursor.getString(0));
		} while (cursor.moveToNext());
	}

	/**
	 * returns the first column of the first entry
	 * 
	 * @param cursor
	 *            database cursor
	 * @return if there are no entries, then null is returned
	 */
	private String getFirstEntryOfResultSet(Cursor cursor) {

		//
		// are there any data
		//
		if (!cursor.moveToFirst())
			return null;

		//
		// get first string
		//
		String result = cursor.getString(0);

		//
		// are there more entries
		//
		if (cursor.moveToNext()) {
			//
			// BUG: duplicate entries
			//
			Logger.e("Duplicate entries found: ");
			StackTraceElement[] elements = Thread.currentThread()
					.getStackTrace();
			for (StackTraceElement element : elements)
				Logger.e("    at " + element.getClassName() + "."
						+ element.getMethodName() + "(" + element.getFileName()
						+ ":" + element.getLineNumber() + ")");
		}

		//
		// done
		//
		return result;
	}

	/**
	 * returns all files present in the tagstore
	 * 
	 * @return
	 */
	public ArrayList<String> getFiles() {

		if (m_db == null)
			return new ArrayList<String>();

		//
		// query database
		//
		Cursor cursor = m_db.query(FILE_TABLE_NAME,
				new String[] { FILE_FIELD_PATH }, null, null, null, null, null);

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		//
		// collect result set
		//
		collectResultSet(cursor, list);

		//
		// close cursor
		//
		cursor.close();

		//
		// done
		//
		return list;
	}

	/**
	 * returns an array list of files which are associated with this tag
	 * 
	 * @param tag_name
	 *            tag list to be queried
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getLinkedFiles(ArrayList<String> tag_names) {

		String query = "";

		//
		// construct string builder
		//
		StringBuilder builder = new StringBuilder();

		if (tag_names.size() == 1) {
			query = "SELECT " + MAP_FIELD_FILE + " FROM " + MAP_TABLE_NAME
					+ " WHERE " + MAP_FIELD_TAG + " =?";
		} else {
			//
			// build query
			//
			for (int index = 0; index < tag_names.size(); index++) {
				if (builder.length() == 0) {
					builder.append("SELECT T1." + MAP_FIELD_FILE + " FROM "
							+ MAP_TABLE_NAME + " AS T1 WHERE T1."
							+ MAP_FIELD_TAG + "=?");
				} else {
					int pos = builder.indexOf(" FROM");
					String str = ", T" + Integer.toString(index + 1) + "."
							+ MAP_FIELD_FILE;
					builder.insert(pos, str);

					pos = builder.indexOf(" WHERE");
					str = ", " + MAP_TABLE_NAME + " AS T"
							+ Integer.toString(index + 1);
					builder.insert(pos, str);

					str = " AND T" + Integer.toString(index + 1) + "."
							+ MAP_FIELD_TAG + " =? AND T1." + MAP_FIELD_FILE
							+ " = T" + Integer.toString(index + 1) + "."
							+ MAP_FIELD_FILE;
					builder.append(str);
				}

				query = builder.toString();
			}
		}

		String[] ids = new String[tag_names.size()];
		for (int index = 0; index < tag_names.size(); index++) {
			//
			// convert to ids
			//
			ids[index] = new String(
					Long.toString(getTagId(tag_names.get(index))));
		}

		Cursor cursor = m_db.rawQuery(query, ids);

		if (cursor.moveToFirst() == false) {
			//
			// no entries
			//
			cursor.close();
			Logger.e("no entries");
			return null;
		}

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		do {

			//
			// add result
			//
			Cursor file_cursor = m_db.query(FILE_TABLE_NAME,
					new String[] { FILE_FIELD_PATH }, FILE_FIELD_ID + "=?",
					new String[] { cursor.getString(0) }, null, null, null);

			//
			// collect result
			//
			collectResultSet(file_cursor, list);

			//
			// close cursor
			//
			file_cursor.close();

		} while (cursor.moveToNext());

		//
		// close cursor
		//
		cursor.close();

		//
		// return result list
		//
		return list;
	}

	/**
	 * returns a list of tags which are linked to these tags
	 * 
	 * @param tag_stack
	 *            list of selected tags
	 * @return
	 */
	public ArrayList<String> getLinkedTags(ArrayList<String> tag_names) {

		ArrayList<String> result_list = new ArrayList<String>();

		for (String tag : tag_names) {
			final String query = "SELECT " + TAG_FIELD_NAME + " FROM "
					+ TAG_TABLE_NAME + " WHERE " + TAG_FIELD_ID
					+ " IN (SELECT DISTINCT " + TAG_FIELD_ID + " FROM "
					+ MAP_TABLE_NAME + " WHERE " + FILE_FIELD_ID
					+ " IN ( SELECT " + FILE_FIELD_ID + " FROM "
					+ MAP_TABLE_NAME + " WHERE " + TAG_FIELD_ID + " = ?) AND "
					+ TAG_FIELD_ID + "<> ? )";

			String[] ids = new String[2];
			ids[0] = new String(Long.toString(getTagId(tag)));
			ids[1] = new String(Long.toString(getTagId(tag)));

			Cursor cursor = m_db.rawQuery(query, ids);

			//
			// construct array list
			//
			ArrayList<String> list = new ArrayList<String>();

			//
			// collect result
			//
			collectResultSet(cursor, list);

			//
			// close cursor
			//
			cursor.close();

			if (tag_names.indexOf(tag) == 0) {
				//
				// first entry
				//
				result_list.addAll(list);
			} else {
				//
				// remove all tags from the result list which were not found
				// while querying the current tag
				//
				result_list.retainAll(list);
			}
		}
		return result_list;
	}

	/**
	 * returns tags sorted alphabetically
	 */
	public ArrayList<String> getAlphabeticTags() {

		if (m_db == null)
			return null;

		Cursor cursor = m_db.query(TAG_TABLE_NAME,
				new String[] { TAG_FIELD_NAME }, null, null, null, null,
				TAG_FIELD_NAME + " ASC");

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		//
		// process result
		//
		collectResultSet(cursor, list);

		//
		// close cursor
		//
		cursor.close();

		//
		// return result list
		//
		return list;
	}

	/**
	 * returns popular tags sorted by their popularity in descending order
	 * 
	 * @return list of popular tags
	 */
	public ArrayList<String> getPopularTags() {

		if (m_db == null)
			return null;

		Cursor cursor = m_db.query(TAG_TABLE_NAME, new String[] {
				TAG_FIELD_NAME, TAG_FIELD_USAGE }, null, null, null, null,
				TAG_FIELD_USAGE + " DESC");

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		//
		// collect result
		//
		collectResultSet(cursor, list);

		//
		// close cursor
		//
		cursor.close();

		//
		// return result list
		//
		return list;
	}

	/**
	 * returns hash map of tags, whose keys are the tags and the values are the
	 * reference count
	 * 
	 * @return
	 */
	public HashMap<String, Integer> getTagReferenceCount() {

		if (m_db == null)
			return null;

		Cursor cursor = m_db
				.query(TAG_TABLE_NAME, new String[] { TAG_FIELD_NAME,
						TAG_FIELD_USAGE }, null, null, null, null, null);

		if (cursor.moveToFirst() == false) {
			//
			// no entries
			//
			cursor.close();
			return null;
		}

		//
		// construct hash map list
		//
		HashMap<String, Integer> list = new HashMap<String, Integer>();

		do {
			//
			// add result
			//
			list.put(cursor.getString(0), new Integer(cursor.getInt(1)));

		} while (cursor.moveToNext());

		//
		// close cursor
		//
		cursor.close();

		//
		// return result list
		//
		return list;
	}

	/**
	 * removes all references of the file in the tag store
	 * 
	 * @param file_name
	 */
	private void removeFileReferences(String file_name) {

		//
		// first get file id
		//
		long fid = getFileId(file_name);

		//
		// convert to string
		//
		String file_id = Long.toString(fid);

		//
		// first get all associated tags
		//
		Cursor cursor = m_db.query(MAP_TABLE_NAME,
				new String[] { MAP_FIELD_TAG }, MAP_FIELD_FILE + "=?",
				new String[] { file_id }, null, null, null);

		if (cursor.moveToFirst() == false) {
			//
			// no entries
			//
			Logger.d("DBManager::removeFileReferences> file " + file_name
					+ " has no references");
			cursor.close();
			return;
		}

		do {
			//
			// get current tag
			//
			String current_tag = cursor.getString(0);

			//
			// get current tag id and reference count
			//
			Cursor tag_cursor = m_db.query(TAG_TABLE_NAME, new String[] {
					TAG_FIELD_NAME, TAG_FIELD_USAGE }, TAG_FIELD_ID + "=?",
					new String[] { current_tag }, null, null, null);
			if (tag_cursor.moveToFirst() == false) {
				//
				// no entries
				//
				Logger.e("DBManager::removeFileReferences file "
						+ " references non existant " + current_tag);
				tag_cursor.close();
				continue;
			}
			do {
				//
				// there should be actually only one result, but don't expect a
				// database to be in good state
				//
				int reference_count = tag_cursor.getInt(1) - 1;
				String tag_name = tag_cursor.getString(0);

				if (reference_count > 0) {
					//
					// update tag reference
					//
					setTagReference(tag_name, reference_count);
				} else {
					//
					// delete tag
					//
					long affected = m_db.delete(TAG_TABLE_NAME, TAG_FIELD_ID
							+ "=?", new String[] { current_tag });
					Logger.d("Deleted tag " + tag_name + " deleted: "
							+ affected);
					break;
				}
			} while (tag_cursor.moveToNext());

			//
			// close tag cursor
			//
			tag_cursor.close();

		} while (cursor.moveToNext());

		//
		// close cursor
		//
		cursor.close();

		//
		// now remove all entries from the mapping table
		//
		m_db.delete(MAP_TABLE_NAME, MAP_FIELD_FILE + "=?",
				new String[] { Long.toString(fid) });
	}

	/**
	 * removes a pending file from the database
	 * 
	 * @param filename
	 *            file to be removed from the pending list
	 * @return true on success
	 */
	public boolean removePendingFile(String filename) {

		if (m_db == null)
			return false;

		//
		// remove file from pending file list
		//
		int pending_affected = m_db.delete(PENDING_FILE_TABLE_NAME,
				PENDING_FIELD_PATH + "=?", new String[] { filename });

		return pending_affected != 0;
	}

	/**
	 * removes a file from the database
	 * 
	 * @param filename
	 *            to be removed
	 * @return true on success
	 */
	public boolean removeFile(String filename) {

		if (m_db == null)
			return false;
		
		try {
			long pending_affected = 0;
			long file_cache_affected = 0;

			//
			// remove all file references
			//
			removeFileReferences(filename);

			//
			// remove file from tag store
			//
			file_cache_affected = m_db.delete(FILE_TABLE_NAME, FILE_FIELD_PATH
					+ "=?", new String[] { filename });

			//
			// informal debug message
			//
			Logger.i("DBManager::removeFile> Pending file: " + pending_affected
					+ " TagStore Cache: " + file_cache_affected);

			//
			// done
			//
			return true;
		} catch (SQLException exc) {
			Logger.e("DBManager::removeFile> exception occured");
			exc.printStackTrace();
			return false;
		}
	}

	/**
	 * returns date of the file
	 * 
	 * @param file_name
	 * @return
	 */
	public String getFileDate(String file_name) {

		//
		// query database
		//
		Cursor cursor = m_db.query(FILE_TABLE_NAME,
				new String[] { FILE_FIELD_CREATE_DATE },
				FILE_FIELD_PATH + "=?", new String[] { file_name }, null, null,
				null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();

		//
		// done
		//
		return result;

	}

	/**
	 * returns the hash sum of a stored file
	 * 
	 * @param file_name
	 *            name of the file
	 * @return hash sum hex coded string
	 */
	public String getHashsum(String file_name) {
		//
		// query database
		//
		Cursor cursor = m_db.query(FILE_TABLE_NAME,
				new String[] { FILE_FIELD_HASH_SUM }, FILE_FIELD_PATH + "=?",
				new String[] { file_name }, null, null, null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();

		//
		// done
		//
		return result;

	}

	/**
	 * returns the tag id corresponding to this tag
	 * 
	 * @param tag
	 *            to be queried
	 * @return tag id, -1 if failed
	 */
	public long getTagId(String tag) {

		Cursor cursor = m_db.query(TAG_TABLE_NAME,
				new String[] { TAG_FIELD_ID }, TAG_FIELD_NAME + "=?",
				new String[] { tag }, null, null, null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();

		if (result == null)
			return -1;
		else
			return Long.parseLong(result);
	}

	/**
	 * returns the file id corresponding to this file path
	 * 
	 * @param tag
	 *            to be queried
	 * @return tag id, -1 if failed
	 */
	public long getFileId(String file_name) {

		if (m_db == null)
			return -1;

		Cursor cursor = m_db.query(FILE_TABLE_NAME,
				new String[] { FILE_FIELD_ID }, FILE_FIELD_PATH + "=?",
				new String[] { file_name }, null, null, null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();

		if (result == null)
			return -1;
		else
			return Long.parseLong(result);
	}

	/**
	 * adds a sync date
	 * 
	 * @param store_name
	 *            name of the store
	 * @param remote_path
	 *            remove path
	 * @param rev
	 *            file revision
	 * @param time_stamp
	 *            file time stamp
	 * @param hash_sum
	 *            file hash sum
	 * @return true on success
	 */
	public boolean addSyncFileLog(String store_name, String remote_path,
			String rev, String time_stamp, String hash_sum) {

		// table name
		String table_name = SYNC_TABLE_PREFIX + store_name;

		// construct new content values
		ContentValues values = new ContentValues();

		// store values
		values.put(SYNC_FIELD_PATH, remote_path);
		values.put(SYNC_FIELD_DATE, time_stamp);
		values.put(SYNC_FIELD_HASH_SUM, hash_sum);
		values.put(SYNC_FIELD_REV, rev);

		long rows_affected;
		if (getSyncFileRev(store_name, remote_path) != null) {

			// update row
			rows_affected = m_db.update(table_name, values, SYNC_FIELD_PATH
					+ "=?", new String[] { remote_path });
		} else {
			// add mapping
			rows_affected = m_db.insert(table_name, null, values);
		}

		if (rows_affected == 0)
			return false;
		else
			return true;

	}

	public String getSyncFileRev(String store_name, String file_name) {

		if (m_db == null)
			return null;

		// table name
		String table_name = SYNC_TABLE_PREFIX + store_name;

		Cursor cursor = m_db.query(table_name, new String[] { SYNC_FIELD_REV },
				SYNC_FIELD_PATH + "=?", new String[] { file_name }, null, null,
				null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();
		return result;
	}

	/**
	 * return true when file is already pending
	 * 
	 * @param file_name
	 * @return
	 */
	public boolean isPendingFile(String file_name) {

		if (m_db == null)
			return false;

		Cursor cursor = m_db.query(PENDING_FILE_TABLE_NAME,
				new String[] { PENDING_FIELD_ID }, PENDING_FIELD_PATH + "=?",
				new String[] { file_name }, null, null, null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();

		if (result == null)
			return false;
		else
			return true;
	}

	/**
	 * set tag reference count
	 * 
	 * @param tag
	 *            to be updated
	 * @param new_reference_count
	 *            new reference count for the tag
	 */
	public void setTagReference(String tag, int new_reference_count) {

		//
		// construct new content values
		//
		ContentValues values = new ContentValues();

		//
		// set reference count
		//
		values.put(TAG_FIELD_USAGE, new Integer(new_reference_count));

		//
		// FIXME: fix race conditions
		//
		long affected = m_db.update(TAG_TABLE_NAME, values, TAG_FIELD_NAME
				+ "=?", new String[] { tag });

		//
		// informal debug print
		//
		Logger.i("DBManager::setTagReference> result " + affected);
	}

	/**
	 * deletes a tag from the database and removes all associated files from it
	 * 
	 * @param tag_name
	 */
	public void deleteTag(String tag_name) {

		//
		// first get the tag id
		//
		long tag_id = getTagId(tag_name);
		if (tag_id < 0) {
			//
			// invalid tag
			//
			return;
		}

		//
		// now query all associated files
		//
		Cursor cursor = m_db.query(MAP_TABLE_NAME,
				new String[] { MAP_FIELD_FILE }, MAP_FIELD_TAG + " =?",
				new String[] { Long.toString(tag_id) }, null, null, null);

		//
		// are there any files associated?
		//
		if (cursor.moveToFirst() == false) {
			//
			// no files associated
			//
			Logger.i("Error: tag " + tag_name
					+ " has no associated files tag id:" + tag_id);
			cursor.close();
			return;
		}

		do {
			//
			// get file id
			//
			long file_id = cursor.getLong(0);

			String raw_query = "SELECT COUNT(*) FROM " + MAP_TABLE_NAME
					+ " WHERE " + MAP_FIELD_FILE + "= ? ";
			Cursor raw_cursor = m_db.rawQuery(raw_query,
					new String[] { Long.toString(file_id) });

			if (raw_cursor.moveToFirst() == false) {
				//
				// no entries -> bug
				//
				raw_cursor.close();
				Logger.e("deleteTag: bug bug");
				continue;
			}

			//
			// get number of tags associated to this file
			//
			int tags_associated = raw_cursor.getInt(0);

			if (tags_associated == 1) {
				//
				// the file has only one tag associated -> the tag which is
				// deleted
				// remove the file from the store
				//
				m_db.delete(FILE_TABLE_NAME, FILE_FIELD_ID + "=?",
						new String[] { Long.toString(file_id) });
				Logger.i("Removed file id: " + file_id + " from store");
			}

			//
			// close cursor
			//
			raw_cursor.close();

		} while (cursor.moveToNext());

		//
		// close the cursor
		//
		cursor.close();

		//
		// now remove all entries from the mapping table
		//
		m_db.delete(MAP_TABLE_NAME, MAP_FIELD_TAG + "=?",
				new String[] { Long.toString(tag_id) });
		//
		// now remove the tag from tag table
		//
		m_db.delete(TAG_TABLE_NAME, TAG_FIELD_ID + "=?",
				new String[] { Long.toString(tag_id) });
	}

	/**
	 * creates an entry in the map table between file and tag
	 * 
	 * @param file_id
	 *            id of file
	 * @param tag_id
	 *            id of tag
	 * @return true on success
	 */
	public boolean addFileTagMapping(long file_id, long tag_id) {

		//
		// construct new content values
		//
		ContentValues values = new ContentValues();

		//
		// add tag id
		//
		values.put(MAP_FIELD_TAG, tag_id);

		//
		// add file id
		//
		values.put(MAP_FIELD_FILE, file_id);

		//
		// add mapping
		//
		long rows_affected = m_db.insert(MAP_TABLE_NAME, null, values);

		Logger.i("DBManager::addFileTagMapping file id " + file_id
				+ " tag id: " + tag_id + " rows id: " + rows_affected);

		if (rows_affected == 0)
			return false;
		else
			return true;
	}

	/**
	 * adds a tag to the database. New tags have reference count of 1
	 * 
	 * @param tag
	 *            to be added
	 */
	public void addTag(String tag) {

		//
		// check if it already exists
		//
		if (getTagId(tag) != -1)
			return;

		//
		// construct new content values
		//
		ContentValues values = new ContentValues();

		//
		// put file path into it
		//
		values.put(TAG_FIELD_NAME, tag);

		//
		// construct date format
		//
		SimpleDateFormat date_format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		//
		// format date
		//
		String date = date_format.format(new Date());

		//
		// insert date
		//
		values.put(TAG_FIELD_CREATE_DATE, date);

		//
		// set reference count to one
		//
		values.put(TAG_FIELD_USAGE, new Integer(1));

		//
		// now execute the insert
		//
		long rows_affected = m_db.insert(TAG_TABLE_NAME, null, values);

		//
		// informal debug message
		//
		Logger.i("DBManager::addTag> Date: " + date + " rows_affected: "
				+ rows_affected);
	}

	/**
	 * returns an array list of pending files to be added
	 * 
	 * @return
	 */
	public ArrayList<String> getPendingFiles() {

		if (m_db == null) {
			//
			// database not yet initialized
			//
			return null;
		}

		Cursor cursor = m_db.query(PENDING_FILE_TABLE_NAME,
				new String[] { PENDING_FIELD_PATH }, null, null, null, null,
				PENDING_FIELD_ID + " DESC");

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		//
		// collect result set
		//
		collectResultSet(cursor, list);

		//
		// close cursor
		//
		cursor.close();

		//
		// done
		//
		return list;

	}

	/**
	 * adds a file path to the pending file list
	 * 
	 * @param file_name
	 *            to be added
	 * @param hash_sum
	 * @return true
	 */
	public void addPendingFile(String file_name) {

		if (m_db == null)
			return;

		// check if it already exists
		if (getPendingFileId(file_name) != -1)
			return;

		//
		// construct new content values
		//
		ContentValues values = new ContentValues();

		//
		// put file path into it
		//
		values.put(PENDING_FIELD_PATH, file_name);

		//
		// now execute the insert
		//
		long rows_affected = m_db.insert(PENDING_FILE_TABLE_NAME, null, values);

		//
		// informal debug message
		//
		Logger.i("DBManager::addPendingFile> rows_affected: " + rows_affected);
	}

	private long getPendingFileId(String file_name) {

		if (m_db == null)
			return -1;

		Cursor cursor = m_db.query(PENDING_FILE_TABLE_NAME,
				new String[] { PENDING_FIELD_ID }, PENDING_FIELD_PATH + "=?",
				new String[] { file_name }, null, null, null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();

		if (result == null)
			return -1;
		else
			return Long.parseLong(result);
	}

	/**
	 * adds a file to the tag store
	 * 
	 * @param file_name
	 *            path of file
	 * @param file_type
	 *            mime type of file
	 * @param file_create_date
	 *            create date of file
	 * @param hash_sum
	 * @return row id of inserted file
	 */
	public long addFile(String file_name, String file_type,
			String file_create_date, String hash_sum) {

		if (m_db == null)
			return -1;

		//
		// construct content values
		//
		ContentValues values = new ContentValues();

		//
		// add filename
		//
		values.put(FILE_FIELD_PATH, file_name);

		//
		// add file type
		//
		values.put(FILE_FIELD_TYPE, file_type);

		//
		// add create date
		//
		values.put(FILE_FIELD_CREATE_DATE, file_create_date);

		//
		// add file hash sum
		//
		values.put(FILE_FIELD_HASH_SUM, hash_sum);

		//
		// now execute the insert
		//
		long rows_affected = m_db.insert(FILE_TABLE_NAME, null, values);

		//
		// informal debug message
		//
		Logger.i("DBManager::addFile>  rows_affected: " + rows_affected);

		//
		// return row id
		//
		return rows_affected;
	}

	/**
	 * adds an directory to the observed directory list
	 * 
	 * @param directory
	 *            to be observed
	 */
	public boolean addDirectory(String directory) {

		if (m_db == null)
			return false;

		//
		// construct new content values
		//
		ContentValues values = new ContentValues();

		//
		// add directory value
		//
		values.put(DIRECTORY_FIELD_PATH, directory);

		//
		// now execute the query
		//
		long rows_affected = m_db.insert(DIRECTORY_TABLE_NAME, null, values);

		//
		// informal debug print
		//
		Logger.i("DBManager::addDirectory> rows_affected " + rows_affected);

		if (rows_affected == 1) {
			//
			// succeeded
			//
			return true;
		}

		//
		// insert failed
		//
		return false;

	}

	/**
	 * removes directory from the list of observed directories
	 * 
	 * @param directory_path
	 *            to be removed
	 * @return true when success, false on failure
	 */
	public boolean removeDirectory(String directory_path) {

		//
		// delete directory from observed table list
		//
		long rows_affected = m_db.delete(DIRECTORY_TABLE_NAME,
				DIRECTORY_FIELD_PATH + "=?", new String[] { directory_path });

		//
		// informal debug message
		//
		Logger.i("DBManager::removeDirectory> executeInsert: " + rows_affected);

		//
		// done
		//
		return rows_affected != 0;
	}

	/**
	 * queries the directory table if the passed directory path is added to the
	 * list of observed directories
	 * 
	 * @param directory_path
	 *            directory path to check
	 * @return true when it is observed false if not
	 */
	public boolean isDirectoryObserved(String directory_path) {

		Cursor cursor = m_db.query(DIRECTORY_TABLE_NAME,
				new String[] { DIRECTORY_FIELD_ID }, DIRECTORY_FIELD_PATH
						+ "=?", new String[] { directory_path }, null, null,
				null);

		//
		// get first entry of result set
		//
		String result = getFirstEntryOfResultSet(cursor);

		//
		// close cursor
		//
		cursor.close();

		if (result == null)
			return false;
		else
			return true;
	}

	/**
	 * returns all observed directories
	 * 
	 * @return array list of directory path
	 */
	public ArrayList<String> getDirectories() {

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		if (m_db == null)
			return list;

		//
		// read directories
		//
		Cursor cursor = m_db.query(DIRECTORY_TABLE_NAME,
				new String[] { DIRECTORY_FIELD_PATH }, null, null, null, null,
				null);

		//
		// collect result
		//
		collectResultSet(cursor, list);

		//
		// close cursor
		//
		cursor.close();

		//
		// return result list
		//
		return list;
	}

	/**
	 * returns a list of directories which have the same file name
	 * 
	 * @param file_name
	 * @return
	 */
	public ArrayList<String> getSimilarFilePaths(String file_name) {

		//
		// read directories
		//
		Cursor cursor = m_db.query(FILE_TABLE_NAME,
				new String[] { FILE_FIELD_PATH }, FILE_FIELD_PATH + " LIKE ?",
				new String[] { "%/" + file_name }, null, null, null);

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		//
		// collect result set
		//
		collectResultSet(cursor, list);

		//
		// close cursor
		//
		cursor.close();

		//
		// return result list
		//
		return list;
	}

	public boolean renameTag(String old_tag_name, String new_tag_name) {
		//
		// construct new content values
		//
		ContentValues values = new ContentValues();

		//
		// set reference count
		//
		values.put(TAG_FIELD_NAME, new_tag_name);

		//
		// rename file
		//
		long affected = m_db.update(TAG_TABLE_NAME, values, TAG_FIELD_NAME
				+ "=?", new String[] { old_tag_name });

		//
		// informal debug print
		//
		Logger.i("DBManager::renameTag> result " + affected);

		//
		// return result
		//
		return affected != 0;
	}

	/**
	 * renames a file in the database
	 * 
	 * @param old_file_name
	 *            old file name
	 * @param new_file_name
	 *            new file name
	 */
	public boolean renameFile(String old_file_name, String new_file_name) {

		//
		// construct new content values
		//
		ContentValues values = new ContentValues();

		//
		// set reference count
		//
		values.put(FILE_FIELD_PATH, new_file_name);

		//
		// rename file
		//
		long affected = m_db.update(FILE_TABLE_NAME, values, FILE_FIELD_PATH
				+ "=?", new String[] { old_file_name });

		if (affected == 0) {
			//
			// try rename in the pending list
			//
			values = new ContentValues();
			values.put(PENDING_FIELD_PATH, new_file_name);

			affected = m_db.update(PENDING_FILE_TABLE_NAME, values,
					PENDING_FIELD_PATH + "=?", new String[] { old_file_name });
		}

		//
		// informal debug print
		//
		Logger.i("DBManager::renameFile> result " + affected);
		return affected != 0;
	}

	/**
	 * returns all associated tags of that file
	 * 
	 * @param file_name
	 *            file name whose tags are returned
	 * @return list of tags
	 */
	public ArrayList<String> getAssociatedTags(String file_name) {

		//
		// first get the file id
		//
		long fid = getFileId(file_name);
		if (fid < 0) {
			//
			// invalid file id
			//
			return null;
		}

		String raw_query = "SELECT " + TAG_FIELD_NAME + " FROM "
				+ TAG_TABLE_NAME + " WHERE " + TAG_FIELD_ID + " IN (SELECT "
				+ MAP_FIELD_TAG + " FROM " + MAP_TABLE_NAME + " WHERE "
				+ MAP_FIELD_FILE + " = ? )";

		Cursor cursor = m_db.rawQuery(raw_query,
				new String[] { Long.toString(fid) });

		//
		// construct array list
		//
		ArrayList<String> list = new ArrayList<String>();

		//
		// return result list
		//
		collectResultSet(cursor, list);

		//
		// close cursor
		//
		cursor.close();

		//
		// return result list
		//
		return list;
	}

	/**
	 * create sync table
	 * 
	 * @param name
	 *            of the store
	 */
	public boolean createSyncTable(String store_name) {

		// table name
		String table_name = SYNC_TABLE_PREFIX + store_name;

		// create table
		String sql_stmt = "CREATE TABLE IF NOT EXISTS " + table_name + "("
				+ SYNC_FIELD_ID + " INTEGER primary key" + ","
				+ SYNC_FIELD_PATH + " TEXT" + "," + SYNC_FIELD_DATE + " TEXT"
				+ "," + SYNC_FIELD_HASH_SUM + " TEXT" + "," + SYNC_FIELD_REV
				+ " TEXT" + ")";

		Logger.i("execute: " + sql_stmt);

		try {
			// execute sql
			m_db.execSQL(sql_stmt);
			return true;
		} catch (SQLException exc) {
			Logger.e("SQLException while executing " + sql_stmt);
		}
		// failed
		return false;
	}

	public boolean resetDatabase(Context ctx) {

		if (m_db == null)
			return false;

		//
		// now close the database
		//
		m_db.close();

		//
		// now delete the database
		//
		boolean deleted_database = ctx.deleteDatabase(DATABASE_NAME);

		//
		// informal debug print
		//
		Logger.i("DBManager::resetDatabase deleted database" + deleted_database);

		//
		// now recreate the database
		//
		m_db = m_db_helper.getWritableDatabase();

		//
		// construct test database
		//
		// constructTestDatabase();

		//
		// return result
		//
		return deleted_database;
	}

	public void initializeDBManager(Context ctx) {

		if (m_db == null) {

			// ctx.deleteDatabase(DATABASE_NAME);

			//
			// initialize database manager
			//
			prepareLayout();

			//
			// construct database helper
			//
			m_db_helper = new OpenHelper(ctx, DATABASE_NAME, DATABASE_VERSION,
					m_layouts);

			//
			// now get an writable database instance
			//
			m_db = m_db_helper.getWritableDatabase();

		}
	}

	/**
	 * this function creates the sql layout of the tables
	 */
	private void prepareLayout() {

		m_layouts = new ArrayList<SQLTableLayout>();

		//
		// construct the table tag
		//
		SQLTableLayout layout = new SQLTableLayout(TAG_TABLE_NAME);
		layout.addFieldToSQLTableLayout(TAG_FIELD_ID, "INTEGER primary key",
				"tag id");
		layout.addFieldToSQLTableLayout(TAG_FIELD_NAME, "TEXT", "tag name");
		layout.addFieldToSQLTableLayout(TAG_FIELD_CREATE_DATE, "TEXT",
				"creation date of tag");
		layout.addFieldToSQLTableLayout(TAG_FIELD_USAGE, "INTEGER",
				"tag usage in repository");
		m_layouts.add(layout);

		//
		// construct the table file
		//
		layout = new SQLTableLayout(FILE_TABLE_NAME);
		layout.addFieldToSQLTableLayout(FILE_FIELD_ID, "INTEGER primary key",
				"file id");
		layout.addFieldToSQLTableLayout(FILE_FIELD_PATH, "TEXT", "path of file");
		layout.addFieldToSQLTableLayout(FILE_FIELD_TYPE, "TEXT", "type of file");
		layout.addFieldToSQLTableLayout(FILE_FIELD_CREATE_DATE, "TEXT",
				"creation date of file");
		layout.addFieldToSQLTableLayout(FILE_FIELD_HASH_SUM, "TEXT",
				"hash sum of file");
		m_layouts.add(layout);

		//
		// construct the mapping table
		//
		layout = new SQLTableLayout(MAP_TABLE_NAME);
		layout.addFieldToSQLTableLayout(MAP_FIELD_ID, "INTEGER primary key",
				"map id");
		layout.addFieldToSQLTableLayout(MAP_FIELD_FILE, "INTEGER", "file id");
		layout.addFieldToSQLTableLayout(MAP_FIELD_TAG, "INTEGER", "tag id");
		m_layouts.add(layout);

		//
		// construct the directory table
		//
		layout = new SQLTableLayout(DIRECTORY_TABLE_NAME);
		layout.addFieldToSQLTableLayout(DIRECTORY_FIELD_ID,
				"INTEGER primary key", "directory id");
		layout.addFieldToSQLTableLayout(DIRECTORY_FIELD_PATH, "TEXT",
				"path of directory");
		m_layouts.add(layout);

		//
		// construct pending file table
		//
		layout = new SQLTableLayout(PENDING_FILE_TABLE_NAME);
		layout.addFieldToSQLTableLayout(PENDING_FIELD_ID,
				"INTEGER primary key", "pending id");
		layout.addFieldToSQLTableLayout(PENDING_FIELD_PATH, "TEXT",
				"pending path of file");
		m_layouts.add(layout);

		//
		// construct synchronized table
		//
		layout = new SQLTableLayout(SYNC_TABLE_NAME);
		layout.addFieldToSQLTableLayout(SYNC_FIELD_ID, "INTEGER primary key",
				"sync id");
		layout.addFieldToSQLTableLayout(SYNC_FIELD_PATH, "TEXT",
				"path of synced file");
		layout.addFieldToSQLTableLayout(SYNC_FIELD_DATE, "TEXT", "sync date");
		layout.addFieldToSQLTableLayout(SYNC_FIELD_TAGS, "TEXT",
				"tags of synced file");
		layout.addFieldToSQLTableLayout(SYNC_FIELD_HASH_SUM, "TEXT",
				"hash sum of synced file");
		m_layouts.add(layout);
	}

	/**
	 * This class is used to describe the layout of an sql table. It stores the
	 * table name, a list of the field names, field types and field description
	 */
	private class SQLTableLayout {

		/**
		 * constructor of class table layout
		 */
		public SQLTableLayout(String table_name) {

			m_table_name = table_name;
			m_field_name = new ArrayList<String>();
			m_field_type = new ArrayList<String>();
			m_field_description = new ArrayList<String>();
			m_field_count = 0;
		}

		/**
		 * name of the table
		 */
		public final String m_table_name;
		/**
		 * name of the field
		 */
		public ArrayList<String> m_field_name;
		/**
		 * type of the field
		 */
		public ArrayList<String> m_field_type;
		/**
		 * description of the field
		 */
		public ArrayList<String> m_field_description;
		/**
		 * table field count
		 */
		public int m_field_count;

		/**
		 * adds a field to an sql table
		 * 
		 * @param layout
		 *            of the sql table
		 * @param field_name
		 *            name of the field
		 * @param field_type
		 *            type of the field
		 * @param field_description
		 *            description of the field
		 */
		public void addFieldToSQLTableLayout(String field_name,
				String field_type, String field_description) {

			if (m_field_name.contains(field_name)) {
				Logger.e("Error: field name " + field_name
						+ "is already present");
				return;
			}

			//
			// add field
			//
			m_field_name.add(field_name);
			m_field_type.add(field_type);
			m_field_description.add(field_description);
			m_field_count++;
		}
	}

	/**
	 * This class is used to interact with SQLite database. It provides methods
	 * to query and add records to the database
	 */
	private class OpenHelper extends SQLiteOpenHelper {

		/**
		 * stores the layout of the sql table
		 */
		ArrayList<SQLTableLayout> m_db_tables;

		/**
		 * constructor of OpenHelper class
		 * 
		 * @param ctx
		 *            application context
		 * @param db_name
		 *            name of the database
		 * @param db_version
		 *            version of the database
		 * @param db_tables
		 *            sql table layout of the tables
		 */
		public OpenHelper(Context ctx, String db_name, int db_version,
				ArrayList<SQLTableLayout> db_tables) {

			super(ctx, db_name, null, db_version);

			m_db_tables = db_tables;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			Logger.i("DBManager::onCreate");
			//
			// now construct the tables
			//
			for (SQLTableLayout layout : m_db_tables) {

				//
				// construct the sql create statement
				//
				String sql_stmt = "CREATE TABLE " + layout.m_table_name + "(";

				for (int field_index = 0; field_index < layout.m_field_count; field_index++) {

					//
					// get field index
					//
					String field_name = layout.m_field_name.get(field_index);

					//
					// get field type
					//
					String field_type = layout.m_field_type.get(field_index);

					//
					// setup type
					//
					String field = field_name + " " + field_type;

					//
					// check if field seperator is needed
					//
					boolean add_colon = (field_index < layout.m_field_count - 1 ? true
							: false);

					if (add_colon) {

						field += ",";
					}

					//
					// add field
					//
					sql_stmt += field;
				}

				//
				// close sql statement
				//
				sql_stmt += ")";

				Logger.i("STMT: " + sql_stmt);

				try {
					db.execSQL(sql_stmt);
				} catch (SQLException exc) {
					exc.printStackTrace();
				}
			}

			m_db = db;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			//
			// TODO: upgrade is not supported yet
			//
			Logger.i("Upgrade not yet supported");

			for (SQLTableLayout layout : m_db_tables) {
				//
				// construct drop table statement
				//
				String sql_stmt = "DROP TABLE IF EXISTS " + layout.m_table_name;
				Logger.i("STMT: " + sql_stmt);
				//
				// execute statement
				//
				db.execSQL(sql_stmt);
			}

			//
			// construct tables
			//
			onCreate(db);
		}
	}

}
