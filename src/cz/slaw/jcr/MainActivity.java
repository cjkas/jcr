package cz.slaw.jcr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import cz.slaw.jcr.billing.Billing;
import cz.slaw.jcr.billing.Billing.BuyCallback;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.SettingsHelper;

public class MainActivity extends FragmentActivity implements BuyCallback {
	private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
	
	MyAdapter mAdapter;
	ViewPager mPager;
	Billing bill;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log.debug("MainActivity create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mAdapter = new MyAdapter(getSupportFragmentManager(),this);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		
		
		try{
			createStorage();
		}catch(IOException e){
			log.error( "create storage err",e);
			errorDialog(e.getLocalizedMessage());
		}

		if (!SettingsHelper.isRecordingEnabled(this)) {
			serviceEnableDialog();
		}
		bill = new Billing(this,this);
		bill.checkIsPro();
		
	}

	private void serviceEnableDialog() {
		log.debug("serviceEnableDialog");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(R.string.confirm));
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		alertDialogBuilder
				.setMessage(getString(R.string.recording_is_disabled_enable))
				.setCancelable(true)
				.setNegativeButton(getString(android.R.string.no), 
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton(getString(android.R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								//save pef
								Editor edit = sharedPref.edit();
								edit.putBoolean(SettingsHelper.ENABLE_RECORDING, true);
								if(edit.commit()){
									AppBootConfig.startService();
								}
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();		
	}
	
	private void errorDialog(String message) {
		log.error( message);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(android.R.string.dialog_alert_title));
		alertDialogBuilder
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close
								// current activity
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void createStorage() throws IOException {
		File dir = SettingsHelper.getStorageDir(this);
		
		if(!dir.exists()){
			if(!dir.mkdirs()){
				throw new IOException("couldn't create dir path "+dir.getAbsolutePath());
			}
		}
		
		File file = new File(dir, ".nomedia");
		if (!file.exists()) {
			mkdir(file);
		} else {
			if (!file.isDirectory()) {
				if (file.delete()) {
					mkdir(file);
				}
			}
		}
	}

	private void mkdir(File file) {
		if (!file.mkdirs()) {
			log.error( ".nomedia not created");
		} else {
			log.debug( ".nomedia created");
		}
	}
	
	public static class MyAdapter extends FragmentPagerAdapter {
		private final Context context;
		private final List<Fragment> fragments = new ArrayList<Fragment>(2);
		
		public MyAdapter(FragmentManager fragmentManager,Context ctx) {
			super(fragmentManager);
			this.context=ctx;
			fragments.add(new RecordsFragment());
			fragments.add(new RecordsSavedFragment());
		}
		@Override
		public int getCount() {
			return fragments.size();
		}
		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}
		@Override
		public CharSequence getPageTitle(int position) {
			if(position==1){
				return context.getString(R.string.title_activity_saved);
			}else{
				return context.getString(R.string.title_activity_recordings);
			}
		}
	}

	public void recordDetail(View view) {
		DbRecord record = (DbRecord)view.getTag();
		log.debug("recordShow record "+record.getPath());
		Intent act = new Intent(this, RecordDetailActivity.class);
		act.putExtra("record", record);
		
		startActivity(act);
		
	}
	public void recordPlay(View view) {
		DbRecord record = (DbRecord)view.getTag();
		log.debug("recordPlay record "+record.getPath());
//		File file = new File(SettingsKeys.getStorageDir(this)+record.getPath());
//		
//		String extension = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toString());
//		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//		log.debug("mime for "+extension+":"+mimeType);
//		
//		Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
//		mediaIntent.setDataAndType(Uri.parse(file.toURI().toString()), mimeType);
//		startActivity(mediaIntent);
		Intent act = new Intent(this, AudioPlayerActivity.class);
		act.putExtra("record", record);
		startActivity(act);
	}
	
	protected void fragmentsRemoveDbRecord(DbRecord record) {
		log.debug("fragmentsRemoveDbRecord");
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		
		for (Fragment fragment : fragments) {
				FragmentCallback c = (FragmentCallback) fragment;
				c.fragmentsRemoveDbRecord(record);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_search, menu);
		MenuItem menuItem = menu.findItem(R.id.action_search);
        /** Get the edit text from the action view */
        final EditText txtSearch = ( EditText ) menuItem.getActionView().findViewById(R.id.txt_search);
        /** Setting an action listener */
        txtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				String searched = txtSearch.getText().toString();
				search(searched);
			}
		});
        
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public void onBackPressed() {
       finish();
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent act = new Intent(this, SettingsActivity.class);
			startActivity(act);
			return true;
		case R.id.action_search:
			EditText txtSearch = ( EditText ) item.getActionView().findViewById(R.id.txt_search);
			txtSearch.requestFocus();
			
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void search(String string) {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		
		for (Fragment fragment : fragments) {
			if(fragment.isVisible()){
				FragmentCallback c = (FragmentCallback) fragment;
				c.search(string);
			}
		}
	}

	public void reloadFragmentsData() {
		log.info("reloadFragmentsData");
		
		for(int i=0;i<mAdapter.getCount();i++){
			log.info("reloadFragmentsData ("+i+") ");
			FragmentCallback frag = (FragmentCallback) mAdapter.getItem(i);
			frag.fragmentsReloadData();
		}
	}

	@Override
	public void buyResult(boolean res) {
	}

	@Override
	public void isPremium(boolean res) {
		SettingsHelper.setPremium(this, res);
		if(bill!=null)
			bill.stop();
		if(!res){
			AdView mAdView = (AdView) findViewById(R.id.adView);
	        AdRequest adRequest = new AdRequest.Builder()
	        		.addTestDevice("E3B93807F79BA1AF682AC9FE3C57A5BB")//S4 slaw
	        		.build();
	        mAdView.loadAd(adRequest);
		}
	}

}
