package cz.slaw.jcr.domain;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class DbCloudQueue implements Serializable{

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		String sx="(id='"+id+"'),"
				+ "(recordId='"+recordId+"')";
		return sx;
	}
	
	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(columnName="_recordId")
	private int recordId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}
	
	
}
