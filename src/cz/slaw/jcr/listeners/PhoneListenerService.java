package cz.slaw.jcr.listeners;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import cz.slaw.jcr.MainActivity;
import cz.slaw.jcr.R;
import cz.slaw.jcr.beans.PhoneCall;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.helpers.DatabaseManager;
import cz.slaw.jcr.helpers.DropBoxHelper;
import cz.slaw.jcr.helpers.GoogleDriveHelper;
import cz.slaw.jcr.helpers.SettingsHelper;
import cz.slaw.jcr.utils.NetworkUtil;

public class PhoneListenerService extends Service implements ShakeListener {
	
	private static final Logger log = LoggerFactory.getLogger(PhoneListenerService.class);
	private PhoneListenerService service;
	private static final int NOTIFI_SRVICE 		= 0;
	private static final int NOTIFI_RECORD 		= 1;
	/**
	 * indicates service is running 
	 */
	public static boolean running				= false;
	
	private MediaRecorder recorder = null;
	private BroadcastReceiver outgoingReceiver = null;
	private PhoneStateListener phoneStateListener=null;
	private ShakeEventManager shakeEventManager=null;
	private int recordId=-1;
	
	/**
	 * indicates that is recording or no
	 */
	private boolean recording = false;
	/**
	 * indicates that want to record but shake enabled and waiting for shake
	 */
	private PhoneCall waitingCall=null;
	private final SimpleDateFormat formatTime = new SimpleDateFormat("hh_mm_ss",Locale.getDefault());
	private final SimpleDateFormat formatDay = new SimpleDateFormat("yyyy_MM_dd",Locale.getDefault());
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Let it continue running until it is stopped.
		log.debug( "starting service ");
		startSrv();
		service=this;
		return START_STICKY;
	}
	
	private void cancelNotify() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFI_SRVICE);		
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showSrvEnabledNotify() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean isShow = sharedPref.getBoolean(SettingsHelper.RECORDING_ENABLED_NOTIFIY, false);
		
		if(!isShow)
			return;
		
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		Notification n = new Notification.Builder(this)
				.setOngoing(true)
				.setContentTitle(getString(R.string.app_name_full))
				.setContentText(getString(R.string.app_recording))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent)
				.setAutoCancel(false)
				.build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.notify(NOTIFI_SRVICE, n);
	}
	
	/**
	 * get contact name by phone number
	 * @param number
	 * @return
	 */
	public String getContactDisplayNameByNumber(String number) {
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    String name = number;

	    ContentResolver contentResolver = getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }
	    log.debug( "contact name is "+name.hashCode()+" for number "+number.hashCode());
	    return name;
	}
	
	@Override
	public void onDestroy() {
		
		log.debug( "stoping service ");
		//remove or leaks
		
		this.unregisterReceiver(outgoingReceiver);
		outgoingReceiver=null;
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		phoneStateListener=null;
		
		recording=false;
		recorder=null;
		running=false;
		cancelNotify();
		super.onDestroy();
	}
	/**
	 * start phone recorder service
	 */
	private void startSrv() {
		if(running)
			return;
		running=true;

		showSrvEnabledNotify();
		
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//incoming calls
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String number) {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					log.debug( "call incoming :" + number.hashCode() );
					waitingCall=new PhoneCall(number,true);
					
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					log.debug( "call in progress :" + number.hashCode()+", waitingCall:"+waitingCall+" ,hc:"+service.hashCode());

					if(SettingsHelper.isShake(PhoneListenerService.this)){
						log.debug("wait for shake");
						SensorManager sManager = (SensorManager) getBaseContext().getSystemService(Context.SENSOR_SERVICE);
						Sensor s = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
						shakeEventManager = new ShakeEventManager(PhoneListenerService.this);
						sManager.registerListener(shakeEventManager, s, SensorManager.SENSOR_DELAY_NORMAL);
						
						
					}else if(SettingsHelper.isFloatingBtn(PhoneListenerService.this)){
						log.debug("show floating");
//						StandOutWindow.closeAll(PhoneListenerService.this, SimpleWindow.class);
//						StandOutWindow.show(PhoneListenerService.this, SimpleWindow.class, StandOutWindow.DEFAULT_ID);
					}else{
						log.debug("run normal");
						try{
							startRecording(waitingCall.getNumber(),waitingCall.isIncoming());
						} catch (Exception ex) {
							log.error( "startRecording err "+service.hashCode(),ex);
							throw new RuntimeException(ex);
						}
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					log.debug( "call idle");
					waitingCall=null;
					stopRecording();
					break;
				}
				
			}
		};
		
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		//out going calls
		outgoingReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				log.debug("call to: " + number.hashCode()+", hc:"+service.hashCode());
				//FIX NPE record from here cause not singleton ?
				waitingCall=new PhoneCall(number,false);
				log.debug("waiting Call : "+waitingCall);
				
			}
		};

		final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		this.registerReceiver(outgoingReceiver, intentFilter);
	}
	/**
	 * start recording new call
	 * @param number call phone number
	 * @param isIncoming is incoming true or outgoing false
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void startRecording(String number,boolean isIncoming) throws IOException, InterruptedException, ExecutionException {
		//if already recording return
		if(recording)
			return;
		recording=true;
		log.debug( "started record");
		
		String fileType = SettingsHelper.getFileType(this);
		if(SettingsHelper.isModeIgnore(this)){
			log.debug("ignore mode");
//			is ignored ?
			if(isIgnored(number))
				return;
		}else{
			log.debug("record mode");
//			is selected ?
			if(!isRecord(number))
				return;			
		}
		
		//notfiy about record in progress
		showRecordProgressNotify();
		
		recorder = new MediaRecorder();
		
		//set volume of call to max
		increaseVolume();
		
		File path = record2File(isIncoming,number,fileType);
		
		String soundSrc = SettingsHelper.getSoundSrc(this);
		int audioSource=0;
		if("in".equals(soundSrc)){
			log.debug( "record voice downlink");
			audioSource = MediaRecorder.AudioSource.VOICE_DOWNLINK;
		}
		else if("out".equals(soundSrc)){
			log.debug( "record voice uplink");
			audioSource = MediaRecorder.AudioSource.VOICE_UPLINK;
		}
		else{
			log.debug( "record voice call");
			audioSource = MediaRecorder.AudioSource.VOICE_CALL;			
		}
		recorder.setAudioSource(audioSource);
		//@see formats http://developer.android.com/guide/appendix/media-formats.html
		if("amr".equals(fileType)){
			recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
			recorder.setAudioSamplingRate(16000); //max amr-wb 16kHz
			recorder.setAudioEncodingBitRate(23850); //max amr 23.85bit
						
		}
		else if("3gp".equals(fileType)){
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			recorder.setAudioSamplingRate(44100); //44kHz
			recorder.setAudioEncodingBitRate(96000); //192kbit
		}

//	    save in db recording details
		DbRecord record = new DbRecord();
		record.setContactName(getContactDisplayNameByNumber(number));
		
		Integer contactId = getContactIdByNumber(number);
		record.setContactId(contactId);
		record.setContactNumber(number);
		record.setPersistent(isPersistent(record.getContactId()));
		record.setStart(new Date());
		record.setIncoming(isIncoming);
		record.setPath(path.getAbsolutePath().replace(SettingsHelper.getStorageDir(this).getAbsolutePath(), ""));
		record.setName(path.getName());
		
		
		if(DatabaseManager.getInstance()==null)
			DatabaseManager.init(getApplicationContext());
		
		DatabaseManager.getInstance().createDbRecord(record);
		recordId = record.getId();
	    
		
		recorder.setOutputFile(path.getAbsolutePath());
		recorder.prepare();
		recorder.start();
		
	}
	/**
	 * increase call volume to max during call record
	 */
	private void increaseVolume() {
		boolean isIncreaseVol = SettingsHelper.isIncreaseVol(this);
		if(!isIncreaseVol)
			return;
		AudioManager audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);		
	}
	/**
	 * file to wich record
	 * @param isIncoming in true out false
	 * @param number call phone number
	 * @param fileType 3gp/amr
	 * @return
	 * @throws IOException
	 */
	private File record2File(boolean isIncoming, String number, String fileType) throws IOException {
		String name = (isIncoming?"in":"out")+"-"+formatTime.format(new Date())+"-"+number.replaceAll("[^0-9]", "");
		
		String day = formatDay.format(new Date());
		
//		make sure the directory we plan to store the recording in exists
		log.debug( "FILES DIR IS "+SettingsHelper.getStorageDir(this).getAbsolutePath());
		
		File daydir =new File(SettingsHelper.getStorageDir(this), day);
		if(!daydir.exists()){
			if(!daydir.mkdirs()){
				throw new IOException("path could not be created.");
			}
		}
		File path = new File(daydir, name + "."+fileType);
		log.debug( "record to file: " + path.getAbsolutePath());
		
		if (!path.createNewFile()) {
			throw new IOException("file could not be created.");
		}
		return path;
	}

	/**
	 * is marked record in preferences
	 * @param number
	 * @param sharedPref
	 * @return
	 */
	private boolean isRecord(String number) {
		Set<String> recordContactsId = SettingsHelper.getRecordContacts(this);
		
		//if noting selected we assume that record all
		if(recordContactsId.size()==0)
			return true;
		
		Integer contactId = getContactIdByNumber(number);
		if(contactId!=null && recordContactsId.contains(contactId.toString())){
			log.debug( "ignore contact "+contactId+": "+number.hashCode());
			return true;
		}
		return false;
	}
	/**
	 * is marked auto save in preferences
	 * @param contactId
	 * @return
	 */
	private boolean isPersistent(Integer contactId) {
		Set<String> persistentContactsId = SettingsHelper.getPersistentContacts(this);
		
		if(contactId!=null && persistentContactsId.contains(contactId.toString())){
			log.debug( "persistent contact "+contactId);
			return true;
		}
		return false;
	}
	/**
	 * is marked ignored in preferences
	 * @param number
	 * @param sharedPref
	 * @return
	 */
	private boolean isIgnored(String number) {
		Set<String> ignoreContactsId = SettingsHelper.getIgnoredContacts(this);
		Integer contactId = getContactIdByNumber(number);
		if(contactId!=null && ignoreContactsId.contains(contactId.toString())){
			log.debug( "ignore contact "+contactId+": "+number.hashCode());
			return true;
		}
		return false;
	}

	/**
	 * show notification about current call recording
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showRecordProgressNotify() {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			Boolean isShow = sharedPref.getBoolean(SettingsHelper.CALL_RECORDING_NOTIFY, true);
			
			if(!isShow)
				return;
			
			Intent intent = new Intent(this, MainActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

			Notification n = new Notification.Builder(this)
					.setContentTitle(getString(R.string.app_name_full))
					.setContentText(getString(R.string.app_recording))
					.setSmallIcon(R.drawable.ic_jcr_player_record)
					.setContentIntent(pIntent)
					.build();

			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFI_RECORD, n);
		
	}
	/**
	 * get contact id by phone number 
	 * @param number
	 * @return
	 */
	private Integer getContactIdByNumber(String number) {

		  Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		  Integer contactId = null;

		    ContentResolver contentResolver = getContentResolver();
		    Cursor contactLookup = contentResolver.query(uri, new String[] {ContactsContract.Contacts._ID}, null, null, null);

		    try {
		        if (contactLookup != null && contactLookup.getCount() > 0) {
		            contactLookup.moveToNext();
		            contactId = contactLookup.getInt(contactLookup.getColumnIndex(ContactsContract.Contacts._ID));
		        }
		    } finally {
		        if (contactLookup != null) {
		            contactLookup.close();
		        }
		    }
		    log.debug( "contact id is "+contactId+" for number "+number.hashCode());
		    return contactId;
	}
	/**
	 * remove records from storage if they count is over desired limit
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void removeOverLimit() throws InterruptedException, ExecutionException {
		log.debug( "remove over limit");
		
		int limit = SettingsHelper.getFileLimit(this);
		
		
			List<DbRecord> recordings = DatabaseManager.getInstance().getDbRecordsNotPersistent();	
			
			if(recordings.size()>=limit){
			Collections.reverse(recordings);
			
			for (int i = 0; i < recordings.size(); i++) {
				if(i>=limit){
					DbRecord record =recordings.get(i);	
					log.debug( "removing "+record.getPath());
					File file = new File(SettingsHelper.getStorageDir(this)+record.getPath());
					if(file.exists())
						file.delete();
						if(DatabaseManager.getInstance()==null)
							DatabaseManager.init(getApplicationContext());
						DatabaseManager.getInstance().removeDbRecordById(record.getId());
				}
			}
		}
	}

	/**
	 * stop recording call
	 */
	private void stopRecording() {
		//close float btn 
		if(SettingsHelper.isFloatingBtn(this)){
			log.debug("close floating button window");
//			StandOutWindow.closeAll(PhoneListenerService.this, SimpleWindow.class);
		}
		//deregister shake handler
		if(SettingsHelper.isShake(this)){
			if(shakeEventManager!=null){
				log.debug("dereg shake handler");
				SensorManager sManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
				sManager.unregisterListener(shakeEventManager);
			}
		}
		
		if(!recording)
			return;
		recording=false;
		log.debug("stop recording");
		
		//stop recorder
		if (recorder != null) {
			cancelNotification();
			recorder.stop();
			recorder.release();
			recorder = null;
		}
		
		//update db record
		if(recordId!=-1){
			if(DatabaseManager.getInstance()==null)
				DatabaseManager.init(getApplicationContext());
			DbRecord dbRecord = DatabaseManager.getInstance().getDbRecord(recordId);
			dbRecord.setEnd(new Date());
			DatabaseManager.getInstance().updateDbRecord(dbRecord);
			copy2Cloud(dbRecord);
			
			recordId=-1;
		}
		
		
		Thread t = new Thread(new Runnable() {
					
				@Override
				public void run() {
					try {
						removeOverLimit();
					} catch (InterruptedException e) {
						log.error( "iee",e);
					} catch (ExecutionException e) {
						log.error( "ee",e);
					}
			}
		});
		
		t.start();
		
	}
	
	private void copy2Cloud(DbRecord dbRecord) {
		log.info("copy2Cloud");
		
		if(SettingsHelper.isDropbox(this)||SettingsHelper.isGdrive(this)){
			if(!NetworkUtil.getNetworkConnection(this).equals(ConnType.OFF)){
				if(NetworkUtil.getNetworkConnection(this).equals(ConnType.MOBILE) && SettingsHelper.onlyWifi(this)){
					addToQueue(dbRecord);	
				}else{
					if(SettingsHelper.isGdrive(this)){
						GoogleDriveHelper drive = new GoogleDriveHelper(this, null,null);
						drive.upload(dbRecord);
					}else 
					if(SettingsHelper.isDropbox(this)){
						try{
							DropBoxHelper dropbox = new DropBoxHelper(this);
							dropbox.connect();
						
							if(dropbox.isConnected()){
								dropbox.upload(dbRecord);
							}
						}catch (Exception e) {
							log.error( "fatal dropbox error ? ",e);
							addToQueue(dbRecord);
						}
						
					}
				}
			}else{
				addToQueue(dbRecord);
			}
		}
	}
	
	private void addToQueue(DbRecord dbRecord) {
		if(DatabaseManager.getInstance()==null)
			DatabaseManager.init(this);
		DatabaseManager.getInstance().createDbCloudQueue(dbRecord);
		
	}


	/**
	 * cancel current call record notification 
	 */
	private void cancelNotification() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean isShow = sharedPref.getBoolean(SettingsHelper.CALL_RECORDING_NOTIFY, false);
		if(!isShow)
			return;
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFI_RECORD);
	}
	
	/**
	 * shake callback records if waiting call 
	 */
	@Override
	public void onShake() {
		log.info("SHAKED");
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean isShake = sharedPref.getBoolean(SettingsHelper.SHAKE, false);
		
		if(isShake && waitingCall!=null){
			try {
				Toast.makeText(this, R.string.recording, Toast.LENGTH_LONG).show();
				startRecording(waitingCall.getNumber(), waitingCall.isIncoming());
			} catch (IOException e) {
				log.error( "io",e);
			} catch (InterruptedException e) {
				log.error( "ie",e);
			} catch (ExecutionException e) {
				log.error( "ee",e);
			}
		}
	}
	
	

	
}
