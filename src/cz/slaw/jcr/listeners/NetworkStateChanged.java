package cz.slaw.jcr.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cz.slaw.jcr.helpers.SettingsHelper;
import cz.slaw.jcr.utils.NetworkUtil;

public class NetworkStateChanged extends BroadcastReceiver  {
	private static final Logger log = LoggerFactory.getLogger(NetworkStateChanged.class);
	private ProcessCloudQueue processCloudQueue=null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    ConnType status = NetworkUtil.getNetworkConnection(context);
	    //TODO if network failed interrupt processCloudQueue make it thread
	    log.info(""+status);
	    if(processCloudQueue!=null)
	    	return;
	    
	    if (ConnType.OFF.equals(status)) {
	        log.info("no connection");
	        return;
	    } else {
	        if(ConnType.MOBILE.equals(status)){
	        	log.info("connection mobile");
	        	if(SettingsHelper.onlyWifi(context)){
	        		log.info("connection only wifi");
	        		return;
	        	}else{
	        		log.info("mobile process queue");
	        		//SEND
	        		processCloudQueue = new ProcessCloudQueue(context);
	        		processCloudQueue = null;
	        	}
	        	
	        }else{
	        	log.info("connection wifi process queue");
	        	//SEND
	        	processCloudQueue = new ProcessCloudQueue(context);
	        	processCloudQueue = null;
	        	
	        }
	    }

	}
	
}
