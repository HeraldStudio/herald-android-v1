package cn.seu.herald_android.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Set;

import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_modulemanager.SeuModule;

/**
 * Created by heyon on 2015/12/14.
 */
public class SettingsHelper {


    /**
     * 新加入功能时需要做的事情有:
     * 功能模块列表增加相应列
     * 对应模块名字增加
     * 对应模块名字中文提示增加
     * 启动模块的ACTION的添加，同时在manifests文件里注册
     * 模块图标资源文件的添加
     *
     */
    //模块类型列表
    public static final int MODULE_TYPE_QUERY =0;
    //功能模块列表
    /**
     * 查询类模块
     */
    public static final int MODULE_SCHOOLBUS = 0;
    public static final int MODULE_LIBRARY = 1;
    public static final int MODULE_LECTURE = 2;
    public static final int MODULE_GRADE = 3;
    public static final int MODULE_EXPERIMENT = 4;
    public static final int MODULE_CARDEXTRA = 5;
    public static final int MODULE_SEUNET = 6;
    public static final int MODULE_PEDETAIL = 7;
    public static final int MODULE_CURRICULUM = 8;
//    public static final int MODULE_EMPTYROOM = 9;
//    public static final int MODULE_GYMORDER = 10;
//    public static final int MODULE_QUANYI = 11;





    //模块名字
    public static final String[] moduleNames = {
            "schoolbus",
            "library",
            "lecture",
            "grade",
            "experiment",
            "cardextra",
            "seunet",
            "pedetail",
            "curriculum",
//            "emptyroom",
//            "gymorder",
//            "quanyi"
    };

    //模块名字文字提示
    public static final String[] moduleNamesTips = {
            "校车助手",
            "图书馆查询",
            "人文讲座",
            "GPA",
            "实验",
            "一卡通",
            "校园网",
            "跑操",
            "课程表",
//            "空教室",
//            "场馆预约",
//            "权益服务"
    };

    //模块action
    public static final String[] moduleActions = {
            "cn.seu.herald_android.MODULE_QUERY_SCHOOLBUS",
            "cn.seu.herald_android.MODULE_QUERY_LIBRARY",
            "cn.seu.herald_android.MODULE_QUERY_LECTURE",
            "cn.seu.herald_android.MODULE_QUERY_GRADE",
            "cn.seu.herald_android.MODULE_QUERY_EXPERIMENT",
            "cn.seu.herald_android.MODULE_QUERY_CARDEXTRA",
            "cn.seu.herald_android.MODULE_QUERY_SEUNET",
            "cn.seu.herald_android.MODULE_QUERY_PEDETAIL",
            "cn.seu.herald_android.MODULE_QUERY_CURRICULUM",
//            "cn.seu.herald_android.WEBMODULE_EMPTYROOM",
//            "cn.seu.herald_android.WEBMODULE_GYMORDER",
//            "cn.seu.herald_android.WEBMODULE_QUANYI",

    };


    //模块图标
    public static final int[] moduleIconsId = {
            R.mipmap.ic_bus,
            R.mipmap.ic_library,
            R.mipmap.ic_lecture,
            R.mipmap.ic_grade,
            R.mipmap.ic_experiment,
            R.mipmap.ic_card,
            R.mipmap.ic_seunet,
            R.mipmap.ic_pedetail,
            R.mipmap.ic_curriculum,
//            R.mipmap.ic_emptyroom,
//            R.mipmap.ic_gym,
//            R.mipmap.ic_quanyi
    };

    //模块类型

    private Context context;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;




    public SettingsHelper(Context context){
        this.context = context;
        this.pref = context.getSharedPreferences("herald_settings", Context.MODE_PRIVATE);
        this.editor = context.getSharedPreferences("herald_settings", Context.MODE_PRIVATE).edit();
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
    public ArrayList<SeuModule> getSeuModuleList(){
        //获得所有模块快捷方式设置列表
        ArrayList<SeuModule> list = new ArrayList<>();
        for(int i=0;i<moduleNames.length;i++){
            list.add(new SeuModule(i,getModuleShortCutEnabled(i),moduleActions[i]));
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



}
