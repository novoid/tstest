package org.me.TagStore;

import java.util.StringTokenizer;

/**
 * This class checks if the supplied tag contains any not valid characters or uses reserved strings
 * @author Johannes Anderwald
 *
 */
public class TagValidator {
	
	private final static String DEFAULT_NOT_ALLOWED_CHARS = "/,?,<,>,\\,:,*,|,\"";
	private final static String DELIMITER =",";
    private final static String DEFAULT_NOT_ALLOWED_STRINGS = "com1,com2,com3,com4,com5,com6,com7,com8,com9,lpt1,lpt2,lpt3,lpt4,lpt5,lpt6,lpt7,lpt8,lpt9,con,nul,prn";

    /**
     * This function verifies that the provided tag does not contain any reserved characters
     * @param tag to be checked
     * @return true if it contains reserved characters
     */
    public static boolean containsReservedCharacters(String tag) {
    	
    	//
    	// construct tokenizer
    	//
    	StringTokenizer tokenizer = new StringTokenizer(DEFAULT_NOT_ALLOWED_CHARS, DELIMITER);
    	
    	while(tokenizer.hasMoreTokens())
    	{
    		//
    		// get reserved token
    		//
    		String reserved_character = tokenizer.nextToken();
    		
    		//
    		// is it contained in the tag
    		//
    		if (tag.contains(reserved_character))
    		{
    			//
    			// found reserved character
    			//
    			return true;
    		}
    	}

    	//
    	// no reserved characters found
    	//
    	return false;
    }

    /**
     * returns true when a tag is a special keyword
     * @param tag
     * @return
     */
    public static boolean isReservedKeyword(String tag) {
    	
    	//
    	// construct string tokenizer
    	//
    	StringTokenizer tokenizer = new StringTokenizer(DEFAULT_NOT_ALLOWED_STRINGS, DELIMITER);
    	
    	while(tokenizer.hasMoreTokens())
    	{
    		//
    		// get reserved token
    		//
    		String reserved_character = tokenizer.nextToken();
    		
    		//
    		// is it contained in the tag
    		//
    		if (tag.compareToIgnoreCase(reserved_character) == 0)
    		{
    			//
    			// found reserved character
    			//
    			return true;
    		}
    	}

    	//
    	// no reserved characters found
    	//
    	return false;
    }    
    
    /**
     * This function replaces all occurrences of reserved characters
     * @param tag to be checked
     * @return String without reserved characters
     */
    public static String removeReservedCharacters(String tag) {
    	
    	//
    	// construct tokenizer
    	//
    	StringTokenizer tokenizer = new StringTokenizer(DEFAULT_NOT_ALLOWED_CHARS, DELIMITER);
    	
    	while(tokenizer.hasMoreTokens())
    	{
    		//
    		// get reserved token
    		//
    		String reserved_character = tokenizer.nextToken();
    		
    		//
    		// is it contained in the tag
    		//
    		if (tag.contains(reserved_character))
    		{
    			//
    			// replace characters
    			//
    			tag = tag.replace(reserved_character, "");
    		}
    	}

    	//
    	// return replaced line
    	//
    	return tag;
    }
    
}
