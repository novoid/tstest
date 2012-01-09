package org.me.TagStore.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;

public class IOUtils {

	
	/**
	 * reads a file into the specified result vector
	 * @param reader buffered reader
	 * @param lines lines to read
	 * @return
	 */
	public static boolean readFile(BufferedReader reader, ArrayList<String> lines) {
		
		try
		{
			do
			{
				//
				// read line
				//
				String line = reader.readLine();
				if (line == null)
					break;

				//
				// append line
				//
				lines.add(line);

			}while(true);
			
			//
			// successfully read file
			//
			return true;
			
		}catch(IOException exc)
		{
			Logger.e("IOException while reading");
			return false;
		}
	}
	
	
	/**
	 * reads the content of a file into a result line vector
	 * @param file_name name of the file
	 * @param lines file contents
	 * @return true on success
	 */
	public static boolean readFile(String file_name, ArrayList<String> lines) 
	{
		BufferedReader reader = null;
		boolean result = false;
		
		try
		{
			//
			// open input stream
			//
			FileInputStream raw_stream = new FileInputStream(file_name);
		
			//
			// create reader
			//
			reader = new BufferedReader(new InputStreamReader(raw_stream, "UTF8"));
		
			//
			// read file
			//
			result = IOUtils.readFile(reader, lines);
		}
		catch(IOException exc)
		{
			Logger.e("Exception while reading file: " + file_name);
		}
		finally
		{
			//
			// close file objects
			//
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		//
		// return result
		//
		return result;
	}	

	
	
	
	/**
	 * copys the contents of the file
	 * @param reader reader of resource file
	 * @param writer writer of the target file
	 */
	public static boolean copyFile(BufferedReader reader, BufferedWriter writer) {
		
		try
		{
			do
			{
				//
				// read line
				//
				String line = reader.readLine();
				if (line == null)
					break;
			
				//
				// write line
				//
				writer.write(line);
				writer.newLine();
				
			}while(true);
			
			//
			// done
			//
			return true;
		}
		catch(IOException exc)
		{
			//
			// exception while writing
			//
			exc.printStackTrace();
			return false;
		}
	}
	
	/**
	 * copies the resource
	 * @param context application context
	 * @param resource_name name of the resource
	 * @param target_name target file name
	 */
	public static boolean copyResource(Context context, String resource_name, File target_name) {
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		boolean result = false;
		
		try
		{
			//
			// open input stream
			//
			InputStream raw_stream = context.getAssets().open(resource_name);
		
			//
			// create reader
			//
			reader = new BufferedReader(new InputStreamReader(raw_stream, "UTF8"));
		
			//
			// create writer
			//
			writer = new BufferedWriter(new FileWriter(target_name, false));
		
			//
			// copy file
			//
			result = IOUtils.copyFile(reader, writer);
		}
		catch(IOException exc)
		{
			Logger.e("Exception while copying resource: " + resource_name);
		}
		finally
		{
			//
			// close file objects
			//
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (writer != null)
			{
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		//
		// return result
		//
		return result;
	}	
	
	
}
