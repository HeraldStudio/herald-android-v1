package cn.seu.herald_android.mod_wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NetworkLoginHelper {

    private Context context;

    private String username, password;

    private Runnable task = null;

    // 用于执行任何需要网络的任务，作用是在执行前检查是否使用校园网络，如果使用了校园网络且未登录，则先登录校园网络
    public void checkNetworkAndDoAfter(Context context, String username, String password, Runnable task) {

        this.context = context;
        this.username = username;
        this.password = password;
        this.task = task;

        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replaceAll("\"", "");
        if (ssid.equals("seu-wlan") || ssid.equals("seu-dorm")) {
            checkOnlineStatus();
        } else if (task != null) {
            task.run();
        }
    }

    private void checkOnlineStatus() {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //检查网络服务是否在线
//                    OkHttpClient client = new OkHttpClient();
//                    client.setReadTimeout(5, TimeUnit.SECONDS);
//                    client.setConnectTimeout(5, TimeUnit.SECONDS);
//                    Request request = new Request.Builder()
//                            .url("http://w.seu.edu.cn/portal/init.php").build();
//                    Response response = client.newCall(request).execute();
//
//                    String str = response.body().string();
//                    Message msg = new Message();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("result", response.isSuccessful() ? "success" : "fail");
//                    bundle.putString("login", str.indexOf("notlogin") >= 0 ? "null" : "old");
//                    bundle.putString("response", str);
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }


    private void loginToService() {
        //登陆网络服务
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    OkHttpClient client = new OkHttpClient();
//                    client.setReadTimeout(5, TimeUnit.SECONDS);
//                    client.setConnectTimeout(5, TimeUnit.SECONDS);
//                    RequestBody body = new FormEncodingBuilder()
//                            .add("username", username)
//                            .add("password", password)
//                            .build();
//                    Request request = new Request.Builder()
//                            .url("http://w.seu.edu.cn/portal/login.php")
//                            .post(body)
//                            .build();
//                    Response response = client.newCall(request).execute();
//
//                    String str = response.body().string();
//                    Message msg = new Message();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("result", response.isSuccessful() ? "success" : "fail");
//                    bundle.putString("login", "new");
//                    bundle.putString("response", str);
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
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
                        Toast.makeText(context, "已帮您自动登录" + ssid + "无线网络\n" +
                                "账户名：" + infoStr[0] + " (已登录" + infoStr[1] + "个设备)\n" +
                                "登录IP：" + infoStr[2] + "\n" +
                                "登录位置：" + infoStr[3] + "\n" +
                                "到期时间：" + infoStr[4] + " (剩余" + infoStr[5] + "天)", Toast.LENGTH_LONG).show();
                        if (task != null) task.run();

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
                        Toast.makeText(context, "您的" + ssid + "仍在线，无需重复登录\n" +
                                "账户名：" + infoStr[0] + " (已登录" + infoStr[1] + "个设备)\n" +
                                "登录IP：" + infoStr[2] + "\n" +
                                "登录位置：" + infoStr[3] + "\n" +
                                "在线时长：" + infoStr[6] + "\n" +
                                "到期时间：" + infoStr[4] + " (剩余" + infoStr[5] + "天)", Toast.LENGTH_LONG).show();
                        if (task != null) task.run();
                    }
                } catch (JSONException e) {
                    try {
                        String error = new JSONObject(data.getString("response")).getString("error");
                        Toast.makeText(context, "尝试登陆" + ssid + "无线网络失败\n" + error
                                , Toast.LENGTH_LONG).show();
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                }
            } else {
                // 连接失败
                Toast.makeText(context, "尝试连接" + ssid + "登录页面失败，请尝试切换网络"
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
