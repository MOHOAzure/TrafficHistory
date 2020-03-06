package network.traffic.monitor.model;

import java.util.ArrayList;
import java.util.List;

public class NetworkTrafficHistory {
    private List<NetworkTrafficOneMonth> history;
    private long totalTrafficAvg, totalTrafficMax, totalTrafficMin;
    private String totalTrafficMaxMonth, totalTrafficMinMonth;

    private long MTTrafficAvg, MTTrafficMax, MTTrafficMin;
    private String MTTrafficMaxMonth, MTTrafficMinMonth;

    public NetworkTrafficHistory(){
        this.history = new ArrayList<>();
        this.MTTrafficAvg = 0;
        this.MTTrafficMax = 0;
        this.MTTrafficMin = 0;
        this.MTTrafficMaxMonth = "";

        this.totalTrafficAvg = 0;
        this.totalTrafficMax = 0;
        this.totalTrafficMin = 0;
        this.totalTrafficMaxMonth = "";
        this.totalTrafficMinMonth = "";
    }

    public void add(NetworkTrafficOneMonth aMonth){
        this.history.add(aMonth);
    }

    public List<NetworkTrafficOneMonth> getHistory(){
        return this.history;
    }

    public void setTotalTrafficAvg(long value){
        totalTrafficAvg = value;
    }

    public void setTotalTrafficMax(long value){
        totalTrafficMax = value;
    }

    public void setTotalTrafficMin(long value){
        totalTrafficMin = value;
    }

    public void setTotalTrafficMaxMonth(String month){
        totalTrafficMaxMonth=month;
    }

    public void setTotalTrafficMinMonth(String month){
        totalTrafficMinMonth=month;
    }

    public long getTotalTrafficAvg(){
        return this.totalTrafficAvg;
    }

    public long getTotalTrafficMax(){
        return this.totalTrafficMax;
    }

    public long getTotalTrafficMin(){
        return this.totalTrafficMin;
    }

    public String getTotalTrafficMaxMonth(){
        return this.totalTrafficMaxMonth;
    }

    public String getTotalTrafficMinMonth(){
        return this.totalTrafficMinMonth;
    }

    public void setMTTrafficAvg(long value){
        MTTrafficAvg = value;
    }

    public void setMTTrafficMax(long value){
        MTTrafficMax = value;
    }

    public void setMTTrafficMin(long value){
        MTTrafficMin = value;
    }

    public void setMTTrafficMaxMonth(String month){
        MTTrafficMaxMonth=month;
    }

    public void setMTTrafficMinMonth(String month){
        MTTrafficMinMonth=month;
    }

    public long getMTTrafficAvg(){
        return this.MTTrafficAvg;
    }

    public long getMTTrafficMax(){
        return this.MTTrafficMax;
    }

    public long getMTTrafficMin(){
        return this.MTTrafficMin;
    }

    public String getMTTrafficMaxMonth(){
        return this.MTTrafficMaxMonth;
    }

    public String getMTTrafficMinMonth(){
        return this.MTTrafficMinMonth;
    }
}
