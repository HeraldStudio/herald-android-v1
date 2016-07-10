package cn.seu.herald_android.helper;

import cn.seu.herald_android.app_framework.$;
import cn.seu.herald_android.app_framework.UserCache;

public class CacheHelper {

    private CacheHelper() {}

    // 缓存名称列表, 注销时将取消这些缓存
    private static String[] cacheNames = new String[]{
            "authUser",
            "authPwd",
            "herald_card",
            "herald_card_left",
            "herald_card_date",
            "herald_card_today",
            "herald_grade_gpa",
            "herald_lecture_records",
            "herald_experiment",
            "herald_nic",
            "herald_srtp",
            "herald_curriculum",
            "herald_pedetail",
            "herald_pe_count",
            "herald_pe_remain",
            "herald_sidebar",
            "herald_exam",
            "herald_exam_definedexam",
            "herald_schedule_cache_time",
            "herald_library_borrowbook",
            "herald_gymreserve_recentfriendlist",
            "herald_gymreserve_phone",
            "herald_gymreserve_userid"
    };

    /** 用 UserCache 代替 SharedPreferences; 此处不能直接初始化, 所以用一个 get 函数代替 */
    private static $<UserCache> cache = new $<UserCache>() {
        @Override public UserCache get() {
            return new UserCache("herald");
        }
    };

    public static String get(String cacheName) {
        return cache.get().get(cacheName);
    }

    // 返回值为该缓存与之前相比是否有变化
    public static boolean set(String cacheName, String cacheValue) {
        String oldValue = cache.get().get(cacheName);
        cache.get().set(cacheName, cacheValue);

        // 此处首先判断旧值是否为空，若旧值为空说明是初次更新
        // 因为初次更新无法判断实际数据是否发生了变化，同时也是为了为了首次启动时不干扰用户，所以初次更新时不显示小红点
        return !oldValue.equals("") && !cacheValue.equals(oldValue);
    }

    public static void clearAllModuleCache() {
        for (String cacheName : cacheNames) {
            set(cacheName, "");
        }
    }
}
