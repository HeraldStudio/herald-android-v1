package cn.seu.herald_android.mod_wifi;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.EncryptHelper;
import cn.seu.herald_android.mod_auth.LoginActivity;
import cn.seu.herald_android.mod_settings.SysSettingsActivity;

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

        // (TODO 貌似是多余的检查，可以考虑去掉)
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp1.getBoolean("autoLogin", SysSettingsActivity.DEFAULT_AUTO_LOGIN)){
            stopSelf();
        }

        sp = getSharedPreferences("Auth", MODE_PRIVATE);
        editor = sp.edit();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);

        return START_STICKY;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
                            + new ApiHelper(NetworkService.this).getUUID();
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
