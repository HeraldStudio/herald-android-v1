package cn.seu.herald_android.mod_query.experiment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_achievement.Achievement;
import cn.seu.herald_android.mod_achievement.AchievementFactory;
import cn.seu.herald_android.mod_achievement.AchievementViewPagerAdapter;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;

public class ExperimentActivity extends BaseAppCompatActivity {

    // 每节实验开始的时间，以(Hour * 60 + Minute)形式表示
    // 本程序假定每节实验都是3小时
    public static final int[] EXPERIMENT_BEGIN_TIME = {
            9 * 60 + 45, 13 * 60 + 45, 18 * 60 + 15
    };
    //实验类型列表
    private ExpandableListView expandableListView;
    //成就墙展示View
    private ViewPager viewPager;
    //成就墙数目
    private TextView tv_numofAchievement;
    //成就列表
    private ArrayList<Achievement> achievementArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        init();
        loadCache();
    }

    private void init() {
        //toolbar初始化
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
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorExperimentprimary));
        enableSwipeBack();

        //实验类型列表加载
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

        //成就墙viewPager
        viewPager = (ViewPager) findViewById(R.id.chengjiu_viewpager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache() {
        //如果缓存不为空则加载缓存，反之刷新缓存
        String cache = getCacheHelper().getCache("herald_experiment");
        if (!cache.equals("")) {
            try {
                JSONObject json_content = new JSONObject(cache).getJSONObject("content");
                //父view和子view数据集合
                ArrayList<String> parentArray = new ArrayList<>();
                ArrayList<ArrayList<ExperimentItem>> childArray = new ArrayList<>();
                achievementArrayList = new ArrayList<>();
                //根据每种集合加载不同的子view
                for (int i = 0; i < json_content.length(); i++) {
                    String jsonArray_str = json_content.getString(json_content.names().getString(i));
                    if (!jsonArray_str.equals("")) {
                        //如果有实验则加载数据和子项布局
                        JSONArray jsonArray = new JSONArray(jsonArray_str);
                        //根据数组长度获得实验的Item集合
                        ArrayList<ExperimentItem> item_list = ExperimentItem.transformJSONArrayToArrayList(jsonArray);
                        //加入到list中
                        parentArray.add(json_content.names().getString(i));
                        childArray.add(item_list);
                        //加入到成就列表中
                        achievementArrayList.addAll(AchievementFactory.getExperimentAchievement(item_list));
                    }
                }
                //设置成就列表
                setupAchievementWall();
                //设置伸缩列表
                ExperimentExpandAdapter experimentExpandAdapter = new ExperimentExpandAdapter(getBaseContext(), parentArray, childArray);
                expandableListView.setAdapter(experimentExpandAdapter);

                if (experimentExpandAdapter.getGroupCount() > 0)
                    expandableListView.expandGroup(0);

            } catch (JSONException e) {
                showSnackBar("缓存解析失败，请刷新后再试");
                e.printStackTrace();
            }
        } else {
            refreshCache();
        }
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest(this).api(ApiHelper.API_PHYLAB).uuid()
                .toCache("herald_experiment", o -> o)
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                        showSnackBar("刷新成功");
                    }
                }).run();
    }

    public static ApiRequest remoteRefreshCache(Context context) {
        return new ApiRequest(context).api(ApiHelper.API_PHYLAB).uuid()
                .toCache("herald_experiment", o -> o);
    }

    private void setupAchievementWall() {
        //设置成就数目
        tv_numofAchievement = (TextView) findViewById(R.id.tv_num);
        tv_numofAchievement.setText(String.format("成就墙(%d/%d)", achievementArrayList.size(), 20));
        if(0 == achievementArrayList.size()){
            //无成就时添加提示
            Achievement achievementTip = new Achievement(Achievement.EXPERIMENT,"暂无成就","你还没有获得任何实验成就，赶紧参加实验，寻找神秘的成就碎片吧！","");
            achievementArrayList.add(achievementTip);
        }
        //设置适配器
        viewPager.setAdapter(new AchievementViewPagerAdapter(getBaseContext(), achievementArrayList));
    }

    /**
     * 读取实验缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getExperimentItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_experiment");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            JSONObject json_content = new JSONObject(cache).getJSONObject("content");
            boolean todayHasExperiments = false;
            // 时间未到的所有实验
            ArrayList<ExperimentBlockLayout> allExperiments = new ArrayList<>();
            // 今天的实验或当前周的实验。若今天无实验，则为当前周的实验
            ArrayList<ExperimentBlockLayout> currExperiments = new ArrayList<>();

            for (int i = 0; i < json_content.length(); i++) {
                String jsonArray_str = json_content.getString(json_content.names().getString(i));
                if (!jsonArray_str.equals("")) {
                    //如果有实验则加载数据和子项布局
                    JSONArray jsonArray = new JSONArray(jsonArray_str);
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        ExperimentItem item = new ExperimentItem(
                                jsonObject.getString("name"),
                                jsonObject.getString("Date"),
                                jsonObject.getString("Day"),
                                jsonObject.getString("Teacher"),
                                jsonObject.getString("Address"),
                                jsonObject.getString("Grade")
                        );
                        String[] ymdStr = item.getDate()
                                .split("日")[0].replace("年", "-").replace("月", "-").split("-");
                        int[] ymd = {
                                Integer.valueOf(ymdStr[0]),
                                Integer.valueOf(ymdStr[1]),
                                Integer.valueOf(ymdStr[2])
                        };
                        Calendar time = Calendar.getInstance();
                        time.set(ymd[0], ymd[1] - 1, ymd[2]);
                        time = CalendarUtils.toSharpDay(time);

                        // 没开始的实验全部单独记录下来
                        if (time.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                            ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                            block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                    SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                            allExperiments.add(block);
                        }

                        // 属于同一周
                        if (CalendarUtils.toSharpWeek(time).getTimeInMillis()
                                == CalendarUtils.toSharpWeek(Calendar.getInstance()).getTimeInMillis()) {
                            // 如果发现今天有实验
                            Calendar nowCal = Calendar.getInstance();
                            if (CalendarUtils.toSharpDay(time).getTimeInMillis()
                                    == CalendarUtils.toSharpDay(nowCal).getTimeInMillis()) {
                                // 如果是半小时之内快要开始的实验，放弃之前所有操作，直接返回这个实验的提醒
                                int nowStamp = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE);
                                int startStamp = item.getBeginStamp();
                                if (nowStamp < startStamp && nowStamp >= startStamp - 30) {
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                    block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                            SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                                    TimelineItem item1 = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                                            now, TimelineItem.CONTENT_NOTIFY, "你有1个实验即将开始，请注意时间准时参加"
                                    );
                                    item1.attachedView.add(block);
                                    return item1;
                                }

                                // 如果是已经开始还未结束的实验，放弃之前所有操作，直接返回这个实验的提醒
                                int endStamp = startStamp + 3 * 60;
                                if (nowStamp >= startStamp && nowStamp < endStamp) {
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                    block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                            SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                                    TimelineItem item1 = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                                            now, TimelineItem.CONTENT_NOTIFY, "1个实验正在进行"
                                    );
                                    item1.attachedView.add(block);
                                    return item1;
                                }

                                // 如果这个实验已经结束，跳过它
                                if (nowStamp >= endStamp) {
                                    continue;
                                }

                                // 如果是第一次发现今天有实验，则清空列表（之前放在列表里的都不是今天的）
                                // 然后做标记，以后不再记录不是今天的实验
                                if (!todayHasExperiments) {
                                    currExperiments.clear();
                                    todayHasExperiments = true;
                                }

                                // 记录今天的实验
                                ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                        SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                                currExperiments.add(block);
                            }

                            // 如果不是今天的实验但已经结束，跳过它
                            if (CalendarUtils.toSharpDay(time).getTimeInMillis()
                                    <= CalendarUtils.toSharpDay(nowCal).getTimeInMillis()) {
                                continue;
                            }

                            // 如果至今还未发现今天有实验，则继续记录本周的实验
                            if (!todayHasExperiments) {
                                ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                        SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                                currExperiments.add(block);
                            }
                        }
                    }
                }
            }

            // 解析完毕，下面做统计
            int N = currExperiments.size();
            int M = allExperiments.size();

            Comparator<ExperimentBlockLayout> experimentsViewComparator =
                    (lhs, rhs) -> (int) ((lhs.getTime() - rhs.getTime()) / 1000 / 60 / 60);

            Collections.sort(currExperiments, experimentsViewComparator);
            Collections.sort(allExperiments, experimentsViewComparator);

            // 今天和本周均无实验
            if (N == 0) {
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                        now, M == 0 ? TimelineItem.NO_CONTENT : TimelineItem.CONTENT_NO_NOTIFY,
                        (M == 0 ? "你没有未完成的实验，" : ("本学期你还有" + M + "个实验，"))
                                + "实验助手可以智能提醒你参加即将开始的实验"
                );
                item.attachedView = new ArrayList<>();
                item.attachedView.addAll(allExperiments);
                return item;
            }

            // 今天或本周有实验
            TimelineItem item = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                    now, TimelineItem.CONTENT_NO_NOTIFY,
                    (todayHasExperiments ? "今天有" : "本周有") + N + "个实验，请注意准时参加"
            );
            item.attachedView = new ArrayList<>();
            item.attachedView.addAll(currExperiments);
            return item;

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新实验
            new CacheHelper(host.getContext()).setCache("herald_experiment", "");
            return new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                    now, TimelineItem.NO_CONTENT, "实验数据加载失败，请手动刷新"
            );
        }
    }
}
