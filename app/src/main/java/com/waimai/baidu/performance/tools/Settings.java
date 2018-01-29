package com.waimai.baidu.performance.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.waimai.baidu.performance.utils.WakeLockHelper;

import java.io.File;

/**
 * Parameters in Setting Activity
 * 
 * @author yrom
 * 
 */
public final class Settings {

	public static final String KEY_SENDER = "sender";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_RECIPIENTS = "recipients";
	public static final String KEY_SMTP = "smtp";
	public static final String KEY_ISFLOAT = "isfloat";
	public static final String KEY_INTERVAL = "interval";
	public static final String KEY_ROOT = "root";
	public static final String KEY_AUTO_STOP = "autoStop";
	public static final String KEY_WACK_LOCK = "wakeLock";
	public static final String EMMAGEE_RESULT_DIR = "/sdcard/Emmagee/";
	private static WakeLockHelper wakeLockHelper;
	
	public static SharedPreferences getDefaultSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static WakeLockHelper getDefaultWakeLock(Context context) {
		if (wakeLockHelper == null) {
			wakeLockHelper = new WakeLockHelper(context);
		}
		return wakeLockHelper;
	}

	public static String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
		if(sdCardExist)
		{
			sdDir = Environment.getExternalStorageDirectory();//获取跟目录
		}
		return sdDir.toString();
	}

	public static String getEMMAGEE_RESULT_DIR(){
		return getSDPath() +"/" + "Emmagee"+"/";
	}


}
