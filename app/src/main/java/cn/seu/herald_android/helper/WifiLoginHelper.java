package cn.seu.herald_android.helper;

import android.content.Context;
import android.os.Vibrator;
import android.util.Base64;

import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

public class WifiLoginHelper {

    private Context context;

    private Vibrator vibrator;

    static boolean working = false;

    public WifiLoginHelper(Context context) {
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void checkAndLogin() {
        if (!ApiHelper.isWifiLoginAvailable()) {
            ApiHelper.showTrialFunctionLimitMessage();
            return;
        }

        if (working) {
            return;
        }

        working = true;
        beginCheck();
    }

    private void beginCheck() {
        new ApiSimpleRequest(Method.POST).url("https://selfservice.seu.edu.cn/selfservice/index.php")
                .onResponse((success, code, response) -> {
                    if (!response.contains("403 Forbidden")) {
                        checkOnlineStatus();
                    } else {
                        working = false;
                        AppContext.showMessage("校园网快捷登录：状态异常，请先手动连接到 seu-wlan 后再试~");
                    }
                }).runWithoutFatalListener();
    }

    private void checkOnlineStatus() {
        new ApiSimpleRequest(Method.GET).url("http://w.seu.edu.cn/index.php/index/init")
                .onResponse((success, code, response) -> {
                    if (success) {
                        JObj responseJSON = new JObj(response);
                        if (responseJSON.$i("status") == 0) {
                            // 未登录状态，直接登录
                            loginToService();
                        } else if (!responseJSON.$s("logout_username").equals(ApiHelper.getWifiUserName())) {
                            logoutThenLogin();
                        } else {
                            working = false;
                            AppContext.showMessage("校园网快捷登录：已登录状态，无需重复登录~");
                        }
                    } else {
                        working = false;
                        AppContext.showMessage("校园网快捷登录：信号不佳，换个姿势试试？");
                    }
                }).run();
    }

    private void logoutThenLogin() {
        new ApiSimpleRequest(Method.POST).url("http://w.seu.edu.cn/index.php/index/logout")
                .onResponse((success, code, response) -> {
                    if (success) {
                        loginToService();
                    } else {
                        working = false;
                        AppContext.showMessage("校园网快捷登录：已登录账号退出失败，请重试~");
                    }
                }).run();
    }

    private void loginToService() {
        String username = ApiHelper.getWifiUserName();
        String password = ApiHelper.getWifiPassword();
        String passwordEncoded = new String(Base64.encode(password.getBytes(), Base64.DEFAULT));

        new ApiSimpleRequest(Method.POST).url("http://w.seu.edu.cn/index.php/index/login")
                .post("username", username, "password", passwordEncoded, "domain", "teacher")
                .onResponse((success, code, response) -> {
                    if (success) {
                        JObj info = new JObj(response);
                        if (info.$i("status") == 1) {
                            AppContext.showMessage("校园网快捷登录成功~");
                        } else {
                            String error = info.$s("info");
                            if (!error.equals("")) {
                                AppContext.showMessage("校园网快捷登录失败：" + error);
                            } else {
                                AppContext.showMessage("校园网快捷登录：信号不佳，换个姿势试试？");
                            }
                        }
                    } else {
                        AppContext.showMessage("校园网快捷登录：信号不佳，换个姿势试试？");
                    }
                    working = false;
                }).run();
    }
}
