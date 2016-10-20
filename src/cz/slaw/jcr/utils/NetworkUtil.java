package cz.slaw.jcr.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import cz.slaw.jcr.listeners.ConnType;

public class NetworkUtil {

	public static ConnType getNetworkConnection(Context ctx) {
		ConnType type=ConnType.OFF;
		
	    ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    if(netInfo!=null)
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                type=ConnType.WIFI;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	            	type=ConnType.MOBILE;
	    }
	    return type;
	}
}
