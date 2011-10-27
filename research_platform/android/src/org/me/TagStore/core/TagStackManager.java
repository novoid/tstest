package org.me.TagStore.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * This class is used to store the currently used tags. The class is used in a singleton pattern as multiple view classes access the tag stack
 * @author Johannes Anderwald
 *
 */
public class TagStackManager {

	/**
	 * sole instance of class TagStackManager
	 */
	private static TagStackManager s_Instance;
	
	
	/**
	 * stores the tags
	 */
	LinkedHashSet<String> m_tags;
	
	/**
	 * constructor of TagStackManager
	 */
	private TagStackManager() {
		
		//
		// create tag list
		//
		m_tags = new LinkedHashSet<String>();
	}

	/**
	 * this function verifies the integrity of current tag stack. If one or more tags get removed, those tags are then removed from the tag stack
	 */
	public void verifyTags() {
		
		//
		// acquire instance of Database Manager
		//
		DBManager db_man = DBManager.getInstance();
		
		//
		// get alphabetic tags
		//
		ArrayList<String> tag_list = db_man.getAlphabeticTags();
		if (tag_list == null || tag_list.isEmpty())
		{
			//
			// no more tags -> clear the tag stack
			//
			m_tags.clear();
			
			//
			// done
			//
			return;
		}
		
		//
		// check if all tags are still existing
		//
		if (tag_list.containsAll(m_tags))
		{
			//
			// tag stack is ok
			//
			return;
		}
		
		//
		// now remove all elements which are not specified in the other collection
		//
		m_tags.retainAll(tag_list);
	}
	
	
	/**
	 * This function acquires an instance of the TagStackManager
	 * @return
	 */
	public static TagStackManager getInstance() {
		
		if (s_Instance == null)
		{
			//
			// construct new instance
			//
			s_Instance = new TagStackManager();
		}
		
		//
		// done
		//
		return s_Instance;
	}
	
	/**
	 * adds the tag to the tag stack if not already present
	 * @param tag to be added
	 */
	public void addTag(String tag) {
		
		//
		// add the tag
		//
		m_tags.add(tag);
	}

	/**
	 * returns the last element in the list
	 * @return String
	 */
	public String getLastTag() {
		
		//
		// convert to array
		//
		String [] tags = m_tags.toArray(new String[1]);
		
		//
		// return last element
		//
		return tags[tags.length-1];
	}
	
	/**
	 * removes the specified tag
	 * @param tag to be removed
	 */
	public void removeTag(String tag) {
		
		//
		// remove the tag
		//
		m_tags.remove(tag);
	}
	
	/**
	 * returns true if the tag stack is empty
	 * @return boolean
	 */
	public boolean isEmpty() {
		
		return m_tags.isEmpty();
	}

	/**
	 * deletes all tags from the tag stack
	 */
	public void clearTags() {
		m_tags.clear();
	}

	/**
	 * returns true if the current tag is already contained in the tag list
	 * @param tag to be checked
	 * @return boolean
	 */
	public boolean containsTag(String tag) {
		return m_tags.contains(tag);
	}
	
	/**
	 * returns an iterator which is set to the  beginning of the tag stack
	 * @return Iterator<String>
	 */
	public Iterator<String> getIterator() {
		return m_tags.iterator();
	}
	
	/**
	 * returns the size of tag stack
	 * @return int
	 */
	public int getSize() {
		return m_tags.size();
	}

	public final ArrayList<String> toArray(String[] strings) {
		return new ArrayList<String>(Arrays.asList(m_tags.toArray(strings)));
	}
	
}
