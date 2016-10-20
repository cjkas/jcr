package cz.slaw.jcr.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.MimeTypeMap;
import cz.slaw.jcr.domain.DbRecord;
import cz.slaw.jcr.listeners.ConnectionState;

public class GoogleDriveHelper implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final Logger log = LoggerFactory.getLogger(GoogleDriveHelper.class);
	
	private ConnectionState notifyChanged;
	public static final int RESOLVE_CONNECTION_REQUEST_CODE = 12344;
	
	private final Context ctx;
	private final Activity activity;
	private File file;
	
	private GoogleApiClient googleApiClient;
	
	
	public GoogleDriveHelper(Context ctx,Activity activity,ConnectionState notifyChanged) {
		this.ctx=ctx;
		this.activity=activity;
		this.notifyChanged=notifyChanged;
	}
	
	private void notifyChanged(boolean connected){
		if(notifyChanged!=null)
			notifyChanged.stateChange(connected);
	}
	
	public void start() {
		if(googleApiClient==null){
			googleApiClient = new GoogleApiClient.Builder(ctx)
				.addApi(Drive.API)
		        .addScope(Drive.SCOPE_FILE)
		        .addScope(Drive.SCOPE_APPFOLDER)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		}
	}
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		log.error("conn failed"+connectionResult);
		loggedNo();
		if (connectionResult.hasResolution()) {
			if(activity!=null){
				try {
					connectionResult.startResolutionForResult(activity, RESOLVE_CONNECTION_REQUEST_CODE);
				} catch (IntentSender.SendIntentException e) {
					log.error( "se", e);
				}
			}else{
				log.error( "need to show login activity");
			}
		} else {
			if(activity!=null){
				GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
			}else{
				log.error( "need to show error code:"+connectionResult.getErrorCode());
			}
		}
	}
	
	@Override
	public void onConnectionSuspended(int arg0) {
		log.debug("conn suspended");
	}

	public boolean isConnected() {
		if(googleApiClient!=null && googleApiClient.isConnected()){
			loggedOk();
			return true;
		}
		loggedNo();
		return false;
	}
	private boolean loggedNo() {
		loggedIn(false);
		return false;
	}
	private boolean loggedOk() {
		loggedIn(true);
		return true;
	}

	private void loggedIn(boolean userLoggedIn) {
		log.debug("loggedIn"+userLoggedIn);
		notifyChanged(userLoggedIn);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor edit = sharedPrefs.edit();
		
		if (!userLoggedIn) {
			edit.remove(SettingsHelper.PREF_GDRIVE);
		}else{
			edit.putBoolean(SettingsHelper.PREF_GDRIVE, true);
		}
		
		edit.commit();
	}
    public void onPause() {
    	log.info("paused");
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    public void logout() {
		log.debug("logout disc");
		googleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {
			@Override
			public void onResult(Status arg0) {
				googleApiClient.disconnect();
				loggedIn(false);				
			}
		});
		
	}

    public void login() {
		log.debug("connect");
		googleApiClient.connect();
	}
    
	//
    public void newFile(final File inFile) {
    	log.debug("new file "+inFile.getAbsolutePath());
    	
    	final ResultCallback<MetadataResult> newFileCreate = new ResultCallback<MetadataResult>() {
    		@Override
    		public void onResult(MetadataResult result) {
    			if (!result.getStatus().isSuccess()) {
    				log.error( "Error while trying to access appdir");
    				makeBaseFolder();
    			}else{
    				Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(uploadFile);
    			}
    		}
    		final ResultCallback<DriveContentsResult> uploadFile=new ResultCallback<DriveApi.DriveContentsResult>() {
    			@Override
    			public void onResult(final DriveContentsResult result) {
    			        new Thread() {
    			            @Override
    			            public void run() {
    			            	DriveContents driveContents = result.getDriveContents();
    							try {
    								FileInputStream fis = new FileInputStream(inFile);
    								OutputStream out = driveContents.getOutputStream();
    				                byte[] buf = new byte[1024];
    				                int len;
    				                while ((len = fis.read(buf)) > 0) {
    				                	out.write(buf, 0, len);
    				                }
    				                fis.close();
    				                out.close();
    							} catch (IOException e) {
    								log.error("IOE",e);
    							}
    							
    							String extension = MimeTypeMap.getFileExtensionFromUrl(inFile.toURI().toString());
    							String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    							
    			                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
    			                        .setTitle(inFile.getName())
    			                        .setMimeType(mimeType)
    			                        .setStarred(true).build();

    			                Drive.DriveApi.getFolder(googleApiClient, DriveId.decodeFromString(SettingsHelper.getGdriveFolderId(ctx)))
    			                        .createFile(googleApiClient, changeSet, driveContents)
    			                        .setResultCallback(fileCallback);
    			            }
    			        }.start();				
    			}
    		};
    		final private ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
    			@Override
    			public void onResult(DriveFileResult result) {
    				if (!result.getStatus().isSuccess()) {
    					log.error( "Error while trying to create file");
    				}else{
    					log.info("Created a file in App Folder: " + result.getDriveFile().getDriveId());
    					file=null;
    					googleApiClient.disconnect();
    				}
    			}
    		};
    	};
    	
    	
		DriveFolder root = Drive.DriveApi.getFolder(googleApiClient, DriveId.decodeFromString(SettingsHelper.getGdriveFolderId(ctx)));
		root.getMetadata(googleApiClient).setResultCallback(newFileCreate);
	}
	@Override
	public void onConnected(Bundle arg0) {
		loggedOk();
		String gdriveFolderId = SettingsHelper.getGdriveFolderId(ctx);
		log.info("get drive by id "+gdriveFolderId);
		if(gdriveFolderId==null){
			makeBaseFolder();
		}else{
			log.info("folder already created");
			checkBaseFolder(gdriveFolderId);
			uploadIfFile();
		}
	}
	private void uploadIfFile() {
		if(file!=null)
			newFile(file);
	}

	private void checkBaseFolder(String gdriveFolderId) {
		log.debug("check base folder");
		Drive.DriveApi.getFolder(googleApiClient, DriveId.decodeFromString(gdriveFolderId)).getMetadata(googleApiClient).setResultCallback(baseFolderResult);		
	}

	final private ResultCallback<MetadataResult> baseFolderResult = new ResultCallback<MetadataResult>() {
        @Override
        public void onResult(MetadataResult result) {
            if (!result.getStatus().isSuccess()) {
                log.error("Cannot find DriveId. Are you authorized to view this file ?");
                makeBaseFolder();
            }else{
            	Metadata metadata = result.getMetadata();
                if(metadata.isTrashed()){
                    log.trace("Folder is trashed make new");
                    makeBaseFolder();
                }else{
                    log.trace("Folder is ok"); 
                }
            }
        }
    };
	//BASE FOLDER
	private void makeBaseFolder() {
		log.info("base folder create");
		Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(folderCreate);	
	}
	final private ResultCallback<DriveContentsResult> folderCreate = new ResultCallback<DriveContentsResult>() {
		@Override
		public void onResult(DriveContentsResult result) {
			if (!result.getStatus().isSuccess()) {
				log.error( "Error while trying to create App folder");
				return;
			}
			MetadataChangeSet newFolder = new MetadataChangeSet.Builder().setTitle("JCR").build();
			Drive.DriveApi.getRootFolder(googleApiClient).createFolder(googleApiClient, newFolder).setResultCallback(folderCreateResult);
		}
		final ResultCallback<DriveFolderResult> folderCreateResult = new ResultCallback<DriveFolderResult>() {
	        @Override
	        public void onResult(DriveFolderResult result) {
	            if (!result.getStatus().isSuccess()) {
	                log.error("Error while trying to create the folder");
	            }else{
	            	log.info("Created a folder: " + result.getDriveFolder().getDriveId());
	            	SettingsHelper.setGdriveFolderId(ctx,result.getDriveFolder().getDriveId().encodeToString());
	            }
	        }
	    };
	};

	public void upload(DbRecord dbRecord) {
		start();
		this.file = new File(SettingsHelper.getStorageDir(ctx),dbRecord.getPath());
	}

}
