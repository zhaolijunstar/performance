
package com.waimai.baidu.wmperformancetool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import service.EmmageeService;
/**
 * Main Page of Emmagee
 *
 * @author andrewleo
 */
public class MainActivity extends Activity {

    private static final String LOG_TAG = "Emmagee-" + MainPageActivity.class.getSimpleName();

    private boolean isServiceStop = false;
    private UpdateReceiver receiver;
    private Button btnTest;
    private Button collectFrequency;
    private Button dataCompare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        collectFrequency = findViewById(R.id.collect_frequency);
        btnTest = findViewById(R.id.start_test);
        dataCompare = findViewById(R.id.data_compare);

        collectFrequency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WMPerformaceTool tool = new WMPerformaceTool(getBaseContext());
                tool.startWMPerformanceTool();
            }
        });

        dataCompare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TestListActivity.toReportList(MainActivity.this);
            }
        });


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
        receiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(EmmageeService.SERVICE_ACTION);
        registerReceiver(receiver, filter);
    }



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



    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
