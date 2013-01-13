package org.me.tagstore.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.me.tagstore.R;
import org.me.tagstore.ui.ToastManager;

import android.webkit.MimeTypeMap;
import android.widget.EditText;

/**
 * this class is responsible to provide helper functions which deal parsing of
 * tags and adding / deleting files and their associated data to the database
 * 
 */
public class FileTagUtility {

	private DBManager m_db_man;
	private SyncFileWriter m_file_writer;
	private TagValidator m_validator;
	private ToastManager m_toast_man;
	private VocabularyManager m_voc_manager;
	private TimeFormatUtility m_time_format;

	/**
	 * initializes the file tag utility
	 * 
	 * @param db_man
	 */
	public void initializeFileTagUtility(DBManager db_man,
			SyncFileWriter file_writer, TagValidator tag_validator,
			ToastManager toast_man, VocabularyManager voc_man, TimeFormatUtility time_format) {
		m_db_man = db_man;
		m_file_writer = file_writer;
		m_validator = tag_validator;
		m_toast_man = toast_man;
		m_voc_manager = voc_man;
		m_time_format = time_format;
	}
	
	/**
	 * this function processes the associated tags. It will create tag entry if
	 * the tag is new, or update the reference count of existing tags
	 * 
	 * @param file_name
	 *            path of the file
	 * @param tag_text
	 *            string containing the tags
	 * @return
	 */
	private boolean processTags(String file_name, String tag_text) {

		//
		// get file id
		//
		long file_id = m_db_man.getFileId(file_name);
		if (file_id < 0) {
			Logger.e("Error FileTagUtility::processTags failed to get file id for file "
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
		HashMap<String, Integer> tag_reference = m_db_man
				.getTagReferenceCount();

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
					m_db_man.addTag(current_tag);
				} else {
					//
					// adds reference to tag
					//
					int reference_count = tag_reference.get(current_tag)
							.intValue() + 1;
					Logger.i("AddFileTagActivity::processTags tag: "
							+ current_tag + " new reference count: "
							+ reference_count);

					m_db_man.setTagReference(current_tag, reference_count);
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
				m_db_man.addTag(current_tag);
			}

			//
			// now get tag id
			//
			long tag_id = m_db_man.getTagId(current_tag);
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
			if (!m_db_man.addFileTagMapping(file_id, tag_id))
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
	public Set<String> splitTagText(String tag_text) {

		//
		// split the tag_text
		//
		StringTokenizer tokenizer = new StringTokenizer(tag_text,
				ConfigurationSettings.TAG_DELIMITER);

		//
		// create hash map to store the tags in order to remove duplicates
		//
		Set<String> tag_list = new HashSet<String>();

		//
		// add tokens to list
		//
		while (tokenizer.hasMoreTokens()) {
			//
			// get current token
			//
			String current_tag = tokenizer.nextToken();

			//
			// trim whitespaces
			//
			current_tag = current_tag.trim();

			if (current_tag.length() != 0) {
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
	 * 
	 * @param tag_text
	 */
	private boolean addFileToDB(String file_name, String tag_text) {

		String mime_type = "";

		//
		// generate hashsum
		//
		String hash_sum = "NOHASHSUMGENEREATED"; // FileHashsumGenerator.generateFileHashsum(ctx,
													// file_name);

		//
		// get time stamp in UTC
		//
		String create_date = m_time_format.getCurrentTimeInUTC();

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
				if (mime_map.getMimeTypeFromExtension(extension) != null) {
					mime_type = mime_map.getMimeTypeFromExtension(extension);
				}
			}
		}

		//
		// let's add the file
		//
		return addFileWithProperties(file_name, tag_text, hash_sum,
				create_date, mime_type);
	}

	/**
	 * adds a file with the following properties to the tagstore
	 * 
	 * @param file_name
	 *            path of the file
	 * @param tag_text
	 *            tags associated
	 * @param hash_sum
	 *            hash sum of the file
	 * @param create_date
	 *            create date of the file
	 * @param mime_type
	 *            mime type of the file
	 * @return true on success
	 */
	private boolean addFileWithProperties(String file_name, String tag_text,
			String hash_sum, String create_date, String mime_type) {
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
		long result = m_db_man.addFile(file_name, mime_type, create_date,
				hash_sum);

		//
		// result
		//
		Logger.i("AddFileTagActivity::addFileToDB> result: " + result);

		//
		// now remove pending file
		//
		m_db_man.removePendingFile(file_name);

		//
		// done
		//
		return true;
	}

	/**
	 * adds a file to the database
	 * 
	 * @param file_name
	 *            path of file
	 * @param write_log
	 *            true if to the log should be written
	 * @param tag_text
	 *            tags of the file
	 */
	public boolean tagFile(String file_name, String tag_text, boolean write_log) {

		//
		// check if file exists
		//
		File file = new File(file_name);
		if (!file.exists()) {
			//
			// display error toast
			//
			m_toast_man.displayToastWithFormat(
					R.string.error_format_file_removed, file_name);

			//
			// done
			//
			Logger.e("File " + file.getPath() + " no longer exists");
			return false;
		}

		//
		// first add the file
		//
		if (!addFileToDB(file_name, tag_text)) {

			//
			// display error toast
			//
			m_toast_man.displayToastWithFormat(R.string.error_format_add_file,
					file_name);

			//
			// done
			//
			Logger.e("addFileToDB " + file.getPath() + " failed");
			return false;
		}

		//
		// now process the tags
		//
		if (!processTags(file_name, tag_text)) {

			//
			// display error toast
			//
			m_toast_man.displayToastWithFormat(R.string.error_format_add_file,
					file_name);

			//
			// remove entry
			//
			m_db_man.removeFile(file_name);

			//
			// done
			//
			Logger.e("processTags " + file.getPath() + " failed");
			return false;
		}

		if (write_log) {
			//
			// write entries
			//
			m_file_writer.writeTagstoreFiles();
		}

		//
		// completed
		//
		return true;
	}

	/**
	 * removes a tag from the tag store
	 * 
	 * @param tagname
	 */
	public void removeTag(String tagname) {

		//
		// remove tag
		//
		m_db_man.deleteTag(tagname);

		//
		// write entries
		//
		m_file_writer.writeTagstoreFiles();
	}

	/**
	 * removes a file from the database and deletes the file from disk and
	 * displays a toast if the operation has been successful
	 * 
	 * @param file_name
	 *            path of the file to be deleted
	 * @param display_toast
	 *            if a toast should be displayed
	 */
	public boolean removeFile(String file_name, boolean display_toast) {

		//
		// FIXME: should display confirmation dialog
		//
		File file = new File(file_name);

		//
		// remove file if pending
		//
		m_db_man.removePendingFile(file_name);

		//
		// remove file
		//
		m_db_man.removeFile(file_name);

		//
		// delete file
		//
		boolean file_deleted = file.delete();

		//
		// check for success
		//
		if (file_deleted) {

			//
			// write entries
			//
			m_file_writer.writeTagstoreFiles();

			if (display_toast) {
				//
				// display toast
				//
				m_toast_man.displayToastWithFormat(R.string.format_delete,
						file_name);
			}

		} else {

			if (display_toast) {
				//
				// display toast
				//
				m_toast_man.displayToastWithFormat(
						R.string.error_format_delete, file_name);
			}
		}
		return file_deleted;

	}

	/**
	 * This function compares the entered tag line
	 * 
	 * @param tags
	 *            to be checked
	 * @return true
	 */
	public boolean validateTags(String tags, EditText edit_text) {

		//
		// first split the tags
		//
		Set<String> tag_list = splitTagText(tags);
		if (tag_list.isEmpty()) {
			//
			// must be at least one tag
			//
			m_toast_man.displayToastWithString(R.string.one_tag_minimum);

			//
			// failed
			//
			return false;
		}

		//
		// now check that all tags are valid
		//
		for (String current_tag : tag_list) {

			if (m_validator.isReservedKeyword(current_tag)) {
				//
				// reserved keyword
				//
				m_toast_man.displayToastWithString(R.string.reserved_keyword);

				if (edit_text != null) {
					//
					// get position
					//
					int position = tags.indexOf(current_tag);
					if (position >= 0) {
						//
						// mark selected position
						//
						edit_text.setSelection(position,
								position + current_tag.length());
					}
				}

				return false;
			}
		}

		//
		// now check if the controlled vocabulary is enabled
		//
		boolean controlled_vocabulary = m_voc_manager
				.getControlledVocabularyState();
		if (!controlled_vocabulary) {
			//
			// controlled vocabulary is not enabled, tags are o.k.
			//
			return true;
		}

		for (String current_tag : tag_list) {
			if (!m_voc_manager.isTagPartOfControlledVocabulary(current_tag)) {
				//
				// tag not part of controlled vocabulary
				//
				m_toast_man.displayToastWithString(
						R.string.tag_not_in_controlled_vocabulary);
				if (edit_text != null) {
					//
					// get position of that tag
					//
					int position = tags.indexOf(current_tag);
					if (position >= 0) {
						//
						// mark selected position
						//
						edit_text.setSelection(position,
								position + current_tag.length());
					}
				}

				//
				// failed
				//
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
	 * 
	 * @param filename
	 *            file to be retagged
	 * @param tags
	 *            new tags for file
	 * @param write_log
	 *            true if the log should be written to
	 * @param context
	 *            context which is invoked in case of errors
	 * @return true on success
	 */
	public boolean retagFile(String filename, String tags, boolean write_log) {

		//
		// remove file from tag store
		//
		m_db_man.removeFile(filename);

		//
		// HACK: mark file as pending
		//
		m_db_man.addPendingFile(filename);

		//
		// re-add them to the store
		//
		return tagFile(filename, tags, write_log);
	}

	/**
	 * renames a file in the databases and then actually renames it in the
	 * filesystem
	 * 
	 * @param old_file_name
	 * @param new_file_name
	 * @return
	 */
	public boolean renameFile(String old_file_name, String new_file_name) {

		//
		// create file objs for renaming
		//
		File file = new File(old_file_name);
		if (!file.exists()) {
			Logger.e("Error: the file " + old_file_name + " no longer exists");

			//
			// lets remove it from tagstore
			//
			m_db_man.removeFile(old_file_name);

			//
			// write entries
			//
			m_file_writer.writeTagstoreFiles();

			//
			// failed
			//
			return false;
		}

		//
		// construct new file name obj
		//
		File new_file_obj = new File(new_file_name);
		if (new_file_obj.exists()) {
			//
			// there is already a file with that name present
			//
			Logger.e("Error: can't rename file to " + new_file_name
					+ " because it already exists");
			return false;
		}

		//
		// now update the database
		//
		m_db_man.renameFile(old_file_name, new_file_name);

		//
		// now rename the file
		//
		boolean renamed_file = file.renameTo(new_file_obj);

		if (renamed_file) {
			//
			// write entries
			//
			m_file_writer.writeTagstoreFiles();
		}

		//
		// done
		//
		return renamed_file;
	}

	/**
	 * returns true when the file name is already taken
	 * 
	 * @param new_file_name
	 *            new file name to check for uniqueness
	 * @return true when filename is already consumed
	 */
	public boolean isFilenameAlreadyTaken(String new_file_name) {

		//
		// get list of file paths which have the same file name
		//
		ArrayList<String> same_files = m_db_man
				.getSimilarFilePaths(new_file_name);

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
	 * 
	 * @param tag
	 *            to be checked
	 * @return boolean
	 */
	public boolean isTagExisting(String tag) {

		//
		// get tag id of given tag
		//
		return m_db_man.getTagId(tag) != -1;

	}

	/**
	 * renames the tag
	 * 
	 * @param old_tag_name
	 *            old tag name
	 * @param new_tag_name
	 *            new tag name
	 */
	public boolean renameTag(String old_tag_name, String new_tag_name) {

		//
		// rename tag
		//
		boolean renamed_tag = m_db_man.renameTag(old_tag_name, new_tag_name);

		if (renamed_tag) {
			//
			// write entries
			//
			m_file_writer.writeTagstoreFiles();
		}

		//
		// done
		//
		return renamed_tag;
	}

	/**
	 * removes pending file from pending file list
	 * 
	 * @param current_file
	 */
	public void removePendingFile(String current_file) {

		//
		// removes pending file
		//
		m_db_man.removePendingFile(current_file);
	}

	/**
	 * returns a list of linked tags which are linked to the current tag stack
	 * 
	 * @return
	 */
	public ArrayList<String> getLinkedTags(TagStackManager tag_man) {

		//
		// get current tag stack
		//
		ArrayList<String> tag_stack = tag_man.toArray(new String[1]);

		//
		// get associated files
		//
		ArrayList<String> linked_tags = m_db_man.getLinkedTags(tag_stack);
		if (linked_tags == null) {
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
	 * 
	 * @return
	 */
	public ArrayList<String> getLinkedFiles(TagStackManager tag_man) {

		//
		// get current tag stack
		//
		ArrayList<String> tag_stack = tag_man.toArray(new String[1]);

		//
		// get associated files
		// m_tag_stack.toArray(new String[1])
		ArrayList<String> linked_files = m_db_man.getLinkedFiles(tag_stack);
		if (linked_files == null) {
			Logger.e("Error: FileTagUtility::getLinkedFiles linked_files null");
			return new ArrayList<String>();
		}

		//
		// build result list
		//
		ArrayList<String> result_list = new ArrayList<String>();

		for (String linked_file : linked_files) {
			//
			// check if file still exists
			//
			// File file = new File(linked_file);
			// if (file.exists())
			result_list.add(linked_file);
		}

		//
		// sort array by collections
		//
		Collections.sort(result_list, new Comparator<String>() {

			public int compare(String arg0, String arg1) {
				return arg0.toLowerCase(Locale.getDefault()).compareTo(arg1.toLowerCase(Locale.getDefault()));
			}
		});

		//
		// done
		//
		return result_list;
	}
}
