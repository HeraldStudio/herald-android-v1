package cn.seu.herald_android.mod_timeline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.custom.FadeOutHeaderContainer;
import cn.seu.herald_android.custom.ShortcutBoxView;
import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.custom.swiperefresh.CustomSwipeRefreshLayout;
import cn.seu.herald_android.helper.ApiThreadManager;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.cardextra.CardActivity;
import cn.seu.herald_android.mod_query.curriculum.CurriculumActivity;
import cn.seu.herald_android.mod_query.exam.ExamActivity;
import cn.seu.herald_android.mod_query.experiment.ExperimentActivity;
import cn.seu.herald_android.mod_query.jwc.JwcActivity;
import cn.seu.herald_android.mod_query.lecture.LectureActivity;
import cn.seu.herald_android.mod_query.pedetail.PedetailActivity;

public class TimelineView extends ListView {

    private ArrayList<TimelineItem> itemList;

    private CustomSwipeRefreshLayout srl;
    private ShortcutBoxView shortcutBox;
    private SliderView slider;
    private FadeOutHeaderContainer fadeContainer;
    private TimelineAdapter adapter;
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

    public void setSrl(CustomSwipeRefreshLayout srl) {
        this.srl = srl;
    }


    /**
     * 刷新卡片列表
     **/
    public void loadContent(boolean refresh) {

        SettingsHelper settingsHelper = new SettingsHelper(getContext());
        CacheHelper cacheHelper = new CacheHelper(getContext());

        /**
         * 本地重载部分
         **/

        // 单独刷新快捷栏，不刷新轮播图。轮播图在轮播图数据下载完成后单独刷新。
        refreshShortcutBox();

        // 清空卡片列表，等待载入
        itemList = new ArrayList<>();

        // 加载版本更新缓存
        TimelineItem item1 = ServiceHelper.getCheckVersionItem(this);
        if (item1 != null) itemList.add(item1);

        // 加载推送缓存
        TimelineItem item = ServiceHelper.getPushMessageItem(this);
        if (item != null) itemList.add(item);

        // 判断各模块是否开启并加载对应数据
        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CURRICULUM)) {
            // 加载并解析课表缓存
            itemList.add(CurriculumActivity.getCurriculumItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_EXPERIMENT)) {
            // 加载并解析实验缓存
            itemList.add(ExperimentActivity.getExperimentItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_EXAM)) {
            // 加载并解析考试缓存
            itemList.add(ExamActivity.getExamItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_LECTURE)) {
            // 加载并解析人文讲座预告缓存
            itemList.add(LectureActivity.getLectureItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_PEDETAIL)) {
            // 加载并解析跑操预报缓存
            itemList.add(PedetailActivity.getPeForecastItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CARDEXTRA)) {
            // 加载并解析一卡通缓存
            itemList.add(CardActivity.getCardItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_JWC)) {
            // 加载并解析教务处缓存
            itemList.add(JwcActivity.getJwcItem(this));
        }

        // 有消息的排在前面，没消息的排在后面
        Collections.sort(itemList, (p1, p2) ->
                p1.getDisplayPriority(getContext()) - p2.getDisplayPriority(getContext()));

        // 更新适配器，结束刷新
        if (adapter == null) {
            setAdapter(adapter = new TimelineAdapter());
        } else {
            adapter.notifyDataSetChanged();
        }

        /**
         * 联网部分
         *
         * 1、此处为懒惰刷新，即当某模块需要刷新时才刷新，不需要时不刷新，
         * 各个模块是否刷新的判断条件可以按不同模块的需求来写。
         *
         * 2、此处改为用 {@link ApiThreadManager} 方式管理线程。
         * 该管理器可以自定义在每个线程结束时、在所有线程结束时执行不同的操作。
         *
         * 3、这部分利用 {@link ApiThreadManager} 的错误处理机制，当管理器添加线程时，
         * 会将每个线程的错误处理改为自动将错误放到管理器的错误池中。当管理器报告运行结束时，
         * 我们可以手动让管理器显示一个错误信息来代替这些线程的多个错误信息。
         **/

        if (refresh) {

            // 线程管理器
            ApiThreadManager manager = new ApiThreadManager().onResponse((success, c, r) -> {
                if (success) loadContent(false);
            });

            // 刷新版本信息和推送消息
            manager.add(ServiceHelper.refreshVersionCache(getContext())
                    .onFinish((success, code, response) -> {
                // 刷新好后单独重载轮播图
                refreshSliders();
            }));

            // 当课表模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CURRICULUM)) {
                // 仅当课表数据不存在时刷新课表
                if (cacheHelper.getCache("herald_curriculum").equals("")
                        || cacheHelper.getCache("herald_sidebar").equals("")) {
                    manager.addAll(CurriculumActivity.remoteRefreshCache(getContext()));
                }
            }

            // 当实验模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_EXPERIMENT)) {
                // 仅当实验数据不存在时刷新实验
                if (cacheHelper.getCache("herald_experiment").equals("")) {
                    manager.add(ExperimentActivity.remoteRefreshCache(getContext()));
                }
            }

            // 当考试模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_EXAM)) {
                // 仅当考试数据不存在时刷新考试
                if (cacheHelper.getCache("herald_exam").equals("")) {
                    manager.add(ExamActivity.remoteRefreshCache(getContext()));
                }
            }

            // 当人文讲座模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_LECTURE)) {
                // 直接刷新人文讲座预告
                manager.add(LectureActivity.remoteRefreshCache(getContext()));
            }

            // 当跑操模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_PEDETAIL)) {
                Calendar nowCal = Calendar.getInstance();
                long now = Calendar.getInstance().getTimeInMillis();
                long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
                long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;

                // 仅当已到开始时间时，允许刷新
                if (now >= startTime) {
                    manager.addAll(PedetailActivity.remoteRefreshCache(getContext()));
                }
            }

            // 当一卡通模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CARDEXTRA)) {
                // 直接刷新一卡通数据
                manager.add(CardActivity.remoteRefreshCache(getContext()));
            }

            // 当教务处模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_JWC)) {
                // 直接刷新教务处数据
                manager.add(JwcActivity.remoteRefreshCache(getContext()));
            }

            /**
             * 结束刷新部分
             * 当最后一个线程结束时调用这一部分，刷新结束
             **/
            manager.onFinish((success) -> {
                if (srl != null) srl.setRefreshing(false);

                if (!success) {
                    ContextUtils.showMessage(getContext(), "刷新过程中出现了一些问题，请重试~");
                }
                slider.startAutoCycle();
            }).run();
        }
    }

    private void refreshShortcutBox() {
        if (shortcutBox == null) {
            ViewGroup vg = (ViewGroup)
                    LayoutInflater.from(getContext()).inflate(R.layout.timeline_shortcut_box, null);
            shortcutBox = (ShortcutBoxView) vg.findViewById(R.id.shorcut_box);
            addHeaderView(vg);
        } else {
            shortcutBox.refresh();
        }
    }

    /**
     * 刷新轮播图
     * 注意：因为轮播图刷新的时候会有明显的界面变化，所以不能跟上面的快捷栏放在一起刷新
     **/
    private void refreshSliders() {

        if (slider == null) {
            slider = (SliderView) LayoutInflater.from(getContext()).inflate(R.layout.timeline_slider, null);

            // 轮播图居中变色动效的调用
            fadeContainer = new FadeOutHeaderContainer<SliderView>(getContext())
                    .maskColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                    .append(slider);

            // 设置高度。在其他地方设置没用。
            float resolution = 5 / 2f;
            int height = (int) (getContext().getResources().getDisplayMetrics().widthPixels / resolution);
            slider.setLayoutParams(new FadeOutHeaderContainer.LayoutParams(-1, height));

            addHeaderView(fadeContainer);
        }

        // 为轮播栏设置内容
        ServiceHelper serviceHelper = new ServiceHelper(getContext());
        ArrayList<SliderView.SliderViewItem> sliderViewItemArrayList = serviceHelper.getSliderViewItemArray();
        slider.setupWithArrayList(sliderViewItemArrayList);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        // 轮播图居中变色动效的实现
        fadeContainer.syncFadeState();
        fadeContainer.syncScrollState();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(timeChangeReceiver, filter);

        refreshSliders();
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(timeChangeReceiver);
        super.onDetachedFromWindow();
    }



    public class TimelineAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public TimelineItem getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TimelineItem item = getItem(position);

            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, null);

            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView content = (TextView) convertView.findViewById(R.id.content);
            ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
            LinearLayout attachedContainer = (LinearLayout) convertView.findViewById(R.id.attachedContainer);
            View header = convertView.findViewById(R.id.header);

            View notifyDot = convertView.findViewById(R.id.notify_dot);

            name.setText(item.getName());
            content.setText(item.getInfo());

            //标识已读消息和未读消息的小点
            notifyDot.setVisibility(
                    item.getDisplayPriority(getContext()) == TimelineItem.CONTENT_NOTIFY ? VISIBLE : GONE);

            avatar.setImageDrawable(ContextCompat.getDrawable(getContext(), item.getIconRes()));

            header.setOnClickListener((v) -> {
                item.markAsRead(getContext());
                item.getOnClickListener().onClick(v);
                loadContent(false);
            });

            attachedContainer.removeAllViews();

            if (item.attachedView.size() != 0) {
                for (View k : item.attachedView) {

                    if (k.getParent() != null) {
                        ((ViewGroup) k.getParent()).removeView(k);
                    }
                    k.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

                    // 默认的点击事件
                    /*if (!k.hasOnClickListeners()) {
                        k.setOnClickListener(item.getOnClickListener());
                    }*/
                    attachedContainer.addView(k);
                }
            }

            return convertView;
        }
    }

}
