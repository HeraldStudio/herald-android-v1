package cn.seu.herald_android.app_main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.factory.ActivityCard;
import cn.seu.herald_android.factory.CardCard;
import cn.seu.herald_android.factory.CurriculumCard;
import cn.seu.herald_android.factory.ExamCard;
import cn.seu.herald_android.factory.ExperimentCard;
import cn.seu.herald_android.factory.JwcCard;
import cn.seu.herald_android.factory.LectureCard;
import cn.seu.herald_android.factory.PedetailCard;
import cn.seu.herald_android.factory.ServiceCard;
import cn.seu.herald_android.factory.ShortcutCard;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiEmptyRequest;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;

public class CardsListView extends ListView implements ApiHelper.OnUserChangeListener,
        SettingsHelper.OnModuleSettingsChangeListener {

    public BaseActivity activity;
    private SliderView slider;
    private CardsAdapter adapter;
    private BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadContent(false);
        }
    };

    public CardsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVerticalScrollBarEnabled(false);

        // 实例化轮播图
        slider = (SliderView) LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_cards__item_slider, null);

        // 设置高度。在其他地方设置没用。
        float resolution = 5 / 2f;
        int height = (int) (getContext().getResources().getDisplayMetrics().widthPixels / resolution);
        slider.setLayoutParams(new AbsListView.LayoutParams(-1, height));

        addHeaderView(slider);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(timeChangeReceiver, filter);

        ApiHelper.registerOnUserChangeListener(this);
        SettingsHelper.registerOnModuleSettingsChangeListener(this);

        refreshSliders();
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(timeChangeReceiver);

        ApiHelper.unregisterOnUserChangeListener(this);
        SettingsHelper.unregisterOnModuleSettingsChangeListener(this);

        super.onDetachedFromWindow();
    }

    @Override
    public void onUserChange() {
        loadContent(true);
    }

    @Override
    public void onModuleSettingsChange() {
        loadContent(false);
    }

    private Handler uiThreadHandler = new Handler();

    private ArrayList<CardsModel> itemList = new ArrayList<>();

    /**
     * 刷新卡片列表
     **/
    public void loadContent(boolean refresh) {
        /**
         * 本地重载部分
         **/

        // 丢你拉姆
        new Thread(() -> {

            // 防止多个刷新请求同时执行导致混乱
            synchronized (this) {

                ArrayList<CardsModel> newItemList = new ArrayList<>();

                // 清空卡片列表，等待载入
                newItemList.clear();

                newItemList.add(ShortcutCard.getCard());

                // 加载版本更新缓存
                CardsModel item1 = ServiceCard.getCheckVersionCard();
                if (item1 != null) newItemList.add(item1);

                // 加载推送缓存
                CardsModel item = ServiceCard.getPushMessageCard();
                if (item != null) newItemList.add(item);

                // 判断各模块是否开启并加载对应数据
                if (Module.card.getCardEnabled()) {
                    // 加载并解析一卡通缓存
                    newItemList.add(CardCard.getCard());
                }

                if (Module.pedetail.getCardEnabled()) {
                    // 加载并解析跑操预报缓存
                    newItemList.add(PedetailCard.getCard());
                }

                if (Module.curriculum.getCardEnabled()) {
                    // 加载并解析课表缓存
                    newItemList.add(CurriculumCard.getCard());
                }

                if (Module.experiment.getCardEnabled()) {
                    // 加载并解析实验缓存
                    newItemList.add(ExperimentCard.getCard());
                }

                if (Module.exam.getCardEnabled()) {
                    // 加载并解析考试缓存
                    newItemList.add(ExamCard.getCard());
                }

                // 活动这项永远保留在首页，加载并解析活动缓存
                CardsModel activityItem = ActivityCard.getCard();
                // 修改默认点击函数，设置为主页滑动至活动页
                activityItem.setOnClickListener(v -> new AppModule(null, "TAB1").open());
                newItemList.add(activityItem);

                if (Module.lecture.getCardEnabled()) {
                    // 加载并解析人文讲座预告缓存
                    newItemList.add(LectureCard.getCard());
                }

                if (Module.jwc.getCardEnabled()) {
                    // 加载并解析教务处缓存
                    newItemList.add(JwcCard.getCard());
                }

                // 有消息的排在前面，没消息的排在后面
                Collections.sort(newItemList, (p1, p2) ->
                        p1.getDisplayPriority().ordinal() - p2.getDisplayPriority().ordinal());

                // 丢你雷姆
                uiThreadHandler.post(() -> {
                    // 深复制，注意这里不能改变 itemList 的地址，因为列表视图已经绑定了原地址上的 itemList
                    itemList.clear();
                    for (CardsModel model : newItemList) {
                        itemList.add(model);
                    }
                    // 更新适配器，结束刷新
                    if (adapter == null) {
                        setAdapter(adapter = new CardsAdapter(itemList));
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();


        /**
         * 联网部分
         *
         * 此处为懒惰刷新，即当某模块需要刷新时才刷新，不需要时不刷新，
         * 各个模块是否刷新的判断条件可以按不同模块的需求来写。
         **/

        if (!refresh) {
            return;
        }
        if (activity != null) activity.showProgressDialog();

        ApiRequest request = new ApiEmptyRequest();

        // 刷新版本信息和推送消息
        request = request.parallel(ServiceCard.getRefresher()
                .onFinish((success, code) -> {
                    // 刷新好后单独重载轮播图
                    refreshSliders();
                }));

        // 当一卡通模块开启时
        if (Module.card.getCardEnabled() && ApiHelper.isLogin()) {
            // 直接刷新一卡通数据
            request = request.parallel(CardCard.getRefresher());
        }

        // 当跑操模块开启时
        if (Module.pedetail.getCardEnabled() && ApiHelper.isLogin()) {
            // 直接刷新跑操
            request = request.parallel(PedetailCard.getRefresher());
        }

        // 当课表模块开启时
        if (Module.curriculum.getCardEnabled() && ApiHelper.isLogin()) {
            // 仅当课表数据不存在时刷新课表
            if (Cache.curriculum.isEmpty() || Cache.curriculumSidebar.isEmpty()) {
                request = request.parallel(CurriculumCard.getRefresher());
            }
        }

        // 当实验模块开启时
        if (Module.experiment.getCardEnabled() && ApiHelper.isLogin()) {
            // 仅当实验数据不存在时刷新实验
            if (Cache.experiment.isEmpty()) {
                request = request.parallel(ExperimentCard.getRefresher());
            }
        }

        // 当考试模块开启时
        if (Module.exam.getCardEnabled() && ApiHelper.isLogin()) {
            // 仅当考试数据不存在时刷新考试
            if (Cache.exam.isEmpty()) {
                request = request.parallel(ExamCard.getRefresher());
            }
        }

        // 直接刷新校园活动
        request = request.parallel(ActivityCard.getRefresher());

        // 当人文讲座模块开启时
        if (Module.lecture.getCardEnabled()) {
            // 直接刷新人文讲座预告
            request = request.parallel(LectureCard.getRefresher());
        }

        // 当教务处模块开启时
        if (Module.jwc.getCardEnabled()) {
            // 直接刷新教务处数据
            request = request.parallel(JwcCard.getRefresher());
        }

        /**
         * 结束刷新部分
         * 当最后一个线程结束时调用这一部分，刷新结束
         **/
        request.onResponse((success1, code, response) -> {
            loadContent(false);
        }).onFinish((success, code) -> {
            if (activity != null) activity.hideProgressDialog();

            if (!success) {
                AppContext.showMessage("部分数据刷新失败");
            }
            slider.startAutoCycleIfNeeded();
        }).run();
    }

    /**
     * 刷新轮播图
     * 注意：因为轮播图刷新的时候会有明显的界面变化，所以不能跟上面的快捷栏放在一起刷新
     **/
    private void refreshSliders() {
        // 为轮播栏设置内容
        ArrayList<SliderView.SliderViewItem> sliderViewItemArrayList = ServiceHelper.getSliderViewItemArray();
        if (slider != null) slider.setupWithArrayList(sliderViewItemArrayList);
    }

    public class CardsAdapter extends BaseAdapter {

        class ViewHolder {
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.content)
            TextView content;
            @BindView(R.id.avatar)
            ImageView avatar;
            @BindView(R.id.attachedContainer)
            LinearLayout attachedContainer;
            @BindView(R.id.header)
            View header;
            @BindView(R.id.notify_dot)
            View notifyDot;
            @BindView(R.id.img_header_bg)
            View headerArrow;

            public ViewHolder(View v) {
                ButterKnife.bind(this, v);
            }
        }

        private ArrayList<CardsModel> itemList;

        public CardsAdapter(ArrayList<CardsModel> itemList) {
            this.itemList = itemList;
        }

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

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_cards__item, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            holder.name.setText(item.getName());
            holder.content.setText(item.getInfo());

            // 标识已读消息和未读消息的小点
            holder.notifyDot.setVisibility(item.getDisplayPriority() == CardsModel.Priority.CONTENT_NOTIFY ? VISIBLE : GONE);

            holder.avatar.setImageDrawable(ContextCompat.getDrawable(getContext(), item.getIconRes()));

            holder.headerArrow.setVisibility(item.getOnClickListener() == null ? View.GONE : View.VISIBLE);

            holder.header.setOnClickListener((v) -> {
                item.markAsRead();

                if (item.getOnClickListener() != null) {
                    item.getOnClickListener().onClick(v);
                }

                loadContent(false);
            });

            holder.attachedContainer.removeAllViews();

            for (View k : item.attachedView) {

                if (k.getParent() != null) {
                    ((ViewGroup) k.getParent()).removeView(k);
                }
                k.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

                holder.attachedContainer.addView(k);
            }

            return convertView;
        }
    }

}
