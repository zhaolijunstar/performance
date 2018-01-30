package com.waimai.baidu.performance.tools;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;

import com.waimai.baidu.performance.activitys.TestMainActivity;
import com.waimai.baidu.performance.utils.ProcessInfo;
import com.waimai.baidu.performance.utils.Programe;
import com.waimai.baidu.wmperformancetool.R;

/**
 * 性能检测工具
 */
public class WMPerformaceTestTool {
    private String FilterPKName = "com.baidu.lbs.waimai";

    private static final String LOG_TAG = "WM-" + TestMainActivity.class.getSimpleName();

    private static final int TIMEOUT = 20000;

    private ProcessInfo processInfo;
    private Intent monitorService;
    private int pid, uid;
    private boolean isServiceStop = false;

    private Context mContext;

    public WMPerformaceTestTool(Context context) {
        mContext = context;
    }


    public void startWMPerformanceTool() {
        Log.i(LOG_TAG, "MainActivity::onCreate");
        processInfo = new ProcessInfo();
//        if (Build.VERSION.SDK_INT < 24) {
            monitorService = new Intent();
            monitorService.setClass(mContext, WMPerformanceTestService.class);
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
//        }

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


