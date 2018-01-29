
package com.waimai.baidu.performance.utils;

import java.io.File;

import android.app.Application;
import android.view.WindowManager;

import com.waimai.baidu.performance.tools.Settings;

/**
 * my application class
 *
 */
public class MyApplication extends Application {

	private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getMywmParams() {
		return wmParams;
	}

	@Override
	public void onCreate() {
		initAppConfig();
		super.onCreate();
	}
	
	private void initAppConfig() {
		File dir = new File(Settings.getEMMAGEE_RESULT_DIR());
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
}
