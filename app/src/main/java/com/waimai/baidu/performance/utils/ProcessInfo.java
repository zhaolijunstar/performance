
package com.waimai.baidu.performance.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

/**
 * get information of processes
 * 
 * @author andrewleo
 */
public class ProcessInfo {

	private static final String LOG_TAG = "Emmagee-"
			+ ProcessInfo.class.getSimpleName();

	private static final String PACKAGE_NAME = "com.waimai.baidu.performance";
	private static final int ANDROID_M = 22;

	/**
	 * get information of all running processes,including package name ,process
	 * name ,icon ,pid and uid.
	 * 
	 * @param context
	 *            context of activity
	 * @return running processes list
	 */
	public List<Programe> getRunningProcess(Context context) {
		Log.i(LOG_TAG, "get running processes");
		List<Programe> progressList = new ArrayList<Programe>();
		PackageManager pm = context.getPackageManager();

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		//得到所有运行时的进程
		List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
		//得到所有应用信息
		for (ApplicationInfo appinfo : getPackagesInfo(context)) {
			Programe programe = new Programe();

			if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
					|| ((appinfo.processName != null) && (appinfo.processName
							.equals(PACKAGE_NAME)))) {
				continue;
			}
			//匹配进程包名和应用包名
			for (RunningAppProcessInfo runningProcess : run) {
				if ((runningProcess.processName != null)
						&& runningProcess.processName
								.equals(appinfo.processName)) {
					programe.setPid(runningProcess.pid);
					programe.setUid(runningProcess.uid);
					break;
				}
			}
			programe.setPackageName(appinfo.processName);
			programe.setProcessName(appinfo.loadLabel(pm).toString());
			programe.setIcon(appinfo.loadIcon(pm));
			progressList.add(programe);
		}
		Collections.sort(progressList);
		return progressList;
	}

	/**
	 * get pid by package name
	 * 
	 * @param context
	 *            context of activity
	 * @return pid
	 */
	public int getPidByPackageName(Context context, String packageName) {
		Log.i(LOG_TAG, "start getLaunchedPid");
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// Note: getRunningAppProcesses return itself in API 22
		if (Build.VERSION.SDK_INT < ANDROID_M) {
			List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
			for (RunningAppProcessInfo runningProcess : run) {
				//当进程名和包名相同时，返回这个进程的pid
				if ((runningProcess.processName != null)
						&& runningProcess.processName.equals(packageName)) {
					return runningProcess.pid;
				}
			}
		} else {
			Log.i(LOG_TAG, "use top command to get pid");
			try {
				Process p = Runtime.getRuntime().exec("top -m 100 -n 1");
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					if (line.contains(packageName)) {
						line = line.trim();
						String[] splitLine = line.split("\\s+");
						if (packageName.equals(splitLine[splitLine.length - 1])) {
							return Integer.parseInt(splitLine[0]);
						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * get information of all installed packages
	 * 
	 * @param context
	 *            context of activity
	 * @return all installed packages
	 */
	public List<Programe> getAllPackages(Context context) {
		Log.i(LOG_TAG, "getAllPackages");
		List<Programe> progressList = new ArrayList<Programe>();
		PackageManager pm = context.getPackageManager();

		for (ApplicationInfo appinfo : getPackagesInfo(context)) {
			Programe programe = new Programe();
			if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
					|| ((appinfo.processName != null) && (appinfo.processName
							.equals(PACKAGE_NAME)))) {
				continue;
			}
			programe.setPackageName(appinfo.processName);
			programe.setProcessName(appinfo.loadLabel(pm).toString());
			programe.setIcon(appinfo.loadIcon(pm));
			progressList.add(programe);
		}
		Collections.sort(progressList);
		return progressList;
	}

	/**
	 * get information of all applications.
	 * 
	 * @param context
	 *            context of activity
	 * @return packages information of all applications
	 */
	private List<ApplicationInfo> getPackagesInfo(Context context) {
		PackageManager pm = context.getApplicationContext().getPackageManager();
		List<ApplicationInfo> appList = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		return appList;
	}

	/**
	 * get pid by package name
	 * 
	 * @param context
	 *            context of activity
	 * @param packageName
	 *            package name of monitoring app
	 * @return pid
	 */
	public Programe getProgrameByPackageName(Context context, String packageName) {
		if (Build.VERSION.SDK_INT < ANDROID_M) {
			List<Programe> processList = getRunningProcess(context);
			for (Programe programe : processList) {
				if ((programe.getPackageName() != null)
						&& (programe.getPackageName().equals(packageName))) {
					return programe;
				}
			}
		} else {
			Programe programe = new Programe();
			int pid = getPidByPackageName(context, packageName);
			programe.setPid(pid);
			programe.setUid(0);
			return programe;
		}
		return null;
	}

	/**
	 * get top activity name
	 * 
	 * @param context
	 *            context of activity
	 * @return top activity name
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// Note: getRunningTasks is deprecated in API 21(Official)
		//getRunningTask方法在Android5.0以上已经被废弃，只会返回自己和系统的一些不敏感的task，不再返回其他应用的task，用此方法来判断自身App是否处于后台，仍然是有效的，但是无法判断其他应用是否位于前台
		//因此集成之后，可以开放此方法
		if (Build.VERSION.SDK_INT >= 21) {
			return Constants.NA;
		}
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		if (runningTaskInfos != null)
			return (runningTaskInfos.get(0).topActivity).toString();
		else
			return null;
	}
}
