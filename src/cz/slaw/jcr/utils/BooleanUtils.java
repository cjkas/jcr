package cz.slaw.jcr.utils;

public class BooleanUtils {
	public static boolean isFalse(Boolean bool) {
		if (bool == null)
			return true;
		return !bool;
	}

	public static boolean isTrue(Boolean bool) {
		if (bool == null)
			return false;
		return bool;
	}
}
