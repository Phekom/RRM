package za.co.xisystems.itis_rrm.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;

public class ServiceUtil {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddress = InetAddress.getByName("https://www.google.com");
            return (ipAddress.equals("")) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }
}
