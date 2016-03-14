package cn.seu.herald_android.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by heyon on 2016/2/23.
 */
public class CacheHelper {
    private Context context;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    //缓存名称列表,注销时将取消这些缓存
    public static String[] cacheNames=new String[]{
            "herald_card",
            "herald_grade_gpa",
            "herald_lecture_records",
            "herald_schoolbus_cache",
            "herald_lecture_notices",
            "herald_experiment",
            "herald_nic",
            "herald_srtp",
            "herald_pc_date",
            "herald_pc_forecast",
            "herald_pc_last_message",
            "herald_curriculum",
            "herald_lecture_notices",
            "herald_sidebar"
    };


    public CacheHelper(Context context){
        this.context = context;
        this.pref = context.getSharedPreferences("herald", Context.MODE_PRIVATE);
        this.editor = context.getSharedPreferences("herald", Context.MODE_PRIVATE).edit();
    }

    public String getCache(String cacheName){
        //可用
        /**
         * uuid         认证用uuid
         * cardnuim     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别
         */
        //获得存储的某项信息
        SharedPreferences pref = context.getSharedPreferences("herald", Context.MODE_PRIVATE);
        String authCache = pref.getString(cacheName,"");
        return authCache;
    }

    public boolean setCache(String cacheName,String cacheValue){
        //用于更新存储的某项信息
        SharedPreferences.Editor editor= context.getSharedPreferences("herald",context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }


    public void clearAllModuleCache(){
        for(int i = 0;i<cacheNames.length;i++){
            setCache(cacheNames[i],"");
        }
    }
}
