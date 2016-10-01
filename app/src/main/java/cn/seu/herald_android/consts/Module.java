package cn.seu.herald_android.consts;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.AppModule;

/**
 * 此处对应 iOS 版 Modules 常量文件
 */
public class Module {

    // 有卡片的模块
    public static AppModule card = new AppModule("cardextra",
            "一卡通", "提供一卡通消费情况查询、一卡通在线充值以及余额提醒服务",
            "MODULE_QUERY_CARDEXTRA", R.mipmap.ic_card, R.mipmap.ic_card_invert, true, true);

    public static AppModule pedetail = new AppModule("pedetail",
            "跑操助手", "提供跑操次数及记录查询、早操预报以及跑操到账提醒服务",
            "MODULE_QUERY_PEDETAIL", R.mipmap.ic_pedetail, R.mipmap.ic_pedetail_invert, true, true);

    public static AppModule curriculum = new AppModule("curriculum",
            "课表助手", "浏览当前学期的课表信息，并提供上课提醒服务",
            "MODULE_QUERY_CURRICULUM", R.mipmap.ic_curriculum, R.mipmap.ic_curriculum_invert, true, true);

    public static AppModule experiment = new AppModule("experiment",
            "实验助手", "浏览当前学期的实验信息，并提供实验提醒服务",
            "MODULE_QUERY_EXPERIMENT", R.mipmap.ic_experiment, R.mipmap.ic_experiment_invert, true, true);

    public static AppModule exam = new AppModule("exam",
            "考试助手", "查询个人考试安排，提供考试倒计时提醒服务",
            "MODULE_QUERY_EXAM", R.mipmap.ic_exam, R.mipmap.ic_exam_invert, true, true);

    public static AppModule lecture = new AppModule("lecture",
            "人文讲座", "查看人文讲座听课记录，并提供人文讲座预告信息",
            "MODULE_QUERY_LECTURE", R.mipmap.ic_lecture, R.mipmap.ic_lecture_invert, true, true);

    public static AppModule jwc = new AppModule("jwc",
            "教务通知", "显示教务处最新通知，提供重要教务通知提醒服务",
            "MODULE_QUERY_JWC", R.mipmap.ic_jwc, R.mipmap.ic_jwc_invert, true, false);

    // 无卡片的模块
    public static AppModule seunet = new AppModule("seunet",
            "校园网络", "显示校园网使用情况及校园网账户余额信息",
            "MODULE_QUERY_SEUNET", R.mipmap.ic_seunet, R.mipmap.ic_seunet_invert, false, true);

    public static AppModule gymreserve = new AppModule("gymreserve",
            "场馆预约", "提供体育场馆预约和查询服务",
            "MODULE_GYMRESERVE", R.mipmap.ic_gymreserve, R.mipmap.ic_gymreserve_invert, false, true);

    public static AppModule library = new AppModule("library",
            "图书馆", "查看图书馆实时借阅排行、已借书籍，并提供图书在线续借服务",
            "MODULE_QUERY_LIBRARY", R.mipmap.ic_library, R.mipmap.ic_library_invert, false, true);

    public static AppModule grade = new AppModule("grade",
            "成绩查询", "查询历史学期的科目成绩、学分以及绩点详情",
            "MODULE_QUERY_GRADE", R.mipmap.ic_grade, R.mipmap.ic_grade_invert, false, true);

    public static AppModule srtp = new AppModule("srtp",
            "课外研学", "提供SRTP学分及得分详情查询服务",
            "MODULE_QUERY_SRTP", R.mipmap.ic_srtp, R.mipmap.ic_srtp_invert, false, true);

    public static AppModule schoolbus = new AppModule("schoolbus",
            "校车助手", "提供可实时更新的校车班车时间表",
            "MODULE_QUERY_SCHOOLBUS", R.mipmap.ic_bus, R.mipmap.ic_bus_invert, false, false);

    public static AppModule schedule = new AppModule("schedule",
            "校历查询 Web", "显示当前年度各学期的学校校历安排",
            "http://heraldstudio.com/static/images/xiaoli.jpg", R.mipmap.ic_schedule, R.mipmap.ic_schedule_invert, false, false);

    public static AppModule quanyi = new AppModule("quanyi",
            "权益服务 Web", "向东大校会权益部反馈投诉信息",
            "https://jinshuju.net/f/By3aTK", R.mipmap.ic_quanyi, R.mipmap.ic_quanyi_invert, false, false);

    public static AppModule emptyroom = new AppModule("emptyroom",
            "空教室 Web", "提供指定时间内的空教室信息查询服务",
            "http://www.heraldstudio.com/queryEmptyClassrooms/m", R.mipmap.ic_emptyroom, R.mipmap.ic_emptyroom_invert, false, false);

    // 特殊的模块，模块管理
    public static AppModule moduleManager = new AppModule("",
            "模块管理", "管理各模块的显示/隐藏状态",
            "MODULE_MANAGER", R.mipmap.ic_add, R.mipmap.ic_add, true, false);

    public static AppModule[] array = new AppModule[]{
            card, pedetail, curriculum, experiment, exam, lecture, jwc,
            seunet, gymreserve, library, grade, srtp, schoolbus, schedule, quanyi, emptyroom
    };
}
