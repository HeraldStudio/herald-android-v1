package cn.seu.herald_android.mod_query.pedetail;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class PedetailActivity extends BaseAppCompatActivity {

    public static final int[] FORECAST_TIME_PERIOD = {
            6 * 60 + 20, 7 * 60 + 20
    };
    // 左右滑动分页的日历容器
    private ViewPager pager;
    // 跑操次数数字
    private TextView count, monthCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedetail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //禁用collapsingToolbarLayout的伸缩标题
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPedetailprimary));
        enableSwipeBack();

        pager = (ViewPager) findViewById(R.id.calendarPager);

        // 设置下拉刷新控件的进度条颜色
        count = (TextView) findViewById(R.id.tv_fullcount);
        monthCount = (TextView) findViewById(R.id.tv_monthcount);

        // 首先加载一次缓存数据（如未登录则弹出登陆窗口）
        readLocal();

        // 检查是否需要联网刷新，如果需要则刷新，不需要则取消
        if (isRefreshNeeded()) refreshCache();
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest(this).api(ApiHelper.API_PEDETAIL).uuid()
                .toCache("herald_pedetail", o -> o.getJSONArray("content"))
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        readLocal();
                    }
                }).run();
    }

    public static ApiRequest[] remoteRefreshCache(Context context) {
        return new ApiRequest[]{
                new ApiRequest(context).api(ApiHelper.API_PC).uuid()
                        .toCache("herald_pc_forecast", o -> {
                            long today = CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis();
                            new CacheHelper(context).setCache("herald_pc_date", String.valueOf(today));
                            return o.getString("content");
                        }),
                new ApiRequest(context).api(ApiHelper.API_PEDETAIL).uuid()
                        .toCache("herald_pedetail", o -> o.getJSONArray("content")),
                new ApiRequest(context).api(ApiHelper.API_PE).uuid()
                        .toCache("herald_pe_count", o -> o.getString("content"))
                        .toCache("herald_pe_remain", o -> o.getString("remain"))
        };
    }

    private boolean isRefreshNeeded() {
        return (pager.getAdapter() == null) || (pager.getAdapter().getCount() == 0);
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

    private void readLocal() {
        try {
            // 读取本地保存的跑操数据
            JSONArray array = new JSONArray(getCacheHelper().getCache("herald_pedetail"));

            // 用户有数据
            // 有效跑操计数器，用于显示每一个跑操是第几次
            int exerciseCount = 0;

            // 创建一个包含所有有效跑操记录的列表（单重列表结构）
            List<ExerciseInfo> infoList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                // 将跑操数据倾倒到列表
                ExerciseInfo info = new ExerciseInfo(obj, exerciseCount + 1);
                if (info.getValid()) {
                    infoList.add(info);
                    exerciseCount++;
                }
            }
            showCount(exerciseCount);

            // 用年月时间戳（年*12+自然月-1）比较器进行排序以防万一
            Collections.sort(infoList, ExerciseInfo.yearMonthComparator);

            // 当前所在月的年月戳
            Calendar cal = Calendar.getInstance();
            int curMonth = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH);

            // 起始月为最早记录所在月
            int startMonth = infoList.size() > 0 ? infoList.get(0).getYearMonth() : curMonth;

            // 终止月为最晚记录所在月
            int endMonth = infoList.size() > 0 ?
                    Math.max(infoList.get(infoList.size() - 1).getYearMonth(), curMonth) : curMonth;

            // 创建一个键值对结构，键为年月戳，值为该月的跑操记录列表
            Map<Integer, List<ExerciseInfo>> pages = new HashMap<>();
            for (int i = startMonth; i <= endMonth; i++) {
                pages.put(i, new ArrayList<>());
            }

            // 将单重列表的每个元素倾倒到双重列表中对应的位置
            for (ExerciseInfo info : infoList) {
                pages.get(info.getYearMonth()).add(info);
            }

            // 删除空白月（当前月除外）
            for (int i = startMonth; i <= endMonth; i++) {
                if (pages.get(i).size() == 0 && infoList.size() > 0)
                    pages.remove(i);
            }

            // 设置水平滚动分页的适配器，负责将双重列表中每一个子列表的数据转换为视图，供水平滚动分页控件调用
            final PagesAdapter adapter = new PagesAdapter(pages, this, this::refreshCache);
            pager.setAdapter(adapter);

            // 根据实际需要，显示时应首先滑动到末页
            pager.setCurrentItem(adapter.getCount() - 1);

            // 初始化当月跑操次数的值
            int monthlyCountNum = adapter.getSubCount(pager.getCurrentItem());
            monthCount.setText(String.valueOf(monthlyCountNum));

            // 水平滚动分页控件的事件监听器
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                // 用户滚动到某页时，更改标题和当月跑操次数的值
                public void onPageSelected(int position) {
                    // 动画切换当月跑操次数数字
                    AlphaAnimation aa1 = new AlphaAnimation(0, 1);
                    aa1.setDuration(250);
                    monthCount.startAnimation(aa1);

                    int monthlyCountNum = adapter.getSubCount(pager.getCurrentItem());
                    monthCount.setText(String.valueOf(monthlyCountNum));
                }

                // 在页面左右滑动过程中临时屏蔽下拉刷新控件
                public void onPageScrollStateChanged(int state) {
                    //srl.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
                }
            });

            if (infoList.size() == 0) {
                showMsg("本学期暂时没有跑操记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCount(int countNum) {
        count.setText(String.valueOf(countNum));
    }

    /**
     * 读取跑操预报缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getPeForecastItem(TimelineView host) {
        CacheHelper helper = new CacheHelper(host.getContext());
        String date = helper.getCache("herald_pc_date");
        String ignoredDate = helper.getCache("herald_pc_ignored_date");
        String forecast = helper.getCache("herald_pc_forecast");
        String record = helper.getCache("herald_pedetail");
        final long now = Calendar.getInstance().getTimeInMillis();

        try {
            int count = Integer.valueOf(helper.getCache("herald_pe_count"));
            int remain = Integer.valueOf(helper.getCache("herald_pe_remain"));

            Calendar nowCal = Calendar.getInstance();
            long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
            long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;
            long endTime = today + PedetailActivity.FORECAST_TIME_PERIOD[1] * 60 * 1000;

            String todayStamp = new SimpleDateFormat("yyyy-MM-dd").format(nowCal.getTime());

            if (record.contains(todayStamp)) {
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                        now, ignoredDate.equals(todayStamp) ?
                        TimelineItem.CONTENT_NO_NOTIFY : TimelineItem.CONTENT_NOTIFY,
                        "你今天的跑操已经到账。" + getRemainNotice(count, remain, false)
                );

                item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "已跑次数", count));
                if (count < 45) {
                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "还需次数", 45 - count));
                }
                item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "剩余天数", remain));

                return item;
            }

            if (now >= startTime && !date.equals(String.valueOf(CalendarUtils.toSharpDay(nowCal).getTimeInMillis()))) {
                return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                        now, TimelineItem.NO_CONTENT, "跑操预告加载失败，请手动刷新"
                );
            }
            if (now < startTime) {
                // 跑操时间没到
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                        now, TimelineItem.CONTENT_NO_NOTIFY, "小猴会在早上跑操时间实时显示跑操预告\n"
                        + getRemainNotice(count, remain, false)
                );

                item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "已跑次数", count));
                if (count < 45) {
                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "还需次数", 45 - count));
                }
                item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "剩余天数", remain));

                return item;
            } else if (now >= endTime) {
                // 跑操时间已过

                if (!forecast.contains("跑操")) {
                    // 没有跑操预告信息
                    TimelineItem item = new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                            now, TimelineItem.CONTENT_NO_NOTIFY, "今天没有跑操预告信息\n"
                            + getRemainNotice(count, remain, false)
                    );

                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "已跑次数", count));
                    if (count < 45) {
                        item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "还需次数", 45 - count));
                    }
                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "剩余天数", remain));

                    return item;
                } else {
                    // 有跑操预告信息但时间已过
                    TimelineItem item = new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                            startTime, TimelineItem.CONTENT_NO_NOTIFY, forecast + "(已结束)\n"
                            + getRemainNotice(count, remain, forecast.contains("今天正常跑操"))
                    );

                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "已跑次数", count));
                    if (count < 45) {
                        item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "还需次数", 45 - count));
                    }
                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "剩余天数", remain));

                    return item;
                }
            } else {
                // 还没有跑操预告信息
                if (!forecast.contains("跑操")) {
                    TimelineItem item = new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                            now, TimelineItem.CONTENT_NO_NOTIFY, "目前暂无跑操预报信息，过一会再来看吧~\n"
                            + getRemainNotice(count, remain, false)
                    );

                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "已跑次数", count));
                    if (count < 45) {
                        item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "还需次数", 45 - count));
                    }
                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "剩余天数", remain));

                    return item;
                }

                // 有跑操预告信息
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                        now, TimelineItem.CONTENT_NOTIFY, "小猴预测" + forecast + "\n"
                        + getRemainNotice(count, remain, forecast.contains("今天正常跑操"))
                );

                item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "已跑次数", count));
                if (count < 45) {
                    item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "还需次数", 45 - count));
                }
                item.attachedView.add(new PedetailTimelineBlockLayout(host.getContext(), "剩余天数", remain));

                return item;
            }
        } catch (Exception e) {
            return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineItem.NO_CONTENT, "跑操预告加载失败，请手动刷新"
            );
        }
    }

    private static String getRemainNotice(int count, int remain, boolean todayAvailable) {

        if (count == 0) return "你这学期还没有跑操，如果是需要跑操的同学要加油咯~";
        if (count >= 45) {
            return "已经跑够次数啦，" + (remain > 0 && remain >= 50 - count ?
                    "你还可以再继续加餐，多多益善哟~" : "小猴给你个满分~");
        }
        float ratio = ((float) remain) / (45 - count);
        if (ratio >= 1.5f) {
            return "时间似乎比较充裕，但还是要加油哟~";
        } else if (ratio >= 1.2f) {
            return "时间比较紧迫了，" + (todayAvailable ? "赶紧加油出门跑操吧~" : "还需要继续锻炼哟~");
        } else if (ratio >= 1f) {
            return "时间紧任务重，" + (todayAvailable ? "没时间解释了，赶紧出门补齐跑操吧~" : "赶紧找机会补齐跑操吧~");
        } else {
            return "似乎没什么希望了，小猴为你感到难过，不如参加一些加跑操的活动试试？";
        }
    }
}
