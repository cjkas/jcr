package cz.slaw.jcr.tasks;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import cz.slaw.jcr.beans.PopupItem;
import cz.slaw.jcr.listeners.AsyncResponse;

public class ContactsTask extends AsyncTask<ContentResolver, Void, List<PopupItem>> {

	private final AsyncResponse<List<PopupItem>> delegate;
	private final Context ctx;
	private ProgressDialog progressDialog;

	public ContactsTask(Context ctx,AsyncResponse<List<PopupItem>> delegate) {
		this.ctx=ctx;
		this.delegate=delegate;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(ctx);
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
	}
	
	@Override
	protected List<PopupItem> doInBackground(ContentResolver... params) {
		List<PopupItem> contacts =  new ArrayList<PopupItem>();
		ContentResolver resolver = params[0];
	    /**
	     *  List all contacts
	     */
	    ContentResolver cr = resolver;
	    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	    if (cursor.getCount()>0) {
	    	while(cursor.moveToNext()){
		        /**
		         *   Get all phone numbers.
		         */
	        	if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))>0) {
	        		int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
	    	        String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	    	        
	        		PopupItem contact = new PopupItem();
	     	        contact.setId(contactId);
	     	        contact.setName(contactName);
	     	        contacts.add(contact);
	        	}
	    	}
	    }
	    cursor.close();
		return contacts;
	}
	
	@Override
	protected void onPostExecute(List<PopupItem> result) {
		delegate.processFinish(result);
		progressDialog.dismiss();
		super.onPostExecute(result);
	}

}
