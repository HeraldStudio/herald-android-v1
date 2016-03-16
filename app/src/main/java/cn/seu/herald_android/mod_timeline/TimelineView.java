package cn.seu.herald_android.mod_timeline;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.ShortcutBoxView;
import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.cardextra.CardActivity;
import cn.seu.herald_android.mod_query.curriculum.CurriculumActivity;
import cn.seu.herald_android.mod_query.experiment.ExperimentActivity;
import cn.seu.herald_android.mod_query.jwc.JwcActivity;
import cn.seu.herald_android.mod_query.lecture.LectureActivity;
import cn.seu.herald_android.mod_query.pedetail.PedetailActivity;

public class TimelineView extends ListView {

    private final Vector<Object> threads = new Vector<>();
    private ArrayList<Item> itemList;

    private BaseAppCompatActivity activity;
    private Runnable hideRefresh = null;
    private ShortcutBoxView shortcutBox;
    private SliderView slider;
    private TimelineAdapter adapter;
    private View topPadding;
    private BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadContent(false);
        }
    };

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(false);
    }

    // 因为此函数在getView()中被调用，视图回收再分配时，时间可能发生变化，所以此函数内不能调用Calendar.getInstance()
    // 而应该用固定的now参数代表刷新时的时间
    private static String timeInNaturalLanguage(Calendar dest, long now) {
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
                float deltaMinute = (time - now) / 1000 / 60f;
                float deltaHour = deltaMinute / 60;
                deltaMinute %= 60;
                if (deltaHour >= 1) return Math.round(deltaHour) + "小时后";
                if (deltaMinute >= 1) return (int) Math.ceil(deltaMinute) + "分钟后";
                return "现在";
            }
            if (time <= dayAfterTomorrow) {
                return "明天 " + new SimpleDateFormat("H:mm").format(dest.getTime());
            }
            if (time <= nextYear) {
                return new SimpleDateFormat("M-d H:mm").format(dest.getTime());
            }
        }
        return "已结束";
    }

    public void setActivity(BaseAppCompatActivity activity) {
        this.activity = activity;
    }

    public void setHideRefresh(Runnable hideRefresh) {
        this.hideRefresh = hideRefresh;
    }

    public void loadContent(boolean refresh) {

        // 刷新快捷方式和轮播图
        refreshHeaders();

        itemList = new ArrayList<>();
        SettingsHelper settingsHelper = new SettingsHelper(getContext());

        if (refresh) {
            // 懒惰刷新

            // 当课表模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_CURRICULUM)) {
                // 仅当课表数据不存在时刷新课表
                if (activity.getCacheHelper().getCache("herald_curriculum").equals("")
                        || activity.getCacheHelper().getCache("herald_sidebar").equals("")) {
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

            // 当跑操模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_PEDETAIL)) {
                CacheHelper helper = new CacheHelper(getContext());
                String date = helper.getCache("herald_pc_date");
                // 服务器端的跑操预告消息可能会出现中途更改的情况，因此只要没有得到跑操结束时的最后消息，就允许重复刷新
                // 这个缓存用来记录当天的最后消息是否已经到手
                boolean gotLastMessage = helper.getCache("herald_pc_last_message").equals("true");
                Calendar nowCal = Calendar.getInstance();
                long now = Calendar.getInstance().getTimeInMillis();
                long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
                long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;

                // 仅当今天缓存不存在或者最后消息还没到手，且已到开始时间时，允许刷新
                if ((!date.equals(String.valueOf(CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis()))
                        || !gotLastMessage) && now >= startTime) {
                    threads.add(new Object());
                    PedetailActivity.remoteRefreshCache(getContext(), () -> {
                        if (threads.size() > 0) threads.remove(0);
                        loadContent(false);
                    });
                }
            }

            // 当一卡通模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_CARDEXTRA)) {
                // 直接刷新一卡通数据
                threads.add(new Object());
                CardActivity.remoteRefreshCache(getContext(), () -> {
                    if (threads.size() > 0) threads.remove(0);
                    loadContent(false);
                });
            }

            // 当教务处模块开启时
            if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_JWC)) {
                // 直接刷新一卡通数据
                threads.add(new Object());
                JwcActivity.remoteRefreshCache(getContext(), () -> {
                    if (threads.size() > 0) threads.remove(0);
                    loadContent(false);
                });
            }
        }

        // 判断各模块是否开启并加载对应数据
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

        if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_PEDETAIL)) {
            // 加载并解析跑操预报数据
            itemList.add(TimelineParser.getPeForecastItem(getContext()));
        }

        if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_CARDEXTRA)) {
            // 加载并解析一卡通数据
            itemList.add(TimelineParser.getCardItem(getContext()));
        }

        if (settingsHelper.getModuleShortCutEnabled(SettingsHelper.MODULE_JWC)) {
            // 加载并解析教务处数据
            itemList.add(TimelineParser.getJwcItem(getContext()));
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

    private void refreshHeaders() {
        // dp单位值
        float dp = getContext().getResources().getDisplayMetrics().density;

        if (slider == null) {
            slider = (SliderView)
                    LayoutInflater.from(getContext()).inflate(R.layout.timeline_slider, null);

            // 设置高度。在其他地方设置没用。
            float resolution = 5 / 2f;
            int height = (int) (getContext().getResources().getDisplayMetrics().widthPixels / resolution);
            slider.setLayoutParams(new AbsListView.LayoutParams(-1, height));

            // 为轮播栏设置内容
            ServiceHelper serviceHelper = new ServiceHelper(getContext());
            ArrayList<SliderView.SliderViewItem> sliderViewItemArrayList = serviceHelper.getSliderViewItemArray();
            slider.setupWithArrayList(sliderViewItemArrayList);

            addHeaderView(slider);
        }

        if (topPadding == null) {
            // 顶部增加一个padding
            topPadding = new View(getContext());
            topPadding.setLayoutParams(new AbsListView.LayoutParams(-1, (int) (8 * dp)));
            addHeaderView(topPadding);
        }

        if (shortcutBox == null) {
            shortcutBox = (ShortcutBoxView)
                    LayoutInflater.from(getContext()).inflate(R.layout.timeline_shortcut_box, null);
            addHeaderView(shortcutBox);
        } else {
            shortcutBox.refresh();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(timeChangeReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(timeChangeReceiver);
        super.onDetachedFromWindow();
    }

    public static class Item {
        // 消息是否重要，不重要的消息总在后面
        public static final int CONTENT_NOTIFY = 0, CONTENT_NO_NOTIFY = 1, NO_CONTENT = 2;
        public ArrayList<View> attachedView = new ArrayList<>();
        private int module;
        private long time;
        private String info;
        private int importance;
        // 按时间先后顺序排列
        public static Comparator<Item> comparator =
                (item1, item2) -> {
                    // 不重要的消息总在后面
                    return item1.importance - item2.importance;
                };

        public Item(int module, long time, int importance, String info) {
            this.module = module;
            this.time = time;
            this.importance = importance;
            this.info = info;
        }

        public int getImportance() {
            return importance;
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

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, null);
            }
            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView time = (TextView) convertView.findViewById(R.id.time);
            TextView content = (TextView) convertView.findViewById(R.id.content);
            ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
            ViewGroup attachedContainer = (ViewGroup) convertView.findViewById(R.id.attachedContainer);
            ViewGroup hsv = (ViewGroup) convertView.findViewById(R.id.hsv);
            View notifyDot = convertView.findViewById(R.id.notify_dot);

            name.setText(SettingsHelper.moduleNamesTips[item.module]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.time);
            String dateTime = timeInNaturalLanguage(calendar, now);
            time.setText(dateTime);
            content.setText(item.info);

            notifyDot.setVisibility(item.getImportance() == Item.CONTENT_NOTIFY ? VISIBLE : GONE);

            avatar.setImageDrawable(ContextCompat.getDrawable(getContext(),
                    SettingsHelper.moduleIconsId[item.module]));
            convertView.setOnClickListener((v1) ->
                    activity.startActivity(new Intent(SettingsHelper.moduleActions[item.module])));

            convertView.setOnLongClickListener(v1 -> {
                SettingsHelper settingsHelper = new SettingsHelper(getContext());
                new AlertDialog.Builder(getContext())
                        .setMessage("确定移除此模块的快捷方式和卡片吗？\n(可在侧边栏→查询助手中找回)")
                        .setPositiveButton("确定", (dialog, which) -> {
                            //设置为不可用
                            settingsHelper.setModuleShortCutEnabled(item.module, false);
                            loadContent(true);
                        })
                        .setNegativeButton("取消", (dialog, which) -> {

                        }).show();
                return true;
            });

            hsv.setVisibility(GONE);
            attachedContainer.removeAllViews();

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

            return convertView;
        }
    }

}
