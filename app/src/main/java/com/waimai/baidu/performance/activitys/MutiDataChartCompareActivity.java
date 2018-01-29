package com.waimai.baidu.performance.activitys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.waimai.baidu.performance.tools.LineChartManager;
import com.waimai.baidu.wmperformancetool.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by iwm on 2018/1/24.
 * 多条测试数据对比图
 */

public class MutiDataChartCompareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_chat_compare);
        LineChart lineChart = findViewById(R.id.line_chart);
        LineChartManager lineChartManager = new LineChartManager(lineChart);

        //设置x轴的数据
        ArrayList<Float> xValues = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            xValues.add((float) i);
        }
        //设置y轴数据
        List<List<Float>> yValues = getTestReportData(2);
        List<Float> maxDatas = new ArrayList<Float>();
        List<Float> minDatas = new ArrayList<Float>();
        float maxData = 0, minData = 0;
        for (List<Float> yValue : yValues) {
            if (yValue != null && yValue.size() > 0) {
                maxData = yValue.get(0);
                minData = yValue.get(0);
                for (Float data : yValue) {
                    Log.i("datadataadad", "onCreate: " + data + " ");
                    if (data >= maxData)
                        maxData = data;
                    if (data <= minData)
                        minData = data;
                }
                maxDatas.add(maxData);
                minDatas.add(minData);

                Log.i("datadataadad", "onCreate: " + "nnnnnnnnnnnnnnnnnnnnnnn");
            }

        }


        //颜色集合
        List<Integer> colours = new ArrayList<>();
        colours.add(Color.GREEN);
        colours.add(Color.BLUE);
        colours.add(Color.RED);
//        colours.add(Color.CYAN);

        //线的名字集合
        List<String> names = new ArrayList<>();
        names.add("折线一");
        names.add("折线二");
        names.add("折线三");
//        names.add("折线四");

        lineChartManager.showLineChart(xValues, yValues, names, colours);

//        if (maxDatas != null && maxDatas.size() > 0 && minDatas != null && minDatas.size() > 0) {
//            lineChartManager.setYAxis(Collections.max(maxDatas), Collections.min(minDatas), 11);
//        }
        lineChartManager.setDescription("内存");


    }

    /**
     * 获取所有已选数据的第n列数据
     */
    private List<List<Float>> getTestReportData(int column) {
        Intent intent = getIntent();
        //获得所有已选的文件路径
        String[] lines = null;
        List<List<Float>> compareData = new ArrayList<>();

        for (int i = 0; i < intent.getIntExtra(TestReportListActivity.CSV_PATH_KEY_COUNT, 0); i++) {
            String csvPath = intent.getStringExtra(TestReportListActivity.CSV_PATH_KEY_N + i);
            try {
                String content = FileUtils.readFileToString(new File(csvPath), "gbk");
                lines = content.split("\r\n");
                List<Float> colunmNData = getColunmNData(lines, intent.getIntExtra(TestReportListActivity.PARAMATER_COLUMN, 2));
                Log.i("dasfadsfa", "getTestReportData: "+colunmNData);
                //存储所有已选文件的第n列数据
                compareData.add(colunmNData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return compareData;
    }


    /**
     * 返回第n列的数据
     *
     * @param column
     * @return
     */
    private List<Float> getColunmNData(String[] lines, int column) {
        List<Float> columnData = new ArrayList<>();
        //最后一行数据可能不完整，去掉
        for (int i = 0; i < lines.length - 1; i++) {
            //第9行才是要对比的数据
            if (i > 8) {
                String[] items = lines[i].split(",");
                for (String item : items) {
                    Log.i("getColunmNData", "getColunm: " + item);
                }
                Log.i("getColunmNData", "getColunmNData: " + items.length);
                String data = items[column];
                float dataF = Float.parseFloat(data);
                columnData.add(dataF);
            }
        }
        return columnData;
    }

}
