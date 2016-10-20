package cz.slaw.jcr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import cz.slaw.jcr.adapters.PopupListAdapter;
import cz.slaw.jcr.beans.PopupItem;
import cz.slaw.jcr.listeners.AsyncResponse;
import cz.slaw.jcr.tasks.ContactsTask;

public class ContactsDialog extends DialogPreference implements AsyncResponse<List<PopupItem>> {

	private static final Logger log = LoggerFactory.getLogger(ContactsDialog.class);
	
	private PopupListAdapter adapter;
	
	public ContactsDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		log.debug("PopupDialog create");
		setPersistent(false);//will save on own
		setDialogLayoutResource(R.layout.popup_dialog);
	}
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

	    if (positiveResult) {
	    	
	    	Set<String> igc = new HashSet<String>();
	    	
	    	for (int i = 0; i < adapter.getCheckStates().size(); i++) {
				Boolean chbox = adapter.getCheckStates().get(i);
				if(Boolean.TRUE.equals(chbox)){
					PopupItem item = adapter.getItem(i);
					igc.add(item.getId().toString());
				}
				
			}
	    	
	        Editor editor = getEditor();
	        editor.putStringSet(getKey(), igc);
	        if(editor.commit())
	        	notifyChanged();
	    }
	}
	@Override
	public void onBindDialogView(View view) {

		ListView list = (ListView) view.findViewById(R.id.popup_list);
		List<PopupItem> items = new ArrayList<PopupItem>();
		adapter = new PopupListAdapter(getContext(), R.layout.popup_list_item, items);
		list.setAdapter(adapter);
		
		ContactsTask contactsTask = new ContactsTask(view.getContext(),this);
		contactsTask.execute(getContext().getContentResolver());
		
		super.onBindDialogView(view);
	}


	@Override
	public void processFinish(List<PopupItem> response) {
		SharedPreferences pref = getSharedPreferences();
        Set<String> igcs = pref.getStringSet(getKey(), new HashSet<String>());
        for (String igc :igcs) {
			for (PopupItem item : response) {
				if(item.getId().toString().equals(igc)){
					item.setChecked(true);
				}
			}
		}
		adapter.addAll(response);
	}


}
