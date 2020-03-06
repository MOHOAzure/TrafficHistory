package network.traffic.monitor.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import network.traffic.monitor.R;
import network.traffic.monitor.model.NetworkTrafficHistory;
import network.traffic.monitor.model.NetworkTrafficOneMonth;
import network.traffic.monitor.util.DurationManager;
import network.traffic.monitor.util.MyValueFormatter;
import network.traffic.monitor.util.NetworkNameTable;

public class HistoryActivity extends AppCompatActivity {
    private TextView tvDuration, tvAVGAllNetworkTypes, tvMaxAllNetworkTypes, tvMinAllNetworkTypes, tvAVGMTNetworkTypes, tvMaxMTNetworkTypes, tvMinMTNetworkTypes;
    private LineChart lineChart;
    private NetworkTrafficHistory history;
    int duration=4; // default design: the last 6 months, includes the current month
    // Notice: Android only record network usage within 5 months.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        tvDuration=findViewById(R.id.tvDuration);
        tvAVGAllNetworkTypes=findViewById(R.id.tvAVGAllNetworkTypes);
        tvMaxAllNetworkTypes=findViewById(R.id.tvMaxAllNetworkTypes);
        tvMinAllNetworkTypes=findViewById(R.id.tvMinAllNetworkTypes);
        tvAVGMTNetworkTypes=findViewById(R.id.tvAVGMTNetworkTypes);
        tvMaxMTNetworkTypes=findViewById(R.id.tvMaxMTNetworkTypes);
        tvMinMTNetworkTypes=findViewById(R.id.tvMinMTNetworkTypes);
        lineChart = findViewById(R.id.lineChart);
    }

    @Override
    protected void onStart() {
        super.onStart();

        showHistory(duration);
    }

    private void showHistory(int duration){
        /* Display the traffic history during the given time frame, including the current month */

        // get network traffic statistics
        long startTime, endTime;
        history = new NetworkTrafficHistory();
        for(int durationIdx=duration; durationIdx>=0; --durationIdx) {
            startTime=DurationManager.getFirstMomentPreviousMonth(durationIdx);
            endTime=DurationManager.getLastMomentPreviousMonth(durationIdx);
            NetworkTrafficOneMonth aMonthTraffic = new NetworkTrafficOneMonth(HistoryActivity.this, startTime, endTime, durationIdx);
            history.add(aMonthTraffic);
        }

        // calc history statistics
        // all kinds of network interfaces, including Mobile, WiFi, and Tethering
        long avg=-1, max=-1, min=-1, aMonthTotal;
        String maxMonth="NIL", minMonth="NIL";
        int idx=0;
        for(NetworkTrafficOneMonth aMonthTraffic : history.getHistory() ){
            //check
//            Log.i("check", "Month: "+aMonthTraffic.getMonth() + " Traffic: "+aMonthTraffic.getTotalTraffic());
            aMonthTotal = aMonthTraffic.getTotalTraffic();
            // get total traffic month avg
            avg += aMonthTotal;
            if (idx==0){
                max = aMonthTotal;
                maxMonth = aMonthTraffic.getMonth();
                min = aMonthTotal;
                minMonth = aMonthTraffic.getMonth();
            } else {
                // get max total traffic of months
                if (max <= aMonthTotal) {
                    max = aMonthTotal;
                    maxMonth = aMonthTraffic.getMonth();
                }
                // get min total traffic of months
                if (min >= aMonthTotal) {
                    min = aMonthTotal;
                    minMonth = aMonthTraffic.getMonth();
                }
            }
            //next loop
            idx++;
        }
        history.setTotalTrafficAvg(avg/duration);
        history.setTotalTrafficMax(max);
        history.setTotalTrafficMaxMonth(maxMonth);
        history.setTotalTrafficMin(min);
        history.setTotalTrafficMinMonth(minMonth);
        
        // 2 kinds of network interfaces, including Mobile and Tethering
        avg=-1; max=-1; min=-1;
        long aMonthMT=-1;
        maxMonth="NIL"; minMonth="NIL";
        idx=0;
        for(NetworkTrafficOneMonth aMonthTraffic : history.getHistory() ){
            aMonthMT = aMonthTraffic.getMobileTraffic()+aMonthTraffic.getTetheringTraffic();
            // get total traffic month avg
            avg+=aMonthMT;
            if (idx==0){
                max = aMonthMT;
                maxMonth = aMonthTraffic.getMonth();
                min = aMonthMT;
                minMonth = aMonthTraffic.getMonth();
            } else {
                // get max mobile and tethering traffic of months
                if (max <= aMonthMT) {
                    max = aMonthMT;
                    maxMonth = aMonthTraffic.getMonth();
                }
                // get min  mobile and tethering traffic of months
                if (min >= aMonthMT) {
                    min = aMonthMT;
                    minMonth = aMonthTraffic.getMonth();
                }
            }
            //next loop
            idx++;
        }
        history.setMTTrafficAvg(avg/duration);
        history.setMTTrafficMax(max);
        history.setMTTrafficMaxMonth(maxMonth);
        history.setMTTrafficMin(min);
        history.setMTTrafficMinMonth(minMonth);
        
        //display statistics on screen
        tvDuration.setText(duration+1 + " Months");
        tvAVGAllNetworkTypes.setText( MyValueFormatter.getRoundedValueForView( history.getTotalTrafficAvg() ));
        tvMaxAllNetworkTypes.setText( history.getTotalTrafficMaxMonth() + " " + MyValueFormatter.getRoundedValueForView( history.getTotalTrafficMax() ));
        tvMinAllNetworkTypes.setText( history.getTotalTrafficMinMonth() + " " + MyValueFormatter.getRoundedValueForView( history.getTotalTrafficMin() ));

        tvAVGMTNetworkTypes.setText( MyValueFormatter.getRoundedValueForView( history.getMTTrafficAvg() ));
        tvMaxMTNetworkTypes.setText( history.getMTTrafficMaxMonth() + " " + MyValueFormatter.getRoundedValueForView( history.getMTTrafficMax() ));
        tvMinMTNetworkTypes.setText( history.getMTTrafficMinMonth() + " " + MyValueFormatter.getRoundedValueForView( history.getMTTrafficMin() ));
        
        //display line chart
        displayHistoryLineChart();
    }

    private void displayHistoryLineChart() {
        // set data to line chart
        LineDataSet allNetworkDataSet = new LineDataSet(getData(NetworkNameTable.ALL),"All Networks");
        allNetworkDataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        allNetworkDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        allNetworkDataSet.setDrawCircleHole( true );
        allNetworkDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value) {
                return MyValueFormatter.getRoundedValueForView( (long) value );
            }
        });

        LineDataSet MTNetworkDataSet = new LineDataSet(getData(NetworkNameTable.MobileAndTethering),"Mobile+Tethering");
        MTNetworkDataSet.setColor(ContextCompat.getColor(this, R.color.colorAccent));
//        MTNetworkDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.bo));
        MTNetworkDataSet.setDrawCircleHole( true );
        MTNetworkDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value) {
                return MyValueFormatter.getRoundedValueForView( (long) value );
            }
        });
        LineData data = new LineData(allNetworkDataSet, MTNetworkDataSet);
        lineChart.setData(data);

        // no legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);

        // set up line chart style

        // set chart description
        lineChart.setDescription(null);

        // enable touch gestures
        lineChart.setTouchEnabled(false);
        lineChart.animateX(1000);

        // set up X-axis with month names
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        final List<String> month_list = new ArrayList<>();
        for(NetworkTrafficOneMonth aMonthTraffic : history.getHistory() ){
            month_list.add(aMonthTraffic.getMonth());
        }
        final String[] months = month_list.toArray(new String[0]);
        ValueFormatter formatterXAxis = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return months[(int) value];
            }
        };
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatterXAxis);

        // set up Y-axis with traffic value scales
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);
        ValueFormatter formatterYAxis = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return MyValueFormatter.getRoundedValueForLineChart(value);
            }
        };
        yAxisLeft.setValueFormatter(formatterYAxis);

        lineChart.invalidate();
    }

    private List<Entry> getData(int type){
        ArrayList<Entry> entries = new ArrayList<>();
        int idx=0;
        for(NetworkTrafficOneMonth aMonthTraffic : history.getHistory() ){
            // one for total network interface, one for Mobile + Tethering
            if(type == NetworkNameTable.ALL)
                entries.add(new Entry(idx, aMonthTraffic.getTotalTraffic()));
            else if(type == NetworkNameTable.MobileAndTethering)
                entries.add(new Entry(idx, aMonthTraffic.getTetheringTraffic()+aMonthTraffic.getMobileTraffic()));
            idx++;
        }
        return entries;
    }
}
