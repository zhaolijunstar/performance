/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.waimai.baidu.wmperformancetool;

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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import service.EmmageeService;
import utils.ProcessInfo;
import utils.Programe;

/**
 * Main Page of Emmagee
 *
 * @author andrewleo
 */
public class MainActivity extends Activity {
    private String FilterPKName = "com.baidu.lbs.waimai";

    private static final String LOG_TAG = "Emmagee-" + MainPageActivity.class.getSimpleName();

    private static final int TIMEOUT = 20000;

    private ProcessInfo processInfo;
    private Intent monitorService;
    private ListView lstViProgramme;
    private Button btnTest;
    private int pid, uid;
    private boolean isServiceStop = false;
    private UpdateReceiver receiver;

    private TextView nbTitle;
    private ImageView ivGoBack;
    private ImageView ivBtnSet;
    private LinearLayout layBtnSet;
    private Long mExitTime = (long) 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "MainActivity::onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        btnTest = findViewById(R.id.test);

        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WMPerformaceTool tool = new WMPerformaceTool(getBaseContext());
                tool.startWMPerformanceTool();
            }
        });


        LineChart mLineChart = (LineChart) findViewById(R.id.lineChart);
        //显示边界
        mLineChart.setDrawBorders(true);
        //设置数据
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(new Entry(i, (float) (Math.random()) * 80));
        }
        //一个LineDataSet就是一条线
        LineDataSet lineDataSet = new LineDataSet(entries, "温度");
        LineData data = new LineData(lineDataSet);
        mLineChart.setData(data);
//        btnTest = findViewById(R.id.test);
//        processInfo = new ProcessInfo();
//        btnTest.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (Build.VERSION.SDK_INT < 24) {
//                    monitorService = new Intent();
//                    monitorService.setClass(MainActivity.this, EmmageeService.class);
//                    if (getString(R.string.start_test).equals(btnTest.getText().toString())) {
//                        List<Programe> allProgrames = processInfo.getAllPackages(getBaseContext());
//                        String packageName = null;
//                        String processName = null;
//                        for (Programe pm : allProgrames) {
//                            if (pm.getPackageName().contains(FilterPKName)) {
//                                packageName = pm.getPackageName();
//                                processName = pm.getProcessName();
//                            }
//                        }
//                        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
//                        String startActivity = "";
//                        Log.d(LOG_TAG, packageName);
//                        // clear logcat
//                        try {
//                            Runtime.getRuntime().exec("logcat -c");
//                        } catch (IOException e) {
//                            Log.d(LOG_TAG, e.getMessage());
//                        }
//                        try {
//                            startActivity = intent.resolveActivity(getPackageManager()).getShortClassName();
//                            startActivity(intent);
//                        } catch (Exception e) {
//                            Toast.makeText(MainActivity.this, getString(R.string.can_not_start_app_toast), Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                        waitForAppStart(packageName);
//                        monitorService.putExtra("processName", processName);
//                        monitorService.putExtra("pid", pid);
//                        monitorService.putExtra("uid", uid);
//                        monitorService.putExtra("packageName", packageName);
//                        monitorService.putExtra("startActivity", startActivity);
//                        startService(monitorService);
//                        isServiceStop = false;
//                        btnTest.setText(getString(R.string.stop_test));
//
//                    } else {
//                        btnTest.setText(getString(R.string.start_test));
//                        Toast.makeText(MainActivity.this, getString(R.string.test_result_file_toast) + EmmageeService.resultFilePath,
//                                Toast.LENGTH_LONG).show();
//                        stopService(monitorService);
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, getString(R.string.nougat_warning), Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//
//        receiver = new UpdateReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(EmmageeService.SERVICE_ACTION);
//        registerReceiver(receiver, filter);
    }

    /**
     * customized BroadcastReceiver
     *
     * @author andrewleo
     */
    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isServiceStop = intent.getExtras().getBoolean("isServiceStop");
            if (isServiceStop) {
                btnTest.setText(getString(R.string.start_test));
            }
        }
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        if (isServiceStop) {
            btnTest.setText(getString(R.string.start_test));
        }
    }

    /**
     * wait for test application started.
     *
     * @param packageName package name of test application
     */
    private void waitForAppStart(String packageName) {
        Log.d(LOG_TAG, "wait for app start");
        boolean isProcessStarted = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + TIMEOUT) {
            pid = processInfo.getPidByPackageName(getBaseContext(), packageName);
            if (pid != 0) {
                isProcessStarted = true;
                break;
            }
            if (isProcessStarted) {
                break;
            }
        }
    }

    /**
     * show a dialog when click return key.
     *
     * @return Return true to prevent this event from being propagated further,
     * or false to indicate that you have not handled this event and it
     * should continue to be propagated.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, R.string.quite_alert, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                if (monitorService != null) {
                    Log.d(LOG_TAG, "stop service");
                    stopService(monitorService);
                }
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
