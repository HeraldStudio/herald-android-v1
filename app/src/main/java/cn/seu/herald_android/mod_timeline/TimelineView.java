package cn.seu.herald_android.mod_timeline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.ShortcutBoxView;
import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.curriculum.CurriculumActivity;
import cn.seu.herald_android.mod_query.experiment.ExperimentActivity;
import cn.seu.herald_android.mod_query.lecture.LectureActivity;
import cn.seu.herald_android.mod_query.pedetail.PedetailActivity;

public class TimelineView extends ListView {

    public static class Item {
        private int module;
        private long time;
        private String info;
        // 消息是否重要，不重要的消息总在后面
        private boolean important;
        public ArrayList<View> attachedView = new ArrayList<>();

        public Item(int module, long time, boolean important, String info) {
            this.module = module;
            this.time = time;
            this.important = important;
            this.info = info;
        }

        // 按时间先后顺序排列
        public static Comparator<Item> comparator =
                (item1, item2) -> {
                    // 不重要的消息总在后面
                    if (item1.important != item2.important) {
                        return item1.important ? -1 : 1;
                    }
                    return 0;
                };
    }

    private ArrayList<Item> itemList;

    private BaseAppCompatActivity activity;

    public void setActivity(BaseAppCompatActivity activity) {
        this.activity = activity;
    }

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(false);
    }

    private Runnable hideRefresh = null;

    public void setHideRefresh(Runnable hideRefresh) {
        this.hideRefresh = hideRefresh;
    }

    private ShortcutBoxView shortcutBox;

    private SliderView slider;

    private TimelineAdapter adapter;

    private View topPadding;

    private final Vector<Object> threads = new Vector<>();

    public void loadContent(boolean refresh) {

        // 刷新快捷方式和轮播图
        refreshHeaders();

        itemList = new ArrayList<>();
        SettingsHelper settingsHelper = new SettingsHelper(getContext());

        if (refresh) {
            // 懒惰刷新

            // 当跑操模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_PEDETAIL)) {
                // 仅当工作日且处于跑操时间时刷新跑操预报
                Calendar nowCal = Calendar.getInstance();
                int week = nowCal.get(Calendar.DAY_OF_WEEK);
                long now = nowCal.getTimeInMillis();
                long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
                long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;
                long endTime = today + PedetailActivity.FORECAST_TIME_PERIOD[1] * 60 * 1000;
                if (week != Calendar.SATURDAY && week != Calendar.SUNDAY && startTime <= now && now < endTime) {
                    threads.add(new Object());
                    PedetailActivity.refreshForecast(getContext(), () -> {
                        if (threads.size() > 0) threads.remove(0);
                        loadContent(false);
                    });
                }
            }

            // 当课表模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_CURRICULUM)) {
                // 仅当课表数据不存在时刷新课表
                if (activity.getCacheHelper().getCache("herald_curriculum").equals("")) {
                    threads.add(new Object());
                    CurriculumActivity.remoteRefreshCache(getContext(), () -> {
                        if (threads.size() > 0) threads.remove(0);
                        loadContent(false);
                    });
                }
            }

            // 当实验模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_EXPERIMENT)) {
                // 仅当实验数据不存在时刷新实验
                if (activity.getCacheHelper().getCache("herald_experiment").equals("")) {
                    threads.add(new Object());
                    ExperimentActivity.remoteRefreshCache(getContext(), () -> {
                        if (threads.size() > 0) threads.remove(0);
                        loadContent(false);
                    });
                }
            }

            // 当人文讲座模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_LECTURE)) {
                // 直接刷新人文讲座预告
                threads.add(new Object());
                LectureActivity.remoteRefreshCache(getContext(), () -> {
                    if (threads.size() > 0) threads.remove(0);
                    loadContent(false);
                });
            }
        }

        // 判断各模块是否开启并加载对应数据
        if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_PEDETAIL)) {
            // 加载并解析跑操预报数据
            Item item = TimelineParser.getPeForecastItem(getContext());
            if (item != null) itemList.add(item);
        }

        if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_CURRICULUM)) {
            // 加载并解析课表数据
            itemList.add(TimelineParser.getCurriculumItem(getContext()));
        }

        if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_EXPERIMENT)) {
            // 加载并解析实验数据
            itemList.add(TimelineParser.getExperimentItem(getContext()));
        }

        if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_LECTURE)) {
            // 加载并解析人文讲座预告数据
            itemList.add(TimelineParser.getLectureItem(getContext()));
        }

        // 有消息的排在前面，没消息的排在后面
        Collections.sort(itemList, Item.comparator);

        // 更新适配器，结束刷新
        if (adapter == null) {
            setAdapter(adapter = new TimelineAdapter());
        } else {
            adapter.notifyDataSetChanged();
        }
        if (hideRefresh != null && threads.size() == 0)
            hideRefresh.run();
    }

    public void refreshHeaders() {
        // dp单位值
        float dp = getContext().getResources().getDisplayMetrics().density;

        if (topPadding == null) {
            // 顶部增加一个padding
            topPadding = new View(getContext());
            topPadding.setLayoutParams(new AbsListView.LayoutParams(-1, (int) (7 * dp)));
            addHeaderView(topPadding);
        }

        if (slider == null) {
            slider = (SliderView)
                    LayoutInflater.from(getContext()).inflate(R.layout.timeline_slider, null);

            // 设置高度。在其他地方设置没用。
            slider.setLayoutParams(new AbsListView.LayoutParams(-1, (int) (200 * dp)));
            addHeaderView(slider);
        }

        if (shortcutBox == null) {
            shortcutBox = (ShortcutBoxView)
                    LayoutInflater.from(getContext()).inflate(R.layout.timeline_shortcut_box, null);
            addHeaderView(shortcutBox);
        } else {
            shortcutBox.refresh();
        }
    }

    public class TimelineAdapter extends BaseAdapter {

        private long now;

        public TimelineAdapter() {
            now = Calendar.getInstance().getTimeInMillis();
        }

        @Override
        public void notifyDataSetChanged() {
            now = Calendar.getInstance().getTimeInMillis();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Item getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);

            View v = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, null);
            TextView name = (TextView) v.findViewById(R.id.name);
            TextView time = (TextView) v.findViewById(R.id.time);
            TextView content = (TextView) v.findViewById(R.id.content);
            ImageView avatar = (ImageView) v.findViewById(R.id.avatar);
            ViewGroup attachedContainer = (ViewGroup) v.findViewById(R.id.attachedContainer);
            ViewGroup hsv = (ViewGroup) v.findViewById(R.id.hsv);

            name.setText(activity.getSettingsHelper().moduleNamesTips[item.module]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.time);
            String dateTime = timeInNaturalLanguage(calendar, now);
            time.setText(dateTime);

            content.setText(item.info);
            avatar.setImageDrawable(getResources()
                    .getDrawable(activity.getSettingsHelper().moduleIconsId[item.module]));
            v.setOnClickListener((v1) -> {
                activity.startActivity(new Intent(activity.getSettingsHelper().moduleActions[item.module]));
            });
            v.setOnLongClickListener(v1 -> {
                SettingsHelper settingsHelper  = new SettingsHelper(getContext());
                new AlertDialog.Builder(getContext())
                        .setMessage("确定移除此模块的快捷方式和卡片吗？\n(可在侧边栏→查询助手中找回)")
                        .setPositiveButton("确定",(dialog, which) -> {
                            //设置为不可用
                            settingsHelper.setModuleShortCutEnabled(item.module,false);
                            loadContent(true);
                        })
                        .setNegativeButton("取消", (dialog, which) ->{

                        }).show();
                return true;
            });


            if (item.attachedView.size() != 0) {
                hsv.setVisibility(VISIBLE);
                boolean firstChild = true;
                float dp = getContext().getResources().getDisplayMetrics().density;
                for (View k : item.attachedView) {
                    if (!firstChild) {
                        View padding = new View(getContext());
                        padding.setLayoutParams(new LinearLayout.LayoutParams((int) (12 * dp), 1));
                        attachedContainer.addView(padding);
                    }

                    if (k.getParent() != null) {
                        ((ViewGroup) k.getParent()).removeView(k);
                    }
                    attachedContainer.addView(k);
                    firstChild = false;
                }
            }

            return v;
        }
    }

    // 因为此函数在getView()中被调用，视图回收再分配时，时间可能发生变化，所以此函数内不能调用Calendar.getInstance()
    // 而应该用固定的now参数代表刷新时的时间
    public static String timeInNaturalLanguage(Calendar dest, long now) {
        long time = dest.getTimeInMillis();
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTimeInMillis(now);
        todayCal = CalendarUtils.toSharpDay(todayCal);
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() + 1000 * 60 * 60 * 24);
        long tomorrow = todayCal.getTimeInMillis();
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() + 1000 * 60 * 60 * 24);
        long dayAfterTomorrow = todayCal.getTimeInMillis();
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() - 2 * 1000 * 60 * 60 * 24);
        todayCal.set(Calendar.YEAR, todayCal.get(Calendar.YEAR) + 1);
        todayCal.set(Calendar.MONTH, 0);
        todayCal.set(Calendar.DATE, 1);
        long nextYear = todayCal.getTimeInMillis();

        // 允许5秒的误差
        if (now <= time + 5 * 1000) {
            if (now > time) time = now;
            // 将明天零点视为明天全天事件
            if (time == tomorrow) {
                return "明天";
            }
            if (time < tomorrow) {
                int deltaMinute = (int) ((time - now) / 1000 / 60);
                int deltaHour = deltaMinute / 60;
                deltaMinute %= 60;
                if (deltaHour != 0) return deltaHour + "小时后";
                if (deltaMinute != 0) return deltaMinute + "分钟后";
                return "现在";
            }
            if (time <= dayAfterTomorrow) {
                return "明天 " + new SimpleDateFormat("H:mm").format(dest.getTime());
            }
            if (time <= nextYear) {
                return new SimpleDateFormat("M-d H:mm").format(dest.getTime());
            }
        }
        return new SimpleDateFormat("yyyy-M-d H:mm").format(dest.getTime());
    }
}
