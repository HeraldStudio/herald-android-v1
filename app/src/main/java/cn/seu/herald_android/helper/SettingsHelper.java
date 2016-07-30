package cn.seu.herald_android.helper;

import java.util.Vector;

import cn.seu.herald_android.framework.UserCache;

public class SettingsHelper {

    private SettingsHelper() {}

    /**
     * 用 UserCache 代替 SharedPreferences; 此处因为会随用户变化，不能直接初始化成定值，而是要用一个 $get 函数代替
     */
    private static UserCache getSettingsCache() {
        return new UserCache("settings_" + ApiHelper.getCurrentUser().userName);
    }

    /** 本 Helper 的缓存已经改成随用户变化，退出登录时不再需要清空 */
    public static String get(String key) {
        return getSettingsCache().get(key);
    }

    public static void set(String key, String value) {
        getSettingsCache().set(key, value);
    }

    public static boolean getWifiAutoLogin() {
        return getSettingsCache().get("herald_settings_wifi_auto_login").equals("1");
    }

    public static void setWifiAutoLogin(boolean newValue) {
        getSettingsCache().set("herald_settings_wifi_auto_login", newValue ? "1" : "0");
    }

    /** 模块设置变化的监听器 */
    private static Vector<Runnable> moduleSettingsChangeListeners = new Vector<>();

    public static void addModuleSettingsChangeListener (Runnable listener) {
        moduleSettingsChangeListeners.add(listener);
    }

    public static void notifyModuleSettingsChanged () {
        for (Runnable listener : moduleSettingsChangeListeners) {
            listener.run();
        }
    }
}
