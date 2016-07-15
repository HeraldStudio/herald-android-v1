package cn.seu.herald_android.helper;

import java.util.Vector;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.$;
import cn.seu.herald_android.app_framework.UserCache;

public class SettingsHelper {

    private SettingsHelper() {}

    /** 此处对应 iOS 版 R 类中的 module 子类 */
    public static class Module {

        // 有卡片的模块
        public static AppModule card = new AppModule(0, "cardextra",
                "一卡通", "提供一卡通消费情况查询、一卡通在线充值以及余额提醒服务",
                "MODULE_QUERY_CARDEXTRA", R.mipmap.ic_card, true);

        public static AppModule pedetail = new AppModule(1, "pedetail",
                "跑操助手", "提供跑操次数及记录查询、早操预报以及跑操到账提醒服务",
                "MODULE_QUERY_PEDETAIL", R.mipmap.ic_pedetail, true);

        public static AppModule curriculum = new AppModule(2, "curriculum",
                "课表助手", "浏览当前学期的课表信息，并提供上课提醒服务",
                "MODULE_QUERY_CURRICULUM", R.mipmap.ic_curriculum, true);

        public static AppModule experiment =  new AppModule(3, "experiment",
                "实验助手", "浏览当前学期的实验信息，并提供实验提醒服务",
                "MODULE_QUERY_EXPERIMENT", R.mipmap.ic_experiment, true);

        public static AppModule lecture = new AppModule(4, "lecture",
                "人文讲座", "查看人文讲座听课记录，并提供人文讲座预告信息",
                "MODULE_QUERY_LECTURE", R.mipmap.ic_lecture, true);

        public static AppModule jwc = new AppModule(5, "jwc",
                "教务通知", "显示教务处最新通知，提供重要教务通知提醒服务",
                "MODULE_QUERY_JWC", R.mipmap.ic_jwc, true);

        public static AppModule exam = new AppModule(6, "exam",
                "考试助手", "查询个人考试安排，提供考试倒计时提醒服务",
                "MODULE_QUERY_EXAM", R.mipmap.ic_exam, true);

        // 无卡片的模块
        public static AppModule seunet = new AppModule(7, "seunet",
                "校园网络", "显示校园网使用情况及校园网账户余额信息",
                "MODULE_QUERY_SEUNET", R.mipmap.ic_seunet, false);

        public static AppModule gymreserve = new AppModule(8, "gymreserve",
                "场馆预约", "提供体育场馆预约和查询服务",
                "MODULE_GYMRESERVE", R.mipmap.ic_gymreserve, false);

        public static AppModule library = new AppModule(9, "library",
                "图书馆", "查看图书馆实时借阅排行、已借书籍，并提供图书在线续借服务",
                "MODULE_QUERY_LIBRARY", R.mipmap.ic_library, false);

        public static AppModule grade = new AppModule(10, "grade",
                "成绩查询", "查询历史学期的科目成绩、学分以及绩点详情",
                "MODULE_QUERY_GRADE", R.mipmap.ic_grade, false);

        public static AppModule srtp = new AppModule(11, "srtp",
                "课外研学", "提供SRTP学分及得分详情查询服务",
                "MODULE_QUERY_SRTP", R.mipmap.ic_srtp, false);

        public static AppModule schoolbus = new AppModule(12, "schoolbus",
                "校车助手", "提供可实时更新的校车班车时间表",
                "MODULE_QUERY_SCHOOLBUS", R.mipmap.ic_bus, false);

        public static AppModule schedule = new AppModule(13, "schedule",
                "校历查询 Web", "显示当前年度各学期的学校校历安排",
                "http://heraldstudio.com/static/images/xiaoli.jpg", R.mipmap.ic_schedule, false);

        public static AppModule quanyi = new AppModule(14, "quanyi",
                "权益服务 Web", "向东大校会权益部反馈投诉信息",
                "https://jinshuju.net/f/By3aTK", R.mipmap.ic_quanyi, false);

        public static AppModule emptyroom = new AppModule(15, "emptyroom",
                "空教室 Web", "提供指定时间内的空教室信息查询服务",
                "http://115.28.27.150/queryEmptyClassrooms/m", R.mipmap.ic_emptyroom, false);

        //public static AppModule deskgame = new AppModule(16, "deskgame",
        //      "桌游助手", "方便大家娱乐的小猴桌游发牌器",
        //      "MODULE_DESKGAME", R.mipmap.ic_emptyroom, false);

        // 特殊的模块，模块管理
        public static AppModule moduleManager = new AppModule(-1, "",
                "模块管理", "管理各模块的显示/隐藏状态",
                "MODULE_MANAGER", R.mipmap.ic_add, true);

        public static AppModule[] array = new AppModule[]{
                card, pedetail, curriculum, experiment, lecture, jwc, exam,
                seunet, gymreserve, library, grade, srtp, schoolbus, schedule, quanyi, emptyroom//, deskgame
        };
    }

    /** 用 UserCache 代替 SharedPreferences; 此处不能直接初始化, 所以用一个 $get 函数代替 */
    private static $<UserCache> settingsCache = new $<>(() -> new UserCache("herald_settings"));

    public static String get(String key) {
        return settingsCache.$get().get(key);
    }

    public static void set(String key, String value) {
        settingsCache.$get().set(key, value);
    }

    public static $<Integer> launchTimes = new $<>(() -> {
        String times = SettingsHelper.get("herald_settings_launch_time");
        if (times.equals("")) {
            SettingsHelper.set("herald_settings_launch_time", "0");
            return 0;
        } else {
            return Integer.valueOf(times);
        }
    }, value -> {
        SettingsHelper.set("herald_settings_launch_time", String.valueOf(value));
    });

    public static $<Boolean> wifiAutoLogin =
            settingsCache.$get().booleanForKey("herald_settings_wifi_autologin", false);

    public static $<Boolean> bottomTabEnabled =
            settingsCache.$get().booleanForKey("herald_settings_bottomtabvisible", true);

    /** 模块设置变化的监听器 */
    private static Vector<Runnable> moduleSettingsChangeListeners = new Vector<>();

    public static void addModuleSettingsChangeListener (Runnable listener) {
        moduleSettingsChangeListeners.add(listener);
    }

    public static void notifyModuleSettingsChanged () {
        for (Runnable listener : moduleSettingsChangeListeners) {
            listener.run();
        }
    }
}
