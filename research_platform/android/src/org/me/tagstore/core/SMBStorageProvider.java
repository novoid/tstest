package org.me.tagstore.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;

import org.me.tagstore.interfaces.StorageProvider;

import android.content.Context;

public class SMBStorageProvider implements StorageProvider {

	/**
	 * stores authentication
	 */
	private NtlmPasswordAuthentication m_authentication;
	
    /**
     * single instance of dropbox api provider
     */
    private static SMBStorageProvider s_Instance = null;	
	
	/**
	 * store address
	 */
    private String m_address;
    
    
	/**
	 * domain
	 */
    private UniAddress m_domain;
	
	
	public boolean isLoggedIn() {
		
		return m_authentication != null;
	}

	
	public ArrayList<String> getFiles(String directory_path, String hash) {

		ArrayList<String> list = new ArrayList<String>();
        SmbFile smb_dir;
		try {
			
			// construct target path
			String target_path = "smb://" + m_address + directory_path;
			
			// construct smb file
			smb_dir = new SmbFile(target_path, m_authentication);
			
			// retrieve all files
	        SmbFile[] files = smb_dir.listFiles();
	        
	        for(SmbFile file : files)
	        {
	            list.add(file.getName());
	        }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(SmbException e)
		{
			e.printStackTrace();
		}
		
		// done
        return list;		
		
		
	}

	
	public boolean isFile(String filepath) {

        try {
        	
			// construct target path
			String target_path = "smb://" + m_address + filepath;

        	// construct smb file
			SmbFile file = new SmbFile(target_path, m_authentication);
			
			// check if file exists
			return file.exists() && file.isFile();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch(SmbException e) {
		}
		
		// failed
		return false;
	}

	
	public boolean uploadFile(String filename, String target_file_path) {

        SmbFileOutputStream out = null;
        FileInputStream in = null;
        boolean ret = true;
		try {
			
			// construct target path
			String target_path = "smb://" + m_address + target_file_path;
			Logger.i("uploadFile path: " + target_path);

			// create smb file
			SmbFile target_file = new SmbFile(target_path, m_authentication);
			
			// create file
			target_file.createNewFile();
			
			// construct file output stream
            out = new SmbFileOutputStream(target_file);

            // construct file input stream
            in = new FileInputStream(new File(filename));
            
            int length;
            byte[] buffer = new byte[4096];
            
            
            // read input buffer
            while ((length = in.read(buffer)) != StreamTokenizer.TT_EOF)
            {
            	// write output buffer
            	out.write(buffer, 0, length);
            }
		}
        catch (MalformedURLException e)
        {
                e.printStackTrace();
                ret = false;
        }
        catch (UnknownHostException e)
        {
                e.printStackTrace();
                ret = false;                
        }
        catch (SmbException e)
        {
                e.printStackTrace();
                ret = false;                
        }
        catch (IOException e)
        {
                e.printStackTrace();
                ret = false;                
        }
        finally
        {
        	if (in != null)
        	{
        		try
        		{
        			in.close();
        		}
        		catch(IOException e) {
        			
        		}
        	}

        	if (out != null)
        	{
        		try
        		{
        			out.close();
        		}
        		catch(Exception e) {
        			
        		}
        	}
        	
        }

		return ret;
	}

	
	public boolean downloadFile(String newfile, String source_file_path) {

	     	SmbFileInputStream in = null;
	        FileOutputStream out = null;
	        boolean ret = true;
			try {
				
				// construct target path
				String source_path = "smb://" + m_address + source_file_path;

				
				// create smb file
				SmbFile target_file = new SmbFile(source_path, m_authentication);
				
				// construct file output stream
	            in = new SmbFileInputStream(target_file);

	            // construct file input stream
	            out = new FileOutputStream(new File(newfile));
	            
	            int length;
	            byte[] buffer = new byte[4096];
	            
	            
	            // read input buffer
	            while ((length = in.read(buffer)) != StreamTokenizer.TT_EOF)
	            {
	            	// write output buffer
	            	out.write(buffer, 0, length);
	            }
			}
	        catch (MalformedURLException e)
	        {
	                e.printStackTrace();
	                ret = false;
	        }
	        catch (UnknownHostException e)
	        {
	                e.printStackTrace();
	                ret = false;                
	        }
	        catch (SmbException e)
	        {
	                e.printStackTrace();
	                ret = false;                
	        }
	        catch (IOException e)
	        {
	                e.printStackTrace();
	                ret = false;                
	        }
	        finally
	        {
	        	if (in != null)
	        	{
	        		try
	        		{
	        			in.close();
	        		}
	        		catch(Exception e) {
	        			
	        		}
	        	}

	        	if (out != null)
	        	{
	        		try
	        		{
	        			out.close();
	        		}
	        		catch(Exception e) {
	        			
	        		}
	        	}
	        	
	        }

			return ret;		
	}

	
	public void unlink(Context context) {
		
		// reset authentication
		m_authentication = null;
	}

	
	public long getFileSize(String filepath) {

        try {
        	
			// construct target path
			String target_path = "smb://" + m_address + filepath;
        	
			// construct smb file
			SmbFile file = new SmbFile(target_path, m_authentication);
			return file.length();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch(SmbException e) {
		}
		
		return -1;
	}

	
	public void deleteFile(String file_path) {

        try {
        	
			// construct target path
			String target_path = "smb://" + m_address + file_path;

			// construct file
			SmbFile file = new SmbFile(target_path, m_authentication);
			file.delete();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch(SmbException e) {
		}
	}

	
	public String getFileRevision(String file_path) {
		// not supported
		return null;
	}

	
	public Date getFileModificationDate(String file_path) {
		
        try {
        	
			// construct target path
			String target_path = "smb://" + m_address + file_path;
        	
        	// construct smb file
			SmbFile file = new SmbFile(target_path, m_authentication);
			return new Date(file.lastModified());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch(SmbException e) {
		}
		return null;
	}

    /**
    *
    * @param address
    * @param username
    * @param password
    * @throws java.lang.Exception
    */
   public boolean login(String address, String username, String password) throws Exception
   {

	   try
	   {
		   // create domain controller address
		   m_domain = UniAddress.getByName(address);

		   // setup authentication
		   m_authentication = new NtlmPasswordAuthentication(address, username, password);
		   
		   // login
		   SmbSession.logon(m_domain, m_authentication);
		   
		   // store address
		   m_address = address;
		   
		   // done
		   return true;
	   }
	   catch(SmbException e) {
		   e.printStackTrace();
	   }
	   catch(UnknownHostException e) {
		   e.printStackTrace();
	   }

	   // clear authentication
	   m_authentication = null;
	   
	   // failed
	   return false;
   }

   private SMBStorageProvider() {
	   
	   // set properties
	   jcifs.Config.setProperty("jcifs.encoding", "Cp1252");
	   jcifs.Config.setProperty("jcifs.smb.lmCompatibility", "0");
	   jcifs.Config.setProperty("jcifs.netbios.hostname", "AndroidPhone");
			
	   // register smb url handler
	   jcifs.Config.registerSmbURLHandler();  
	   
   }
   
	public static StorageProvider getInstance() {

		if (s_Instance == null) {
			
			// build new instance
			s_Instance = new SMBStorageProvider();
		}
		
		// done
		return s_Instance;

	}
}
