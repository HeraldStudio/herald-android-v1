package cn.seu.herald_android.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2015/12/14.
 */
public class SettingsHelper {

    //功能模块列表
    public static final int MODULE_SCHOOLBUS = 0;
    public static final int MODULE_LIBRARY = 1;
    public static final int MODULE_LECTURE = 2;
    public static final int MODULE_GRADE = 3;
    public static final int MODULE_EXPERIMENT = 4;
    public static final int MODULE_EMPTYROOM = 5;
    public static final int MODULE_CARDEXTRA = 6;

    //模块数目
    public static final int NUM_OF_MODULE = 7;


    //模块名字
    public static final String[] moduleNames = {
            "schoolbus",
            "library",
            "lecture",
            "grade",
            "experiment",
            "emptyroom",
            "cardextra",
    };

    //模块action
    public static final String[] moduleActions = {
            "cn.seu.herald_android.MODULE_QUERY_SCHOOLBUS",
            "cn.seu.herald_android.MODULE_QUERY_LIBRARY",
            "cn.seu.herald_android.MODULE_QUERY_LECTURE",
            "cn.seu.herald_android.MODULE_QUERY_GRADE",
            "cn.seu.herald_android.MODULE_QUERY_EXPERIMENT",
            "cn.seu.herald_android.MODULE_QUERY_EMPTYROOM",
            "cn.seu.herald_android.MODULE_QUERY_CARDEXTRA",
    };

    private Context context;
    private Activity activity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    public SettingsHelper(Activity activity){
        this.activity = activity;
        this.context = activity.getBaseContext();
        this.pref = activity.getSharedPreferences("herald_settings", Context.MODE_PRIVATE);
        this.editor = activity.getSharedPreferences("herald_settings", Context.MODE_PRIVATE).edit();
    }


    public void setModuleShortCutEnabled(int moduleID,boolean flag){
        //flag为true则设置为选中，否则设置为不选中
        if (flag){
            setCache("herald_settings_module_shortcutenabled_"+moduleNames[moduleID],"1");
        }else {
            setCache("herald_settings_module_shortcutenabled_"+moduleNames[moduleID],"0");
        }
    }

    public boolean getModuleShortCutEnabled(int moduleID){
        //获得某项模块的快捷图标是否显示
        if(getCache("herald_settings_module_shortcutenabled_"+moduleNames[moduleID]).equals("0")){
            return false;
        }else {
            return true;
        }
    }

    public class ShortCutSetting{
        int moduleId;
        boolean enabled;
        String actions;
        public ShortCutSetting(int moduleId, boolean enabled, String actions) {
            this.moduleId = moduleId;
            this.enabled = enabled;
            this.actions = actions;
        }
    }

    public ArrayList<ShortCutSetting> getShortCutSettingList(){
        //获得所有模块快捷方式设置列表
        ArrayList<ShortCutSetting> list = new ArrayList<>();
        for(int i=0;i<NUM_OF_MODULE;i++){
            list.add(new ShortCutSetting(i,getModuleShortCutEnabled(i),moduleActions[i]));
        }
        return list;
    }

    private String getCache(String cacheName){
        //可用
        /**
         * uuid         认证用uuid
         * cardnuim     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别
         */
        //获得存储的某项信息
        return pref.getString(cacheName,"");
    }

    private boolean setCache(String cacheName,String cacheValue){
        //用于更新存储的某项信息
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }
}
