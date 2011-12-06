package org.me.TagStore.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class IOUtils {
	
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
