package org.me.tagstore.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.me.tagstore.interfaces.StorageProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropboxStorageProvider implements StorageProvider {

	/**
	 * Dropbox application key & secret
	 */
    final static private String APP_KEY = "ad35fyo1q9o0x1w";
    final static private String APP_SECRET = "us1bjrmurvlspb5";	

    /**
     * tagstore's Dropbox access type
     */
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    
    /* Dropbox preferences name and access key */
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "3xa8vq5vclq5aqo";
    final static private String ACCESS_SECRET_NAME = "d7nst5iuwuyazz1";
    
    /**
     * single instance of dropbox api provider
     */
    private static DropboxStorageProvider s_Instance = null;
    
    /**
     * Dropbox API object
     */
    private DropboxAPI<AndroidAuthSession> mDBApi = null;
    
    /**
     * starts the authentication process
     * @param context application context
     * @return true on success, false if object were failed to be constructed
     */
    public boolean startAuthentication(Context context) {
    	
    	// build authentication session
    	AndroidAuthSession session = buildSession(context);
    	if (session == null)
    	{
    		Logger.e("failed to build Dropbox authentication session");
    		return false;
    	}
    	
    	// construct Dropbox API object
    	mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    	if (mDBApi == null)
    	{
    		Logger.e("Failed to construct Dropbox API Object");
    		return false;
    	}
    	
    	// informal debugs
    	Logger.i("Dropbox API object constructed, starting authentication");
    	
    	// if we have already authenticated, we only need to set
    	// the token pair
    	String[] keys = getKeys(context);
    	if (keys != null) {
        	
    		// we are authenticated
    		return true;
    	}
    	
    	// start authentication
    	mDBApi.getSession().startAuthentication(context);
    	return true;
    }
    
    /* (non-Javadoc)
	 * @see org.me.TagStore.core.StorageProvider#isLoggedIn()
	 */
    
	public boolean isLoggedIn() {
    	
    	if (mDBApi != null) {
    		
    		// check if we are logged in
    		return mDBApi.getSession().isLinked();
    	}

    	// we are not logged in
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see org.me.TagStore.core.StorageProvider#getFiles(java.lang.String, java.lang.String)
	 */
    
	public ArrayList<String> getFiles(String directory_path, String hash) {
    	
    	// check if the api has already been initialized
    	if (mDBApi == null)
    		return null;
    	
    	// retrieve data
    	 try {
    		 
    		 // grab latest entry
    	     Entry existingEntry = mDBApi.metadata(directory_path, 0, hash, true, null);
    	     
    	     // grab files
    	     ArrayList<String> files = new ArrayList<String>();
    	     for(Entry current_entry : existingEntry.contents)
    	     {
    	    	 files.add(current_entry.path);
    	    	 Logger.i(current_entry.path);
    	     }

    	     // done
    	     return files;
    	     
    	     
    	 } catch (DropboxException e) {
    	     System.out.println("Something went wrong: " + e);
    	 }
    	 
    	 // failed to grab files
    	 return null;
    }
    
    /* (non-Javadoc)
	 * @see org.me.TagStore.core.StorageProvider#isFile(java.lang.String)
	 */
    
	public boolean isFile(String filepath) {
    	
    	// check if the api has already been initialized
    	if (mDBApi == null)
    		return false;
    	
    	// retrieve data
    	 try {
    		 
    		 // grab latest entry
    	     Entry existingEntry = mDBApi.metadata(filepath,  1, null, false, null);
    	     
    	     // return result
    	     return !existingEntry.isDir;
    	     
    	     
    	 } catch (DropboxException e) {
    	     System.out.println("Something went wrong: " + e);
    	 }

    	 // failed
    	 return false;
    	
    }
    
    
    /**
     * finishes the authentication
     * @param context application context
     * @return true on success
     */
    public boolean finishAuthentication(Context context) {
    	
    	// if we have already authenticated, we only need to check
    	// the token pair
    	String[] keys = getKeys(context);
    	if (keys != null) {
        	
    		// we are authenticated
    		return true;
    	}    	
    	
        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

                // store access keys
                storeKeys(context, tokens.key, tokens.secret);
                
                // done 
                return true;
            } catch (IllegalStateException e) {
                Logger.e("Error authenticating IllegalStateException");
            }
        }	
        else
        {
        	Logger.e("Failed to authenticate");
        }
        return false;    	
    }
    
    /* (non-Javadoc)
	 * @see org.me.TagStore.core.StorageProvider#uploadFile(java.lang.String, java.lang.String, java.lang.String)
	 */
    
	public boolean uploadFile(String filename,
    		                  String target_path) {
    	
    	FileInputStream inputStream = null;
    	try {
    		
    		// open file
    	    File file = new File(filename);
    	    inputStream = new FileInputStream(file);
    	    
    	    // get file revision
    	    String parentRev = getFileRevision(target_path);
    	    
    	    // upload file
    	    Entry newEntry = mDBApi.putFile(target_path, inputStream,
    	            file.length(), parentRev, null);
    	    
    	    Logger.i("File " + filename + " was uploaded");
    	    Logger.i("File rev: " + newEntry.rev);
    	    return true;
    	} catch (DropboxUnlinkedException e) {
    	    // User has unlinked, ask them to link again here.
    	    Logger.e("User has unlinked.");
    	    return false;
    	} catch (DropboxException e) {
    	    Logger.e("Something went wrong while uploading " + filename);
    	} catch (FileNotFoundException e) {
    	    Logger.e("File " + filename + " not found.");
    	} finally {
    	    if (inputStream != null) {
    	        try {
    	            inputStream.close();
    	        } catch (IOException e) {}
    	    }
    	}
    	// failed to upload file
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see org.me.TagStore.core.StorageProvider#downloadFile(java.lang.String, java.lang.String)
	 */
    
	public boolean downloadFile(String newfile,
    		                    String source_path) {
    	
    	FileOutputStream outputStream = null;
    	try {
    		
    		// create new file
    	    File file = new File(newfile);
    	    outputStream = new FileOutputStream(file);
    	    
    	    // download file
    	    DropboxFileInfo info = mDBApi.getFile(source_path, null, outputStream, null);
    	    Logger.i("The file's rev is: " + info.getMetadata().rev);

    	    // done
    	    return true;
    	} catch (DropboxException e) {
    	    Logger.e("Something went wrong while downloading.");
    	} catch (FileNotFoundException e) {
    	    Logger.e("File not found.");
    	} finally {
    	    if (outputStream != null) {
    	        try {
    	            outputStream.close();
    	        } catch (IOException e) {}
    	
    	    }
    	}
    	
    	// failed to download
    	return false;
    }
    
    public Date getFileModificationDate(String filename) {
    	
    	try {
    		
    		// build folder path
    		String filepath = filename;
    		
    		// get metadata
    	    Entry existingEntry = mDBApi.metadata(filepath, 1, null, false, null);

    	    // parse date
    	    return RESTUtility.parseDate(existingEntry.modified);
    	} catch (DropboxException e) {
    	    Logger.e("Something went wrong while getting metadata.");
    	}
    	return null;
    	
    	
    }
    
    
    /**
     * returns the file revision
     * @param filename target file path and file name
     * @return revision
     */
    public String getFileRevision(String filename) {
    	
    	try {
    		
    		// build folder path
    		String filepath = filename;
    		
    		// get metadata
    	    Entry existingEntry = mDBApi.metadata(filepath, 1, null, false, null);
    	    
    	    
    	    
    	    // informal debug
    	    Logger.i("The file's rev is now: " + existingEntry.rev);
    	    return existingEntry.rev;
    	} catch (DropboxException e) {
    	    Logger.e("Something went wrong while getting metadata.");
    	}
    	return null;
    }
    
    /**
     * returns the file revision
     * @param filename target file path and file name
     * @return revision
     */
    public void deleteFile(String filename) {
    	
    	try {
    		
    		// get delete file
    	    mDBApi.delete(filename);
    	} catch (DropboxException e) {
    	    Logger.e("Something went wrong while getting metadata.");
    	}
    }
    
    
    /**
     * stores the access keys returned from the Trusted Authenticator
     * @param context application context
     * @param key access key
     * @param secret access secret
     */
    private void storeKeys(Context context, String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    /**
     * clears the keys stored
     * @param context application context
     */
    private void clearKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }
    
    /**
     * constructs an AndroidAuthSession
     * @param context
     * @return
     */
    private AndroidAuthSession buildSession(Context context) {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys(context);
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }

    /* (non-Javadoc)
	 * @see org.me.TagStore.core.StorageProvider#unlink(android.content.Context)
	 */
	
	public void unlink(Context context) {

		if (mDBApi != null)
		{
			// close session
			mDBApi.getSession().unlink();
			
			// remove keys
			clearKeys(context);
		}
	}

	public static DropboxStorageProvider getInstance() {

		if (s_Instance == null) {
			
			// build new instance
			s_Instance = new DropboxStorageProvider();
		}
		
		// done
		return s_Instance;

	}
	
	private DropboxStorageProvider() {
		
	}

	
	public long getFileSize(String filepath) {

    	try {
    		
    		// get metadata
    	    Entry existingEntry = mDBApi.metadata(filepath, 1, null, false, null);
    	    return existingEntry.bytes;
    	} catch (DropboxException e) {
    	    Logger.e("Something went wrong while getting metadata.");
    	}
    	return -1;		
	}
}
