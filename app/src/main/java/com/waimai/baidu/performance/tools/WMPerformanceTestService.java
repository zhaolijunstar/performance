
package com.waimai.baidu.performance.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.waimai.baidu.performance.activitys.TestMainActivity;
import com.waimai.baidu.wmperformancetool.R;

import com.waimai.baidu.performance.utils.Constants;
import com.waimai.baidu.performance.utils.CpuInfo;
import com.waimai.baidu.performance.utils.CurrentInfo;
import com.waimai.baidu.performance.utils.EncryptData;
import com.waimai.baidu.performance.utils.FpsInfo;
import com.waimai.baidu.performance.utils.MemoryInfo;
import com.waimai.baidu.performance.utils.MyApplication;
import com.waimai.baidu.performance.utils.ProcessInfo;
import com.waimai.baidu.performance.utils.Programe;

/**
 * Service running in background
 */
public class WMPerformanceTestService extends Service {

    private final static String LOG_TAG = "WMPerformace-"
            + WMPerformanceTestService.class.getSimpleName();

    private static final String BLANK_STRING = "";

    private WindowManager windowManager = null;
    private WindowManager.LayoutParams wmParams = null;
    private View viFloatingWindow;
    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;
    private TextView txtTotalMem;
    private TextView txtUnusedMem;
    private TextView txtTraffic;
    private TextView btnStop;
    private TextView btnStart;
    private TextView recodeDataTip;
    private int delaytime;
    private DecimalFormat fomart;
    private MemoryInfo memoryInfo;
    private Handler handler = new Handler();
    private CpuInfo cpuInfo;
    private boolean isFloating;
    private boolean isRoot;
    private boolean isAutoStop = false;
    private String processName, packageName, startActivity;
    private int pid, uid;
    private boolean isServiceStop = false;
    private String sender, password, recipients, smtp;
    private String[] receivers;
    private EncryptData des;
    private ProcessInfo procInfo;
    private int statusBarHeight;

    public static BufferedWriter bw;
    public static FileOutputStream out;
    public static OutputStreamWriter osw;
    public static String resultFilePath;
    public static boolean isStop = false;

    private String totalBatt;
    private String temperature;
    private String voltage;
    private CurrentInfo currentInfo;
    private FpsInfo fpsInfo;
    private BatteryInfoBroadcastReceiver batteryBroadcast = null;

    // get start time
    private static final int MAX_START_TIME_COUNT = 5;
    private static final String START_TIME = "#startTime";
    private int getStartTimeCount = 0;
    private boolean isGetStartTime = true;
    private String startTime = "";
    public static final String SERVICE_ACTION = "com.waimai.action.performanceService";
    private static final String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
    //写入数据开关
    private boolean recodeDataSwitch = false;
    //是否已经创建表格头
    private boolean isCreateResultCsv = false;

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "service onCreate");
        super.onCreate();
        isServiceStop = false;
        isStop = false;
        fpsInfo = new FpsInfo();
        memoryInfo = new MemoryInfo();
        procInfo = new ProcessInfo();
        fomart = new DecimalFormat();
        fomart.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        fomart.setGroupingUsed(false);
        fomart.setMaximumFractionDigits(2);
        fomart.setMinimumFractionDigits(0);
        des = new EncryptData("emmagee");
        currentInfo = new CurrentInfo();
        statusBarHeight = getStatusBarHeight();
        batteryBroadcast = new BatteryInfoBroadcastReceiver();
        registerReceiver(batteryBroadcast, new IntentFilter(BATTERY_CHANGED));
    }

    /**
     * 电池信息监控监听器
     */
    public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                totalBatt = String.valueOf(level * 100 / scale);
                voltage = String.valueOf(intent.getIntExtra(
                        BatteryManager.EXTRA_VOLTAGE, -1) * 1.0 / 1000);
                temperature = String.valueOf(intent.getIntExtra(
                        BatteryManager.EXTRA_TEMPERATURE, -1) * 1.0 / 10);
            }

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PendingIntent contentIntent = PendingIntent.getActivity(
                getBaseContext(), 0, new Intent(this, TestMainActivity.class),
                0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.waimai_logo)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true)
                .setContentTitle("WMPerformace");
        startForeground(startId, builder.build());

        pid = intent.getExtras().getInt("pid");
        //uid = intent.getExtras().getInt("uid");
        processName = intent.getExtras().getString("processName");
        packageName = intent.getExtras().getString("packageName");
        startActivity = intent.getExtras().getString("startActivity");

        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ainfo = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
            uid = ainfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        cpuInfo = new CpuInfo(getBaseContext(), pid, Integer.toString(uid));
        readSettingInfo();
        if (isFloating) {
            viFloatingWindow = LayoutInflater.from(this).inflate(
                    R.layout.floating, null);
            txtUnusedMem = viFloatingWindow
                    .findViewById(R.id.memunused);
            txtTotalMem = viFloatingWindow
                    .findViewById(R.id.memtotal);
            txtTraffic = viFloatingWindow.findViewById(R.id.traffic);
            btnStart = viFloatingWindow.findViewById(R.id.start);
            recodeDataTip = viFloatingWindow.findViewById(R.id.tip);

            txtUnusedMem.setText(getString(R.string.waiting));
            txtUnusedMem.setTextColor(android.graphics.Color.RED);
            txtTotalMem.setTextColor(android.graphics.Color.RED);
            txtTraffic.setTextColor(android.graphics.Color.RED);
            btnStop = viFloatingWindow.findViewById(R.id.stop);
            btnStop.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("isServiceStop", true);
                    intent.setAction(SERVICE_ACTION);
                    sendBroadcast(intent);
                    stopSelf();
                }
            });
            createFloatingWindow();
        }
//        createResultCsv();

        handler.postDelayed(task, 500);
        dataRefresh();

        return START_NOT_STICKY;
    }

    /**
     * read configuration file.
     *
     * @throws IOException
     */
    private void readSettingInfo() {
        SharedPreferences preferences = Settings
                .getDefaultSharedPreferences(getApplicationContext());
        int interval = preferences.getInt(Settings.KEY_INTERVAL, 3);
        delaytime = interval * 1000;
        isFloating = preferences.getBoolean(Settings.KEY_ISFLOAT, true);
        sender = preferences.getString(Settings.KEY_SENDER, BLANK_STRING);
        password = preferences.getString(Settings.KEY_PASSWORD, BLANK_STRING);
        recipients = preferences.getString(Settings.KEY_RECIPIENTS,
                BLANK_STRING);
        receivers = recipients.split("\\s+");
        smtp = preferences.getString(Settings.KEY_SMTP, BLANK_STRING);
        isRoot = preferences.getBoolean(Settings.KEY_ROOT, false);
        isAutoStop = preferences.getBoolean(Settings.KEY_AUTO_STOP, true);
    }

    /**
     * write the test result to csv format report.
     */
    private void createResultCsv() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yy年MM月dd日 HH:mm:ss");
        String heapData = "";
        String mDateTime = formatter.format(cal.getTime().getTime());
        resultFilePath = Settings.getEMMAGEE_RESULT_DIR() + mDateTime + " " + "的测试报告"
                + ".csv";
        try {
            File resultFile = new File(resultFilePath);
            resultFile.getParentFile().mkdirs();
            resultFile.createNewFile();
            out = new FileOutputStream(resultFile);
            osw = new OutputStreamWriter(out);
            bw = new BufferedWriter(osw);
            long totalMemorySize = memoryInfo.getTotalMemory();
            String totalMemory = fomart.format((double) totalMemorySize / 1024);
            String multiCpuTitle = BLANK_STRING;
            // titles of multiple cpu cores
            ArrayList<String> cpuList = cpuInfo.getCpuList();
            for (int i = 0; i < cpuList.size(); i++) {
                multiCpuTitle += Constants.COMMA + cpuList.get(i)
                        + getString(R.string.total_usage);
            }
            bw.write(getString(R.string.process_package) + Constants.COMMA
                    + packageName + Constants.LINE_END
                    + getString(R.string.process_name) + Constants.COMMA
                    + processName + Constants.LINE_END
                    + getString(R.string.process_pid) + Constants.COMMA + pid
                    + Constants.LINE_END + getString(R.string.mem_size)
                    + Constants.COMMA + totalMemory + "MB" + Constants.LINE_END
                    + getString(R.string.cpu_type) + Constants.COMMA
                    + cpuInfo.getCpuName() + Constants.LINE_END
                    + getString(R.string.android_system_version)
                    + Constants.COMMA + memoryInfo.getSDKVersion()
                    + Constants.LINE_END + getString(R.string.mobile_type)
                    + Constants.COMMA + memoryInfo.getPhoneType()
                    + Constants.LINE_END + "UID" + Constants.COMMA + uid
                    + Constants.LINE_END);

            if (isGrantedReadLogsPermission()) {
                bw.write(START_TIME);
            }
            if (isRoot) {
                heapData = getString(R.string.native_heap) + Constants.COMMA
                        + getString(R.string.dalvik_heap) + Constants.COMMA;
            }
            bw.write(getString(R.string.timestamp) + Constants.COMMA
                    + getString(R.string.top_activity) + Constants.COMMA
                    + heapData + getString(R.string.used_mem_PSS)
                    + Constants.COMMA + getString(R.string.used_mem_ratio)
                    + Constants.COMMA + getString(R.string.mobile_free_mem)
                    + Constants.COMMA + getString(R.string.app_used_cpu_ratio)
//                    + Constants.COMMA + getString(R.string.total_used_cpu_ratio)
//                    + multiCpuTitle
                    + Constants.COMMA + getString(R.string.traffic)
                    + Constants.COMMA + getString(R.string.battery)
                    + Constants.COMMA + getString(R.string.current)
                    + Constants.COMMA + getString(R.string.temperature)
//                    + Constants.COMMA + getString(R.string.voltage)
                    + Constants.COMMA + getString(R.string.fps)
                    + Constants.LINE_END);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * 创建一个小浮窗显示实时数据
     */
    private void createFloatingWindow() {
        SharedPreferences shared = getSharedPreferences("float_flag",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("float", 1);
        editor.commit();
        windowManager = (WindowManager) getApplicationContext()
                .getSystemService("window");
        wmParams = ((MyApplication) getApplication()).getMywmParams();
        wmParams.type = 2002;
        wmParams.flags |= 8;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.format = 1;
        windowManager.addView(viFloatingWindow, wmParams);
        viFloatingWindow.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                x = event.getRawX();
                y = event.getRawY() - statusBarHeight;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchStartX = event.getX();
                        mTouchStartY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updateViewPosition();
                        break;
                    case MotionEvent.ACTION_UP:
                        updateViewPosition();
                        mTouchStartX = mTouchStartY = 0;
                        break;
                }
                return true;
            }
        });

        //开始记录数据
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recodeDataSwitch = true;
                if (!recodeDataSwitch) {
                    recodeDataTip.setText("点击start开始记录数据");
                } else {
                    recodeDataTip.setText("正在记录数据...");
                    if (!isCreateResultCsv) {
                        createResultCsv();
                        isCreateResultCsv = true;
                    }
                }
            }
        });
    }


    private Runnable task = new Runnable() {

        public void run() {
            if (!isServiceStop) {
                dataRefresh();
                handler.postDelayed(this, delaytime);
                if (isFloating && viFloatingWindow != null) {
                    windowManager.updateViewLayout(viFloatingWindow, wmParams);
                }
                // get app start time from logcat on every task running
                getStartTimeFromLogcat();
            } else {
                Intent intent = new Intent();
                intent.putExtra("isServiceStop", true);
                intent.setAction(SERVICE_ACTION);
                sendBroadcast(intent);
                stopSelf();
            }
        }
    };

    /**
     * Try to get start time from logcat.
     */
    private void getStartTimeFromLogcat() {
        if (!isGetStartTime || getStartTimeCount >= MAX_START_TIME_COUNT) {
            return;
        }
        try {
            // filter logcat by Tag:ActivityManager and Level:Info
            String logcatCommand = "logcat -v time -d ActivityManager:I *:S";
            Process process = Runtime.getRuntime().exec(logcatCommand);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();
            String line = BLANK_STRING;

            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(line);
                strBuilder.append(Constants.LINE_END);
                String regex = ".*Displayed.*" + startActivity
                        + ".*\\+(.*)ms.*";
                if (line.matches(regex)) {
                    Log.w("my logs", line);
                    if (line.contains("total")) {
                        line = line.substring(0, line.indexOf("total"));
                    }
                    startTime = line.substring(line.lastIndexOf("+") + 1,
                            line.lastIndexOf("ms") + 2);
                    Toast.makeText(WMPerformanceTestService.this,
                            getString(R.string.start_time) + startTime,
                            Toast.LENGTH_LONG).show();
                    isGetStartTime = false;
                    break;
                }
            }
            getStartTimeCount++;
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    /**
     * Above JellyBean, we cannot grant READ_LOGS permission...
     *
     * @return
     */
    private boolean isGrantedReadLogsPermission() {
        int permissionState = getPackageManager().checkPermission(
                android.Manifest.permission.READ_LOGS, getPackageName());
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 刷新小浮窗中的性能数据.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void dataRefresh() {
        int pidMemory = memoryInfo.getPidMemorySize(pid, getBaseContext());
        long freeMemory = memoryInfo.getFreeMemorySize(getBaseContext());
        String freeMemoryKb = fomart.format((double) freeMemory / 1024);
        String processMemory = fomart.format((double) pidMemory / 1024);
        String currentBatt = String.valueOf(currentInfo.getCurrentValue());
        // 异常数据过滤
        try {
            if (Math.abs(Double.parseDouble(currentBatt)) >= 500) {
                currentBatt = Constants.NA;
            }
        } catch (Exception e) {
            currentBatt = Constants.NA;
        }

        //写入数据，后期需重构
        ArrayList<String> processInfo = cpuInfo.getCpuRatioInfo(totalBatt,
                currentBatt, temperature, voltage,
                String.valueOf(FpsInfo.fps()), isRoot, recodeDataSwitch);

        if (isFloating) {
            String processCpuRatio = "0.00";
            String totalCpuRatio = "0.00";
            String trafficSize = "0";
            long tempTraffic = 0L;
            double trafficMb = 0;
            boolean isMb = false;
            if (!processInfo.isEmpty()) {
                processCpuRatio = processInfo.get(0);
                totalCpuRatio = processInfo.get(1);
                trafficSize = processInfo.get(2);
                if (!(BLANK_STRING.equals(trafficSize))
                        && !("-1".equals(trafficSize))) {
                    tempTraffic = Long.parseLong(trafficSize);
                    if (tempTraffic > 1024) {
                        isMb = true;
                        trafficMb = (double) tempTraffic / 1024;
                    }
                }
                // 如果cpu使用率存在且都不小于0，则输出
                if (processCpuRatio != null && totalCpuRatio != null) {
                    txtUnusedMem.setText(getString(R.string.process_free_mem)
                            + processMemory + "/" + freeMemoryKb + "MB");
                    txtTotalMem.setText(getString(R.string.process_overall_cpu)
                            + processCpuRatio + "%/" + totalCpuRatio + "%");
                    String batt = getString(R.string.current) + currentBatt;
                    if ("-1".equals(trafficSize)) {
                        txtTraffic.setText(batt + Constants.COMMA
                                + getString(R.string.traffic) + Constants.NA);
                    } else if (isMb)
                        txtTraffic.setText(batt + Constants.COMMA
                                + getString(R.string.traffic)
                                + fomart.format(trafficMb) + "MB");
                    else
                        txtTraffic.setText(batt + Constants.COMMA
                                + getString(R.string.traffic) + trafficSize
                                + "KB");
                }
                if (!recodeDataSwitch) {
                    recodeDataTip.setText("点击start开始记录数据");
                }
                // 当内存为0切cpu使用率为0时则是被测应用退出
                if ("0".equals(processMemory)) {
                    if (isAutoStop) {
                        closeOpenedStream();
                        isServiceStop = true;
                        return;
                    } else {
                        Log.i(LOG_TAG, "未设置自动停止测试，继续监听");
                        // 如果设置应用退出后不自动停止，则需要每次监听时重新获取pid
                        Programe programe = procInfo.getProgrameByPackageName(
                                this, packageName);
                        if (programe != null && programe.getPid() > 0) {
                            pid = programe.getPid();
                            uid = programe.getUid();
                            cpuInfo = new CpuInfo(getBaseContext(), pid,
                                    Integer.toString(uid));
                        }
                    }
                }
            }

        }
    }

    /**
     * update the position of floating window.
     */
    private void updateViewPosition() {
        wmParams.x = (int) (x - mTouchStartX);
        wmParams.y = (int) (y - mTouchStartY);
        if (viFloatingWindow != null) {
            windowManager.updateViewLayout(viFloatingWindow, wmParams);
        }
    }

    /**
     * close all opened stream.
     */
    public void closeOpenedStream() {
        try {
            if (bw != null) {
                bw.close();
            }
            if (osw != null)
                osw.close();
            if (out != null)
                out.close();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "service onDestroy");
        if (windowManager != null) {
            windowManager.removeView(viFloatingWindow);
            viFloatingWindow = null;
        }
        handler.removeCallbacks(task);
        closeOpenedStream();

        //文件存储成功
        if (resultFilePath != null && recodeDataSwitch == true) {

            // replace the start time in file
            if (!BLANK_STRING.equals(startTime)) {
                replaceFileString(resultFilePath, START_TIME,
                        getString(R.string.start_time) + startTime
                                + Constants.LINE_END);
            } else {
                replaceFileString(resultFilePath, START_TIME, BLANK_STRING);
            }

            Toast.makeText(
                    this,
                    getString(R.string.file_save_successful_toast)
                            + WMPerformanceTestService.resultFilePath, Toast.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(
                    this, getString(R.string.file_save_fail_toast)
                    , Toast.LENGTH_LONG)
                    .show();
        }

        isStop = true;
        unregisterReceiver(batteryBroadcast);

        super.onDestroy();
        stopForeground(true);
    }

    /**
     * Replaces all matches for replaceType within this replaceString in file on
     * the filePath
     *
     * @param filePath
     * @param replaceType
     * @param replaceString
     */
    private void replaceFileString(String filePath, String replaceType,
                                   String replaceString) {
        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = BLANK_STRING;
            String oldtext = BLANK_STRING;
            while ((line = reader.readLine()) != null) {
                oldtext += line + Constants.LINE_END;
            }
            reader.close();
            // replace a word in a file
            String newtext = oldtext.replaceAll(replaceType, replaceString);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath),
                    getString(R.string.csv_encoding)));
            writer.write(newtext);
            writer.close();
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    /**
     * get height of status bar
     *
     * @return height of status bar, if default method does not work, return 25
     */
    public int getStatusBarHeight() {
        // set status bar height to 25
        int barHeight = 25;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            barHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return barHeight;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}