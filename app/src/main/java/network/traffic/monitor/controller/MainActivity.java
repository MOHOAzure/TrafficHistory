package network.traffic.monitor.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;

import network.traffic.monitor.R;
import network.traffic.monitor.model.NetworkTrafficOneMonth;
import network.traffic.monitor.util.DurationManager;
import network.traffic.monitor.util.NetworkNameTable;
import network.traffic.monitor.util.MyValueFormatter;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;


public class MainActivity extends AppCompatActivity {
    private TextView tvMonth, tvAllTraffic;
    private HorizontalBarChart horizontalChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMonth = findViewById(R.id.tvMonthName);
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
        // get network traffic statistics of current month
        NetworkTrafficOneMonth thisMonthTraffic = new NetworkTrafficOneMonth(MainActivity.this, DurationManager.getFirstMomentThisMonth(), System.currentTimeMillis(), 0);
        // display network traffic statistics
        tvMonth.setText(thisMonthTraffic.getMonth());
        tvAllTraffic.setText( MyValueFormatter.getRoundedValueForView(thisMonthTraffic.getTotalTraffic()));
        displayHBarChart(thisMonthTraffic.getTetheringTraffic(), thisMonthTraffic.getWiFiTraffic(), thisMonthTraffic.getMobileTraffic());
    }

    private void createHBarChart(final ArrayList<BarEntry> entryValue, final ArrayList<BarEntry> entryName, String[] networkType) {
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
                return MyValueFormatter.getRoundedValueForView( (long) value );
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

        //to display all text at the top of bars, bar chart end at 2 times of the max bar value
        mAxisLeft.setAxisMaximum(barData.getYMax()*2f);
        mAxisRight.setAxisMaximum(barData.getYMax()*2f);

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

    private void displayHBarChart(long tetheringRxTx, long wifiRxTx, long mobileRxTx) {
        final String[] networkType = new String[]{"WiFi", "Tethering", "Mobile"};

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, wifiRxTx));
        entries.add(new BarEntry(1f, tetheringRxTx));
        entries.add(new BarEntry(2f, mobileRxTx));

        ArrayList<BarEntry> entryName = new ArrayList<>();
        entryName.add(new BarEntry(0f, NetworkNameTable.WiFi));
        entryName.add(new BarEntry(1f, NetworkNameTable.Tethering));
        entryName.add(new BarEntry(2f, NetworkNameTable.Mobile));

        createHBarChart(entries, entryName, networkType);
    }

    // for display bar name 'above' a bar
    private String getNetworkName(float value){
        if(value== NetworkNameTable.WiFi) return "WiFi";
        if(value== NetworkNameTable.Tethering) return "Tethering";
        if(value== NetworkNameTable.Mobile) return "Mobile";
        return "";
    }

    /** Called when the user taps the "History" button */
    public void showHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}
