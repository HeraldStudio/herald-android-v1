package cn.seu.herald_android.mod_query.curriculum;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;

public class CurriculumActivity extends BaseAppCompatActivity {

    // 水平分页控件
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorCurriculumPrimary));
        enableSwipeBack();

        pager = (ViewPager) findViewById(R.id.pager);

        //异步加载背景图
        final ImageView iv = (ImageView) findViewById(R.id.curriculum_bg);
        runMeasurementDependentTask(() -> {
            Picasso.with(this)
                    .load(R.drawable.curriculum_bg)
                    .resize(iv.getWidth(), iv.getHeight())
                    .centerCrop().into(iv);
        });

        readLocal();
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest(this).api(ApiHelper.API_SIDEBAR).addUUID()
                .toCache("herald_sidebar", o -> o.getJSONArray("content"))
                .onFinish((success, code, response) -> {
                    if (success) {
                        refreshCacheStep2();
                    } else {
                        hideProgressDialog();
                    }
                }).run();
    }

    private void refreshCacheStep2() {
        new ApiRequest(this).api(ApiHelper.API_CURRICULUM).addUUID()
                .toCache("herald_curriculum", o -> o.getJSONObject("content"))
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) readLocal();
                }).run();
    }

    public static ApiRequest[] remoteRefreshCache(Context context) {
        return new ApiRequest[]{
                new ApiRequest(context).api(ApiHelper.API_SIDEBAR).addUUID()
                        .toCache("herald_sidebar", o -> o.getJSONArray("content")),

                new ApiRequest(context).api(ApiHelper.API_CURRICULUM).addUUID()
                        .toCache("herald_curriculum", o -> o.getJSONObject("content"))
        };
    }

    private void readLocal() {
        String data = getCacheHelper().getCache("herald_curriculum");
        String sidebar = getCacheHelper().getCache("herald_sidebar");
        if (data.equals("")) {
            refreshCache();
            return;
        }

        PagesAdapter adapter = new PagesAdapter(this, data, sidebar);
        pager.setAdapter(adapter);
        pager.setCurrentItem(adapter.getCurrentPage());
        setTitle("第" + (adapter.getCurrentPage() + 1) + "周");

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int p1, float p2, int p3) {
            }

            @Override
            public void onPageSelected(int position) {
                setTitle("第" + (position + 1) + "周");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 读取课表缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getCurriculumItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_curriculum");
        final long now = Calendar.getInstance().getTimeInMillis();
        if (!cache.equals("")) try {
            JSONObject jsonObject = new JSONObject(cache);
            // 读取侧栏信息
            String sidebar = new CacheHelper(host.getContext()).getCache("herald_sidebar");
            Map<String, Pair<String, String>> sidebarInfo = new HashMap<>();

            // 将课程的授课教师和学分信息放入键值对
            JSONArray sidebarArray = new JSONArray(sidebar);
            for (int i = 0; i < sidebarArray.length(); i++) {
                JSONObject obj = sidebarArray.getJSONObject(i);
                sidebarInfo.put(obj.getString("course"),
                        new Pair<>(obj.getString("lecturer"), obj.getString("credit")));
            }

            // 读取开学日期
            int startMonth = jsonObject.getJSONObject("startdate").getInt("month");
            int startDate = jsonObject.getJSONObject("startdate").getInt("day");
            Calendar termStart = Calendar.getInstance();
            termStart.set(termStart.get(Calendar.YEAR), startMonth, startDate);

            // 如果开学日期比今天还晚，则是去年开学的。这里用while保证了thisWeek永远大于零
            while (termStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                termStart.set(Calendar.YEAR, termStart.get(Calendar.YEAR) - 1);
            }
            termStart = CalendarUtils.toSharpDay(termStart);

            // 计算当前周
            Calendar today = Calendar.getInstance();
            today = CalendarUtils.toSharpDay(today);

            int dayDelta = (int) ((today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24);
            int week = dayDelta / 7 + 1;
            int dayOfWeek = dayDelta % 7; // 0代表周一，以此类推

            // 枚举今天的课程
            JSONArray array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            int classCount = 0;
            boolean classAlmostEnd = false;

            ArrayList<View> remainingClasses = new ArrayList<>();

            for (int j = 0; j < array.length(); j++) {
                ClassInfo info = new ClassInfo(array.getJSONArray(j));
                // 如果该课程本周上课
                if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                    classCount++;
                    // 上课时间
                    long startTime = today.getTimeInMillis();
                    startTime += CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getStartTime() - 1] * 60 * 1000;

                    // 下课时间
                    long endTime = today.getTimeInMillis();
                    endTime += (CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getEndTime() - 1] + 45) * 60 * 1000;

                    // 快要下课的时间
                    long almostEndTime = today.getTimeInMillis();
                    almostEndTime += (CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getEndTime() - 1] + 35) * 60 * 1000;

                    // 如果是还没到时间的课，放在“你今天(还)有x节课”的列表里备用
                    // 只要没有快上课或正在上课的提醒导致中途退出循环的话，这个列表就会显示
                    if (now < startTime) {
                        if (now >= almostEndTime && now < endTime) {
                            classAlmostEnd = true;
                        }
                        info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                        Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                                info, pair == null ? "获取失败" : pair.first);
                        remainingClasses.add(block);
                    }

                    // 快要上课的紧急提醒
                    if (now >= startTime - 15 * 60 * 1000 && now < startTime) {
                        TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                                startTime, TimelineItem.CONTENT_NOTIFY, "即将开始上课，请注意时间，准时上课"
                        );
                        info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                        Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                                info, pair == null ? "获取失败" : pair.first);

                        item.attachedView.add(block);
                        return item;
                    } else if (now >= startTime && now < almostEndTime) {
                        // 正在上课的提醒
                        TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                                now, TimelineItem.CONTENT_NOTIFY, "正在上课中"
                        );
                        info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                        Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                                info, pair == null ? "获取失败" : pair.first);

                        item.attachedView.add(block);
                        return item;
                    }
                }
            }
            // 此处退出循环有三种可能：可能是今天没课，可能是课与课之间或早上的没上课状态，也可能是课上完了的状态

            // 如果不是课上完了的状态
            if (remainingClasses.size() > 0) {
                boolean firstClass = remainingClasses.size() == classCount;
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                        now, TimelineItem.CONTENT_NO_NOTIFY,
                        (classAlmostEnd ? "快要下课了，" : "") +
                                (firstClass ? "你今天有" : "你今天还有") + remainingClasses.size() + "节课，点我查看详情"
                );
                item.attachedView = remainingClasses;
                return item;
            }

            // 课上完了的状态

            // 若今天没课，或者课上完了，显示明天课程
            // 枚举明天的课程
            dayDelta = (int) ((today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24) + 1;
            week = dayDelta / 7 + 1;
            dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
            array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            boolean todayHasClasses = classCount != 0;

            classCount = 0;
            ArrayList<View> viewList = new ArrayList<>();
            for (int j = 0; j < array.length(); j++) {
                ClassInfo info = new ClassInfo(array.getJSONArray(j));
                // 如果该课程本周上课
                if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                    classCount++;
                    info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];

                    Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                    CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                            info, pair == null ? "获取失败" : pair.first);
                    viewList.add(block);
                }
            }
            TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                    // 如果两天都没课，显示时间为今天，否则显示为明天
                    today.getTimeInMillis() + (classCount == 0 && !todayHasClasses ? 0 : 1000 * 60 * 60 * 24),
                    // 若明天有课，则属于有内容不提醒状态；否则属于无内容状态
                    classCount == 0 ? TimelineItem.NO_CONTENT : TimelineItem.CONTENT_NO_NOTIFY,
                    // 如果明天没课
                    classCount == 0 ? (todayHasClasses ? "明天" : "今明两天都") + "没有课程，娱乐之余请注意作息安排哦"
                            // 如果明天有课
                            : (todayHasClasses ? "今天的课程已经结束，" : "今天没有课程，") + "明天有" + classCount + "节课"
            );

            item.attachedView = viewList;

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            // 清除出错的数据，使下次懒惰刷新时刷新课表
            new CacheHelper(host.getContext()).setCache("herald_curriculum", "");
        }
        return new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                now, TimelineItem.NO_CONTENT, "课表数据加载失败，请手动刷新"
        );
    }
}
