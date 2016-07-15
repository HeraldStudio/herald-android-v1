package cn.seu.herald_android.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import cn.seu.herald_android.app_framework.$;
import cn.seu.herald_android.app_framework.AppContext;

public class ApiHelper {
    // heraldstudio.com 主站API
    public static String WWW_ROOT = "http://www.heraldstudio.com/";
    public static String API_ROOT = WWW_ROOT + "api/";

    public static String auth_url = WWW_ROOT + "uc/auth";
    public static String auth_update_url = WWW_ROOT + "uc/update";
    public static String wechat_lecture_notice_url = WWW_ROOT + "wechat2/lecture";
    public static String feedback_url = WWW_ROOT + "service/feedback";

    private ApiHelper() {}

    public static $<String> appid = new $<>(() -> {
        Context context = AppContext.currentContext.$get();
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

        return "34cc6df78cfa7cd457284e4fc377559e";// todo*/213151933 appid;
        /**
         * 以后不再通过修改此处return语句的方法来使用测试appid，而是在手机剪贴板中事先复制好如下字符串既可登录：
         * IAmTheGodOfHerald|OverrideAppidWith:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
         * 其中最后一串代表你的测试appid。这个后门即使被发现，也不会泄漏我们的测试appid，所以是安全的。
         * 该后门实现见LoginActivity.java
         **/
    });

    public static $<SharedPreferences> authCache = new $<>(() -> {
        Context context = AppContext.currentContext.$get();
        return context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE);
    });

    public static String getApiUrl(String api) {
        return API_ROOT + api;
    }

    public static void doLogout(String message) {
        //清除授权信息
        setAuthCache("authUser", "");
        setAuthCache("authPwd", "");
        setAuthCache("uuid", "");
        setAuthCache("schoolnum", "");

        //清除模块缓存
        //注意此处的clearAllmoduleCache里的authUser和authPwd与上面清除的是不同的
        CacheHelper.clearAllModuleCache();

        //跳转到登录页
        //如果activity为空会抛出异常
        AppContext.showLogin();

        if(message != null) {
            AppContext.showMessage(message);
        }
    }

    public static boolean isLogin() {
        //判断是否已登录
        String uuid = authCache.$get().getString("uuid", "");
        return !uuid.equals("");
    }

    public static String getUUID() {
        //获得存储的uuid
        return authCache.$get().getString("uuid", "");
    }

    public static void setAuth(String username, String password) {
        try {
            String encrypted = new EncryptHelper(username).encrypt(password);
            CacheHelper.set("authUser", username);
            CacheHelper.set("authPwd", encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUserName() {
        return CacheHelper.get("authUser");
    }

    public static String getPassword() {
        String username = getUserName();
        EncryptHelper helper1 = new EncryptHelper(username);
        return helper1.decrypt(CacheHelper.get("authPwd"));
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

    public static String getWifiUserName() {
        String cacheUser = CacheHelper.get("wifiAuthUser");

        // 若无校园网独立用户缓存，则使用登陆应用的账户
        if (cacheUser.equals("")) return getUserName();
        return cacheUser;
    }

    public static String getWifiPassword() {
        String username = getWifiUserName();
        EncryptHelper helper1 = new EncryptHelper(username);
        String cachePwd = CacheHelper.get("wifiAuthPwd");

        // 若无校园网独立用户缓存，则使用登陆应用的账户
        if (cachePwd.equals("") || helper1.decrypt(cachePwd).equals("")) return getPassword();
        return helper1.decrypt(cachePwd);
    }

    public static void clearWifiAuth() {
        CacheHelper.set("wifiAuthUser", "");
        CacheHelper.set("wifiAuthPwd", "");
    }

    public static String getAuthCache(String cacheName) {
        //可用
        /**
         * uuid         认证用uuid
         * cardnum     一卡通号
         * schoolnum    学号
         */
        //获得存储的某项信息
        return authCache.$get().getString(cacheName, "");
    }

    public static void setAuthCache(String cacheName, String cacheValue) {
        //用于更新存储的某项信息
        SharedPreferences.Editor editor = authCache.$get().edit();
        editor.putString(cacheName, cacheValue);
        editor.commit();
    }

    public static String getSchoolnum() {
        return getAuthCache("schoolnum");
    }
}
