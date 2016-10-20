package cz.slaw.jcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cz.slaw.jcr.helpers.GoogleDriveHelper;
import cz.slaw.jcr.listeners.ConnectionState;

public class GoogleDriveActivity extends Activity implements OnClickListener,ConnectionState {

	private static final Logger log = LoggerFactory.getLogger(GoogleDriveActivity.class);
	private Button loginBtn;
	private GoogleDriveHelper drive;
	private ProgressDialog progressDialog;
	
	@Override
	public void onBackPressed() {
		finish();
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.debug("GoogleDriveActivity start");
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.gdrive_layout);
		
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resultCode != ConnectionResult.SUCCESS){
			Toast.makeText(this, "Google services not available (cause : "+resultCode+") ", Toast.LENGTH_LONG).show();
			finish();
		}
		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		drive = new GoogleDriveHelper(this,this,this);
		
		loginBtn = (Button) findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		drive.start();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case GoogleDriveHelper.RESOLVE_CONNECTION_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				drive.login();
			}
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtn:
			if (drive.isConnected()) {
				drive.logout();
			} else {
				progressDialog.show();	
				drive.login();
			}
			break;
		default:
			break;
		}
	}
	
	@Override
    protected void onPause() {
		drive.onPause();
        super.onPause();
    }
	@Override
	public void stateChange(boolean connected) {
		loginBtn.setText(connected ? R.string.logout : R.string.login);
		if(progressDialog!=null)
			progressDialog.dismiss();
	}
	
	

}
