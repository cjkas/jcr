package cz.slaw.jcr;

import cz.slaw.jcr.domain.DbRecord;

public interface FragmentCallback {
	void fragmentsRemoveDbRecord(DbRecord record);
	void fragmentsReloadData();
	void search(String searched);
}
