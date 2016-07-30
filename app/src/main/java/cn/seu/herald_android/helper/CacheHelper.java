package cn.seu.herald_android.helper;

import cn.seu.herald_android.framework.UserCache;

public class CacheHelper {

    private CacheHelper() {}

    /**
     * 用 UserCache 代替 SharedPreferences; 此处因为会随用户变化，不能直接初始化成定值，而是要用一个 $get 函数代替
     */
    private static UserCache getCache() {
        return new UserCache("herald_" + ApiHelper.getCurrentUser().userName);
    }

    /** 本 Helper 的缓存已经改成随用户变化，退出登录时不再需要清空 */
    public static String get(String cacheName) {
        return getCache().get(cacheName);
    }

    // 返回值为该缓存与之前相比是否有变化
    public static boolean set(String cacheName, String cacheValue) {
        String oldValue = getCache().get(cacheName);
        getCache().set(cacheName, cacheValue);

        // 此处首先判断旧值是否为空，若旧值为空说明是初次更新
        // 因为初次更新无法判断实际数据是否发生了变化，同时也是为了为了首次启动时不干扰用户，所以初次更新时不显示小红点
        return !oldValue.equals("") && !cacheValue.equals(oldValue);
    }
}
