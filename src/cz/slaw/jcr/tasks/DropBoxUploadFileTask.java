package cz.slaw.jcr.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import android.os.AsyncTask;

public class DropBoxUploadFileTask extends AsyncTask<File, Void, Boolean> {

	private static final Logger log = LoggerFactory.getLogger(DropBoxUploadFileTask.class);
	
	private DropboxAPI<AndroidAuthSession> dropboxApi;
	private String path;

	public DropBoxUploadFileTask(DropboxAPI<AndroidAuthSession> dropboxApi, String path) {
		this.dropboxApi = dropboxApi;
		this.path = path;
	}

	@Override
	protected Boolean doInBackground(File... params) {
		

		try {
			File file2upload = params[0];
			FileInputStream fileInputStream = new FileInputStream(file2upload);
			dropboxApi.putFile(path+file2upload.getName(), fileInputStream, file2upload.length(), null, null);
			return true;
		} catch (IOException e) {
			log.error("err", e);
		} catch (DropboxException e) {
			log.error("err", e);
		}

		return false;
	}

//	@Override
//	protected void onPostExecute(Boolean result) {
//		if (result) {
//			Toast.makeText(context, "File has been successfully uploaded!", Toast.LENGTH_LONG).show();
//		} else {
//			Toast.makeText(context, "An error occured while processing the upload request.", Toast.LENGTH_LONG).show();
//		}
//	}
}
