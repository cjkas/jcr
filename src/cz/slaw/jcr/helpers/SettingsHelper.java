package cz.slaw.jcr.helpers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

public class SettingsHelper {
	public static final String CALL_RECORDING_NOTIFY = "notifications_new_record";
	public static final String SOUND_SOURCE = "sound_source";
	public static final String FILE_TYPE = "file_type";
	public static final String INCREASE_VOLUME = "increase_volume";
	public static final String RECORDS_SIZE = "records_size";
	
	public static final String RECORD_DESTINATION = "record_destination";
	public static final String RECORD_DESTINATION_DEF = new File(Environment.getExternalStorageDirectory(), "jcr").getAbsolutePath();
	public static final String RECORD_DESTINATION_RESTORE = "record_destination_restore";
	
	public static final String IGNORED_CONTACTS = "ignore_contacts";
	public static final String RECORD_CONTACTS = "record_contacts";
	public static final String RECORDING_ENABLED_NOTIFIY = "recording_notification";
	public static final String ENABLE_RECORDING = "enable_recording";
	public static final String SHAKE = "record_by_shake";
	public static final String PERSISTENT_CONTACTS = "persistent_contacts";
	public static final String PREF_DROPBOX = "pref_dropbox";
	public static final String PREF_GDRIVE = "pref_gdrive";
	public static final String PREF_GDRIVE_ID = "pref_gdrive_id";
	public static final String PREF_WIFI_ONLY = "wifi_only";
	public static final String PREF_RECORD_OR_IGNORE = "record_or_ignore";
	public static final String PREF_RECORD_OR_IGNORE_DEFIGNORE = "ignore";
	public static final String PREF_LANG = "language";
	public static final String PREF_LANG_DEF = "sys";
	public static final String PREF_ABOUT = "pref_about_app";
	public static final String PREF_BUY_PRO = "pref_buy_pro";
	public static final String PREF_FLOAT_BTN= "pref_use_float_btn";
	private static final String PREF_PRO= "pref_pro_version";
	public static final String PURCH_KEY= "jcr_pro";
	
	public static File getStorageDir(Context ctx){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		String destPath = sharedPref.getString(SettingsHelper.RECORD_DESTINATION, SettingsHelper.RECORD_DESTINATION_DEF);
		
		File dir=new File(destPath);
		
		return dir;
	}
	public static boolean isModeIgnore(Context ctx){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		String mode = sharedPref.getString(SettingsHelper.PREF_RECORD_OR_IGNORE, PREF_RECORD_OR_IGNORE_DEFIGNORE);
		if(PREF_RECORD_OR_IGNORE_DEFIGNORE.equals(mode)){
			return true;
		}
		return false;
	}
	
	public static boolean isPremium(Context ctx){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean premium = sharedPref.getBoolean(SettingsHelper.PREF_PRO, false);
		
		return premium;
	}
	public static void setPremium(Context ctx,boolean val) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = sharedPref.edit();
		edit.putBoolean(PREF_PRO, val);
		edit.commit();
	}

	public static boolean isRecordingEnabled(Context ctx){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean dropbox = sharedPref.getBoolean(SettingsHelper.ENABLE_RECORDING, false);
		
		return dropbox;
	}

	public static boolean isGdrive(Context ctx){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean dropbox = sharedPref.getBoolean(SettingsHelper.PREF_GDRIVE, false);
		
		return dropbox;
	}
	public static boolean isDropbox(Context ctx){
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean dropbox = sharedPref.getBoolean(SettingsHelper.PREF_DROPBOX, false);
		
		return dropbox;
	}
	public static void setGdriveFolderId(Context ctx,String driveId) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = sharedPref.edit();
		edit.putString(PREF_GDRIVE_ID, driveId);
		edit.commit();
	}
	public static String getGdriveFolderId(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		String string = sharedPref.getString(PREF_GDRIVE_ID, null);
		return string;
	}
	public static boolean isFloatingBtn(Context ctx){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean r = sharedPref.getBoolean(SettingsHelper.PREF_FLOAT_BTN, false);
		return r;
	}
	public static boolean isShake(Context ctx){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean r = sharedPref.getBoolean(SettingsHelper.SHAKE, false);
		return r;
	}
	public static boolean onlyWifi(Context ctx){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean r = sharedPref.getBoolean(SettingsHelper.PREF_WIFI_ONLY, true);
		return r;
	}
	public static int getFileLimit(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		int r = sharedPref.getInt(SettingsHelper.RECORDS_SIZE, 40);
		return r;
	}
	
	public static String getFileType(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		String r = sharedPref.getString(SettingsHelper.FILE_TYPE, "amr");
		return r;
	}
	public static Set<String> getRecordContacts(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		Set<String> r = sharedPref.getStringSet(SettingsHelper.RECORD_CONTACTS, new HashSet<String>());
		return r;
	}
	public static Set<String> getIgnoredContacts(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		Set<String> r = sharedPref.getStringSet(SettingsHelper.IGNORED_CONTACTS, new HashSet<String>());
		return r;
	}
	public static String getSoundSrc(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		String r = sharedPref.getString(SettingsHelper.SOUND_SOURCE, "in_and_out");
		return r;
	}
	
	public static boolean isIncreaseVol(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		boolean r = sharedPref.getBoolean(SettingsHelper.INCREASE_VOLUME, true);
		return r;
	}
	public static Set<String> getPersistentContacts(Context ctx) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		Set<String> r = sharedPref.getStringSet(SettingsHelper.PERSISTENT_CONTACTS, new HashSet<String>());
		return r;
	}
	
}
