package com.waimai.baidu.wmperformancetool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import service.EmmageeService;
import utils.ProcessInfo;
import utils.Programe;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import service.EmmageeService;
import utils.ProcessInfo;
import utils.Programe;

public class WMPerformaceTool {
    private String FilterPKName = "com.baidu.lbs.waimai";

    private static final String LOG_TAG = "Emmagee-" + MainPageActivity.class.getSimpleName();

    private static final int TIMEOUT = 20000;

    private ProcessInfo processInfo;
    private Intent monitorService;
    private ListView lstViProgramme;
    private Button btnTest;
    private int pid, uid;
    private boolean isServiceStop = false;

    private TextView nbTitle;
    private ImageView ivGoBack;
    private ImageView ivBtnSet;
    private LinearLayout layBtnSet;
    private Long mExitTime = (long) 0;
    private Context mContext;

    public WMPerformaceTool(Context context) {
        mContext = context;
    }


    public void startWMPerformanceTool() {
        Log.i(LOG_TAG, "MainActivity::onCreate");
        processInfo = new ProcessInfo();
        if (Build.VERSION.SDK_INT < 24) {
            monitorService = new Intent();
            monitorService.setClass(mContext, EmmageeService.class);
            List<Programe> allProgrames = processInfo.getAllPackages(mContext);
            String packageName = null;
            String processName = null;
            for (Programe pm : allProgrames) {
                if (pm.getPackageName().contains(FilterPKName)) {
                    packageName = pm.getPackageName();
                    processName = pm.getProcessName();
                }
            }
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            String startActivity = "";
            Log.d(LOG_TAG, packageName);
            // clear logcat
            try {
                Runtime.getRuntime().exec("logcat -c");
            } catch (IOException e) {
                Log.d(LOG_TAG, e.getMessage());
            }
            try {
                startActivity = intent.resolveActivity(mContext.getPackageManager()).getShortClassName();
                mContext.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(mContext, mContext.getString(R.string.can_not_start_app_toast), Toast.LENGTH_LONG).show();
                return;
            }
            waitForAppStart(packageName);
            monitorService.putExtra("processName", processName);
            monitorService.putExtra("pid", pid);
            monitorService.putExtra("uid", uid);
            monitorService.putExtra("packageName", packageName);
            monitorService.putExtra("startActivity", startActivity);
            mContext.startService(monitorService);
            isServiceStop = false;
            btnTest.setText(mContext.getString(R.string.stop_test));
        }

    }

    private void waitForAppStart(String packageName) {
        Log.d(LOG_TAG, "wait for app start");
        boolean isProcessStarted = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            pid = processInfo.getPidByPackageName(mContext, packageName);
            if (pid != 0) {
                isProcessStarted = true;
                break;
            }
            if (isProcessStarted) {
                break;
            }
        }
    }


}


