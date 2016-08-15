package cn.seu.herald_android.helper;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.LinkedList;

import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.User;
import cn.seu.herald_android.framework.UserCache;
import cn.seu.herald_android.framework.json.JObj;

public class ApiHelper {
    // heraldstudio.com 主站API
    public static String WWW_ROOT = "http://www.heraldstudio.com/";
    public static String API_ROOT = WWW_ROOT + "api/";

    public static String auth_url = WWW_ROOT + "uc/auth";
    public static String auth_update_url = WWW_ROOT + "uc/update";
    public static String wechat_lecture_notice_url = WWW_ROOT + "wechat2/lecture";
    public static String feedback_url = WWW_ROOT + "service/feedback";

    private ApiHelper() {}

    @NonNull
    public static String getAppid() {
        Context context = AppContext.instance;
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead;
        MessageDigest md5;
        String appid = "";
        try {
            fis = new FileInputStream(context.getPackageResourcePath());
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            appid = HashHelper.toHexString(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return /*"34cc6df78cfa7cd457284e4fc377559e";// todo*/ appid;
        /**
         * 以后不再通过修改此处return语句的方法来使用测试appid，而是在手机剪贴板中事先复制好如下字符串既可登录：
         * IAmTheGodOfHerald|OverrideAppidWith:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
         * 其中最后一串代表你的测试appid。这个后门即使被发现，也不会泄漏我们的测试appid，所以是安全的。
         * 该后门实现见LoginActivity.java
         **/
    }

    @NonNull
    public static String getApiUrl(String api) {
        return API_ROOT + api;
    }

    public interface OnUserChangeListener {
        void onUserChange();
    }

    private static LinkedList<OnUserChangeListener> userChangedListeners = new LinkedList<>();

    public static void registerOnUserChangeListener(OnUserChangeListener listener) {
        userChangedListeners.add(listener);
    }

    public static void unregisterOnUserChangeListener(OnUserChangeListener listener) {
        userChangedListeners.remove(listener);
    }

    public static void notifyUserChanged() {
        for (OnUserChangeListener listener : userChangedListeners) {
            if (listener != null) {
                listener.onUserChange();
            }
        }
    }

    public static void notifyUserIdentityExpired() {
        doLogout("用户身份已过期，请重新登录");
    }

    /**
     * 当前用户，可以直接调用 $set() 来切换用户
     */
    @NonNull
    public static User getCurrentUser() {
        return new User(new JObj(get("currentUser")));
    }

    public static void setCurrentUser(User newValue) {
        set("currentUser", newValue.toJson().toString());
        notifyUserChanged();
    }

    public static void doLogout(String message) {
        if (isLogin()) {
            setCurrentUser(User.trialUser);

            AppContext.showLogin();

            if (message != null) {
                AppContext.showMessage(message);
            }
        }
    }

    // 显示一个提示用户处于未登录模式，不能使用此功能的对话框
    public static void showTrialFunctionLimitMessage() {
        AppContext.showMessage("该功能需要登录使用", "立即登录", () -> doLogout(null));
    }

    public static boolean isLogin() {
        // 判断是否已登录
        return !getCurrentUser().equals(User.trialUser);
    }

    // 单独更新校园网登陆账户
    public static void setWifiAuth(String username, String password) {
        try {
            String encrypted = new EncryptHelper(username).encrypt(password);
            CacheHelper.set("wifiAuthUser", username);
            CacheHelper.set("wifiAuthPwd", encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public static String getWifiUserName() {
        String cacheUser = CacheHelper.get("wifiAuthUser");

        // 若无校园网独立用户缓存，则使用登陆应用的账户
        if (cacheUser.equals("")) return getCurrentUser().userName;
        return cacheUser;
    }

    @NonNull
    public static String getWifiPassword() {
        String username = getWifiUserName();
        EncryptHelper helper1 = new EncryptHelper(username);
        String cachePwd = CacheHelper.get("wifiAuthPwd");

        // 若无校园网独立用户缓存，则使用登陆应用的账户
        if (cachePwd.equals("") || helper1.decrypt(cachePwd).equals("")) {
            return getCurrentUser().password;
        }
        return helper1.decrypt(cachePwd);
    }

    public static boolean isWifiLoginAvailable() {
        return isLogin() || !getWifiUserName().equals(User.trialUser.userName);
    }

    public static void clearWifiAuth() {
        CacheHelper.set("wifiAuthUser", "");
        CacheHelper.set("wifiAuthPwd", "");
    }

    @NonNull
    public static UserCache getAuthCache() {
        return new UserCache("auth");
    }

    @NonNull
    private static String get(String key) {
        return getAuthCache().get(key);
    }

    private static void set(String key, String value) {
        getAuthCache().set(key, value);
    }
}
