package cz.slaw.jcr.helpers;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.tasks.DropBoxUploadFileTask;

public class DropBoxHelper {

	private static final Logger log = LoggerFactory.getLogger(DropBoxHelper.class);
	
	public final static String DROPBOX_FILE_DIR = "/";
	private final static String ACCESS_KEY = "4fm1y1621bjb683";
	private final static String ACCESS_SECRET = "gntjkz60tqf9013";
	private final static AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	private final Context context;
	private DropboxAPI<AndroidAuthSession> dropboxApi;

	public DropBoxHelper(Context ctx) {
		this.context = ctx;
	}

	public void connect() {
		AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
		AndroidAuthSession session;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String key = prefs.getString(ACCESS_KEY, null);
		String secret = prefs.getString(ACCESS_SECRET, null);

		if (key != null && secret != null) {
			log.debug("dp auth remembered");
			AccessTokenPair token = new AccessTokenPair(key, secret);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, token);
		} else {
			log.debug("dp auth new ");
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		dropboxApi = new DropboxAPI<AndroidAuthSession>(session);
	}

	public boolean isConnected() {
		AndroidAuthSession session = dropboxApi.getSession();
		log.debug("res dp auth " + session.authenticationSuccessful() + ":" + session.isLinked());
		if (session.isLinked()) {
			return loggedOk();
		} else if (session.authenticationSuccessful()) {
			try {
				session.finishAuthentication();
				return loggedOk();
			} catch (IllegalStateException e) {
				log.error( "ise", e);
				throw e;
			}
		}else{
			return loggedNo();
		}
	}
	private boolean loggedNo() {
		loggedIn(false);
		return false;
	}
	private boolean loggedOk() {
		loggedIn(true);
		return true;
	}

	private void loggedIn(boolean userLoggedIn) {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = sharedPrefs.edit();
		
		
		if (!userLoggedIn) {
			edit.remove(SettingsHelper.PREF_DROPBOX);
			edit.remove(ACCESS_KEY);
			edit.remove(ACCESS_SECRET);
		}else{
			TokenPair tokens = dropboxApi.getSession().getAccessTokenPair();
			
			edit.putBoolean(SettingsHelper.PREF_DROPBOX, true);
			edit.putString(ACCESS_KEY, tokens.key);
			edit.putString(ACCESS_SECRET, tokens.secret);
		}
		
		edit.commit();
	}

	public void logout() {
		end();		
		loggedIn(false);
	}

	public void upload(DbRecord dbRecord) {
		DropBoxUploadFileTask task = new DropBoxUploadFileTask(dropboxApi, DROPBOX_FILE_DIR);
		File file = new File(SettingsHelper.getStorageDir(context),dbRecord.getPath());
		task.execute(file);
	}

	public void login() {
		dropboxApi.getSession().startAuthentication(context);
	}

	public void end() {
		dropboxApi.getSession().unlink();
	}
}
