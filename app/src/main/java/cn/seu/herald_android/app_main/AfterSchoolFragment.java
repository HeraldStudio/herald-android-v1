package cn.seu.herald_android.app_main;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.custom.CustomDividerItemDecoration;
import cn.seu.herald_android.custom.refreshrecyclerview.RefreshRecyclerView;
import cn.seu.herald_android.custom.swiperefresh.CustomSwipeRefreshLayout;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.mod_afterschool.AfterSchoolActivityAdapter;
import cn.seu.herald_android.mod_afterschool.AfterSchoolActivityBlockLayout;
import cn.seu.herald_android.mod_afterschool.AfterSchoolActivityItem;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;


/**
 * Created by heyon on 2016/5/17.
 */
public class AfterSchoolFragment extends Fragment{
    private View contentView;
    //展示活动列表
    RefreshRecyclerView recyclerView;
    //适配器
    AfterSchoolActivityAdapter afterSchoolActivityAdapter;
    //当前展示的页
    int page = 1;
    //缓存助手
    CacheHelper cacheHelper;
    //下拉刷新控件
    CustomSwipeRefreshLayout srl;
    //标识刷新状态，如果还在刷新就拒绝刷新请求
    boolean isRefreshing = false;

    public static AfterSchoolFragment getInstance(BaseAppCompatActivity baseAppCompatActivity){
        AfterSchoolFragment fragment = new AfterSchoolFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_main_afterschool, container, false);
        //加载活动列表
        init();
        loadActivityList();
        return contentView;
    }

    @Override
    public void onResume() {
        // 从模块管理界面返回时,重载模块列表
        super.onResume();
    }

    public void loadActivityList(){
        //如果缓存不为空的话先加载第一页的缓存
        String cache = cacheHelper.getCache("herald_afterschoolactivity");
        if (!cache.equals("")) {
            addNewItemWithData(cache);
        }
        //同时刷新第一页的缓存,刷新后载入最新的第一页，同时重置page为第一页
        refreshCache();
    }

    public void refreshCache(){
        //刷新第一页的缓存,刷新后载入最新的第一页，同时重置page为第一页
        if (isRefreshing)
            return;
        isRefreshing = true;
        page = 1;
        new ApiRequest(getContext())
                .get()
                .url(ApiHelper.getLiveApiUrl(ApiHelper.API_LIVE_AFTERSCHOOLACTIVITY)+"?page="+page)
                .toCache("herald_afterschoolschool", o -> o)
                .onFinish((success, code, response) -> {
                    ContextUtils.showMessage(getContext(),"刷新成功");
                    isRefreshing = false;
                    if ( srl != null) srl.setRefreshing(false);
                    //成功则
                    if (success){
                        afterSchoolActivityAdapter.removeAll();
                        addNewItemWithData(response);
                    }
                }).run();
    }


    private void init(){
        //活动列表初始化
        recyclerView = (RefreshRecyclerView) contentView.findViewById(R.id.recyclerview_afterschoolactivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CustomDividerItemDecoration(
                getContext()));
        cacheHelper = new CacheHelper(getContext());
        //初始化适配器
        afterSchoolActivityAdapter = new AfterSchoolActivityAdapter(
                getContext(),
                new ArrayList<>());
        //绑定适配器跟上拉加载监听函数
        recyclerView.setAdapter(afterSchoolActivityAdapter);
        recyclerView.setOnFooterListener((footerposition)-> {
            //每次上拉记载时，页数都加1
            page += 1;
            new ApiRequest(getContext())
                    .url(ApiHelper.getLiveApiUrl(ApiHelper.API_LIVE_AFTERSCHOOLACTIVITY)+"?page="+page)
                    .get()
                    .onFinish((success, code, response) -> {
                        if (success){
                            addNewItemWithData(response);
                        }
                    }).run();
        });
        //设置下拉刷新
        srl = (CustomSwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        srl.setOnRefreshListener(this::refreshCache);
    }

    public void addNewItemWithData(String data){
        //根据所给数据(格式为服务器中返回的数据格式)将活动添加到列表中
        try {
            JSONArray array = new JSONObject(data).getJSONArray("content");
            if(array.length() == 0)
            {
                afterSchoolActivityAdapter.setLoadFinished(true);
                ContextUtils.showMessage(getContext(), "已无更多内容");
            }else{
                //新一页的内容
                ArrayList<AfterSchoolActivityItem> newcontent =
                        AfterSchoolActivityItem.transfromJSONArrayToArrayList(array);
                for(AfterSchoolActivityItem item : newcontent){
                    //逐项加入列表中
                    afterSchoolActivityAdapter.addItem(item);
                }
            }
            afterSchoolActivityAdapter.notifyDataSetChanged();
        }catch (JSONException e){
            ContextUtils.showMessage(getContext(),"数据加载错误");
            e.printStackTrace();
        }
    }

    //获取最新热门活动
    public static ApiRequest remoteRefreshCache(Context context) {
        return new ApiRequest(context)
                .get()
                .url(ApiHelper.getLiveApiUrl(ApiHelper.API_LIVE_HOTAFTERSCHOOLACTIVITY))
                .toCache("herald_afterschoolschool_hot", o -> o);
    }

    /**
     * 读取热门活动缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getAfterSchoolActivityItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_afterschoolschool_hot");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            List<AfterSchoolActivityItem> afterSchoolActivityItems = AfterSchoolActivityItem.transfromJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            if (afterSchoolActivityItems.size() == 0) {
                return new TimelineItem("校园活动","最近没有热门活动",now,TimelineItem.NO_CONTENT,R.mipmap.ic_activity);
            } else {
                TimelineItem item  = new TimelineItem("校园活动","最近有" + afterSchoolActivityItems.size() + "个热门活动",
                        now,TimelineItem.CONTENT_NOTIFY,R.mipmap.ic_activity);
                for (AfterSchoolActivityItem afterSchoolActivityItem : afterSchoolActivityItems) {
                    item.attachedView.add(new AfterSchoolActivityBlockLayout(host.getContext(), afterSchoolActivityItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            new CacheHelper(host.getContext()).setCache("herald_afterschoolschool_hot", "");
            return new TimelineItem("校园活动","热门活动数据加载失败",
                    now,TimelineItem.NO_CONTENT,R.mipmap.ic_activity);
        }
    }

}
