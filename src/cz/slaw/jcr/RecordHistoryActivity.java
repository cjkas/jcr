package cz.slaw.jcr;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import cz.slaw.jcr.adapters.ContactHistoryAdapter;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.DatabaseManager;

public class RecordHistoryActivity extends Activity {

	private static final Logger log = LoggerFactory.getLogger(RecordHistoryActivity.class);
	
	private DbRecord dbRecord;
	private ContactHistoryAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.debug("history show");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.contact_history);
		Bundle b = getIntent().getExtras();
		
		this.dbRecord  = (DbRecord) b.get("dbRecord");
		log.info("showing history for "+dbRecord.getContactName());
		
		getActionBar().setTitle(getString(R.string.ar_history)+" "+this.dbRecord.getContactName());
		
		ListView list = (ListView) findViewById(R.id.contact_history);
		
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
		
		if(DatabaseManager.getInstance()==null)
			DatabaseManager.init(getApplicationContext());
		
		List<DbRecord> dbRecords = new ArrayList<DbRecord>();
		adapter = new ContactHistoryAdapter(this, R.layout.contact_history_list_item, dbRecords);
		list.setAdapter(adapter);
		
		
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				adapter.addAll(DatabaseManager.getInstance().getDbRecordsByContactId(dbRecord.getContactId()));
				progressDialog.dismiss();
				
			}
		},"RecordHistoryRetrieve");
		thread.start();
		
	}
	public void recordDetail(View view) {
		DbRecord dbrecord = (DbRecord)view.getTag();
		log.debug("recordShow record "+dbrecord);
		
		Intent act = new Intent(this, RecordDetailActivity.class);
		act.putExtra("record", dbrecord);
		startActivity(act);
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
