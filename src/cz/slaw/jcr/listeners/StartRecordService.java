package cz.slaw.jcr.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cz.slaw.jcr.AppBootConfig;
import cz.slaw.jcr.helpers.SettingsHelper;

public class StartRecordService extends BroadcastReceiver {
	
	private static final Logger log = LoggerFactory.getLogger(StartRecordService.class);
	
	private Context ctx;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.ctx=context;
		log.info("recived boot broadcast");
		if(SettingsHelper.isRecordingEnabled(ctx)){
			log.info("start service on boot");
			AppBootConfig.startService();
		}
		
	}
	
}
