package network.traffic.monitor.util;

import android.Manifest;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

public class NetworkTrafficCollector {
    private Context context;
    private NetworkStatsManager networkStatsManager;

    public NetworkTrafficCollector(Context context){
        this.context=context;
        networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
    }

    public long getMobileTrafficData(long startTime, long endTime){
        long traffic=0;
        NetworkStats.Bucket bucket;
        int networkType;

        // set up "network type" for different Android ver.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) networkType = NetworkCapabilities.TRANSPORT_CELLULAR;
        else networkType = ConnectivityManager.TYPE_MOBILE;

        try {
            //for device summary, SubscriberId must be null
            bucket = networkStatsManager.querySummaryForDevice(networkType,
                    getSubscriberId(),
                    startTime,
                    endTime);
            traffic = bucket.getRxBytes() + bucket.getTxBytes();
//          Log.i("Info", "Mobile RX: " + (bucket.getRxBytes() + "Mobile TX: " +  bucket.getTxBytes()));
        } catch (RemoteException e) {
            System.out.println("mobile remote exp-1");
        }
        return traffic;
    }

    public long getWiFiTrafficData(long startTime, long endTime){
        long traffic=0;
        NetworkStats.Bucket bucket;
        int networkType;

        // set up "network type" for different Android ver.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) networkType = NetworkCapabilities.TRANSPORT_WIFI;
        else networkType = ConnectivityManager.TYPE_WIFI;

        try {
            //for device summary, SubscriberId must be null
            bucket = networkStatsManager.querySummaryForDevice(networkType,
                    getSubscriberId(),
                    startTime,
                    endTime);
            traffic = bucket.getRxBytes() + bucket.getTxBytes();
//          Log.i("Info", "WiFi RX: " + (bucket.getRxBytes() + "WiFi TX: " +  bucket.getTxBytes()));
        } catch (RemoteException e) {
            System.out.println("mobile remote exp-1");
        }
        return traffic;
    }

    public long getTetheringTrafficData(long startTime, long endTime){
        long traffic=0;
        NetworkStats networkStats;
        NetworkStats.Bucket bucket;
        int networkType;

        // set up "network type" for different Android ver.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) networkType = NetworkCapabilities.TRANSPORT_CELLULAR;
        else networkType = ConnectivityManager.TYPE_MOBILE;

        // wifi tethering
        try{
            networkStats = networkStatsManager.queryDetailsForUid(networkType,
                    getSubscriberId(),
                    startTime,
                    endTime,
                    android.app.usage.NetworkStats.Bucket.UID_TETHERING);
            bucket = new NetworkStats.Bucket();
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                traffic += bucket.getRxBytes() + bucket.getTxBytes();
//                    Log.i("Info", "tetheringBytes: " + traffic);
            }
            networkStats.close();
        } catch (Exception e) {
            System.out.println("tethering remote exp-1");
        }
        return traffic;
    }

    // set up "SubscriberId" for different Android ver.
    private String getSubscriberId() {
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

}
