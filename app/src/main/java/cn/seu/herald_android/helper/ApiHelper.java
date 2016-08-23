package cn.seu.herald_android.helper;

import android.support.annotation.NonNull;

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
    public static final String appid = "9f9ce5c3605178daadc2d85ce9f8e064";

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
