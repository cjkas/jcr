package cz.slaw.jcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cz.slaw.jcr.helpers.DropBoxHelper;

public class DropboxActivity extends Activity implements OnClickListener {

	private static final Logger log = LoggerFactory.getLogger(DropboxActivity.class);
	
	private Button loginBtn;
	private DropBoxHelper dropbox;

	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.debug("DropboxActivity create");
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.dropbox_layout);
		loginBtn = (Button) findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(this);
		dropbox= new DropBoxHelper(this);
		dropbox.connect();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		loginBtn.setText(dropbox.isConnected() ? R.string.logout : R.string.login);
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtn:
			if (dropbox.isConnected()) {
				dropbox.logout();
			} else {
				dropbox.login();
			}
			loginBtn.setText(dropbox.isConnected() ? R.string.logout : R.string.login);
			break;
		default:
			break;
		}
	}

	
}
