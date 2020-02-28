package network.traffic.monitor.model;

import java.util.List;

public class NetworkTrafficHistory {
    private List<NetworkTrafficOneMonth> history;
    private long totalTrafficAvg, totalTrafficMax, totalTrafficMin;
    private String totalTrafficMaxMonth, totalTrafficMinMonth;

    public NetworkTrafficHistory(){

    }

    public List<NetworkTrafficOneMonth> getHistory(){
        List<NetworkTrafficOneMonth> listHistory = null;
        return listHistory;
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
        long traffic = 0;
        return traffic;
    }

    public long getTotalTrafficMax(){
        long traffic = 0;
        return traffic;
    }

    public long getTotalTrafficMin(){
        long traffic = 0;
        return traffic;
    }

    public String getTotalTrafficMaxMonth(){
        String month="";
        return month;
    }

    public String getTotalTrafficMinMonth(){
        String month="";
        return month;
    }
}
