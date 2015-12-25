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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkOnlineStatus();
                    }
                }).start();
            }
        }
    };

    private void checkOnlineStatus() throws NetworkOnMainThreadException {
        try {
            //检查网络服务是否在线
            OkHttpClient client = new OkHttpClient();
            client.setReadTimeout(5, TimeUnit.SECONDS);
            client.setConnectTimeout(5, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url("http://w.seu.edu.cn/portal/init.php").build();
            Response response = client.newCall(request).execute();

            String str = response.body().string();
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("result", response.isSuccessful() ? "success" : "fail");
            bundle.putString("login", str.indexOf("notlogin") >= 0 ? "null" : "old");
            bundle.putString("response", str);
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loginToService() throws NetworkOnMainThreadException {
        //登陆网络服务
        String cardnum = sp.getString("cardnum", "");
        String key = "Auth" + cardnum + new AuthHelper(NetworkService.this).getUUID();
        try {
            EncryptHelper helper = new EncryptHelper(key);
            String pwd = helper.decrypt(sp.getString("password", ""));

            OkHttpClient client = new OkHttpClient();
            client.setReadTimeout(5, TimeUnit.SECONDS);
            client.setConnectTimeout(5, TimeUnit.SECONDS);
            RequestBody body = new FormEncodingBuilder()
                    .add("username", cardnum)
                    .add("password", pwd)
                    .build();
            Request request = new Request.Builder()
                    .url("http://w.seu.edu.cn/portal/login.php")
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            String str = response.body().string();
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("result", response.isSuccessful() ? "success" : "fail");
            bundle.putString("login", "new");
            bundle.putString("response", str);
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID().replaceAll("\"", "");
            Bundle data = msg.getData();
            if (data.getString("result").equals("success")) {
                try {
                    if (data.getString("login").equals("null")) {

                        // 未登录状态，开始登录
                        loginToService();

                    } else if (data.getString("login").equals("new")) {

                        // 登陆成功状态
                        JSONObject info = new JSONObject(data.getString("response"));
                        String[] infoStr = {
                                info.getString("login_username"),
                                info.getString("login_index"),
                                info.getString("login_ip"),
                                unicodeToString(info.getString("login_location")),
                                info.getString("login_expire"),
                                info.getString("login_remain"),
                                formatTime(info.getString("login_time"))
                        };
                        Toast.makeText(NetworkService.this, "已帮您自动登录" + ssid + "无线网络\n" +
                                "账户名：" + infoStr[0] + " (已登录" + infoStr[1] + "个设备)\n" +
                                "登录IP：" + infoStr[2] + "\n" +
                                "登录位置：" + infoStr[3] + "\n" +
                                "到期时间：" + infoStr[4] + " (剩余" + infoStr[5] + "天)", Toast.LENGTH_LONG).show();

                    } else if (data.getString("login").equals("old")) {

                        // 早已登录状态
                        JSONObject info = new JSONObject(data.getString("response"));
                        String[] infoStr = {
                                info.getString("login_username"),
                                info.getString("login_index"),
                                info.getString("login_ip"),
                                unicodeToString(info.getString("login_location")),
                                info.getString("login_expire"),
                                info.getString("login_remain"),
                                formatTime(info.getString("login_time"))
                        };
                        Toast.makeText(NetworkService.this, "您的" + ssid + "仍在线，无需重复登录\n" +
                                "账户名：" + infoStr[0] + " (已登录" + infoStr[1] + "个设备)\n" +
                                "登录IP：" + infoStr[2] + "\n" +
                                "登录位置：" + infoStr[3] + "\n" +
                                "在线时长：" + infoStr[6] + "\n" +
                                "到期时间：" + infoStr[4] + " (剩余" + infoStr[5] + "天)", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    try {
                        String error = new JSONObject(data.getString("response")).getString("error");
                        Toast.makeText(NetworkService.this, "尝试登陆" + ssid + "无线网络失败\n" + error
                                , Toast.LENGTH_LONG).show();
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                }
            } else {
                // 连接失败
                Toast.makeText(NetworkService.this, "尝试连接" + ssid + "登录页面失败，请联系管理员"
                        , Toast.LENGTH_LONG).show();
            }
        }
    };

    public static String formatTime(String time) {
        int seconds = Integer.valueOf(time);
        int minutes = seconds / 60;
        seconds %= 60;
        return minutes + ":" + ((seconds < 10) ? "0" : "") + seconds;
    }

    public static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
}
