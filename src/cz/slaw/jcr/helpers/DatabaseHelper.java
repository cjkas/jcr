package cz.slaw.jcr.helpers;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cz.slaw.jcr.domain.DbCloudQueue;
import cz.slaw.jcr.domain.DbRecord;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final Logger log = LoggerFactory.getLogger(DatabaseHelper.class);
    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "DbRecordDB.sqlite";

    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 15;

    // the DAO object we use to access the SimpleData table
    private Dao<DbRecord, Integer> dbRecordDao = null;
    private Dao<DbCloudQueue, Integer> dbCloudQueueDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase writableDatabase = getWritableDatabase();
//        if(writableDatabase.isOpen())
//        	writableDatabase.close();
    }
    
    @Override
    public void onCreate(SQLiteDatabase database,ConnectionSource connectionSource) {
    	log.debug( "create TABLES");
        try {
            TableUtils.createTable(connectionSource, DbRecord.class);
            TableUtils.createTable(connectionSource, DbCloudQueue.class);
        }catch (SQLException e) {
        	log.error("sql err onCreate", e);
        	throw new RuntimeException(e);
        }
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,ConnectionSource connectionSource, int oldVersion, int newVersion) {
    	log.debug( "upgrade TABLES from "+oldVersion+" to "+newVersion);
		
		try {
			if(oldVersion<15){
				TableUtils.dropTable(connectionSource, DbRecord.class, true);
				TableUtils.dropTable(connectionSource, DbCloudQueue.class, true);
				TableUtils.createTable(connectionSource, DbRecord.class);
				TableUtils.createTable(connectionSource, DbCloudQueue.class);
			}
//			List<String> allSql = new ArrayList<String>();
//			switch (oldVersion) {
//			case 1:
//				 allSql.add("alter table AdData add column `new_col` VARCHAR");
//				 allSql.add("alter table AdData add column `new_col2` VARCHAR");
//			}
//			for (String sql : allSql) {
//				db.execSQL(sql);
//			}

		} catch (SQLException e) {
			log.error("sql err onUpgrade", e);
			throw new RuntimeException(e);
		}
        
    }
    public Dao<DbCloudQueue, Integer> getDbCludQueueDao() {
        if (null == dbCloudQueueDao) {
            try {
            	dbCloudQueueDao = getDao(DbCloudQueue.class);
            }catch (java.sql.SQLException e) {
            	log.error("sql err getDbCludQueueDao", e);
            	throw new RuntimeException(e);
            }
        }
        return dbCloudQueueDao;
    }
    public Dao<DbRecord, Integer> getDbRecordDao() {
        if (null == dbRecordDao) {
            try {
            	dbRecordDao = getDao(DbRecord.class);
            }catch (java.sql.SQLException e) {
            	log.error("sql err getDbRecordDao", e);
            	throw new RuntimeException(e);
            }
        }
        return dbRecordDao;
    }

}
