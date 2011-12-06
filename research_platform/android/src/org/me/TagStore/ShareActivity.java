package org.me.TagStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.me.TagStore.core.DBManager;

import org.me.TagStore.core.Logger;
import org.me.TagStore.ui.StatusBarNotification;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ShareActivity extends ListActivity {

	/**
	 * stores the uri object
	 */
	private Uri m_uri;
	
	/**
	 * stores the source file name without path
	 */
	private String m_file_name = null;
	
	/**
	 * stores the source file path
	 */
	private String m_file_path = null;
	
	/**
	 * directory name key
	 */
	private static final String DIRECTORY_NAME = "DIRECTORY_NAME";

	/**
	 * stores mapping for list view
	 */
	private ArrayList<HashMap<String, Object>> m_ListViewMap;

	/**
	 * reference to database manager
	 */
	private DBManager m_db;
	
	/**
	 * status bar notification
	 */
	private StatusBarNotification m_status_bar;
	
	   public void onCreate(Bundle savedInstanceState) {

		   //
		   // call base class
		   //
		   super.onCreate(savedInstanceState);
		   
		   //
		   // set layout
		   //
		   setContentView(R.layout.share_list_view);
		   
		   //
		   // initialize database manager
		   //
		   m_db = DBManager.getInstance();
		   m_db.initialize(this);
		   
		   //
		   // build status bar
		   //
		   m_status_bar = new StatusBarNotification(getApplicationContext());
		   
		   //
		   // get intent
		   //
		   Intent intent = getIntent();
		   
		   //
		   // verify action
		   //
		   String action = intent.getAction();
		   if (!action.equals(Intent.ACTION_SEND))
		   {
			   //
			   // wrong action
			   //
			   displayToast(R.string.error_wrong_action);
			   finish();
			   return;
		   }
	   
		   //
		   // initialize list view
		   //
		   boolean result = initializeListView();
		   if (!result)
		   {
			   //
			   // no observed directories, load error message
			   //
			   displayToast(R.string.error_no_directories);
			   finish();
			   return;			   
		   }
   
		   //
		   // try #1 has the intent data associated
		   //
		   m_uri = intent.getData();
		   if (m_uri == null)
		   {
			   //
			   // get extra parameters
			   //
			   Bundle bundle = intent.getExtras();
			   if (bundle != null)
			   {
				   if (bundle.containsKey(Intent.EXTRA_STREAM))
				   {
					   //
					   // try #2 get uri from extra parameters
					   //
					   m_uri = (Uri)bundle.getParcelable(Intent.EXTRA_STREAM);
				   }
			   }
		   }
		   
		   //
		   // did we get the uri
		   //
		   if (m_uri == null)
		   {
			   //
			   // failed
			   //
			   displayToast(R.string.error_failed_retrieve_parameters);
			   finish();
			   return;				   
		   }
		   
			//
			// now resolve path
			//
			String path = resolvePath(m_uri);
			
			//
			// extract file name from path
			//
			int index = path.lastIndexOf("/");
			if (index == -1 || index == path.length()-1)
			{
				//
				// failed to retrieve file name
				//
				 displayToast(R.string.error_failed_retrieve_parameters);
				 finish();
				 return;				
			}
		   
			//
			// check if the file path is valid
			// some apps like ES Explorer use a private content resolver for particular file types
			// which results in a messed path i.e content://com.package.name/instanceid/path/to/file
			//
			File file = new File(path);
			if (file.exists())
			{
				//
				// store file name
				//
				m_file_path = path;
				
				//
				// check if the file is already pending
				// 
				if (m_db.isPendingFile(path))
				{
					//
					// notify user that the file is already added but not yet tagged
					//
					displayToast(R.string.file_present_tagstore_not_tagged);
					finish();
					return;
					
				}else if (m_db.getFileId(m_file_path) != -1)
				{
					//
					// notify user that the file is already shared
					//
					displayToast(R.string.file_present_tagstore);
					finish();
					return;
				}
				else if (isFileInObservedDirectory(path))
				{
					//
					// the file is in a observed directory
					// just add it
					//
					m_db.addPendingFile(path);
					
					//
					// check if status bar notifications are enabled
					//
					if (m_status_bar.isStatusBarNotificationEnabled())
					{
						//
						// add status bar notification
						//
						m_status_bar.addStatusBarNotification(path);
						
						//
						// done
						//
						finish();
						return;
					}
				}
				
			}
			
			//
			// store file name
			//
			m_file_name = path.substring(index+1);
	   }

	   /**
	    * checks if the file is in the observed directory list
	    * @param path path to check
	    * @return true when it is in a observed directory list
	    */
	   private boolean isFileInObservedDirectory(String path) {
		   
		   //
		   // get parent directory
		   //
		   String parent_directory = new File(path).getParent();
		   if (parent_directory == null)
			   return false;
		   
		   //
		   // get observed directories
		   //
		   ArrayList<String> directories = m_db.getDirectories();
		   for(String directory : directories)
		   {
			   if (directory.equals(parent_directory))
				   return true;
		   }
		   
		   //
		   // not in observed list
		   //
		   return false;
	   }
	   
	   
	   /**
	    * displays toast
	    * @param id
	    */
	   private void displayToast(int id) {
		   
		   //
		   // failed, load localized error message
		   //
		   String message = getString(id);
		   
		   //
		   // display toast
		   //
		   Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		   toast.show();
	   }
	   
	   
	   /**
	    * initialize the list view
	    * @return true on success
	    */
	   private boolean initializeListView() {
		   
		   //
		   // get list of directories
		   //
		   ArrayList<String> directories = m_db.getDirectories();
		   if (directories.size() == 0)
		   {
			   //
			   // no observed directories
			   //
			   return false;
		   }
		   
			//
			// construct file map
			//
			m_ListViewMap = new ArrayList<HashMap<String, Object>>();
		   
			//
			// now construct adapter for list view
			//
			SimpleAdapter adapter = new SimpleAdapter(this, m_ListViewMap,
					R.layout.share_list_view_row, new String[] {
							DIRECTORY_NAME}, new int[] {
							R.id.directory_name_text});	
		   
			//
			// add all directories
			//
			for(String directory : directories)
			{
				//
				// construct new hash map entry
				//
				HashMap<String, Object> map_entry = new HashMap<String, Object>();

				//
				// store directory name and path in there
				//
				map_entry.put(DIRECTORY_NAME, directory);
				Logger.e("dir: " + directory);
				//
				// add map entry to list view map
				//
				m_ListViewMap.add(map_entry);
			}
			
			//
			// notify data changed
			//
			adapter.notifyDataSetChanged();

			//
			// set list adapter to list view
			//
			setListAdapter(adapter);		
			
			
			//
			// done
			//
			return true;
	   }
	   
		@Override
		protected void onListItemClick(ListView listView, View view, int position,
				long id) {
			
			//
			// get map entry
			//
			HashMap<String, Object> map_entry = m_ListViewMap.get(position);

			//
			// get current directory
			//
			String current_directory = (String) map_entry.get(DIRECTORY_NAME);
			
			//
			// build target file name
			//
			String target_file = current_directory + File.separator + m_file_name;
			
			//
			// check if that file already exists in that file
			//
			File file = new File(target_file);
			if (file.exists())
			{
				//
				// can't save here, file already exists
				//
				displayToast(R.string.error_file_target_directory);
				return;
			}
			
			//
			// declare streams
			//
			InputStream in = null;
			OutputStream out = null;
			boolean success = false;
			
			try {
				
				//
				// get input stream from source file
				//
				in = getContentResolver().openInputStream(m_uri);
				

				//
				// build file output stream
				//
				out = new FileOutputStream(file);
				
				//
				// allocate buffer 
				//
				byte buf[]=new byte[1024];
				
				//
				// copy file
				//
				int len;
				while ((len = in.read(buf)) != -1) 
				{
				    out.write(buf, 0, len);
				}

				//
				// close streams
				//
				in.close();
				out.close();
				success = true;
				
			} catch (FileNotFoundException e) {

				//
				// the file was deleted in between
				//
				displayToast(R.string.error_file_removed);
				return;
				
			}catch(IOException exc)
			{
				//
				// exception while accessing
				//
			}finally
			{
				//
				// cleanup streams
				//
				try
				{
					if (in != null)
						in.close();
				}catch(IOException exc)
				{
					Logger.e("exception while closing");
				}
				
				try
				{
					if (out != null)
						out.close();
				}catch(IOException exc)
				{
					Logger.e("exception while closing");					
				}
			}
			
			if (success)
				displayToast(R.string.share_success);
			else
				displayToast(R.string.error_failed_share);
			
			//
			// exit
			//
			finish();
		}
	   
		/**
		 * resolves path of an uri from an content provider
		 * @param content_uri uri content
		 * @param column_name name of the column
		 * @return path of media item
		 */
		private String resolvePathWithColumn(Uri content_uri, String column_name) {
			
			//
			// build column
			//
			String[] columns = new String[]{column_name};

			//
			// get cursor to dataset
			//
	        Cursor cursor = managedQuery(content_uri, columns, null, null, null);
	        if (cursor == null)
	        	return null;
			
	        
	        //
	        // is there a result set
	        //
	        if (!cursor.moveToFirst())
	        	return null;
	        
	        //
	        // resolve column index
	        //
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        
	        //
	        // get path
	        //
	        String path = cursor.getString(column_index);
	        
	        //
	        // close cursor
	        //
	        cursor.close();
	        
	        //
	        // done
	        //
	        return path;
		}
		
		/**
		 * resolves the path
		 * @param content_uri content uri
		 * @return path of file
		 */
		private String resolvePath(Uri content_uri)
		{
			//
			// get intent type
			//
			String type = getIntent().getType().toLowerCase();
			
			String path = null;
			if (type.startsWith("image"))
			{
				path = resolvePathWithColumn(content_uri, MediaStore.Images.Media.DATA);
			}
			else if (type.startsWith("audio"))
			{
				path = resolvePathWithColumn(content_uri, MediaStore.Audio.Media.DATA);
			}
			else if (type.startsWith("video"))
			{
				path = resolvePathWithColumn(content_uri, MediaStore.Video.Media.DATA);
			}
			
			if(path == null)
			{
				//
				// failed to resolve by quering content providers
				// try directly quering it
				//
				path = content_uri.getPath();
			}

			//
			// done
			//
			return path;
		}
		
		
		
		
}
