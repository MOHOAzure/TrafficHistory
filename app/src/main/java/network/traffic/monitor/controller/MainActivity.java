package network.traffic.monitor.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.RequiresApi;

import network.traffic.monitor.R;
import network.traffic.monitor.model.FileSize;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;


public class MainActivity extends AppCompatActivity {

    TextView tvAllTraffic;
    private HorizontalBarChart horizontalChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAllTraffic = findViewById(R.id.tvAllTraffic);

        horizontalChart = findViewById(R.id.horizontalChart);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();

        showNetworkStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissions()) {
            return;
        }
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }

    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }

    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }

    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private void requestPhoneStateStats() {
        final int READ_PHONE_STATE_REQUEST = 37;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    private void showNetworkStatistics(){
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
        NetworkStats.Bucket bucket;
        long mobileRxTx = 0, wifiRxTx = 0, tetheringRxTx = 0, allRxTx;

        // get network statistics
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            //mobile traffic
            try {
                //for device summary, SubscriberId must be null
                bucket = networkStatsManager.querySummaryForDevice(NetworkCapabilities.TRANSPORT_CELLULAR,
                        getSubscriberId(MainActivity.this),
                        getTimesMonthMorning(),
                        System.currentTimeMillis());
                mobileRxTx = bucket.getRxBytes() + bucket.getTxBytes();
//                Log.i("Info", "Mobile RX: " + (bucket.getRxBytes() + "Mobile TX: " +  bucket.getTxBytes()));
            } catch (RemoteException e) {
                System.out.println("mobile remote exp-1");
            }

            //wifi traffic
            try {
                bucket = networkStatsManager.querySummaryForDevice(NetworkCapabilities.TRANSPORT_WIFI,
                        "",
                        getTimesMonthMorning(),
                        System.currentTimeMillis());

                wifiRxTx = bucket.getRxBytes() + bucket.getTxBytes();
            } catch (RemoteException e) {
                System.out.println("wifi remote exp-1");
            }

            //tethering traffic
            NetworkStats networkStats;
            try{
                networkStats = networkStatsManager.queryDetailsForUid(NetworkCapabilities.TRANSPORT_CELLULAR,
                        getSubscriberId(MainActivity.this),
                        getTimesMonthMorning(),
                        System.currentTimeMillis(),
                        android.app.usage.NetworkStats.Bucket.UID_TETHERING);
                bucket = new NetworkStats.Bucket();
                while (networkStats.hasNextBucket()) {
                    networkStats.getNextBucket(bucket);
                    tetheringRxTx += bucket.getRxBytes() + bucket.getTxBytes();
//                    Log.i("Info", "tetheringBytes: " + tetheringRxTx);
                }
                networkStats.close();
            } catch (Exception e) {
                System.out.println("tethering remote exp-1");
            }
        }
        // Android Ver. < M
        else {
            try {
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                        getSubscriberId(MainActivity.this),
                        getTimesMonthMorning(),
                        System.currentTimeMillis());
                mobileRxTx = bucket.getRxBytes() + bucket.getTxBytes();
            } catch (RemoteException e) {
                System.out.println("mobile remote exp-1");
            }
            try {
                bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                        "",
                        getTimesMonthMorning(),
                        System.currentTimeMillis());

                wifiRxTx = bucket.getRxBytes() + bucket.getTxBytes();
            } catch (RemoteException e) {
                System.out.println("wifi remote exp-1");
            }
            //tethering traffic
            NetworkStats networkStats;
            try{
                networkStats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE,
                        getSubscriberId(MainActivity.this),
                        getTimesMonthMorning(),
                        System.currentTimeMillis(),
                        android.app.usage.NetworkStats.Bucket.UID_TETHERING);
                bucket = new NetworkStats.Bucket();
                while (networkStats.hasNextBucket()) {
                    networkStats.getNextBucket(bucket);
                    tetheringRxTx += bucket.getRxBytes() + bucket.getTxBytes();
//                    Log.i("Info", "tetheringBytes: " + tetheringRxTx);
                }
                networkStats.close();
            } catch (Exception e) {
                System.out.println("tethering remote exp-1");
            }
        }

        allRxTx = mobileRxTx + wifiRxTx + tetheringRxTx;

        //show by text
        tvAllTraffic.setText( getRoundedValueForView(allRxTx) );

        //show by chart
        initHBarChart(tetheringRxTx, wifiRxTx, mobileRxTx);
    }

    private void setBarDataSet(final ArrayList<BarEntry> entryValue, final ArrayList<BarEntry> entryName, String[] networkType) {

        // set distance between bars // note: (barWidth+barSpace)*2+groupSpace = 1.0
        float fromX = 0f;
        float barWidth = 0.18f;
        float barSpace = 0.15f;
        float groupSpace = 1-2*(barWidth+barSpace);

        BarDataSet barNameDataSet=new BarDataSet(entryName,"");
        // set presentation style at the 'top' of bars
        barNameDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value) {
                return getNetworkName(value);
            }
        });

        BarDataSet barDataSet=new BarDataSet(entryValue,"");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        // set presentation style at the 'top' of bars
        barDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value) {
                return getRoundedValueForView( (long) value );
            }
        });

        // no icon for bar color
        barDataSet.setFormLineWidth(0f);
        barDataSet.setFormSize(0f);
        barNameDataSet.setFormLineWidth(0f);
        barNameDataSet.setFormSize(0f);

        BarData barData=new BarData(barDataSet,barNameDataSet);
        barData.setValueTextSize(25f);
        barData.setValueTextColor(Color.GRAY);
        barData.setDrawValues(true);
        barData.setBarWidth(barWidth);

        // display types of network traffics
//        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(new String[]{"","",""});
//        horizontalChart.getXAxis().setValueFormatter(formatter);

        horizontalChart.getXAxis().setDrawLabels(false);

        // give data to chart
        horizontalChart.setData(barData);

        //no zooming
        horizontalChart.setScaleXEnabled(false);
        horizontalChart.setScaleYEnabled(false);
        horizontalChart.setScaleEnabled(false);

        //background color
        horizontalChart.setBackgroundColor(Color.TRANSPARENT);

        //no grid line
        horizontalChart.setDrawGridBackground(false);
        horizontalChart.setDrawBorders(false);

        //no background shadow
        horizontalChart.setDrawBarShadow(false);
        horizontalChart.setHighlightFullBarEnabled(false);

        //no bar shadow
        horizontalChart.setDrawBarShadow(false);

        //no chart description
        horizontalChart.setDescription(null);

        //animation
        horizontalChart.animateY(1000, Easing.EaseInOutExpo);

        /* x-axis and y-axis setting */
        //set x-axis to bottom of chart
        XAxis mXAxis = horizontalChart.getXAxis();
        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mXAxis.setGranularityEnabled(true);
        mXAxis.setLabelCount(networkType.length);
        mXAxis.setTextSize(20f);

        //do not display x-axis and central bar lines of x-axis
        mXAxis.setDrawAxisLine(false);
        mXAxis.setDrawGridLines(false);

        // y-axis start from value zero
        YAxis mAxisLeft = horizontalChart.getAxisLeft();
        mAxisLeft.setAxisMinimum(0f);
        YAxis mAxisRight = horizontalChart.getAxisRight();
        mAxisRight.setAxisMinimum(0f);

        //to display all text at the top of bars, bar chart end at 1.5 times of the max bar value
        mAxisLeft.setAxisMaximum(barData.getYMax()*1.5f);
        mAxisRight.setAxisMaximum(barData.getYMax()*1.5f);

        // do not display "upper & bottom" y-axis
        mAxisLeft.setEnabled(false);//bottom
        mAxisRight.setEnabled(false);//upper

        // set distance between bars
        horizontalChart.getXAxis().setAxisMinimum(0f);
        horizontalChart.getXAxis().setAxisMaximum(0f + horizontalChart.getBarData().getGroupWidth(groupSpace, barSpace) * networkType.length);
        horizontalChart.groupBars(fromX, groupSpace, barSpace);

        // center the x-axis value
        horizontalChart.getXAxis().setCenterAxisLabels(true);

        horizontalChart.invalidate();
    }

    private void initHBarChart(long tetheringRxTx, long wifiRxTx, long mobileRxTx) {
        final String[] networkType = new String[]{"WiFi", "Tethering", "Mobile"};

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, Float.valueOf(wifiRxTx)));
        entries.add(new BarEntry(1f, Float.valueOf(tetheringRxTx)));
        entries.add(new BarEntry(2f, Float.valueOf(mobileRxTx)));

        ArrayList<BarEntry> entryName = new ArrayList<>();
        entryName.add(new BarEntry(0f, 0.1f));
        entryName.add(new BarEntry(1f, 0.2f));
        entryName.add(new BarEntry(2f, 0.3f));

        setBarDataSet(entries, entryName, networkType);
    }

    // for display bar name 'above' a bar
    private String getNetworkName(float value){
        if(value==0.1f) return "WiFi";
        if(value==0.2f) return "Tethering";
        if(value==0.3f) return "Mobile";
        return "";
    }

    private String getRoundedValueForView(long value){
        String text="";
        DecimalFormat df = new DecimalFormat("###.00");
        if(value >= FileSize.sizeOfGB){
            text = df.format(value/FileSize.sizeOfGB)+" GB";
        } else if (value < FileSize.sizeOfGB && value >= FileSize.sizeOfMB){
            text = df.format(value/FileSize.sizeOfMB)+" MB";
        } else if (value < FileSize.sizeOfMB && value >= FileSize.sizeOfKB){
            text = df.format(value/FileSize.sizeOfKB)+" KB";
        } else {
            text = value+" bytes";
        }
        return text;
    }

    private String getSubscriberId(Context context) {
        String deviceId = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = null;
        } else {
            // request old storage permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = tm.getSubscriberId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deviceId;
    }

    private static long getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }


    /** Called when the user taps the "History" button */
    public void showHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}
