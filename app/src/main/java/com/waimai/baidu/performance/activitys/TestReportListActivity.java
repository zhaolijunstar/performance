
package com.waimai.baidu.performance.activitys;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.waimai.baidu.performance.utils.Settings;
import com.waimai.baidu.wmperformancetool.R;

/**
 * 所有测试报告数据列表
 */
public class TestReportListActivity extends Activity {

    static final String CSV_PATH_KEY = "csvPath";
    //用于传递选中的测试报告数据的路径
    static final String CSV_PATH_KEY_N = "csvPath";
    //用于传递选中的行数
    static final String CSV_PATH_KEY_COUNT = "reportCount";
    //用于传递要对比的性能参数
    static final String PARAMATER_ROW = "paramaterRow";
    //测试报告list适配器
    private TestReportListAdapter testReportListAdapter;
    //测试数据报告列表
    private ListView testReportList;
    //数据对比按钮
    private TextView dataCompareButton;
    //查看单条数据的按钮
    private TextView lookDataButton;
    //位置对应列转换SparseIntArray，后期需要改进
    SparseIntArray positionToColumn = new SparseIntArray();
    //开启MutiDataChartCompareActivity的intent
    Intent dataCompareIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.test_report_list);

        testReportList = findViewById(R.id.test_report_list);
        dataCompareButton = findViewById(R.id.data_compare_button);
        lookDataButton = findViewById(R.id.look_data_button);

        testReportListAdapter = new TestReportListAdapter(listReports());
        testReportList.setAdapter(testReportListAdapter);

        //dialog位置对应列，后期需要改进
        positionToColumn.append(0, 2);
        positionToColumn.append(1, 3);
        positionToColumn.append(2, 4);
        positionToColumn.append(3, 5);
        positionToColumn.append(4, 6);
        positionToColumn.append(5, 11);
        positionToColumn.append(6, 12);
        positionToColumn.append(7, 14);
        positionToColumn.append(8, 16);

        //选择对比按钮后，弹需要对比的性能参数列表浮窗
        dataCompareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Integer> selectedData = getSelectedTestData();

                if (selectedData.size() <= 3) {
                    toSelectCompareParamaterDialog();
                    dataCompareIntent = new Intent();
                    dataCompareIntent.setClass(TestReportListActivity.this, MutiDataChartCompareActivity.class);
                    if (!selectedData.isEmpty()) {
                        for (int j = 0; j < selectedData.size(); j++) {
                            //选中的测试报告数据的对应路径传递过去
                            dataCompareIntent.putExtra(CSV_PATH_KEY_N + j, testReportListAdapter.getCSVPath(selectedData.get(j)));
                        }
                    }
                    dataCompareIntent.putExtra(CSV_PATH_KEY_COUNT, selectedData.size());

                } else {
                    Toast.makeText(TestReportListActivity.this, "最多只能对比三天测试报告", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lookDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Integer> selectedData = getSelectedTestData();

                if (selectedData.size() == 1) {
                    SingleDataDetailActivity.toTestReportDataDetailActivity(TestReportListActivity.this, testReportListAdapter.getCSVPath(selectedData.get(0)));
                } else {
                    Toast.makeText(TestReportListActivity.this, "只能查看一条测试报告", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /**
     * 获得已选的测试数据
     *
     * @return
     */
    private List<Integer> getSelectedTestData() {
        SparseBooleanArray checkedArray = testReportList.getCheckedItemPositions();
        List<Integer> selectedData = new ArrayList<>();
        for (int i = 0; i < checkedArray.size(); i++) {
            if (checkedArray.valueAt(i)) {
                selectedData.add(checkedArray.keyAt(i));
            }
        }
        return selectedData;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 待对比的性能参数浮窗列表
     */
    public void toSelectCompareParamaterDialog() {
        View paramaterDialog = View.inflate(this, R.layout.paramater_dialog, null);//填充ListView布局
        ListView paramaterDialogList = (ListView) paramaterDialog.findViewById(R.id.paramater_dialog_list);//初始化ListView控件
        paramaterDialogList.setAdapter(new ParamaterDailogListAdapter(this));//ListView设置适配器

        final AlertDialog parkIdsdialog = new AlertDialog.Builder(this)
                .setTitle("选择对比参数").setView(paramaterDialog)//在这里把写好的这个listview的布局加载dialog中
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        parkIdsdialog.show();

        paramaterDialogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parkIdsdialog.dismiss();
                dataCompareIntent.putExtra(PARAMATER_ROW, positionToColumn.get(position));
                startActivity(dataCompareIntent);
            }
        });

    }


    /**
     * 测试报告list适配器
     */
    private class TestReportListAdapter extends BaseAdapter {
        List<String> reports;

        public TestReportListAdapter(List<String> reports) {
            this.reports = reports;
        }

        @Override
        public int getCount() {
            return reports.size();
        }

        @Override
        public Object getItem(int position) {
            return reports.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public String getCSVPath(int position) {
            return Settings.getEMMAGEE_RESULT_DIR() + getItem(position) + ".csv";
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String pr = reports.get(position);
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.test_list_item, parent, false);
            Viewholder holder = (Viewholder) convertView.getTag();
            if (holder == null) {
                holder = new Viewholder();
                convertView.setTag(holder);
                holder.name = convertView.findViewById(R.id.package_name);
            }
            holder.name.setText(pr);
            return convertView;
        }

    }

    private static class Viewholder {
        TextView name;
    }

    /**
     * 获取所有的测试数据（的名称）
     */
    private ArrayList<String> listReports() {
        ArrayList<String> reportList = new ArrayList<String>();
        File reportDir = new File(Settings.getEMMAGEE_RESULT_DIR());
        if (reportDir.isDirectory()) {
            File files[] = reportDir.listFiles();
            Arrays.sort(files, Collections.reverseOrder());
            for (File file : files) {
                if (isLegalReport(file)) {
                    String baseName = file.getName().substring(0, file.getName().lastIndexOf("."));
                    reportList.add(baseName);
                }
            }
        }
        return reportList;
    }

    /**
     * 是否是合法的路径
     *
     * @param file
     * @return
     */
    private boolean isLegalReport(File file) {
        return !file.isDirectory() && file.getName().endsWith(".csv");
    }

    public static void toReportList(Context context) {
        Intent intent = new Intent(context, TestReportListActivity.class);
        context.startActivity(intent);
    }


    /**
     * 性能参数浮窗list适配器
     */
    public class ParamaterDailogListAdapter extends BaseAdapter {
        private Activity activity;
        private List<String> compareParamater;


        public ParamaterDailogListAdapter(Activity activity) {
            this.activity = activity;
            compareParamater = new ArrayList<>();
            compareParamater.add("App Used Memory PSS(MB)");
            compareParamater.add("App Used Memory (%)");
            compareParamater.add("System Available Memory (MB)");
            compareParamater.add("App Used CPU (%)");
            compareParamater.add("Total Used CPU (%)");
            compareParamater.add("Net Traffic (KB)");
            compareParamater.add("Battery (%)");
            compareParamater.add("Temperature (C)");
            compareParamater.add("FPS");


        }

        @Override
        public int getCount() {
            return compareParamater.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = View.inflate(activity, R.layout.paramater_dialog_item, null);
            TextView paramaterName = (TextView) view.findViewById(R.id.paramater_name);
            paramaterName.setText(compareParamater.get(i));
            return view;
        }
    }
}
