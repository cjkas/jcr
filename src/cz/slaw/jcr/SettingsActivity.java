package cz.slaw.jcr;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cz.slaw.jcr.billing.Billing;
import cz.slaw.jcr.billing.Billing.BuyCallback;
import cz.slaw.jcr.helpers.SettingsHelper;
import cz.slaw.jcr.listeners.PhoneListenerService;
import net.bgreco.DirectoryPicker;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener,BuyCallback {
	
	private static final Logger log = LoggerFactory.getLogger(RecordsSavedFragment.class);
	Billing bill;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.info("SettingsActivity create");
		bill = new Billing(SettingsActivity.this,SettingsActivity.this);
		bill.checkIsPro();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.pref_general);
		PreferenceManager.setDefaultValues(getBaseContext(), R.xml.pref_general, false);
		
		
		Preference destPick = findPreference(SettingsHelper.RECORD_DESTINATION);
		destPick.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(SettingsActivity.this, DirectoryPicker.class);
				// optionally set options here
				startActivityForResult(intent, DirectoryPicker.PICK_DIRECTORY);
				return true;
			}
		});
		
		Preference recDestRestore = findPreference(SettingsHelper.RECORD_DESTINATION_RESTORE);
		recDestRestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {

				SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
				
				Editor edit = defaultSharedPreferences.edit();
				edit.putString(SettingsHelper.RECORD_DESTINATION, SettingsHelper.RECORD_DESTINATION_DEF);
				edit.commit();
				updatePrefSummary(findPreference(SettingsHelper.RECORD_DESTINATION));
				Toast.makeText(getApplicationContext(),R.string.pref_record_destination_restored, Toast.LENGTH_LONG).show();
				return true;
			}
		});
		
		Preference dbox = findPreference(SettingsHelper.PREF_DROPBOX);
		dbox.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent act = new Intent(SettingsActivity.this,DropboxActivity.class);
				startActivity(act);
				return true;
			}
		});
		
		Preference gdrive = findPreference(SettingsHelper.PREF_GDRIVE);
		gdrive.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent act = new Intent(SettingsActivity.this,GoogleDriveActivity.class);
				startActivity(act);
				return true;
			}
		});
		
		Preference plang = findPreference(SettingsHelper.PREF_LANG);
		plang.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				setLocale((String) newValue);
				return true;
			}
		});
		Preference buyPro = findPreference(SettingsHelper.PREF_BUY_PRO);
		buyPro.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				bill = new Billing(SettingsActivity.this,SettingsActivity.this);
				bill.buyPro();
				return true;
			}
		});
		Preference about = findPreference(SettingsHelper.PREF_ABOUT);
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent act = new Intent(SettingsActivity.this,AboutActivity.class);
				startActivity(act);
				return true;
			}
		});
		
		premiumUpgrade(SettingsHelper.isPremium(this));
		
	}
	@Override
	public void buyResult(boolean res) {
		premiumUpgrade(res);
		if(bill != null)
			bill.stop();
	}

	private void premiumUpgrade(boolean res) {
		log.debug("sett pre  "+res);
		Preference buyPro = findPreference(SettingsHelper.PREF_BUY_PRO);
		buyPro.setEnabled(!res);
		Preference gdrive = findPreference(SettingsHelper.PREF_GDRIVE);
		gdrive.setEnabled(res);
		log.debug("gdrive "+res);
		Preference dbox = findPreference(SettingsHelper.PREF_DROPBOX);
		dbox.setEnabled(res);
		Preference wifi = findPreference(SettingsHelper.PREF_WIFI_ONLY);
		wifi.setEnabled(res);
		Preference shake = findPreference(SettingsHelper.SHAKE);
		shake.setEnabled(res);
		Preference persistentContacts = findPreference(SettingsHelper.PERSISTENT_CONTACTS);
		persistentContacts.setEnabled(res);
		Preference recordContacts = findPreference(SettingsHelper.RECORD_CONTACTS);
		persistentContacts.setEnabled(res);
		SettingsHelper.setPremium(this, res);
	}

	@Override
	public void isPremium(boolean res) {
		premiumUpgrade(res);
		if(bill!=null)
			bill.stop();
	}

	private void setLocale(String lang) {
		log.debug("setting locale to :" + lang);

		if (SettingsHelper.PREF_LANG_DEF.equals(lang)) {
			lang = Resources.getSystem().getConfiguration().locale.getLanguage();
		}
		AppBootConfig.changeLang(lang, getBaseContext());
		AppBootConfig.restartApplication();
		finish();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SettingsHelper.ENABLE_RECORDING) || key.equals(SettingsHelper.RECORDING_ENABLED_NOTIFIY)) {

			if (sharedPreferences.getBoolean(SettingsHelper.ENABLE_RECORDING, false)) {
				try {
					AppBootConfig.stopService();
					Thread.sleep(100);
					AppBootConfig.startService();
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("iee", e);
					throw new RuntimeException(e);
				}
			} else {
				log.debug("serv run1 " + AppBootConfig.isServiceRunning(PhoneListenerService.class));
				AppBootConfig.stopService();
				log.debug("serv run2 " + AppBootConfig.isServiceRunning(PhoneListenerService.class));
			}
		}
		updatePrefSummary(findPreference(key));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		log.info("onActivityResult:" + requestCode + "," + resultCode);
		if (requestCode == DirectoryPicker.PICK_DIRECTORY && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			String path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
			log.info("choosed path : " + path);

			SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

			Editor edit = defaultSharedPreferences.edit();
			edit.putString(SettingsHelper.RECORD_DESTINATION, path);
			edit.commit();

			updatePrefSummary(findPreference(SettingsHelper.RECORD_DESTINATION));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		initSummary(getPreferenceScreen());
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceGroup) {
			PreferenceGroup pGrp = (PreferenceGroup) p;
			for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
				initSummary(pGrp.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			p.setSummary(listPref.getEntry());

			if (SettingsHelper.PREF_RECORD_OR_IGNORE.equals(p.getKey())) {
				ContactsDialog ignCont = (ContactsDialog) findPreference(SettingsHelper.IGNORED_CONTACTS);
				ContactsDialog recCont = (ContactsDialog) findPreference(SettingsHelper.RECORD_CONTACTS);

				if (SettingsHelper.PREF_RECORD_OR_IGNORE_DEFIGNORE.equals(listPref.getValue())) {
					ignCont.setEnabled(true);
					recCont.setEnabled(false);
				} else {
					ignCont.setEnabled(false);
					recCont.setEnabled(true);
				}
			}
			return;
		}
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			p.setSummary(editTextPref.getText());
			return;
		}
		if (p instanceof MultiSelectListPreference) {
			MultiSelectListPreference msPref = (MultiSelectListPreference) p;
			if (msPref.getValues().size() > 0) {
				p.setSummary(getString(R.string.selected) + "(" + msPref.getValues().size() + ")");
			} else {
				p.setSummary(null);
			}
			return;
		}
		if (p instanceof ContactsDialog) {
			Set<String> set = getPreferenceScreen().getSharedPreferences().getStringSet(p.getKey(),
					new HashSet<String>());
			if (set.size() > 0) {
				p.setSummary(getString(R.string.selected) + "(" + set.size() + ")");
			} else {
				p.setSummary(null);
			}
			return;
		}
		if (SettingsHelper.PREF_DROPBOX.equals(p.getKey()) || SettingsHelper.PREF_GDRIVE.equals(p.getKey())) {
			Boolean bool = getPreferenceScreen().getSharedPreferences().getBoolean(p.getKey(), false);
			p.setSummary(bool ? R.string.enabled : R.string.disabled);
			return;
		}
		if (SettingsHelper.RECORD_DESTINATION.equals(p.getKey())) {
			String string = getPreferenceScreen().getSharedPreferences().getString(p.getKey(),
					SettingsHelper.RECORD_DESTINATION_DEF);
			p.setSummary(string);
			return;
		}
	}

}