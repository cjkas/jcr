package cz.slaw.jcr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import cz.slaw.jcr.adapters.DbRecordsListAdapter;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.DatabaseManager;

public class RecordsFragment extends RecordsListBase {

	private static final Logger log = LoggerFactory.getLogger(RecordsFragment.class);
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log.debug("RecordsFragment createView");
		final View layoutView = inflater.inflate(R.layout.fragment_records, container, false);
		final ListView list = (ListView) layoutView.findViewById(R.id.records_list);
		
		final List<DbRecord> records = new ArrayList<DbRecord>();
		adapter = new DbRecordsListAdapter(this.getActivity(), R.layout.records_list_item, records);
		
		list.setAdapter(adapter);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		final MultiChoiceModeListener multiChoiceModeListener = new MultiChoiceModeListener() {
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				// Calls toggleSelection method from ListViewAdapter Class
				adapter.selectView(position, checked);
				// Capture total checked items
				final int checkedCount = list.getCheckedItemCount();
				// Set the CAB title according to total checked items
				mode.setTitle(checkedCount +" "+ getString(R.string.selected));
			}
			
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.msl_delete:
					final SparseBooleanArray selected = adapter.getSelectedIds();
					final List<DbRecord> recsr=new ArrayList<DbRecord>();
					for (int i = 0; i < selected.size(); i++) {
						if (selected.valueAt(i)) {
							final DbRecord selecteditem = adapter.getItem(selected.keyAt(i));
							recsr.add(selecteditem);
						}
					}
					confirmRemoveDialog(recsr);
					mode.finish();
					return true;
				case R.id.msl_save:
					final SparseBooleanArray selected2 = adapter.getSelectedIds();
					for (int i = 0; i < selected2.size(); i++) {
						if (selected2.valueAt(i)) {
							final DbRecord selecteditsem = adapter.getItem(selected2.keyAt(i));
							move2saved(selecteditsem);
						}
					}
					reloadFragmentsData();
					mode.finish();
					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.records_list_actions, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				adapter.removeSelection();
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
		};		
		list.setMultiChoiceModeListener(multiChoiceModeListener);
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	list.setItemChecked(position, !adapter.isSelected(position));
                return false;
            }
        });
		return layoutView;
	}
	
	protected void move2saved(DbRecord selectediteem) {
		if (DatabaseManager.getInstance() == null)
			DatabaseManager.init(this.getActivity().getApplicationContext());
		final DbRecord dbRecord = DatabaseManager.getInstance().getDbRecord(selectediteem.getId());
		dbRecord.setPersistent(true);
		DatabaseManager.getInstance().updateDbRecord(dbRecord);
		
	}

	@Override
	protected void reloadData() {
		log.debug("reloadData");
		if(DatabaseManager.getInstance()==null)
			DatabaseManager.init(this.getActivity().getApplicationContext());
		
		final List<DbRecord> records = DatabaseManager.getInstance().getDbRecordsNotPersistent();
		Collections.reverse(records);
		adapter.clear();
		adapter.addAll(records);		
	}
	@Override
	public void search(String string) {
		if(DatabaseManager.getInstance()==null)
			DatabaseManager.init(this.getActivity().getApplicationContext());
		
		final List<DbRecord> records = DatabaseManager.getInstance().getDbRecordsNotPersistentSearch(string);
		Collections.reverse(records);
		adapter.clear();
		adapter.addAll(records);	
		
	}
	private Activity activity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity=activity;
	}

	@Override
	public void reloadFragmentsData() {
		log.debug("reloadFragmentsData");
		final MainActivity a = (MainActivity) this.activity;
		a.reloadFragmentsData();
	}
	
}
