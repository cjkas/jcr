package cz.slaw.jcr;

import java.io.File;
import java.util.List;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import cz.slaw.jcr.adapters.DbRecordsListAdapter;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.DatabaseManager;
import cz.slaw.jcr.helpers.SettingsHelper;

public abstract class RecordsListBase extends Fragment implements FragmentCallback{
	
	protected DbRecordsListAdapter adapter;

	protected abstract void reloadData();
	public abstract void reloadFragmentsData();
	
	@Override
	public void onLowMemory() {
		adapter.clear();
		super.onLowMemory();
	}
	@Override
	public void onResume() {
		reloadData();
		super.onResume();
	}
	@Override
	public void fragmentsRemoveDbRecord(DbRecord record) {
		adapter.remove(record);
	}
	@Override
	public void fragmentsReloadData() {
		reloadData();
	}
	
	protected void confirmRemoveDialog(final List<DbRecord> records){
		final Activity a = this.getActivity();
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(a);
		alertDialogBuilder.setTitle(getString(R.string.confirm));
		alertDialogBuilder
				.setIcon(drawable.ic_menu_delete)
				.setMessage(getString(R.string.are_you_sure))
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
								
								if(DatabaseManager.getInstance()==null)
									DatabaseManager.init(a);
								
								for (DbRecord record : records) {
									
									File file = new File(SettingsHelper.getStorageDir(a)+record.getPath());
									if(file.exists())
										file.delete();
									
									fragmentsRemoveDbRecord(record);
									DatabaseManager.getInstance().removeDbRecordById(record.getId());
								}
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

}
