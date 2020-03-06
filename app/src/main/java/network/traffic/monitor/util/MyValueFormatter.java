package network.traffic.monitor.util;

import java.text.DecimalFormat;

import network.traffic.monitor.model.FileSize;

public class MyValueFormatter {

    public static String getRoundedValueForView(long value){
        String text;
        DecimalFormat df = new DecimalFormat("###.00");
        if(value >= FileSize.sizeOfGB){
            text = df.format(value/FileSize.sizeOfGB)+" GB";
        } else if (value >= FileSize.sizeOfMB){
            text = df.format(value/FileSize.sizeOfMB)+" MB";
        } else if (value >= FileSize.sizeOfKB){
            text = df.format(value/FileSize.sizeOfKB)+" KB";
        } else {
            text = value+" bytes";
        }
        return text;
    }

    public static String getRoundedValueForLineChart(float value){
        DecimalFormat df = new DecimalFormat("###");
        return df.format(value/FileSize.sizeOfGB)+" GB";
    }

}
