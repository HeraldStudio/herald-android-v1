package cn.seu.herald_android.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheHelper {
    //缓存名称列表,注销时将取消这些缓存
    private static String[] cacheNames = new String[]{
            "authUser",
            "authPwd",
            "herald_card",
            "herald_card_left",
            "herald_card_date",
            "herald_grade_gpa",
            "herald_lecture_records",
            "herald_experiment",
            "herald_nic",
            "herald_srtp",
            "herald_curriculum",
            "herald_pedetail",
            "herald_pe_count",
            "herald_pe_remain",
            "herald_pc_ignored_date",
            "herald_sidebar",
            "herald_exam",
            "herald_exam_definedexam",//自定义考试信息
            "herald_schedule_cache_time",
            "herald_gymreserve_recentfriendlist",
            "herald_gymreserve_phone",

    };
    private Context context;


    public CacheHelper(Context context) {
        this.context = context;
    }

    public String getCache(String cacheName) {
        //可用
        /**
         * uuid         认证用uuid
         * cardnuim     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别
         * phone        手机号：在第一次打开场馆预约时会获取
         */
        //获得存储的某项信息
        SharedPreferences pref = context.getSharedPreferences("herald", Context.MODE_PRIVATE);
        return pref.getString(cacheName, "");
    }

    public boolean setCache(String cacheName, String cacheValue) {
        //用于更新存储的某项信息
        SharedPreferences.Editor editor = context.getSharedPreferences("herald", Context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }


    public void clearAllModuleCache() {
        for (String cacheName : cacheNames) {
            setCache(cacheName, "");
        }
    }
}
