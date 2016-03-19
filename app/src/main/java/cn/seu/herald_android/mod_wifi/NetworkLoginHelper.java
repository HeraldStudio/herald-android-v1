package cn.seu.herald_android.mod_wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Vibrator;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;


public class NetworkLoginHelper {

    private Context context;

    private Vibrator vibrator;

    public NetworkLoginHelper(Context context) {
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private static String formatTime(String time) {
        int seconds = Integer.valueOf(time);
        int minutes = seconds / 60;
        seconds %= 60;
        return minutes + ":" + ((seconds < 10) ? "0" : "") + seconds;
    }

    private static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }

    public void checkAndLogin() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replaceAll("\"", "");
        if (ssid.equals("seu-wlan") || ssid.equals("seu-dorm")) {
            checkOnlineStatus();
        } else {
            vibrator.vibrate(50);
            showTrickyMessage("你需要手动连接到seu网络才能摇一摇登录~");
        }
    }

    private void checkOnlineStatus() {
        OkHttpUtils.get().url("http://w.seu.edu.cn/portal/init.php").build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                vibrator.vibrate(50);
                ContextUtils.showMessage(context, "似乎信号有点差，不妨换个姿势试试？");
            }

            @Override
            public void onResponse(String response) {
                if (response.contains("notlogin")) {
                    // 未登录状态，开始登录
                    loginToService();
                } else {
                    vibrator.vibrate(50);
                    showTrickyMessage("你已经登录校园网，不用再摇了~");
                }
            }
        });
    }

    private void loginToService() {
        //登陆网络服务
        ApiHelper helper = new ApiHelper(context);
        String username = helper.getUserName();
        String password = helper.getPassword();

        OkHttpUtils.post().url("http://w.seu.edu.cn/portal/login.php")
                .addParams("username", username)
                .addParams("password", password).build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                vibrator.vibrate(50);
                ContextUtils.showMessage(context, "似乎信号有点差，不妨换个姿势试试？");
            }

            @Override
            public void onResponse(String response) {
                try {
                    // 登陆成功状态
                    JSONObject info = new JSONObject(response);
                    String[] infoStr = {
                            info.getString("login_username"),
                            info.getString("login_index"),
                            info.getString("login_ip"),
                            unicodeToString(info.getString("login_location")),
                            info.getString("login_expire"),
                            info.getString("login_remain"),
                            formatTime(info.getString("login_time"))
                    };
                    vibrator.vibrate(new long[]{0, 50, 100, 50}, -1);
                    ContextUtils.showMessage(context, "小猴已经成功帮你登陆seu网络啦，开始使用吧");
                } catch (JSONException e) {
                    try {
                        String error = new JSONObject(response).getString("error");
                        vibrator.vibrate(50);
                        ContextUtils.showMessage(context, "登陆失败，" + error);
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
    }

    private static String lastMessage = "";

    private static final String[] trickyMessages = new String[]{
            "喂喂，小猴要被你摇晕啦~",
            "停一下，停一下，不要再摇啦X_X~",
            "主人你再这样摇下去的话，小猴要被摇坏了呜呜",
            "好了好了，你再摇一次我就要罢工了~！"
    };

    private static int trickyCount = 0;

    private void showTrickyMessage(String message) {
        if (message.equals(lastMessage)) {
            message = trickyMessages[trickyCount];// TODO 此处故意留出数组越界，摇多了直接任性闪退
            trickyCount++;
        } else {
            lastMessage = message;
            trickyCount = 0;
        }
        ContextUtils.showMessage(context, message);
    }
}
