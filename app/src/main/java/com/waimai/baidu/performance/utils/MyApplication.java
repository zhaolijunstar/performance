
package com.waimai.baidu.performance.utils;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.view.WindowManager;

import com.github.moduth.blockcanary.BlockCanary;
import com.waimai.baidu.performance.tools.Settings;

/**
 * my application class
 *
 */
public class MyApplication extends Application {

	private static Context sContext;

	private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getMywmParams() {
		return wmParams;
	}

	@Override
	public void onCreate() {
		initAppConfig();
		super.onCreate();
		sContext = this;
		BlockCanary.install(this, new AppContext()).start();

	}
	
	private void initAppConfig() {
		File dir = new File(Settings.getEMMAGEE_RESULT_DIR());
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public static Context getAppContext() {
		return sContext;
	}


}
