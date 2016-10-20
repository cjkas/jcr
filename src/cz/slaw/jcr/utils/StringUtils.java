package cz.slaw.jcr.utils;

public class StringUtils {

	public static boolean isNullOrEmpty(String in) {
		if(in==null)
			return true;
		if(in.trim().length()==0)
			return true;
		
		return false;
	}
	
}
