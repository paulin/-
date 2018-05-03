package com.mop.friendflare;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

/**
 * Special class used to setup the resources involved in this application
 * 
 * @author paulin
 * 
 */
public class ResourceInstaller {

	public static final String TAG = "ResourceInstaller";
	public static final String DATABASE_NAME = "friendflare.db";
	public static final String INSTALL_DIR = "/data/data/com.mop.friendflare/";
	public static final String DB_DIR = INSTALL_DIR + "databases/";
	public static final String DB_DESTINATION = DB_DIR + DATABASE_NAME;
	

	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	

	/**
	 * True if there are no
	 * 
	 * @return
	 */
	public static boolean isDatabaseFileExisting() {
		File file = new File(DB_DESTINATION);
		return file.exists();
	}


	
	/**
	 * Creates the file and sets up the permissions correctly
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	private static File createFile(String filename) throws IOException {
		File outFile = new File(filename);
		if (outFile.exists()) {
			Log.v(TAG, "[" + filename + "] File exists, deleting it");
			outFile.delete();
		}

		Log.v(TAG, "[" + filename + "] File does not exist, attempting to create");

		if (outFile.createNewFile()) {
			Log.v(TAG, "[" + filename + "] File creation successful");
		} else {
			Log.e(TAG, "Could not create the file [" + filename + "]");
		}

		// Make sure we have permission to work on the file
		Runtime.getRuntime().exec("chmod 771 " + filename);
		return outFile;
	}

	/**
	 * Creates the dir
	 * 
	 * @param dir
	 */
	private static void createDir(String dir) {
		// Make sure the dirs exists
		File installDir = new File(dir);
		if (!installDir.exists()) {
			if (installDir.mkdir()) {
				Log.i(TAG, "Created DIR [" + dir + "]");
			} else {
				Log.e(TAG, "Could not create the DIR [" + dir + "]");
			}
		} else {
			Log.i(TAG, "DIR exists [" + dir + "]");
		}
	}

	/**
	 * Deletes the database
	 */
	public static void deleteDatabaseFile() {
		try {
			Runtime.getRuntime().exec("chmod 771 " + DB_DIR);
			File outFile = new File(DB_DESTINATION);
			if (outFile.exists()) {
				Log.v(TAG, "DB File exists, deleting it");
				outFile.delete();
			}
		} catch (IOException e) {
			Log.e(TAG, "IOException deleting old database [" + DB_DIR + "]", e);
		}
	}

}
