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


    /**
     * 新加入功能时需要做的事情有:
     * 功能模块列表增加相应列
     * 模块数目增加
     * 对应模块名字增加
     * 对应模块名字中文提示增加
     * 启动模块的ACTION的添加，同时在manifests文件里注册
     * 模块图标资源文件的添加
     *
     */
    //功能模块列表
    public static final int MODULE_SCHOOLBUS = 0;
    public static final int MODULE_LIBRARY = 1;
    public static final int MODULE_LECTURE = 2;
    public static final int MODULE_GRADE = 3;
    public static final int MODULE_EXPERIMENT = 4;
    public static final int MODULE_EMPTYROOM = 5;
    public static final int MODULE_CARDEXTRA = 6;
    public static final int MODULE_SEUNET = 7;

    //模块数目
    public static final int NUM_OF_MODULE = 8;


    //模块名字
    public static final String[] moduleNames = {
            "schoolbus",
            "library",
            "lecture",
            "grade",
            "experiment",
            "emptyroom",
            "cardextra",
            "seunet"
    };

    //模块名字文字提示
    public static final String[] moduleNamesTips = {
            "校车助手",
            "图书馆查询",
            "人文讲座",
            "GPA",
            "实验",
            "空教室",
            "一卡通",
            "校园网"
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
            "cn.seu.herald_android.MODULE_QUERY_SEUNET"
    };


    //模块图标
    public static final int[] moduleIconsId = {
            R.mipmap.ic_bus,
            R.mipmap.ic_library,
            R.mipmap.ic_lecture,
            R.mipmap.ic_grade,
            R.mipmap.ic_experiment,
            R.mipmap.ic_emptyroom,
            R.mipmap.ic_card,
            R.mipmap.ic_seunet
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

    public void setDefaultConfig(){
        //恢复默认设置
        setDefaultModuleShortCut();
    }

    public void setDefaultModuleShortCut(){
        //快捷方式的默认设置
        setModuleShortCutEnabled(MODULE_GRADE,true);
        setModuleShortCutEnabled(MODULE_CARDEXTRA, true);
    }




    /**
     * 用于设置某个模块是否在快捷方式盒子中显示
     * @param moduleID 模块ID
     * @param flag      true为启用，false为禁用
     */
    public void setModuleShortCutEnabled(int moduleID,boolean flag){
        //flag为true则设置为选中，否则设置为不选中
        if (flag){
            setCache("herald_settings_module_shortcutenabled_"+moduleNames[moduleID],"1");
        }else {
            setCache("herald_settings_module_shortcutenabled_"+moduleNames[moduleID],"0");
        }
    }

    /**
     * 获得某个模块的快捷方式使用情况
     * @param moduleID  模块ID
     * @return
     */
    public boolean getModuleShortCutEnabled(int moduleID){
        //获得某项模块的快捷图标是否显示
        if(getCache("herald_settings_module_shortcutenabled_"+moduleNames[moduleID]).equals("0")){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 获得所有模块的快捷方式设置情况对象
     * @return
     */
    public ArrayList<ShortCutSetting> getShortCutSettingList(){
        //获得所有模块快捷方式设置列表
        ArrayList<ShortCutSetting> list = new ArrayList<>();
        for(int i=0;i<NUM_OF_MODULE;i++){
            list.add(new ShortCutSetting(i,getModuleShortCutEnabled(i),moduleActions[i]));
        }
        return list;
    }

    /**
     * 获得应用启动次数
     * @return
     */
    public int getLaunchTimes(){
        String times = getCache("herald_settings_launtchtime");
        if(times.equals("")){
            setCache("herald_settings_launtchtime","0");
            return 0;
        }else{
            return Integer.parseInt(times);
        }
    }

    /**
     *
     * 设置应用启动次数
     * @param times 要设置的次数
     */
    public void updateLanuchTimes(int times){
        setCache("herald_settings_launtchtime",times+"");
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


    public class ShortCutSetting{
        //模块ID
        int moduleId;
        //图片资源
        int ic_id;
        //是否被选为快捷方式
        boolean enabled;
        //启动的activity的action
        String actions;
        //名字
        String name;
        public ShortCutSetting(int moduleId, boolean enabled, String actions) {
            this.moduleId = moduleId;
            this.enabled = enabled;
            this.actions = actions;
            this.ic_id = moduleIconsId[moduleId];
            this.name = moduleNamesTips[moduleId];
        }

        public int getModuleId() {
            return moduleId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getActions() {
            return actions;
        }

        public int getIc_id() {
            return ic_id;
        }

        public String getName() {
            return name;
        }
    }
}
