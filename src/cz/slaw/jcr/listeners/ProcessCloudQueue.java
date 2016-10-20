package cz.slaw.jcr.listeners;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import cz.slaw.jcr.domain.DbCloudQueue;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.DatabaseManager;
import cz.slaw.jcr.helpers.DropBoxHelper;
import cz.slaw.jcr.helpers.GoogleDriveHelper;
import cz.slaw.jcr.helpers.SettingsHelper;
import cz.slaw.jcr.utils.NetworkUtil;

public class ProcessCloudQueue {
	
	private static final Logger log = LoggerFactory.getLogger(ProcessCloudQueue.class);
	private final Context context;

	public ProcessCloudQueue(Context context) {
		this.context=context;
		run();
	}

	private void run() {
		if(SettingsHelper.isDropbox(context) || SettingsHelper.isGdrive(context)){
			log.info("cloud is enabled");
			
		if(DatabaseManager.getInstance()==null)
			DatabaseManager.init(context);
		
		List<DbCloudQueue> list =  DatabaseManager.getInstance().getAllDbCloudQueue();
		
		log.info("queue size "+list.size());
		
		for (DbCloudQueue dbCloudQueue : list) {
			//send
			DbRecord record = DatabaseManager.getInstance().getDbRecord(dbCloudQueue.getRecordId());
			if(record==null){
				DatabaseManager.getInstance().removeDbCloudeQueueById(dbCloudQueue.getId());
				continue;
			}
			//check network is ok
			ConnType networkConnection = NetworkUtil.getNetworkConnection(context);
			if(ConnType.OFF.equals(networkConnection)){
				log.info("network down leaving");
				return;
			}else{
				if(ConnType.MOBILE.equals(networkConnection) && SettingsHelper.onlyWifi(context)){
					log.info("mobile on but only wifi set so leaving");
					return;
				}
			}
			
			if(SettingsHelper.isDropbox(context)){
				DropBoxHelper dropbox = new DropBoxHelper(context);
				dropbox.connect();
				if(dropbox.isConnected()){
					dropbox.upload(record);
				}
			}
			if(SettingsHelper.isGdrive(context)){
				GoogleDriveHelper drive = new GoogleDriveHelper(context, null,null);
				drive.upload(record);
			}
			//remove
			DatabaseManager.getInstance().removeDbCloudeQueueById(dbCloudQueue.getId());
		}
		
		}else{
			log.info("cloud is disabled");
		}
	}
	

}
