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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.custom.FadeOutHeaderContainer;
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
    private ArrayList<TimelineItem> itemList;

    private Runnable hideRefresh = null;
    private ShortcutBoxView shortcutBox;
    private SliderView slider;
    private FadeOutHeaderContainer fadeContainer;
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

    public void setHideRefresh(Runnable hideRefresh) {
        this.hideRefresh = hideRefresh;
    }

    public void loadContent(boolean refresh) {
        /**
         * 刷新列表
         *
         * 为了方便理解，说明如下：
         *
         * 1、若refresh为true，为联网刷新操作。
         * 此时将用Vector的方式管理线程，每个线程开始前添加一个对象，结束时先删除一个对象，然后递归调用本函数。
         * 被递归调用的子函数中，refresh参数强制为false，它们首先会进行本地重载（每个线程结束都重载一次），
         * 然后判断Vector中的对象数是否为零，如果为零则执行刷新结束的操作。
         *
         * 2、若refresh为false，为本地重载操作，本地重载不涉及上述的线程管理和递归问题。
         **/

        // 清空卡片列表，等待载入
        itemList = new ArrayList<>();
        SettingsHelper settingsHelper = new SettingsHelper(getContext());
        CacheHelper cacheHelper = new CacheHelper(getContext());

        /**
         * 联网部分
         *
         * 只有refresh为true时，主函数才执行此部分。因此被递归调用的子函数不执行此部分。
         *
         * 1、此处为懒惰刷新，即当某模块需要刷新时才刷新，不需要时不刷新，
         * 各个模块是否刷新的判断条件可以按不同模块的需求来写。
         *
         * 2、此处使用各个模块类的remoteRefreshCache()函数进行各个模块的刷新。
         * 因为多数模块类都是Activity，为了不构造这些实例就调用它们，这些函数被设置为静态的。
         *
         * 3、因为需要多个模块联网，所以这里需要一个延迟显示错误信息的机制，防止连续显示好几个错误信息。
         * 又因为第2点提到的这些静态函数已经从对应的动态函数中分离出来，专门用于这里的联网刷新，
         * 所以我索性把这种有特殊需求的错误处理机制也放在了那些静态函数中，
         * 那些静态函数在联网出错时不会调用ApiHelper.dealApiException()直接显示错误信息，
         * 而是调用ApiHelper.dealApiExceptionSilently()函数，
         * 该函数会把生成的错误信息先交给ContextUtils暂存起来，
         * 然后在本函数最末尾刷新完成的时候将让ContextUtils吐出最后一条错误消息。
         **/
        // 没有头部的时候加载头部，否则就不重载头部，防止轮播图连续多次刷新
        refreshShortcutBox();

        if (refresh) {

            // 刷新版本信息和推送消息
            threads.add(new Object());
            ServiceHelper.refreshVersionCache(getContext(), () -> {
                if (threads.size() > 0) threads.remove(0);

                // 单独重载轮播图
                refreshSliders();
                loadContent(false);
            });

            // 当课表模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CURRICULUM)) {
                // 仅当课表数据不存在时刷新课表
                if (cacheHelper.getCache("herald_curriculum").equals("")
                        || cacheHelper.getCache("herald_sidebar").equals("")) {
                    threads.add(new Object());
                    CurriculumActivity.remoteRefreshCache(getContext(), () -> {
                        if (threads.size() > 0) threads.remove(0);
                        loadContent(false);
                    });
                }
            }

            // 当实验模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_EXPERIMENT)) {
                // 仅当实验数据不存在时刷新实验
                if (cacheHelper.getCache("herald_experiment").equals("")) {
                    threads.add(new Object());
                    ExperimentActivity.remoteRefreshCache(getContext(), () -> {
                        if (threads.size() > 0) threads.remove(0);
                        loadContent(false);
                    });
                }
            }

            // 当人文讲座模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_LECTURE)) {
                // 直接刷新人文讲座预告
                threads.add(new Object());
                LectureActivity.remoteRefreshCache(getContext(), () -> {
                    if (threads.size() > 0) threads.remove(0);
                    loadContent(false);
                });
            }

            // 当跑操模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_PEDETAIL)) {
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
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CARDEXTRA)) {
                // 直接刷新一卡通数据
                threads.add(new Object());
                CardActivity.remoteRefreshCache(getContext(), () -> {
                    if (threads.size() > 0) threads.remove(0);
                    loadContent(false);
                });
            }

            // 当教务处模块开启时
            if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_JWC)) {
                // 直接刷新一卡通数据
                threads.add(new Object());
                JwcActivity.remoteRefreshCache(getContext(), () -> {
                    if (threads.size() > 0) threads.remove(0);
                    loadContent(false);
                });
            }
        }

        /**
         * 本地重载部分
         *
         * 主函数和被递归调用的子函数都执行这一部分
         * 这样每刷出来一个模块都会本地重载一次列表，以便于在有些模块没刷出来的时候第一时间看到已经刷出来的模块
         **/

        // 加载版本更新消息
        TimelineItem item1 = TimelineParser.getCheckVersionItem(this);
        if (item1 != null) itemList.add(item1);

        // 加载推送消息
        TimelineItem item = TimelineParser.getPushMessageItem(this);
        if (item != null) itemList.add(item);

        // 判断各模块是否开启并加载对应数据
        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CURRICULUM)) {
            // 加载并解析课表数据
            itemList.add(TimelineParser.getCurriculumItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_EXPERIMENT)) {
            // 加载并解析实验数据
            itemList.add(TimelineParser.getExperimentItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_LECTURE)) {
            // 加载并解析人文讲座预告数据
            itemList.add(TimelineParser.getLectureItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_PEDETAIL)) {
            // 加载并解析跑操预报数据
            itemList.add(TimelineParser.getPeForecastItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_CARDEXTRA)) {
            // 加载并解析一卡通数据
            itemList.add(TimelineParser.getCardItem(this));
        }

        if (settingsHelper.getModuleCardEnabled(SettingsHelper.MODULE_JWC)) {
            // 加载并解析教务处数据
            itemList.add(TimelineParser.getJwcItem(this));
        }

        // 有消息的排在前面，没消息的排在后面
        Collections.sort(itemList, TimelineItem.comparator);

        // 更新适配器，结束刷新
        if (adapter == null) {
            setAdapter(adapter = new TimelineAdapter());
        } else {
            adapter.notifyDataSetChanged();
        }

        /**
         * 结束刷新部分
         *
         * 只有最后结束的线程在结束时调用的递归子函数能执行到这一部分
         * 它负责隐藏刷新控件，也可以在这里加入其它后续处理工作
         **/
        if (hideRefresh != null && threads.size() == 0 && !refresh) {
            hideRefresh.run();
            ContextUtils.flushMessage(getContext(), "刷新过程中出现了一些问题，请重试~");
        }
    }

    private void refreshShortcutBox() {
        // dp单位值
        float dp = getContext().getResources().getDisplayMetrics().density;

        if (shortcutBox == null) {
            shortcutBox = (ShortcutBoxView)
                    LayoutInflater.from(getContext()).inflate(R.layout.timeline_shortcut_box, null);
            addHeaderView(shortcutBox);
        } else {
            shortcutBox.refresh();
        }

       /* if (topPadding == null) {
            // 顶部增加一个padding
            topPadding = new View(getContext());
            topPadding.setLayoutParams(new AbsListView.LayoutParams(-1, (int) (9 * dp)));
            addHeaderView(topPadding);
        }*/
    }

    /**
     * 刷新轮播图
     * 注意：因为轮播图刷新的时候会有明显的界面变化，所以不能跟上面的快捷栏放在一起刷新
     **/
    private void refreshSliders() {
        if (slider == null) {
            slider = (SliderView) LayoutInflater.from(getContext()).inflate(R.layout.timeline_slider, null);
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

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, null);

            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView time = (TextView) convertView.findViewById(R.id.time);
            TextView content = (TextView) convertView.findViewById(R.id.content);
            ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
            ViewGroup attachedContainer = (ViewGroup) convertView.findViewById(R.id.attachedContainer);
            ViewGroup hsv = (ViewGroup) convertView.findViewById(R.id.hsv);
            View notifyDot = convertView.findViewById(R.id.notify_dot);

            name.setText(item.getName());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.getTime());
            String dateTime = timeInNaturalLanguage(calendar, now);
            time.setText(dateTime);
            content.setText(item.getInfo());

            notifyDot.setVisibility(item.getImportance() == TimelineItem.CONTENT_NOTIFY ? VISIBLE : GONE);

            avatar.setImageDrawable(ContextCompat.getDrawable(getContext(), item.getIconRes()));

            convertView.setOnClickListener(item.getOnClickListener());

            if (item.moduleId != -1) {
                convertView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(getContext()).setMessage("确定要隐藏该卡片吗？")
                            .setPositiveButton("确定", (dialog, which) -> {
                                new SettingsHelper(getContext()).setModuleCardEnabled(item.moduleId, false);
                                if (getContext() instanceof MainActivity) {
                                    ((MainActivity) getContext()).syncModuleSettings();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                    return true;
                });
            }

            hsv.setVisibility(GONE);
            attachedContainer.removeAllViews();

            if (item.getImportance() == TimelineItem.NO_CONTENT) {
                time.setText(item.getInfo());
                content.setVisibility(GONE);
            } else {
                content.setVisibility(VISIBLE);
            }

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
