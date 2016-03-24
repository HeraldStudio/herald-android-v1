package cn.seu.herald_android.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_modulemanager.SeuModule;

public class SettingsHelper {


    /**
     * 新加入功能时需要做的事情有:
     * 功能模块列表增加相应列
     * 对应模块名字增加
     * 对应模块名字中文提示增加
     * 启动模块的ACTION的添加，同时在manifests文件里注册
     * 模块图标资源文件的添加
     */
    //模块类型列表
    public static final int MODULE_TYPE_QUERY = 0;
    //功能模块列表
    /**
     * 查询类模块
     */
    public static final int MODULE_CARDEXTRA = 0;
    public static final int MODULE_PEDETAIL = 1;
    public static final int MODULE_CURRICULUM = 2;
    public static final int MODULE_EXPERIMENT = 3;
    public static final int MODULE_LECTURE = 4;
    public static final int MODULE_JWC = 5;
    public static final int MODULE_TODO = 6;
    public static final int MODULE_SEUNET = 7;
    public static final int MODULE_LIBRARY = 8;
    public static final int MODULE_GRADE = 9;
    public static final int MODULE_SRTP = 10;
    public static final int MODULE_SCHOOLBUS = 11;
    public static final int MODULE_SCHEDULE = 12;
    public static final int MODULE_GYMRESERVE = 13;
    public static final int WEBMODULE_QUANYI = 14;
    public static final int WEBMODULE_EMPTYROOM = 15;


    //模块名字
    public static final String[] moduleNames = {
            "cardextra",
            "pedetail",
            "curriculum",
            "experiment",
            "lecture",
            "jwc",
            "todo",
            "seunet",
            "library",
            "grade",
            "srtp",
            "schoolbus",
            "schedule",
            "gymreserve",
            "quanyi",
            "emptyroom"
    };

    //模块名字文字提示
    public static final String[] moduleNamesTips = {
            "一卡通",
            "跑操助手",
            "课表助手",
            "实验助手",
            "人文讲座",
            "教务通知",
            "日程提醒",
            "校园网络",
            "图书馆",
            "绩点查询",
            "课外研学",
            "校车助手",
            "校历查询",
            "场馆预约",
            "权益服务",
            "空教室"
    };

    //模块说明，显示在快捷方式编辑界面
    public static final String[] moduleDescriptions = {
            "提供一卡通消费情况查询、一卡通在线充值以及余额提醒服务",
            "提供跑操次数及记录查询、早操预报以及跑操到账提醒服务",
            "浏览当前学期的课表信息，并提供上课提醒服务",
            "浏览当前学期的实验信息，并提供实验提醒服务",
            "查看人文讲座听课记录，并提供人文讲座预告信息",
            "显示教务处最新通知，提供重要教务通知提醒服务",
            "提供日程安排提醒、倒计日、正计日等个性化提醒功能",
            "显示校园网使用情况及校园网账户余额信息",
            "查看图书馆实时借阅排行、已借书籍和馆藏图书搜索",
            "查询历史学期的科目成绩、学分以及绩点详情",
            "提供SRTP学分及得分详情查询服务",
            "提供可实时更新的校车班车时间表",
            "显示当前年度各学期的学校校历安排",
            "提供体育场馆在线预约和查询服务",
            "向东大校会权益部反馈投诉信息",
            "提供指定时间内的空教室信息查询服务"
    };

    //模块action
    public static final String[] moduleActions = {
            "cn.seu.herald_android.MODULE_QUERY_CARDEXTRA",
            "cn.seu.herald_android.MODULE_QUERY_PEDETAIL",
            "cn.seu.herald_android.MODULE_QUERY_CURRICULUM",
            "cn.seu.herald_android.MODULE_QUERY_EXPERIMENT",
            "cn.seu.herald_android.MODULE_QUERY_LECTURE",
            "cn.seu.herald_android.MODULE_QUERY_JWC",
            "cn.seu.herald_android.MODULE_LOCAL_TODO",
            "cn.seu.herald_android.MODULE_QUERY_SEUNET",
            "cn.seu.herald_android.MODULE_QUERY_LIBRARY",
            "cn.seu.herald_android.MODULE_QUERY_GRADE",
            "cn.seu.herald_android.MODULE_QUERY_SRTP",
            "cn.seu.herald_android.MODULE_QUERY_SCHOOLBUS",
            "cn.seu.herald_android.MODULE_QUERY_SCHEDULE",
            "cn.seu.herald_android.MODULE_QUERY_GYMRESERVE",
            "cn.seu.herald_android.WEBMODULE_QUANYI",
            "cn.seu.herald_android.WEBMODULE_EMPTYROOM"
    };


    //模块图标
    public static final int[] moduleIconsId = {
            R.mipmap.ic_card,
            R.mipmap.ic_pedetail,
            R.mipmap.ic_curriculum,
            R.mipmap.ic_experiment,
            R.mipmap.ic_lecture,
            R.mipmap.ic_jwc,
            R.mipmap.ic_todo,
            R.mipmap.ic_seunet,
            R.mipmap.ic_library,
            R.mipmap.ic_grade,
            R.mipmap.ic_srtp,
            R.mipmap.ic_bus,
            R.mipmap.ic_schedule,
            R.mipmap.ic_gymreserve,
            R.mipmap.ic_quanyi,
            R.mipmap.ic_emptyroom
    };

    public static final boolean[] moduleHasCard = {
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false
    };

    //模块类型
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    public SettingsHelper(Context context) {
        this.pref = context.getSharedPreferences("herald_settings", Context.MODE_PRIVATE);
        this.editor = context.getSharedPreferences("herald_settings", Context.MODE_PRIVATE).edit();
    }

    public void setDefaultConfig() {
        setDefaultShortcutEnabled();
    }

    private void setDefaultShortcutEnabled() {
        //默认快捷栏只显示无卡片的模块
        for (int i = 0; i < moduleNames.length; i++) {
            setModuleShortCutEnabled(i, !moduleHasCard[i]);
        }
    }
    
    /**
     * 用于设置某个模块是否在快捷方式盒子中显示
     *
     * @param moduleID 模块ID
     * @param flag     true为启用，false为禁用
     */
    public void setModuleShortCutEnabled(int moduleID, boolean flag) {
        //flag为true则设置为选中，否则设置为不选中
        if (flag) {
            setCache("herald_settings_module_shortcutenabled_" + moduleNames[moduleID], "1");
        } else {
            setCache("herald_settings_module_shortcutenabled_" + moduleNames[moduleID], "0");
        }
    }

    /**
     * 获得某个模块的快捷方式使用情况
     *
     * @param moduleID 模块ID
     */
    public boolean getModuleShortCutEnabled(int moduleID) {
        //获得某项模块的快捷图标是否显示
        return !getCache("herald_settings_module_shortcutenabled_" + moduleNames[moduleID]).equals("0");
    }

    /**
     * 用于设置某个模块是否在首页卡片中显示
     *
     * @param moduleID 模块ID
     * @param flag     true为启用，false为禁用
     */
    public void setModuleCardEnabled(int moduleID, boolean flag) {
        if (!moduleHasCard[moduleID]) return;
        //flag为true则设置为选中，否则设置为不选中
        if (flag) {
            setCache("herald_settings_module_cardenabled_" + moduleNames[moduleID], "1");
        } else {
            setCache("herald_settings_module_cardenabled_" + moduleNames[moduleID], "0");
        }
    }

    /**
     * 获得某个模块在首页卡片中的显示情况
     *
     * @param moduleID 模块ID
     */
    public boolean getModuleCardEnabled(int moduleID) {
        //获得某项模块的快捷图标是否显示
        return !getCache("herald_settings_module_cardenabled_" + moduleNames[moduleID]).equals("0");
    }

    /**
     * 获得所有模块的快捷方式设置情况对象
     */
    public ArrayList<SeuModule> getSeuModuleList() {
        //获得所有模块快捷方式设置列表
        ArrayList<SeuModule> list = new ArrayList<>();
        for (int i = 0; i < moduleNames.length; i++) {
            list.add(new SeuModule(i, getModuleShortCutEnabled(i), getModuleCardEnabled(i), moduleActions[i]));
        }
        return list;
    }

    /**
     * 获得是否选择自动登录seu
     */
    public boolean getWifiAutoLogin(){
        String seuauto = getCache("herald_settings_wifi_autologin");
        return seuauto.equals("") || seuauto.equals("1");
    }

    public void setWifiAutoLogin(boolean flag){
        if (flag){
            setCache("herald_settings_wifi_autologin","1");
        }else {
            setCache("herald_settings_wifi_autologin","0");
        }
    }

    /**
     * 获得应用启动次数
     */
    public int getLaunchTimes() {
        String times = getCache("herald_settings_launch_time");
        if (times.equals("")) {
            setCache("herald_settings_launch_time", "0");
            return 0;
        } else {
            return Integer.parseInt(times);
        }
    }

    /**
     * 设置应用启动次数
     *
     * @param times 要设置的次数
     */
    public void updateLaunchTimes(int times) {
        setCache("herald_settings_launch_time", times + "");
    }


    private String getCache(String cacheName) {
        //可用
        /**
         * uuid         认证用uuid
         * cardnum     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别
         */
        //获得存储的某项信息
        return pref.getString(cacheName, "");
    }

    private boolean setCache(String cacheName, String cacheValue) {
        //用于更新存储的某项信息
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }


}
