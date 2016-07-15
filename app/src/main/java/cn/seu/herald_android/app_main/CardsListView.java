package cn.seu.herald_android.app_main;

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

import java.util.ArrayList;
import java.util.Collections;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.custom.FadeOutHeaderContainer;
import cn.seu.herald_android.custom.ShortcutBoxView;
import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.custom.swiperefresh.CustomSwipeRefreshLayout;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.ApiThreadManager;
import cn.seu.herald_android.helper.AppModule;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_cards.ActivityCard;
import cn.seu.herald_android.mod_cards.CardCard;
import cn.seu.herald_android.mod_cards.CurriculumCard;
import cn.seu.herald_android.mod_cards.ExamCard;
import cn.seu.herald_android.mod_cards.ExperimentCard;
import cn.seu.herald_android.mod_cards.JwcCard;
import cn.seu.herald_android.mod_cards.LectureCard;
import cn.seu.herald_android.mod_cards.PedetailCard;
import cn.seu.herald_android.mod_cards.ServiceCard;
import cn.seu.herald_android.mod_query.grade.GradeActivity;
import cn.seu.herald_android.mod_query.gymreserve.GymReserveActivity;
import cn.seu.herald_android.mod_query.library.LibraryActivity;
import cn.seu.herald_android.mod_query.srtp.SrtpActivity;

public class CardsListView extends ListView {

    private ArrayList<CardsModel> itemList;
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

    public CardsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(false);
        init();
    }

    public void init(){
        // 加入快捷栏
        setupShortCutBox();
        // 加入轮播栏
        setupSliderView();

        // 监听模块设置改变事件
        SettingsHelper.addModuleSettingsChangeListener(() -> {
            loadContent(false);
        });
    }

    public void setupSliderView(){
        ViewGroup vg = (ViewGroup)
                LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_cards__item_shortcut_box, null);
        shortcutBox = (ShortcutBoxView) vg.findViewById(R.id.shorcut_box);
        addHeaderView(vg);
    }

    public void setupShortCutBox(){
        slider = (SliderView) LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_cards__item_slider, null);

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

    public void setSrl(CustomSwipeRefreshLayout srl) {
        this.srl = srl;
    }


    /**
     * 刷新卡片列表
     **/
    public void loadContent(boolean refresh) {
        /**
         * 本地重载部分
         **/

        // 单独刷新快捷栏，不刷新轮播图。轮播图在轮播图数据下载完成后单独刷新。
        refreshShortcutBox();

        // 清空卡片列表，等待载入
        itemList = new ArrayList<>();

        // 加载版本更新缓存
        CardsModel item1 = ServiceCard.getCheckVersionCard();
        if (item1 != null) itemList.add(item1);

        // 加载推送缓存
        CardsModel item = ServiceCard.getPushMessageCard();
        if (item != null) itemList.add(item);

        // 判断各模块是否开启并加载对应数据
        if (SettingsHelper.Module.curriculum.cardEnabled.$get()) {
            // 加载并解析课表缓存
            itemList.add(CurriculumCard.getCard());
        }

        if (SettingsHelper.Module.experiment.cardEnabled.$get()) {
            // 加载并解析实验缓存
            itemList.add(ExperimentCard.getCard());
        }

        if (SettingsHelper.Module.exam.cardEnabled.$get()) {
            // 加载并解析考试缓存
            itemList.add(ExamCard.getCard());
        }

        //活动这项永远保留在首页，加载并解析活动缓存
        CardsModel activityItem = ActivityCard.getCard();
        //修改默认点击函数，设置为主页滑动至活动页
        activityItem.setOnClickListener(v -> new AppModule(null, "TAB1").open());
        itemList.add(activityItem);

        if (SettingsHelper.Module.lecture.cardEnabled.$get()) {
            // 加载并解析人文讲座预告缓存
            itemList.add(LectureCard.getCard());
        }

        if (SettingsHelper.Module.pedetail.cardEnabled.$get()) {
            // 加载并解析跑操预报缓存
            itemList.add(PedetailCard.getCard());
        }

        if (SettingsHelper.Module.card.cardEnabled.$get()) {
            // 加载并解析一卡通缓存
            itemList.add(CardCard.getCard());
        }

        if (SettingsHelper.Module.jwc.cardEnabled.$get()) {
            // 加载并解析教务处缓存
            itemList.add(JwcCard.getCard());
        }

        // 有消息的排在前面，没消息的排在后面
        Collections.sort(itemList, (p1, p2) ->
                p1.getDisplayPriority().ordinal() - p2.getDisplayPriority().ordinal());

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
            manager.add(ServiceCard.getRefresher()
                    .onFinish((success, code, response) -> {
                // 刷新好后单独重载轮播图
                refreshSliders();
            }));

            // 当课表模块开启时
            if (SettingsHelper.Module.curriculum.cardEnabled.$get()) {
                // 仅当课表数据不存在时刷新课表
                if (CacheHelper.get("herald_curriculum").equals("")
                        || CacheHelper.get("herald_sidebar").equals("")) {
                    manager.addAll(CurriculumCard.getRefresher());
                }
            }

            // 当实验模块开启时
            if (SettingsHelper.Module.experiment.cardEnabled.$get()) {
                // 仅当实验数据不存在时刷新实验
                if (CacheHelper.get("herald_experiment").equals("")) {
                    manager.add(ExperimentCard.getRefresher());
                }
            }

            // 当考试模块开启时
            if (SettingsHelper.Module.exam.cardEnabled.$get()) {
                // 仅当考试数据不存在时刷新考试
                if (CacheHelper.get("herald_exam").equals("")) {
                    manager.add(ExamCard.getRefresher());
                }
            }

            // 当人文讲座模块开启时
            if (SettingsHelper.Module.lecture.cardEnabled.$get()) {
                // 直接刷新人文讲座预告
                manager.add(LectureCard.getRefresher());
            }

            // 当跑操模块开启时
            if (SettingsHelper.Module.pedetail.cardEnabled.$get()) {
                // 直接刷新跑操
                manager.addAll(PedetailCard.getRefresher());
            }

            // 当一卡通模块开启时
            if (SettingsHelper.Module.card.cardEnabled.$get()) {
                // 直接刷新一卡通数据
                manager.add(CardCard.getRefresher());
            }

            // 当教务处模块开启时
            if (SettingsHelper.Module.jwc.cardEnabled.$get()) {
                // 直接刷新教务处数据
                manager.add(JwcCard.getRefresher());
            }

            // 活动为非模块，永远保持在首页
            manager.add(ActivityCard.getRefresher());

            manager.addAll(new ApiRequest[]{
                    GymReserveActivity.remoteRefreshNotifyDotState(),
                    SrtpActivity.remoteRefreshNotifyDotState(),
                    GradeActivity.remoteRefreshNotifyDotState(),
                    LibraryActivity.remoteRefreshNotifyDotState()
            });

            /**
             * 结束刷新部分
             * 当最后一个线程结束时调用这一部分，刷新结束
             **/
            manager.onFinish((success) -> {
                if (srl != null) srl.setRefreshing(false);
                if (!success) {
                    AppContext.showMessage("部分数据刷新失败");
                }
                slider.startAutoCycle();
            }).run();
        }
    }

    private void refreshShortcutBox() {
        if (shortcutBox != null) shortcutBox.refresh();
    }

    /**
     * 刷新轮播图
     * 注意：因为轮播图刷新的时候会有明显的界面变化，所以不能跟上面的快捷栏放在一起刷新
     **/
    private void refreshSliders() {
        // 为轮播栏设置内容
        ArrayList<SliderView.SliderViewItem> sliderViewItemArrayList = ServiceHelper.getSliderViewItemArray();
        if (slider!=null) slider.setupWithArrayList(sliderViewItemArrayList);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 轮播图居中变色动效的实现
        if (fadeContainer!=null){
            fadeContainer.syncFadeState();
            fadeContainer.syncScrollState();
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
        public CardsModel getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CardsModel item = getItem(position);

            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_cards__item, null);

            TextView name = (TextView) convertView.findViewById(R.id.name);
            TextView content = (TextView) convertView.findViewById(R.id.content);
            ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
            LinearLayout attachedContainer = (LinearLayout) convertView.findViewById(R.id.attachedContainer);
            View header = convertView.findViewById(R.id.header);

            View notifyDot = convertView.findViewById(R.id.notify_dot);

            name.setText(item.getName());
            content.setText(item.getInfo());

            //标识已读消息和未读消息的小点
            notifyDot.setVisibility(item.getDisplayPriority() == CardsModel.Priority.CONTENT_NOTIFY ? VISIBLE : GONE);

            avatar.setImageDrawable(ContextCompat.getDrawable(getContext(), item.getIconRes()));

            header.setOnClickListener((v) -> {
                item.markAsRead();
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
