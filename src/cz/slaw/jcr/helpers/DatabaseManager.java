package cz.slaw.jcr.helpers;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import cz.slaw.jcr.domain.DbCloudQueue;
import cz.slaw.jcr.domain.DbRecord;

public class DatabaseManager {
	private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
	
	private static DatabaseManager instance;
	private DatabaseHelper helper;
	
    static public void init(Context ctx) {
        if (null==instance) {
            instance = new DatabaseManager(ctx);
        }
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    
    private DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    private DatabaseHelper getHelper() {
        return helper;
    }
    
    public void updateDbRecord(DbRecord record){
    	try {
			getHelper().getDbRecordDao().update(record);
		} catch (SQLException e) {
			log.error("sql err updateDbRecord", e);
			throw new RuntimeException(e);
		}
    }
    
    public DbRecord getDbRecord(int id){
    	DbRecord queryForId=null;
		try {
			queryForId = getHelper().getDbRecordDao().queryForId(id);
		} catch (SQLException e) {
			log.error("sql err getDbRecord", e);
			throw new RuntimeException(e);
		}
    	return queryForId;
    }
    
    public int createDbRecord(DbRecord record){
    	int id=-1;
    	try {
			id = getHelper().getDbRecordDao().create(record);
		} catch (SQLException e) {
			log.error("sql err createDbRecord", e);
			throw new RuntimeException(e);
		}
    	return id;
    }
    
    public List<DbRecord> getAllDbRecords() {
        List<DbRecord> dbRecords = null;
        try {
            dbRecords = getHelper().getDbRecordDao().queryForAll();
        } catch (SQLException e) {
			log.error("sql err getAllDbRecords", e);
			throw new RuntimeException(e);
		}
        return dbRecords;
    }
    
//	public DbRecord getDbRecordName(String name) {
//		DbRecord queryForFirst=null;
//		try {
//			queryForFirst = getHelper().getDbRecordDao().queryBuilder().where().eq("_name", name).queryForFirst();
//		} catch (SQLException e) {
//			log.error("sql err getDbRecordName", e);
//			throw new RuntimeException(e);
//		}
//		return queryForFirst;
//	}
//
//	public void removeDbRecordName(String name) {
//		DbRecord dbRecordName = getDbRecordName(name);
//		if(dbRecordName==null)
//			return;
//		try {
//			getHelper().getDbRecordDao().delete(dbRecordName);
//		} catch (SQLException e) {
//			log.error("sql err removeDbRecordName", e);
//			throw new RuntimeException(e);
//		}
//	}

	public List<DbRecord> getDbRecordsByContactId(Integer contactId) {
		List<DbRecord> queryForFirst=null;
		try {
			queryForFirst = getHelper().getDbRecordDao().queryBuilder().where().eq("_contactId", contactId).query();
		} catch (SQLException e) {
			log.error("sql err getDbRecordByContactId", e);
			throw new RuntimeException(e);
		}
		return queryForFirst;
	}
	public List<DbRecord> getDbRecordsNotPersistent() {
		List<DbRecord> queryForFirst=null;
		try {
			queryForFirst = getHelper().getDbRecordDao().queryBuilder().where().eq("_persistent", false).query();
		} catch (SQLException e) {
			log.error("sql err getDbRecordsNotPersistent", e);
			throw new RuntimeException(e);
		}
		return queryForFirst;
	}
	public List<DbRecord> getDbRecordsPersistent() {
		List<DbRecord> queryForFirst=null;
		try {
			queryForFirst = getHelper().getDbRecordDao().queryBuilder().where().eq("_persistent", true).query();
		} catch (SQLException e) {
			log.error("sql err getDbRecordsPersistent", e);
			throw new RuntimeException(e);
		}
		return queryForFirst;
	}

	public void removeDbRecordById(int id) {
		try {
			getHelper().getDbRecordDao().deleteById(id);
		} catch (SQLException e) {
			log.error("sql err removeDbRecordById", e);
			throw new RuntimeException(e);
		}		
	}

	public List<DbRecord> getDbRecordsNotPersistentSearch(String string) {
		List<DbRecord> queryForFirst=null;
		try {
			queryForFirst = getHelper().getDbRecordDao().queryBuilder().where().eq("_persistent", false).and().like("_contactName", "%"+string+"%").query();
		} catch (SQLException e) {
			log.error("sql err getDbRecordsNotPersistentSearch", e);
			throw new RuntimeException(e);
		}
		return queryForFirst;
	}
	public List<DbRecord> getDbRecordsPersistentSearch(String string) {
		List<DbRecord> queryForFirst=null;
		try {
			queryForFirst = getHelper().getDbRecordDao().queryBuilder().where().eq("_persistent", true).and().like("_contactName", "%"+string+"%").query();
		} catch (SQLException e) {
			log.error("sql err getDbRecordsPersistentSearch", e);
			throw new RuntimeException(e);
		}
		return queryForFirst;
	}

	public int createDbCloudQueue(DbRecord dbRecord) {
		log.debug("added to db queue "+dbRecord.getContactName());
		DbCloudQueue q = new DbCloudQueue();
		q.setRecordId(dbRecord.getId());
		int id=-1;
    	try {
			id = getHelper().getDbCludQueueDao().create(q);
		} catch (SQLException e) {
			log.error("sql err createDbCloudQueue", e);
			throw new RuntimeException(e);
		}
    	return id;		
	}

	public List<DbCloudQueue> getAllDbCloudQueue() {
		 List<DbCloudQueue> dbCloudQueues = null;
	        try {
	        	dbCloudQueues = getHelper().getDbCludQueueDao().queryForAll();
	        } catch (SQLException e) {
				log.error("sql err getAllDbCloudQueue", e);
				throw new RuntimeException(e);
			}
        return dbCloudQueues;
	}
	
	public void removeDbCloudeQueueById(int id) {
		try {
			getHelper().getDbCludQueueDao().deleteById(id);
		} catch (SQLException e) {
			log.error("sql err removeDbCloudeQueueById", e);
			throw new RuntimeException(e);
		}		
	}
}
