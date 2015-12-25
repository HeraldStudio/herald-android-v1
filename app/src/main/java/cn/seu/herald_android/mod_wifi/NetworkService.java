package cn.seu.herald_android.mod_wifi;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.seu.herald_android.helper.AuthHelper;
import cn.seu.herald_android.helper.EncryptHelper;
import cn.seu.herald_android.mod_auth.LoginActivity;

/**
 * Created by vhyme on 2015/12/25 025.
 */
public class NetworkService extends Service {

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Not a bound service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sp = getSharedPreferences("Auth", MODE_PRIVATE);
        editor = sp.edit();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);

        return START_STICKY;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("onReceive", intent.getAction());
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID().replaceAll("\"", "");
            if (ssid.equals("seu-wlan") || ssid.equals("seu-dorm")) {
                if (sp.getString("password", "").equals("")) {
                    Toast.makeText(context, "您没有登录，无法享受自动登录seu-wlan的功能，" +
                            "请先登录您的校园网账户~", Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(NetworkService.this, LoginActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                    return;
                }
                try {
                    String key = "Auth" + sp.getString("cardnum", "")
                            + new AuthHelper(NetworkService.this).getUUID();
                    EncryptHelper helper = new EncryptHelper(key);
                    String pwd = helper.decrypt(sp.getString("password", ""));

                    new NetworkLoginHelper().checkNetworkAndDoAfter(NetworkService.this,
                            sp.getString("cardnum", ""), pwd, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
