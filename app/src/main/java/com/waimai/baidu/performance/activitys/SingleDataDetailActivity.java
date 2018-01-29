
package com.waimai.baidu.performance.activitys;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.waimai.baidu.wmperformancetool.R;

import org.apache.commons.io.FileUtils;

/**
 * 单条测试数据详情页
 */

public class SingleDataDetailActivity extends Activity {

	private TableLayout tl;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.test_report);

		TextView title = findViewById(R.id.nb_title);
		ImageView btnSave = findViewById(R.id.btn_set);
		tl = findViewById(R.id.table_layout);

		btnSave.setVisibility(ImageView.INVISIBLE);
		title.setText(R.string.test_report);

		LinearLayout layGoBack = findViewById(R.id.lay_go_back);

		layGoBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SingleDataDetailActivity.this.finish();
			}
		});

		Intent intent = getIntent();
		String csvPath = intent.getStringExtra(TestReportListActivity.CSV_PATH_KEY);
		
		try {
			String content = FileUtils.readFileToString(new File(csvPath), "gbk");
			String[] lines = content.split("\r\n");
			int index = 0;
			for (String line: lines) {
				addTableRow(line, index);
				index++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void addTableRow(String line, int index) {
		TableRow row = new TableRow(this);
		TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
		row.setLayoutParams(lp);
		String[] items = line.split(",");
		int i = 0;
		for (String item: items) {
			TextView tv = new TextView(this);
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(18);
			tv.setText(item);
			tv.setBackgroundResource(R.drawable.table_border);
			if (i != 0) {
				tv.setGravity(Gravity.RIGHT);
			}
			row.addView(tv, i);
			i++;
		}
		tl.addView(row, index);
	}

	public static void toTestReportDataDetailActivity(Context context,String path){
		Intent intent = new Intent(context, SingleDataDetailActivity.class);
		intent.putExtra(TestReportListActivity.CSV_PATH_KEY,path);
		context.startActivity(intent);
	}

}
