package cn.seu.herald_android.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by heyon on 2016/2/23.
 */
public class CacheHelper {
    private Context context;
    private Activity activity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public CacheHelper(Context context,Activity activity){
        this.context = context;
        this.pref = activity.getSharedPreferences("herald", Context.MODE_PRIVATE);
        this.editor = activity.getSharedPreferences("herald", Context.MODE_PRIVATE).edit();
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
        SharedPreferences pref = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        String authCache = pref.getString(cacheName,"");
        return authCache;
    }

    public boolean setCache(String cacheName,String cacheValue){
        //用于更新存储的某项信息
        SharedPreferences.Editor editor= context.getSharedPreferences("Auth",context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }
}
