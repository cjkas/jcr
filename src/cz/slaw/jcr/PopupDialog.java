package cz.slaw.jcr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import cz.slaw.jcr.adapters.PopupListAdapter;
import cz.slaw.jcr.beans.PopupItem;

public class PopupDialog extends DialogPreference {

	private static final Logger log = LoggerFactory.getLogger(PopupDialog.class);

	private PopupListAdapter adapter;
	private final String[] entries;
	private final int[] values;
	private final int[] disabled;

	
	public PopupDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		log.info("PopupDialog create");
		
		setPersistent(false);// will save on own
		setDialogLayoutResource(R.layout.popup_dialog);
		
		final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Popup);
		final int ide = array.getResourceId(R.styleable.Popup_arrayEntries, 0);
		final int idv = array.getResourceId(R.styleable.Popup_arrayValues, 0);
		final int idd = array.getResourceId(R.styleable.Popup_arrayDisabled, 0);
		
	    entries = context.getResources().getStringArray(ide);
	    values = context.getResources().getIntArray(idv);
	    disabled = context.getResources().getIntArray(idd);
		array.recycle();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {

			Set<String> igc = new HashSet<String>();

			for (int i = 0; i < adapter.getCheckStates().size(); i++) {
				Boolean chbox = adapter.getCheckStates().get(i);
				if (Boolean.TRUE.equals(chbox)) {
					PopupItem item = adapter.getItem(i);
					igc.add(item.getId().toString());
				}

			}

			Editor editor = getEditor();
			editor.putStringSet(getKey(), igc);
			if (editor.commit())
				notifyChanged();
		}
	}

	@Override
	public void onBindDialogView(View view) {

		ListView list = (ListView) view.findViewById(R.id.popup_list);
		List<PopupItem> items = new ArrayList<PopupItem>();
		adapter = new PopupListAdapter(getContext(), R.layout.popup_list_item, items);
		list.setAdapter(adapter);

		prepareItems();

		super.onBindDialogView(view);
	}

	public void prepareItems() {
		SharedPreferences pref = getSharedPreferences();
		int selected = pref.getInt(getKey(), 0);
		List<PopupItem> items = new ArrayList<PopupItem>();
		
		for (int i = 0; i < entries.length; i++) {
			String name = entries[i];
			int val = values[i];
			
			PopupItem item = new PopupItem();
			item.setName(name);
			item.setId(val);
			if(i==3)item.setEnabled(false);
			items.add(item);
			
		}
		
			for (PopupItem item : items) {
				if (item.getId() == selected) {
					item.setChecked(true);
				}
			}
		adapter.addAll(items);
	}

}
