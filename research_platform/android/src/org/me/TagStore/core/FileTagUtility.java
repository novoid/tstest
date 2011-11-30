package org.me.TagStore.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.me.TagStore.R;

import android.content.Context;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

/**
 * this class is responsible to provide helper functions which deal parsing of tags and adding / deleting files and their associated data to the database 
 * @author Johannes Anderwald
 *
 */
public class FileTagUtility {

	/**
	 * tag separator
	 */
	public final static String DELIMITER = ",";	
	
	/**
	 * this function processes the associated tags. It will create tag entry if
	 * the tag is new, or update the reference count of existing tags
	 * 
	 * @param file_name path of the file
	 * @param tag_text
	 *            string containing the tags
	 * @return
	 */
	private static boolean processTags(String file_name, String tag_text) {

		//
		// get instance of database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get file id
		//
		long file_id = db_man.getFileId(file_name);
		if (file_id < 0) {
			Logger.e("Error AddFileTagActivity::processTags failed to get file id for file "
					+ file_name);
			return false;
		}

		//
		// split the tag text into unique set of tokens
		//
		Set<String> tag_list = splitTagText(tag_text);

		//
		// get tag reference count
		//
		HashMap<String, Integer> tag_reference = db_man.getTagReferenceCount();

		//
		// iterate tag list
		//
		for (String current_tag : tag_list) {
			//
			// is there a tag list
			//
			if (tag_reference != null) {
				//
				// check is the tag known
				//
				if (tag_reference.containsKey(current_tag) == false) {
					//
					// user entered new tag
					//
					Logger.i("AddFileTagActivity::processTags new tag: "
							+ current_tag);

					//
					// add tag to database
					//
					db_man.addTag(current_tag);
				} else {
					//
					// adds reference to tag
					//
					int reference_count = tag_reference.get(current_tag)
							.intValue() + 1;
					Logger.i("AddFileTagActivity::processTags tag: "
							+ current_tag + " new reference count: "
							+ reference_count);

					db_man.setTagReference(current_tag, reference_count);
				}
			} else {
				//
				// first tag ever
				//
				Logger.i("AddFileTagActivity::processTags first tag: "
						+ current_tag);

				//
				// add tag to database
				//
				db_man.addTag(current_tag);
			}

			//
			// now get tag id
			//
			long tag_id = db_man.getTagId(current_tag);
			if (tag_id < 0) {
				//
				// failed to get tag
				//
				Logger.e("Error AddFileTagActivity::processTags> failed to get tag id");
				return false;
			}

			//
			// add file < - > tag mapping
			//
			if (!db_man.addFileTagMapping(file_id, tag_id))
				return false;
		}

		//
		// done
		//
		return true;
	}

	/**
	 * splitTagText splits the tag text line into unique sets of token
	 * 
	 * @param tag_text
	 *            to be splitted
	 * @return set of token
	 */
	public static Set<String> splitTagText(String tag_text) {

		//
		// split the tag_text
		//
		StringTokenizer tokenizer = new StringTokenizer(tag_text, DELIMITER);

		//
		// create hash map to store the tags in order to remove duplicates
		//
		Set<String> tag_list = new HashSet<String>();

		//
		// add tokens to list
		//
		while (tokenizer.hasMoreTokens())
		{
			//
			// get current token
			//
			String current_tag = tokenizer.nextToken();
			
			//
			// trim whitespaces
			//
			current_tag = current_tag.trim();
			
			if (current_tag.isEmpty() == false)
			{
				//
				// add to list
				//
				tag_list.add(current_tag);
			}
		}
		//
		// done
		//
		return tag_list;
	}
	
	/**
	 * adds a file to the database
	 * @param tag_text 
	 */
	private static boolean addFileToDB(String file_name, String tag_text, Context ctx) {

		String mime_type = "";

		//
		// generate hashsum
		//
		String hash_sum = "NOHASHSUMGENEREATED"; //FileHashsumGenerator.generateFileHashsum(ctx, file_name);
		
		//
		// get time stamp in UTC
		//
		String create_date = TimeFormatUtility.getCurrentTimeInUTC();

		//
		// get file extension
		//
		int index = file_name.lastIndexOf('.');
		if (index > 0) {
			String extension = file_name.substring(index + 1);
			if (extension.length() != 0) {
				//
				// get mime type map instance
				//
				MimeTypeMap mime_map = MimeTypeMap.getSingleton();

				//
				// guess file extension
				//
				mime_type = mime_map.getMimeTypeFromExtension(extension);
			}
		}

		//
		// let's add the file
		//
		return addFileWithProperties(file_name, tag_text, hash_sum, create_date, mime_type);
	}	

	/**
	 * adds a file with the following properties to the tagstore
	 * @param file_name path of the file
	 * @param tag_text tags associated
	 * @param hash_sum hash sum of the file
	 * @param create_date create date of the file
	 * @param mime_type mime type of the file
	 * @return true on success
	 */
	private static boolean addFileWithProperties(String file_name, String tag_text, String hash_sum, 
												String create_date, String mime_type)
	{
		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// informal debug prints
		//
		Logger.i("AddFileTagActivity::addFileToDB> file_name: " + file_name);
		Logger.i("AddFileTagActivity::addFileToDB> create_date: " + create_date);
		Logger.i("AddFileTagActivity::addFileToDB> mime_type: " + mime_type);
		Logger.i("AddFileTagActivitiy::addFileToDB> hash sum: " + hash_sum);
		
		//
		// add to the database
		//
		long result = db_man.addFile(file_name, mime_type, create_date, hash_sum);

		//
		// result
		//
		Logger.i("AddFileTagActivity::addFileToDB> result: " + result);

		//
		// now remove pending file
		//
		db_man.removePendingFile(file_name);

		//
		// done
		//
		return true;
	}
	
	
	/**
	 * adds a file to the database
	 * @param file_name path of file
	 * @param write_log true if to the log should be written
	 * @param tag_text tags of the file
	 */
	public static boolean addFile(String file_name, String tag_text, boolean write_log, Context ctx) {
		
		//
		// check if file exists
		//
		File file = new File(file_name);
		if (!file.exists())
		{
			if (ctx == null)
				return false;
			
			//
			// get localized error format string
			//
			String error_format = ctx.getString(R.string.error_format_file_removed);
			
			//
			// format the error
			//
			String msg = String.format(error_format, file_name);
			
			
			//
			// inform user that it failed to add the file
			//
			Toast toast = Toast
					.makeText(ctx,
							msg,
							Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// done
			//
			return false;
		}
		
		//
		// first add the file
		//
		if (!FileTagUtility.addFileToDB(file_name, tag_text, ctx)) {
			
			if (ctx == null)
				return false;
			
			//
			// get localized error format string
			//
			String error_format = ctx.getString(R.string.error_format_add_file);
			
			//
			// format the error
			//
			String msg = String.format(error_format, file_name);
			
			
			//
			// inform user that it failed to add the file
			//
			Toast toast = Toast
					.makeText(ctx,
							msg,
							Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// done
			//
			return false;
		}

		//
		// now process the tags
		//
		if (!FileTagUtility.processTags(file_name, tag_text)) {
			
			if (ctx == null)
				return false;
			
			//
			// get localized error format string
			//
			String error_format = ctx.getString(R.string.error_format_add_file);
			
			//
			// format the error
			//
			String msg = String.format(error_format, file_name);
			
			//
			// inform user that it failed to process tags
			//
			Toast toast = Toast.makeText(ctx,
					msg, Toast.LENGTH_SHORT);

			//
			// display toast
			//
			toast.show();

			//
			// remove file from database store
			//
			DBManager db_man = DBManager.getInstance();

			//
			// remove entry
			//
			db_man.removeFile(file_name);

			//
			// done
			//
			return false;
		}
		
		if (write_log)
		{
			//
			// instantiate the sync file write
			//
			SyncFileWriter file_writer = new SyncFileWriter();
			
			//
			// write entries
			//
			file_writer.writeTagstoreFiles();
		}
		
		
		
		//
		// completed
		//
		return true;
	}
	
	/**
	 * removes a tag from the tag store
	 * @param tagname
	 */
	public static void removeTag(String tagname) {
		
		//
		// instantiate database manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// remove tag
		//
		db_man.deleteTag(tagname);
		
		//
		// instantiate the sync file write
		//
		SyncFileWriter file_writer = new SyncFileWriter();
		
		//
		// write entries
		//
		file_writer.writeTagstoreFiles();			
	}
	
	
	/**
	 * removes a file from the database and deletes the file from disk and displays a toast if the operation has been successful
	 * @param file_name path of the file to be deleted
	 * @param ctx application context
	 */
	public static void removeFile(String file_name, Context ctx) {
		
		//
		// FIXME: should display confirmation dialog
		//
		File file = new File(file_name);

		//
		// lets delete the file from the database
		//
		DBManager db_man = DBManager.getInstance();

		//
		// remove file
		//
		db_man.removeFile(file_name);

		//
		// delete file
		//
		boolean file_deleted = file.delete();

		//
		// check for success
		//
		String msg;
		if (file_deleted) {
			
			//
			// instantiate the sync file write
			//
			SyncFileWriter file_writer = new SyncFileWriter();
			
			//
			// write entries
			//
			file_writer.writeTagstoreFiles();	
			
			if (ctx == null)
				return;
			
			//
			// get localized format string
			//
			String format_delete = ctx.getString(R.string.format_delete);
			
			//
			// format the string
			//
			msg = String.format(format_delete, file_name);
			
			
		
			

		} else {
			
			if (ctx == null)
				return;
			
			//
			// get localized format string
			//
			String format_error_delete = ctx.getString(R.string.error_format_delete);
			
			//
			// format the string
			//
			msg = String.format(format_error_delete, file_name);
		}

		//
		// create toast
		//
		Toast toast = Toast.makeText(ctx, msg,
				Toast.LENGTH_SHORT);

		//
		// display toast
		//
		toast.show();
	}

	/**
	 * This function compares the entered tag line
	 * @param tags to be checked
	 * @return true
	 */
	public static boolean validateTags(String tags, Context ctx, EditText edit_text) {
		
		//
		// first split the tags
		//
		Set<String> tag_list = splitTagText(tags);
		if (tag_list.isEmpty())
		{
			if (ctx != null)
			{
				//
				// must be at least one tag
				//
				String message = ctx.getString(R.string.one_tag_minimum);
			
				Toast toast = Toast.makeText(ctx,
						message,
						Toast.LENGTH_SHORT);

				//
				// display toast
				//
				toast.show();
			}
			
			//
			// failed
			//
			return false;
		}
		
		
		//
		// now check that all tags are valid
		//
		for(String current_tag : tag_list) {
			
			if (TagValidator.isReservedKeyword(current_tag))
			{
				if (ctx != null)
				{
					//
					// the tag line has at least one reserved keyword in use
					//
					String message = ctx.getString(R.string.reserved_keyword);
				
					Toast toast = Toast.makeText(ctx,
							message,
							Toast.LENGTH_SHORT);

					//
					// display toast
					//
					toast.show();				
				}

				if (edit_text != null)
				{
					//
					// get position
					//
					int position = tags.indexOf(current_tag);
					if (position >= 0)
					{
						//
						// mark selected position
						//
						edit_text.setSelection(position, position + current_tag.length());
					}
				}
				
				return false;
			}
		}
		
		//
		// tags appear to be o.k.
		//
		return true;
	}
	
	/**
	 * this function retags a file
	 * @param filename file to be retagged
	 * @param tags new tags for file
	 * @param write_log true if the log should be written to
	 * @param context context which is invoked in case of errors
	 * @return true on success
	 */
	public static boolean retagFile(String filename, String tags, boolean write_log, Context context) {
		
		//
		// instantiate database manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// remove file from tag store
		//
		db_man.removeFile(filename);
		
		//
		// HACK: mark file as pending
		//
		db_man.addPendingFile(filename);
		
		//
		// re-add them to the store
		//
		return FileTagUtility.addFile(filename, tags, write_log, context);
	}
	
	
	/**
	 * renames a file in the databases and then actually renames it in the filesystem
	 * @param old_file_name
	 * @param new_file_name
	 * @return
	 */
	public static boolean renameFile(String old_file_name, String new_file_name) {
	
		//
		// instantiate database manager
		//
		DBManager db_man = DBManager.getInstance();
	
		//
		// instantiate the sync file write
		//
		SyncFileWriter file_writer = new SyncFileWriter();
		
		//
		// create file objs for renaming
		//
		File file = new File(old_file_name);
		if (!file.exists())
		{
			Logger.e("Error: the file " + old_file_name + " no longer exists");
			
			//
			// lets remove it from tagstore
			//
			db_man.removeFile(old_file_name);
			
			//
			// write entries
			//
			file_writer.writeTagstoreFiles();			
			
			//
			// failed
			//
			return false;
		}
		
		//
		// construct new file name obj
		//
		File new_file_obj = new File(new_file_name);
		if (new_file_obj.exists())
		{
			//
			// there is already a file with that name present
			//
			Logger.e("Error: can't rename file to " + new_file_name + " because it already exists");
			return false;
		}
		
		//
		// now update the database
		//
		db_man.renameFile(old_file_name, new_file_name);
	
		//
		// now rename the file
		//
		boolean renamed_file = file.renameTo(new_file_obj);
		
		if (renamed_file)
		{
			//
			// write entries
			//
			file_writer.writeTagstoreFiles();			
		}
		
		//
		// done
		//
		return renamed_file;
	}
	
	/**
	 * returns true when the file name is already taken
	 * @param new_file_name new file name to check for uniqueness
	 * @return true when filename is already consumed
	 */
	public static boolean isFilenameAlreadyTaken(String new_file_name) {
		
		//
		// check if there is already a file with that name entered
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// get list of file paths which have the same file name
		//
		ArrayList<String> same_files = db_man.getSimilarFilePaths(new_file_name);
		
		//
		// check result
		//
		if (same_files == null)
			return false;
		else
			return (same_files.size() > 0);
	}
	
	/**
	 * returns true when the tag already exists
	 * @param tag to be checked
	 * @return boolean
	 */
	public static boolean isTagExisting(String tag) {
		
		//
		// check if there is already a file with that name entered
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// get tag id of given tag
		//
		return db_man.getTagId(tag) != -1;
		
	}
	
	/**
	 * renames the tag
	 * @param old_tag_name old tag name
	 * @param new_tag_name new tag name
	 */
	public static boolean renameTag(String old_tag_name, String new_tag_name) {
		
		//
		// check if there is already a file with that name entered
		//
		DBManager db_man = DBManager.getInstance();
		
		
		//
		// rename tag
		//
		boolean renamed_tag = db_man.renameTag(old_tag_name, new_tag_name);
		
		
		if (renamed_tag)
		{
			//
			// instantiate the sync file write
			//
			SyncFileWriter file_writer = new SyncFileWriter();
			
			//
			// write entries
			//
			file_writer.writeTagstoreFiles();
		}
		
		//
		// done
		//
		return renamed_tag;
	}


	/**
	 * removes pending file from pending file list
	 * @param current_file
	 */
	public static void removePendingFile(String current_file) {
		//
		// check if there is already a file with that name entered
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// removes pending file
		//
		db_man.removePendingFile(current_file);
	}

	/**
	 * returns a list of linked tags which are linked to the current tag stack
	 * @return
	 */
	public static ArrayList<String> getLinkedTags() {
		
		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get current tag stack
		//
		ArrayList<String> tag_stack = TagStackManager.getInstance().toArray(new String[1]);
		
		//
		// get associated files
		//
		ArrayList<String> linked_tags = db_man.getLinkedTags(tag_stack);
		if (linked_tags == null)
		{
			Logger.e("Error: FileTagUtility::getLinkedTags linked_tags null");
			return new ArrayList<String>();
		}
		
		//
		// done
		//
		return linked_tags;
	}
	
	
	/**
	 * returns a list of linked files in respect to their current tag stack
	 * @return
	 */
	public static ArrayList<String> getLinkedFiles() {
		
		//
		// acquire database manager
		//
		DBManager db_man = DBManager.getInstance();

		//
		// get current tag stack
		//
		ArrayList<String> tag_stack = TagStackManager.getInstance().toArray(new String[1]);
		
		//
		// get associated files
		// m_tag_stack.toArray(new String[1])
		ArrayList<String> linked_files = db_man.getLinkedFiles(tag_stack);
		if (linked_files == null)
		{
			Logger.e("Error: FileTagUtility::getLinkedFiles linked_files null");
			return new ArrayList<String>();
		}
		
		//
		// build result list
		//
		ArrayList<String> result_list = new ArrayList<String>();
		
		for(String linked_file : linked_files)
		{
			//
			// check if file still exists
			//
			//File file = new File(linked_file);
			//if (file.exists())
			result_list.add(linked_file);
		}
		
		//
		// sort array by collections
		//
		Collections.sort(result_list, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return arg0.toLowerCase().compareTo(arg1.toLowerCase());
			}
		});
		
		//
		// done
		//
		return result_list;
	}
}
