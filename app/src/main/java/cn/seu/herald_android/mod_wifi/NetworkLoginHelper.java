package cn.seu.herald_android.mod_wifi;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.EncryptHelper;
import okhttp3.Call;


public class NetworkLoginHelper {

    private static NetworkLoginHelper instance;

    private Context context;

    private boolean registered = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
                        Toast.makeText(context, "登陆" + ssid + "无线网络成功\n" +
                                "账户名：" + infoStr[0] + " (已登录" + infoStr[1] + "个设备)\n" +
                                "登录IP：" + infoStr[2] + "\n" +
                                "登录位置：" + infoStr[3] + "\n" +
                                "到期时间：" + infoStr[4] + " (剩余" + infoStr[5] + "天)", Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    try {
                        String error = new JSONObject(data.getString("response")).getString("error");
                        Toast.makeText(context, "尝试登陆" + ssid + "无线网络失败\n" + error
                                , Toast.LENGTH_SHORT).show();
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    };

    public static NetworkLoginHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkLoginHelper();
            instance.context = context;
        }
        return instance;
    }

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

    public void registerReceiver() {
        if (!registered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(NetworkChangeReceiver.getInstance(), filter);
            registered = true;
        }
    }

    public void unregisterReceiver() {
        if (registered) {
            context.unregisterReceiver(NetworkChangeReceiver.getInstance());
            registered = false;
        }
    }

    public void setAuth(String username, String password) {
        try {
            String encrypted = new EncryptHelper(username).encrypt(password);
            CacheHelper helper = new CacheHelper(context);
            helper.setCache("netUser", username);
            helper.setCache("netPwd", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkAndLogin() {
        Log.e("login", String.valueOf(this.hashCode()));
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replaceAll("\"", "");
        if (ssid.equals("seu-wlan") || ssid.equals("seu-dorm")) {
            checkOnlineStatus();
        }
    }

    private void checkOnlineStatus() {
        OkHttpUtils.get().url("http://w.seu.edu.cn/portal/init.php").build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("result", "fail");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(String response) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("result", "success");
                bundle.putString("login", response.contains("notlogin") ? "null" : "old");
                bundle.putString("response", response);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
    }

    private void loginToService() {
        //登陆网络服务
        CacheHelper helper = new CacheHelper(context);
        String username = helper.getCache("netUser");
        EncryptHelper helper1 = new EncryptHelper(username);
        String password = helper1.decrypt(helper.getCache("netPwd"));

        OkHttpUtils.post().url("http://w.seu.edu.cn/portal/login.php")
                .addParams("username", username)
                .addParams("password", password).build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("result", "fail");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(String response) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("result", "success");
                bundle.putString("login", "new");
                bundle.putString("response", response);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
    }
}
