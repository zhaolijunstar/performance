
package com.waimai.baidu.performance.activitys;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import com.waimai.baidu.performance.tools.EmmageeService;
import com.waimai.baidu.performance.tools.WMPerformaceTestTool;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        collectFrequency = findViewById(R.id.collect_frequency);
        btnTest = findViewById(R.id.start_test);
        dataCompare = findViewById(R.id.data_compare_button);

        collectFrequency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WMPerformaceTestTool tool = new WMPerformaceTestTool(getBaseContext());
                tool.startWMPerformanceTool();
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
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
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
