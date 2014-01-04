package by.xgear.launchhelepr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.litl.leveldb.DB;

public class Utils {

	public final static String DB_PATH = "data";
	public static final String NAME = "name";
	public static final String PACKAGE_NAME = "package_name";
	public static final String IMG = "img";
	
	public static byte[] bytes(String str) {
	    try {
	        return str.getBytes("UTF-8");
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException(e);
	    }
	}
	public static byte[] intToByteArray(int a) {
	    byte[] ret = new byte[4];
	    ret[3] = (byte) (a & 0xFF);   
	    ret[2] = (byte) ((a >> 8) & 0xFF);   
	    ret[1] = (byte) ((a >> 16) & 0xFF);   
	    ret[0] = (byte) ((a >> 24) & 0xFF);
	    return ret;
	}
	public static int byteArrayToInt(byte[] b) {
	    return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
	}
	public static AppDataHolder loadWidgetData(DB db, int appWidgetId) {
		byte[] appName = db.get(bytes(NAME+appWidgetId));
		String appNameStr = null;
		if(appName != null && appName.length > 0 )
			appNameStr = new String(appName);
		
		byte[] appPackageName = db.get(bytes(PACKAGE_NAME+appWidgetId));
		String appPackageNameStr = null;
		if(appPackageName != null && appName.length > 0 )
			appPackageNameStr = new String(appPackageName);
		
		byte[] icon = db.get(bytes(IMG+appWidgetId));
		Bitmap appIcon = null;
		if(icon != null && icon.length > 0 ) {
			InputStream is = new ByteArrayInputStream(icon);
			appIcon = BitmapFactory.decodeStream(is);
		}
		if(appNameStr != null && appIcon != null && appPackageNameStr != null) {
			return new AppDataHolder(appIcon, appNameStr, appPackageNameStr, appWidgetId);
		} else
			return null;
	}

}
