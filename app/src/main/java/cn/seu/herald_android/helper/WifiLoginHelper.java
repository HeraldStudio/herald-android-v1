package cn.seu.herald_android.helper;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Vibrator;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import cn.seu.herald_android.framework.AppContext;
import okhttp3.Call;

public class WifiLoginHelper {

    private Context context;

    private Vibrator vibrator;

    public WifiLoginHelper(Context context) {
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void checkAndLogin() {
        if (!ApiHelper.isWifiLoginAvailable()) {
            ApiHelper.showTrialFunctionLimitMessage();
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replaceAll("\"", "");
        if (ssid.equals("seu-wlan") || ssid.equals("seu-dorm")) {
            vibrator.vibrate(50);
            AppContext.showMessage("正在快捷登录校园网，请稍候~");

            // 防止两条消息同时显示会吞掉前一条消息
            new Handler().postDelayed(this::checkOnlineStatus, 500);
        } else {
            vibrator.vibrate(50);
            AppContext.showMessage("你需要手动连接到seu网络才能登录校园网~");
        }
    }

    private void checkOnlineStatus() {
        OkHttpUtils.get().url("http://w.seu.edu.cn/portal/init.php").build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                vibrator.vibrate(50);
                AppContext.showMessage("校园网信号不佳，换个姿势试试？");
            }

            @Override
            public void onResponse(String response) {
                if (response.contains("notlogin")
                        // 如果已经登陆的账号与当前设置的校园网账号不同，也视为未登录
                        || !response.contains(ApiHelper.getWifiUserName())) {
                    // 未登录状态，开始登录
                    loginToService();
                } else {
                    vibrator.vibrate(50);
                    AppContext.showMessage("已登录校园网，无需重复登录~", "退出登录", () -> logoutFromService());
                }
            }
        });
    }

    private void loginToService(String username,String password){
        // 登陆网络服务
        OkHttpUtils.post().url("http://w.seu.edu.cn/portal/login.php")
                .addParams("username", username)
                .addParams("password", password).build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                vibrator.vibrate(50);
                AppContext.showMessage("校园网信号不佳，换个姿势试试？");
            }

            @Override
            public void onResponse(String response) {
                // 登陆成功状态
                vibrator.vibrate(50);
                AppContext.showMessage("小猴登录校园网成功~", "退出登陆", () -> logoutFromService());
            }
        });
    }

    private void loginToService() {
        // 登陆网络服务
        String username = ApiHelper.getWifiUserName();
        String password = ApiHelper.getWifiPassword();
        loginToService(username,password);
    }



    private void logoutFromService() {
        OkHttpUtils.post().url("http://w.seu.edu.cn/portal/logout.php").build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                vibrator.vibrate(50);
                AppContext.showMessage("校园网退出登录失败，请重试");
            }

            @Override
            public void onResponse(String response) {
                vibrator.vibrate(50);
                AppContext.showMessage("校园网退出登录成功");
            }
        });
    }
}
