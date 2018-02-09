
package com.waimai.baidu.performance.activitys;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.moduth.blockcanary.BlockCanary;
import com.waimai.baidu.performance.tools.Settings;
import com.waimai.baidu.performance.tools.WMPerformanceTestService;
import com.waimai.baidu.performance.tools.WMPerformaceTestTool;
import com.waimai.baidu.performance.utils.AppContext;
import com.waimai.baidu.wmperformancetool.R;

/**
 * 性能检测工具入口Activity
 */

public class TestMainActivity extends Activity {

    private boolean isServiceStop = false;
    private UpdateReceiver receiver;
    private Button btnTest;
    private Button collectFrequency;
    private Button dataCompare;
    private SharedPreferences preferences;
    private TextView tvTime;
    private Button baseDataTestEntry;
    private Button smothingTestEntry;
    SeekBar timeBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        collectFrequency = findViewById(R.id.collect_frequency);
        btnTest = findViewById(R.id.start_test);
        dataCompare = findViewById(R.id.data_compare_button);
        preferences = Settings.getDefaultSharedPreferences(getApplicationContext());

        collectFrequency.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                View paramaterDialog = View.inflate(TestMainActivity.this, R.layout.collect_frequency_layout, null);
                tvTime = (TextView) paramaterDialog.findViewById(R.id.time);
                timeBar = (SeekBar) paramaterDialog.findViewById(R.id.timeline);
                timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                        tvTime.setText(Integer.toString(arg1 + 1));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                        // when tracking stoped, update preferences
                        int interval = arg0.getProgress();
                        preferences.edit().putInt(Settings.KEY_INTERVAL, interval).commit();
                    }
                });
                
                int interval = preferences.getInt(Settings.KEY_INTERVAL, 3);
                tvTime.setText(interval+"");
                timeBar.setProgress(interval);

                AlertDialog collectFrequencyDialog = new AlertDialog.Builder(TestMainActivity.this)
                        .setTitle("调节采集数据频率").setView(paramaterDialog)//在这里把写好的这个listview的布局加载dialog中
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();

                collectFrequencyDialog.show();
            }
        });

        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                View paramaterDialog = View.inflate(TestMainActivity.this, R.layout.select_test_layout, null);
                baseDataTestEntry = (Button) paramaterDialog.findViewById(R.id.base_data_entry);
                smothingTestEntry = (Button) paramaterDialog.findViewById(R.id.smothing_entry);

                final AlertDialog selectTestDialog = new AlertDialog.Builder(TestMainActivity.this)
                        .setTitle("调节采集数据频率").setView(paramaterDialog)//在这里把写好的这个listview的布局加载dialog中
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
                selectTestDialog.show();

                baseDataTestEntry.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WMPerformaceTestTool tool = new WMPerformaceTestTool(getBaseContext());
                        tool.startWMPerformanceTool();
                        selectTestDialog.dismiss();
                    }
                });

                smothingTestEntry.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BlockCanary.install(TestMainActivity.this, new AppContext()).start();
                        selectTestDialog.dismiss();
                        Toast.makeText(TestMainActivity.this,"已打开流畅性测试开关",Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        dataCompare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TestReportListActivity.toReportList(TestMainActivity.this);
            }
        });


        receiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WMPerformanceTestService.SERVICE_ACTION);
        registerReceiver(receiver, filter);
    }



    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isServiceStop = intent.getExtras().getBoolean("isServiceStop");
            if (isServiceStop) {
                btnTest.setText("开始测试");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isServiceStop) {
            btnTest.setText("开始测试");
        }
    }



    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
