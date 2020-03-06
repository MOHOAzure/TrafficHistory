package network.traffic.monitor.model;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import network.traffic.monitor.util.NetworkTrafficCollector;

public class NetworkTrafficOneMonth {
    private String month;
    private long mobile;
    private long wifi;
    private long tethering;

    private Context context;
    private NetworkTrafficCollector ntc;

    public NetworkTrafficOneMonth(Context context, long startTime, long endTime, int monthOffset){
        this.context = context;
        NetworkTrafficCollector ntc = new NetworkTrafficCollector(context);

        month = setMonth(monthOffset);

        //mobile usage data
        mobile = ntc.getMobileTrafficData(startTime, endTime);

        //wifi  usage data
        wifi = ntc.getWiFiTrafficData(startTime, endTime);

        //tethering usage data
        tethering = ntc.getTetheringTrafficData(startTime, endTime);
    }

    private String setMonth(int monthOffset) {
        DateFormat df = new SimpleDateFormat("MMM");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 0-monthOffset);
        return df.format(cal.getTime());
    }

    public String getMonth(){
        return this.month;
    }

    public long getMobileTraffic(){
        return this.mobile;
    }

    public long getWiFiTraffic(){
        return this.wifi;
    }

    public long getTetheringTraffic(){
        return this.tethering;
    }

    public long getTotalTraffic(){
        return this.mobile+this.wifi+this.tethering;
    }

}
