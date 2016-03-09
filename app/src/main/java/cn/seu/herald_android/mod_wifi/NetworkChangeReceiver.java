package cn.seu.herald_android.mod_wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static NetworkChangeReceiver instance;

    public static NetworkChangeReceiver getInstance() {
        if (instance == null) {
            instance = new NetworkChangeReceiver();
        }
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String ssid = bundle.getString("extraInfo");
        if (ssid == null) return;
        ssid = ssid.replaceAll("\"", "");
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                && bundle.get("extraInfo") != null) {
            if (ssid.equals("seu-wlan") || ssid.equals("seu-dorm")) {
                NetworkLoginHelper.getInstance(context).checkAndLogin();
            }
        }
    }
}
