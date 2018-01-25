
package com.waimai.baidu.wmperformancetool;

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
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import utils.Settings;

import static android.R.id.list;

public class TestListActivity extends Activity {

	private static final String LOG_TAG = "Emmagee-"
			+ TestListActivity.class.getSimpleName();
	static final String CSV_PATH_KEY = "csvPath";
	static final String CSV_PATH_KEY_N = "csvPath";
	static final String CSV_PATH_KEY_COUNT = "dataCount";
	static final String CSV_PATH_KEY_COLUMN = "dataColumn";



	private ListAdapter la;
	private ListView lstViReport;
	private TextView dataCompare;
    SparseIntArray positionToColumn = new SparseIntArray();


    @Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.test_list);
		
		TextView title = findViewById(R.id.nb_title);
		lstViReport = findViewById(R.id.test_list);
		ImageView btnSave = findViewById(R.id.btn_set);
		dataCompare = findViewById(R.id.data_compare);

		btnSave.setVisibility(ImageView.INVISIBLE);
		title.setText(R.string.test_report);
		
		LinearLayout layGoBack = findViewById(R.id.lay_go_back);
		
		layGoBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				TestListActivity.this.finish();
			}
		});
		la = new ListAdapter(listReports());
		lstViReport.setAdapter(la);
//		lstViReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//				Intent intent = new Intent();
//				intent.setClass(TestListActivity.this, TestReportActivity.class);
//				intent.putExtra(CSV_PATH_KEY, la.getCSVPath(i));
//
//				startActivity(intent);
//			}
//		});
        positionToColumn.append(0,2);
        positionToColumn.append(1,3);
        positionToColumn.append(2,4);
        positionToColumn.append(3,5);
        positionToColumn.append(4,6);
        positionToColumn.append(5,11);
        positionToColumn.append(6,12);
        positionToColumn.append(7,14);
        positionToColumn.append(8,16);

		dataCompare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                toSelectCompareDataDialog();


//				SparseBooleanArray checkedArray = lstViReport.getCheckedItemPositions();
//				List<Integer> selectedData = new ArrayList<Integer>();
//				for (int i = 0; i < checkedArray.size(); i++) {
//					if (checkedArray.valueAt(i)){
//						Log.i("dsafdsaf", "onClick: "+i);
//						selectedData.add(i);
//
//					}
//				}
//				Intent intent = new Intent();
//				intent.setClass(TestListActivity.this, DataChartCompareActivity.class);
//				if (!selectedData.isEmpty()){
//					for (int i = 0; i < selectedData.size(); i++) {
//						intent.putExtra(CSV_PATH_KEY_N +i, la.getCSVPath(selectedData.get(i)));
//					}
//				}
//				intent.putExtra(CSV_PATH_KEY_COUNT , selectedData.size());
//
//				startActivity(intent);
			}
		});


	}

    @Override
    protected void onResume() {
        super.onResume();
        la.notifyDataSetChanged();
    }

    public void toSelectCompareDataDialog(){
		View bottomView = View.inflate(this,R.layout.carids_dialog,null);//填充ListView布局
		ListView lvCarIds = (ListView) bottomView.findViewById(R.id.lv_carids);//初始化ListView控件
		lvCarIds.setAdapter(new LvCarIdsDailogAdapter(this));//ListView设置适配器

		final AlertDialog parkIdsdialog = new AlertDialog.Builder(this)
				.setTitle("选择对比参数").setView(bottomView)//在这里把写好的这个listview的布局加载dialog中
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

//                        parkIdsdialog.dismiss();
					}
				}).create();
		parkIdsdialog.show();

        lvCarIds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                parkIdsdialog.dismiss();
                SparseBooleanArray checkedArray = lstViReport.getCheckedItemPositions();
                Log.i("dasfdfadfasd", "onItemClick: "+checkedArray.size());
                List<Integer> selectedData = new ArrayList<Integer>();
				for (int i = 0; i < checkedArray.size(); i++) {
                    Log.i("dasfdfadfasd", "onItemClick: "+checkedArray.get(i));
                    if (checkedArray.valueAt(i)){
						Log.i("dsafdsaf", "onClick: "+i);
						selectedData.add(i);

					}
				}
				Intent intent = new Intent();
				intent.setClass(TestListActivity.this, DataChartCompareActivity.class);
				if (!selectedData.isEmpty()){
					for (int j = 0; j < selectedData.size(); j++) {
						intent.putExtra(CSV_PATH_KEY_N +j, la.getCSVPath(selectedData.get(j)));
					}
				}
				intent.putExtra(CSV_PATH_KEY_COUNT , selectedData.size());
				intent.putExtra(CSV_PATH_KEY_COLUMN , positionToColumn.get(position));

				startActivity(intent);
            }
        });

	}
	

	private class ListAdapter extends BaseAdapter {
		List<String> reports;

		public ListAdapter(List<String> reports) {
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
	 * list all test report
	 */
	private ArrayList<String> listReports() {
		ArrayList<String> reportList = new ArrayList<String>();
		File reportDir = new File(Settings.getEMMAGEE_RESULT_DIR());
		if (reportDir.isDirectory()) {
			File files[] = reportDir.listFiles();
			Arrays.sort(files, Collections.reverseOrder());
			for (File file: files) {
				if (isLegalReport(file)) {
					String baseName = file.getName().substring(0, file.getName().lastIndexOf("."));
					reportList.add(baseName);
				}
			}
		}
		return reportList;
	}
	
	private boolean isLegalReport(File file) {
		return !file.isDirectory() && file.getName().endsWith(".csv");
	}

	public static void toReportList(Context context){
		Intent intent = new Intent(context,TestListActivity.class);
		context.startActivity(intent);
	}



	public class LvCarIdsDailogAdapter extends BaseAdapter {
		private Activity activity;
        private List<String> compareParamater;


		public LvCarIdsDailogAdapter(Activity activity) {
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
			view = View.inflate(activity, R.layout.carids_dialog_item,null);
			TextView tvCarId = (TextView) view.findViewById(R.id.tv_carId);
			tvCarId.setText(compareParamater.get(i));
			return view;
		}
	}
}
