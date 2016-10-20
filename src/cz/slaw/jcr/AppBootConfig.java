package cz.slaw.jcr;


import java.io.File;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import cz.slaw.jcr.helpers.AnalyticsTrackers;
import cz.slaw.jcr.helpers.SettingsHelper;
import cz.slaw.jcr.listeners.PhoneListenerService;

public class AppBootConfig extends Application {
	private static Context context;
	private static AppBootConfig mInstance;
	private static final String JCR_LOG = "jcr.log";
	private static final String JCR_LOG_DIR = "logs";
	private static final Logger log = LoggerFactory.getLogger(AppBootConfig.class);
	
	@Override
	public void onCreate() {
		configureLogbackDirectly();
		//LOG after logger config init
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String plang = preferences.getString(SettingsHelper.PREF_LANG, SettingsHelper.PREF_LANG_DEF);
		if(!SettingsHelper.PREF_LANG_DEF.equals(plang)){
			changeLang(plang,getBaseContext());
		}
		
		super.onCreate();
		context = getApplicationContext();
		if(SettingsHelper.isRecordingEnabled(this)){
			startService();
		}
		AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
		log.debug("app start end");
	}
	
	public static void restartApplication(){
		
	    Intent i = new Intent(AppBootConfig.getContext(), MagicAppRestart.class);
	    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    AppBootConfig.getContext().startActivity(i);
//	    finish();
	}
	
	public static void changeLang(String plang,Context baseContext) {
		log.debug("changing lang to :"+plang);
		Locale myLocale = new Locale(plang);
	    Locale.setDefault(myLocale);
	    android.content.res.Configuration config = new android.content.res.Configuration();
	    config.locale = myLocale;
	    baseContext.getResources().updateConfiguration(config, baseContext.getResources().getDisplayMetrics());		
	}

// 	Method to start the service
	public static void startService() {
		if(!isServiceRunning(PhoneListenerService.class)){
			context.startService(new Intent(context, PhoneListenerService.class));
		}
	}
	
//	Method to stop the service
	public static void stopService() {
		context.stopService(new Intent(context, PhoneListenerService.class));
	}
	
	public static boolean isServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void configureLogbackDirectly() {
	    // reset the default context (which may already have been initialized)
	    // since we want to reconfigure it
	    LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
	    lc.reset();

	    // setup FileAppender
	    PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
	    encoder1.setContext(lc);
	    encoder1.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
	    encoder1.start();
	    
	    File bdir = SettingsHelper.getStorageDir(this);
	    File logdir = new File(bdir,JCR_LOG_DIR);
	    if(!logdir.exists()){
	    	logdir.mkdirs();
	    }
	    File file = new File(logdir,JCR_LOG);
	    
	    RollingFileAppender<ILoggingEvent> rollingFileAppender  = new RollingFileAppender<ILoggingEvent>();
	    rollingFileAppender.setContext(lc);
	    rollingFileAppender.setFile(file.getAbsolutePath());
	    rollingFileAppender.setEncoder(encoder1);
	    
	    FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
	    rollingPolicy.setContext(lc);
	    rollingPolicy.setFileNamePattern(logdir.getAbsolutePath()+File.separator+JCR_LOG+".%i.zip");
	    rollingPolicy.setMinIndex(1);
	    rollingPolicy.setMaxIndex(2);
	    rollingPolicy.setParent(rollingFileAppender);
	    rollingPolicy.start();
	    
	    
	    SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
	    triggeringPolicy.setMaxFileSize("5MB");
	    triggeringPolicy.start();
	    
	    rollingFileAppender.setRollingPolicy(rollingPolicy);
	    rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
	    rollingFileAppender.start();
	    
	    // setup LogcatAppender
	    PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
	    encoder2.setContext(lc);
	    encoder2.setPattern("[%thread] %msg%n");
	    encoder2.start();
	    
	    LogcatAppender logcatAppender = new LogcatAppender();
	    logcatAppender.setContext(lc);
	    logcatAppender.setEncoder(encoder2);
	    logcatAppender.start();

	    // add the newly created appenders to the root logger;
	    // qualify Logger to disambiguate from org.slf4j.Logger
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	    root.addAppender(rollingFileAppender);
	    root.addAppender(logcatAppender);
	    
	}

	public static synchronized Context getContext() {
		return context;
	}
	
	public static synchronized AppBootConfig getInstance() {
        return mInstance;
    }
 
    public synchronized Tracker getGoogleAnalyticsTracker() {
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.get(AnalyticsTrackers.Target.APP);
    }
 
    /***
     * Tracking screen view
     *
     * @param screenName screen name to be displayed on GA dashboard
     */
    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();
 
        // Set screen name.
        t.setScreenName(screenName);
 
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
 
        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }
 
    /***
     * Tracking exception
     *
     * @param e exception to be tracked
     */
    public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getGoogleAnalyticsTracker();
 
            t.send(new HitBuilders.ExceptionBuilder()
                            .setDescription(
                                    new StandardExceptionParser(this, null)
                                            .getDescription(Thread.currentThread().getName(), e))
                            .setFatal(false)
                            .build()
            );
        }
    }
 
    /***
     * Tracking event
     *
     * @param category event category
     * @param action   action of the event
     * @param label    label
     */
    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();
 
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

}
