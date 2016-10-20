package cz.slaw.jcr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.DatabaseManager;
import cz.slaw.jcr.helpers.SettingsHelper;

public class RecordDetailActivity extends Activity {
	
	private static final Logger log = LoggerFactory.getLogger(RecordDetailActivity.class);
	
	private DbRecord dbRecord = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.record_detail);
		Bundle b = getIntent().getExtras();

		this.dbRecord = (DbRecord) b.get("record");

		log.info("showing record " + dbRecord.getContactName());

		// String name =
		// dbRecord.getContactName()!=null?dbRecord.getContactName():(dbRecord.getContactNumber()!=null?dbRecord.getContactNumber():dbRecord.getName());
		getActionBar().setTitle(dbRecord.getContactName() + ", " + dbRecord.getContactNumber());

		if (dbRecord.getStart() != null) {
			TextView created = (TextView) findViewById(R.id.ar_call_time);
			CharSequence date = DateUtils.getRelativeDateTimeString(this, dbRecord.getStart().getTime(), DateUtils.MINUTE_IN_MILLIS,
					DateUtils.WEEK_IN_MILLIS, 0);
			created.setText(date);
		}

		TextView duration = (TextView) findViewById(R.id.ar_call_duration);
		duration.setText(DateUtils.formatElapsedTime(dbRecord.getDuration()));

		ImageView directionImg = (ImageView) findViewById(R.id.ar_call_direction);
		if (dbRecord.getIncoming()) {
			directionImg.setImageResource(R.drawable.ic_in_icon);
		} else {
			directionImg.setImageResource(R.drawable.ic_out_icon);
		}
		
		File file = new File(SettingsHelper.getStorageDir(this) + dbRecord.getPath());
		
		TextView type = (TextView) findViewById(R.id.ar_type);
		type.setText(MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString()));

		TextView fsize = (TextView) findViewById(R.id.ar_file_size);

		fsize.setText(toNumInUnits(file.length()));
		
		if(dbRecord.getContactId()!=null){
			ImageView imgPerson = (ImageView) findViewById(R.id.ar_call_person);
			Bitmap photo = getContactPhoto(dbRecord.getContactId().longValue());
			if(photo!=null){
				imgPerson.setImageBitmap(photo);
			}
		}
		reloadMode();
		reloadSelects();
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (DatabaseManager.getInstance() == null)
			DatabaseManager.init(this.getApplicationContext());
		this.dbRecord = DatabaseManager.getInstance().getDbRecord(this.dbRecord.getId());
		
		reloadMode();
		reloadSelects();
		
		TableRow row = (TableRow) findViewById(R.id.ar_autoSave);
		row.setEnabled(SettingsHelper.isPremium(this));
		row.setClickable(SettingsHelper.isPremium(this));		
	}
	
	private void reloadSelects() {
		Set<String> ignored = SettingsHelper.getIgnoredContacts(this);
		TextView tv = (TextView) findViewById(R.id.ar_text_ingore);
		if(this.dbRecord.getContactId()!=null && ignored.contains(this.dbRecord.getContactId().toString())){
			tv.setText(R.string.ar_ignore_contact_rem);
		}else{
			tv.setText(R.string.ar_ignore_contact_add);	
		}
		
		Set<String> record = SettingsHelper.getRecordContacts(this);
		TextView tv2 = (TextView) findViewById(R.id.ar_text_record);
		if(this.dbRecord.getContactId()!=null && record.contains(this.dbRecord.getContactId().toString())){
			tv2.setText(R.string.ar_record_contact_rem);
		}else{
			tv2.setText(R.string.ar_record_contact_add);	
		}
		
		Set<String> pers = SettingsHelper.getPersistentContacts(this);
		TextView tv3 = (TextView) findViewById(R.id.ar_text_autosave);
		if(this.dbRecord.getContactId()!=null && pers.contains(this.dbRecord.getContactId().toString())){
			tv3.setText(getString(R.string.ar_auto_save_rem)+" (PRO)");
		}else{
			tv3.setText(getString(R.string.ar_auto_save_add)+" (PRO)");	
		}	
		if(!SettingsHelper.isPremium(this))
			tv3.setTextColor(R.style.light_bg_disabled);
		
		TextView tv4 = (TextView) findViewById(R.id.ar_text_save);
		if(this.dbRecord.getPersistent()){
			tv4.setText(R.string.ar_unsave);
		}else{
			tv4.setText(R.string.ar_save);
		}
	}

	private void reloadMode() {
		
		TableRow rowig  = (TableRow) findViewById(R.id.ar_ignore);
		TableRow rowre  = (TableRow) findViewById(R.id.ar_record);
		
		if(SettingsHelper.isModeIgnore(this)){
			rowig.setVisibility(View.VISIBLE);
			rowre.setVisibility(View.GONE);
		}else{
			rowig.setVisibility(View.GONE);
			rowre.setVisibility(View.VISIBLE);
		}		
	}

	/**
	 * @return the photo URI
	 */
	public Bitmap getContactPhoto(long contactId) {
		log.debug("getContactPhoto");
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getContentResolver().query(photoUri,new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
	}	

	public void jcr_play(View view) {
		log.debug("jcr_play");
		 Intent act = new Intent(this, AudioPlayerActivity.class);
		 act.putExtra("record", dbRecord);
		 startActivity(act);
		 
	}

	public void jcr_delete(View view) {
		log.debug("jcr_delete");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(R.string.confirm));

		alertDialogBuilder.setIcon(drawable.ic_menu_delete).setMessage(getString(R.string.are_you_sure)).setCancelable(true)
				.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						File file = new File(SettingsHelper.getStorageDir(RecordDetailActivity.this) + dbRecord.getPath());
						if (file.delete()) {
							if (DatabaseManager.getInstance() == null)
								DatabaseManager.init(getApplicationContext());
							DatabaseManager.getInstance().removeDbRecordById(dbRecord.getId());
						}
						dialog.cancel();
						finish();
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

	public void jcr_detail(View view) {
		log.debug("jcr_detail");
		if (this.dbRecord == null || this.dbRecord.getContactId() == null) {
			showNoContact();
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(this.dbRecord.getContactId()));
		intent.setData(uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void jcr_call(View view) {
		log.debug("jcr_call");
		Intent callIntent = new Intent(Intent.ACTION_DIAL);
		callIntent.setData(Uri.parse("tel:" + Uri.encode(dbRecord.getContactNumber().trim())));
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(callIntent);
	}

	public void jcr_share(View view) {
		log.debug("jcr_share");
		
		File file = new File(SettingsHelper.getStorageDir(this), dbRecord.getPath());
		String extension = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString());
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		shareIntent.setType(mimeType);
		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.ar_share)));
	}

	public void jcr_history(View view) {
		log.debug("jcr_history");
		if (this.dbRecord == null || this.dbRecord.getContactId() == null) {
			showNoContact();
			return;
		}
		Intent act = new Intent(RecordDetailActivity.this, RecordHistoryActivity.class);
		act.putExtra("dbRecord", this.dbRecord);
		startActivity(act);
	}
	public void jcr_record(View view) {
		log.debug("jcr_record");
		if (this.dbRecord == null || this.dbRecord.getContactId() == null) {
			showNoContact();
			return;
		}

		Set<String> recorded = SettingsHelper.getRecordContacts(this);

		boolean was = false;

		if (!recorded.contains(this.dbRecord.getContactId().toString())) {
			recorded.add(this.dbRecord.getContactId().toString());
		} else {
			was = true;
			recorded.remove(this.dbRecord.getContactId().toString());
		}

		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = defaultSharedPreferences.edit();
		edit.putStringSet(SettingsHelper.RECORD_CONTACTS, recorded);
		if (edit.commit()) {
			if (!was)
				Toast.makeText(this, getString(R.string.added_to) + " " + getString(R.string.pref_record_contacts), Toast.LENGTH_LONG).show();
			else
				Toast.makeText(this, getString(R.string.removed_from) + " " + getString(R.string.pref_record_contacts), Toast.LENGTH_LONG).show();
		}
		reloadSelects();

	}
	public void jcr_ignore(View view) {
		log.debug("jcr_ignore");
		if (this.dbRecord == null || this.dbRecord.getContactId() == null) {
			showNoContact();
			return;
		}

		Set<String> ignored = SettingsHelper.getIgnoredContacts(this);

		boolean was = false;

		if (!ignored.contains(this.dbRecord.getContactId().toString())) {
			ignored.add(this.dbRecord.getContactId().toString());
		} else {
			was = true;
			ignored.remove(this.dbRecord.getContactId().toString());
		}

		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = defaultSharedPreferences.edit();
		edit.putStringSet(SettingsHelper.IGNORED_CONTACTS, ignored);
		if (edit.commit()) {
			if (!was)
				Toast.makeText(this, getString(R.string.added_to) + " " + getString(R.string.pref_ignore_contacts), Toast.LENGTH_LONG).show();
			else
				Toast.makeText(this, getString(R.string.removed_from) + " " + getString(R.string.pref_ignore_contacts), Toast.LENGTH_LONG).show();
		}
		reloadSelects();
	}

	public void jcr_save(View view) {
		log.debug("jcr_save");
		if (DatabaseManager.getInstance() == null)
			DatabaseManager.init(this.getApplicationContext());
		this.dbRecord = DatabaseManager.getInstance().getDbRecord(this.dbRecord.getId());
		boolean was = false;
		if (this.dbRecord.getPersistent())
			was = true;
		this.dbRecord.setPersistent(!dbRecord.getPersistent());

		DatabaseManager.getInstance().updateDbRecord(dbRecord);

		if (!was)
			Toast.makeText(this, getString(R.string.ar_moved_saved), Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, getString(R.string.ar_moved_back), Toast.LENGTH_LONG).show();
		
		reloadSelects();
	}

	public void jcr_autoSave(View view) {
		log.debug("jcr_autoSave");
		if (this.dbRecord == null || this.dbRecord.getContactId() == null) {
			showNoContact();
			return;
		}
		Set<String> persistent = SettingsHelper.getPersistentContacts(this);

		boolean was = false;

		if (!persistent.contains(this.dbRecord.getContactId().toString())) {
			persistent.add(this.dbRecord.getContactId().toString());
		} else {
			was = true;
			persistent.remove(this.dbRecord.getContactId().toString());
		}

		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = defaultSharedPreferences.edit();
		edit.putStringSet(SettingsHelper.PERSISTENT_CONTACTS, persistent);
		if (edit.commit()) {
			if (!was)
				Toast.makeText(this, getString(R.string.added_to) + " " + getString(R.string.pref_persistent_contacts), Toast.LENGTH_LONG).show();
			else
				Toast.makeText(this, getString(R.string.removed_from) + " " + getString(R.string.pref_persistent_contacts), Toast.LENGTH_LONG).show();
		}
		reloadSelects();
	}

	private void showNoContact() {
		log.debug("showNoContact");
		
		Toast.makeText(this, getString(R.string.no_such_contact), Toast.LENGTH_LONG).show();
	}

	public static String toNumInUnits(long bytes) {
		int u = 0;
		for (; bytes > 1024 * 1024; bytes >>= 10) {
			u++;
		}
		if (bytes > 1024)
			u++;
		return String.format(Locale.getDefault(), "%.1f %cB", bytes / 1024f, " kMGTPE".charAt(u));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent act = new Intent(this, SettingsActivity.class);
			startActivity(act);
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}
