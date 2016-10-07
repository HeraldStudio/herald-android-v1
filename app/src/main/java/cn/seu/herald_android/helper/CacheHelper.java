package cn.seu.herald_android.helper;

import android.support.annotation.NonNull;

import cn.seu.herald_android.framework.UserCache;

public class CacheHelper {

    private CacheHelper() {}

    /**
     * 用 UserCache 代替 SharedPreferences; 此处因为会随用户变化，不能直接初始化成定值，而是要用一个 $get 函数代替
     */
    @NonNull
    private static UserCache getCache() {
        return new UserCache("herald_" + ApiHelper.getCurrentUser().userName);
    }

    /** 本 Helper 的缓存已经改成随用户变化，退出登录时不再需要清空 */
    @NonNull
    public static String get(String cacheName) {
        return getCache().get(cacheName);
    }

    // 返回值为该缓存与之前相比是否有变化
    public static void set(String cacheName, String cacheValue) {
        getCache().set(cacheName, cacheValue);
    }
}
