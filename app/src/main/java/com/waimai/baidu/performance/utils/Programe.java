
package com.waimai.baidu.performance.utils;

import android.graphics.drawable.Drawable;

/**
 * details of installed processes ,including
 * icon,packagename,pid,uid,processname
 */
public class Programe implements Comparable<Programe> {
	private Drawable icon;
	private String processName;
	private String packageName;
	private int pid;
	private int uid;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {

		this.processName = processName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public int compareTo(Programe arg0) {
		return (this.getProcessName().compareTo(arg0.getProcessName()));
	}
}
