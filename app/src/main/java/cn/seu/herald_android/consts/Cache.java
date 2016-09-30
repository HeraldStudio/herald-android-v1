package cn.seu.herald_android.consts;

import java.util.Calendar;

import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.AppCache;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;

/**
 * 各模块缓存
 */
public class Cache {

    /**
     * 活动缓存
     */

    // 活动第一页
    public static AppCache activities = new AppCache("herald_activities", () ->
            new ApiSimpleRequest(Method.GET)
                    .url("http://www.heraldstudio.com/herald/api/v1/huodong/get")
                    .toCache("herald_activities")
    );

    // 热门活动
    public static AppCache activitiesHot = new AppCache("herald_activities_hot", () ->
            new ApiSimpleRequest(Method.GET)
                    .url("http://www.heraldstudio.com/herald/api/v1/huodong/get?type=hot")
                    .toCache("herald_activities_hot")
    );

    /**
     * 一卡通模块缓存
     */

    // 一卡通完整数据
    public static AppCache card = new AppCache("herald_card", () ->
            new ApiSimpleRequest(Method.POST).api("card").addUuid().post("timedelta", "31")
                    .toCache("herald_card")
    );

    // 一卡通刷新日期
    public static AppCache cardDate = new AppCache("herald_card_date");

    // 一卡通当日消费
    public static AppCache cardToday = new AppCache("herald_card_today", () ->
            new ApiSimpleRequest(Method.POST).api("card").addUuid().post("timedelta", "1")
                    .toCache("herald_card_today")
    );

    /**
     * 跑操模块缓存
     */

    // 跑操预报
    public static AppCache pcForecast = new AppCache("herald_pc_forecast", () ->
            new ApiSimpleRequest(Method.POST).api("pc").addUuid()
                    .toCache("herald_pc_forecast", o -> o.$s("content"))
                    .onResponse((success, code, response) -> {
                        long today = CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis();
                        if (success) {
                            CacheHelper.set("herald_pc_date", String.valueOf(today));
                        } else if (code == 201) { // 今天还没有预告
                            CacheHelper.set("herald_pc_date", String.valueOf(today));
                            // 覆盖旧的预告信息
                            CacheHelper.set("herald_pc_forecast", "refreshing");
                        }
                    })
    );

    // 跑操次数
    public static AppCache peCount = new AppCache("herald_pe_count", () ->
            new ApiSimpleRequest(Method.POST).api("pe").addUuid()
                    .toCache("herald_pe_count", o -> o.$s("content"))
                    .toCache("herald_pe_remain", o -> o.$s("remain"))
    );

    // 跑操剩余天数（刷新请用 peCount）
    public static AppCache peRemain = new AppCache("herald_pe_remain");

    // 跑操详细记录
    public static AppCache peDetail = new AppCache("herald_pedetail", () ->
            new ApiSimpleRequest(Method.POST).api("pedetail").addUuid()
                    .toCache("herald_pedetail", o -> o.$a("content"))
    );

    /**
     * 课表模块缓存
     */

    // 固定课程数据
    public static AppCache curriculum = new AppCache("herald_curriculum", () ->
            new ApiSimpleRequest(Method.POST).api("curriculum").addUuid()
                    .toCache("herald_curriculum", o -> o.$o("content"))
    );

    // 课表侧栏数据
    public static AppCache curriculumSidebar = new AppCache("herald_sidebar", () ->
            new ApiSimpleRequest(Method.POST).api("sidebar").addUuid()
                    .toCache("herald_sidebar", o -> o.$a("content"))
    );

    /**
     * 实验模块缓存
     */
    public static AppCache experiment = new AppCache("herald_experiment", () ->
            new ApiSimpleRequest(Method.POST).api("phylab").addUuid()
                    .toCache("herald_experiment")
    );

    /**
     * 考试模块缓存
     */

    // 考试数据
    public static AppCache exam = new AppCache("herald_exam", () ->
            new ApiSimpleRequest(Method.POST).api("exam").addUuid()
                    .toCache("herald_exam")
    );

    // 自定义考试数据
    public static AppCache examCustom = new AppCache("herald_exam_custom");

    /**
     * 人文讲座缓存
     */

    // 人文讲座预告
    public static AppCache lectureNotices = new AppCache("herald_lecture_notices", () ->
            new ApiSimpleRequest(Method.POST).url(ApiHelper.wechat_lecture_notice_url).addUuid()
                    .toCache("herald_lecture_notices")
    );

    // 人文讲座记录
    public static AppCache lectureRecords = new AppCache("herald_lecture_records", () ->
            new ApiSimpleRequest(Method.POST).api("lecture").addUuid()
                    .toCache("herald_lecture_records")
    );

    /**
     * 教务通知缓存
     */
    public static AppCache jwc = new AppCache("herald_jwc", () ->
            new ApiSimpleRequest(Method.POST).api("jwc").addUuid()
                    .toCache("herald_jwc")
    );

    /**
     * 校园网络缓存
     */
    public static AppCache seunet = new AppCache("herald_nic", () ->
            new ApiSimpleRequest(Method.POST).api("nic").addUuid()
                    .toCache("herald_nic")
    );

    /**
     * 场馆预约缓存
     */

    // 场馆预约项目列表
    public static AppCache gymReserveGetDate = new AppCache("herald_gymreserve_timelist_and_itemlist", () ->
            new ApiSimpleRequest(Method.POST).api("yuyue").addUuid().post("method", "getDate")
                    .toCache("herald_gymreserve_timelist_and_itemlist")
    );

    // 场馆预约记录
    public static AppCache gymReserveMyOrder = new AppCache("herald_gymreserve_myorder", () ->
            new ApiSimpleRequest(Method.POST).api("yuyue").addUuid().post("method", "myOrder")
                    .toCache("herald_gymreserve_myorder")
    );

    // 场馆预约个人电话
    public static AppCache gymReserveGetPhone = new AppCache("herald_gymreserve_phone", () ->
            new ApiSimpleRequest(Method.POST).api("yuyue").addUuid().post("method", "getPhone")
                    .toCache("herald_gymreserve_phone", o -> o.$o("content").$s("phone"))
    );

    // 场馆预约个人用户ID
    public static AppCache gymReserveUserId = new AppCache("herald_gymreserve_userid", () ->
            new ApiSimpleRequest(Method.POST).api("yuyue").addUuid()
                    .post("method", "getFriendList").post("cardNo", ApiHelper.getCurrentUser().userName)
                    .toCache("herald_gymreserve_userid", o -> o.$a("content").$o(0).$s("userId"))
    );

    // 场馆预约好友列表
    public static AppCache gymReserveFriend = new AppCache("herald_gymreserve_friend_list");

    /**
     * 图书馆模块缓存
     */

    // 图书馆借书记录
    public static AppCache libraryBorrowBook = new AppCache("herald_library_borrowbook", () ->
            new ApiSimpleRequest(Method.POST).api("library").addUuid()
                    .toCache("herald_library_borrowbook")
    );

    // 图书馆热门书目
    public static AppCache libraryHotBook = new AppCache("herald_library_hotbook", () ->
            new ApiSimpleRequest(Method.POST).api("library_hot").addUuid()
                    .toCache("herald_library_hotbook")
    );

    /**
     * 成绩模块缓存
     */
    public static AppCache grade = new AppCache("herald_grade_gpa", () ->
            new ApiSimpleRequest(Method.POST).api("gpa").addUuid()
                    .toCache("herald_grade_gpa", Module.grade)
    );

    /**
     * 课外研学模块缓存
     */
    public static AppCache srtp = new AppCache("herald_srtp", () ->
            new ApiSimpleRequest(Method.POST).api("srtp").addUuid().post("schoolnum", ApiHelper.getCurrentUser().schoolNum)
                    .toCache("herald_srtp")
    );

    /**
     * 校车助手缓存
     */
    public static AppCache schoolbus = new AppCache("herald_schoolbus", () ->
            new ApiSimpleRequest(Method.POST).api("schoolbus").addUuid()
                    .toCache("herald_schoolbus")
    );
}
