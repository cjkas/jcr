package cz.slaw.jcr.domain;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class DbRecord implements Serializable{

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		String sx="(id='"+id+"'),"
				+ "(path='"+path+"'),"
				+ "(name='"+name+"'),"
				+ "(contactName='"+contactName+"'),"
				+ "(contactId='"+contactId+"'),"
				+ "(contactNumber='"+contactNumber+"'),"
				+ "(incoming='"+incoming+"'),"
				+ "(start='"+start+"')"
				+ "(end='"+end+"')";
		
		return sx;
	}
	
	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(unique=true,uniqueIndex=true,columnName="_name")
	private String name;
	
	@DatabaseField(unique=true,uniqueIndex=true,columnName="_path")
	private String path;
	
	@DatabaseField(columnName="_contactName")
	private String contactName;

	@DatabaseField(columnName="_contactNumber")
	private String contactNumber;
	
	@DatabaseField(columnName="_contactId")
	private Integer contactId;
	
	@DatabaseField(columnName="_incoming")
	private boolean incoming=false;
	
	@DatabaseField(columnName="_persistent")
	private boolean persistent=false;
	
	@DatabaseField
	private Date start;
	
	@DatabaseField
	private Date end;

	
	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public boolean getIncoming() {
		return incoming;
	}

	public void setIncoming(boolean incoming) {
		this.incoming = incoming;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getContactId() {
		return contactId;
	}

	public void setContactId(Integer contactId) {
		this.contactId = contactId;
	}
	
	public Long getDuration(){
		if(getStart()!=null && getEnd()!=null){
			long duration = getEnd().getTime()-getStart().getTime();
			return (duration/1000);	
		}
		return 0L;	
	}

	public boolean getPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
	
}
