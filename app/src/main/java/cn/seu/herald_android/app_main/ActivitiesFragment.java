package cn.seu.herald_android.app_main;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.custom.CustomDividerItemDecoration;
import cn.seu.herald_android.custom.refreshrecyclerview.RefreshRecyclerView;
import cn.seu.herald_android.custom.swiperefresh.CustomSwipeRefreshLayout;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.mod_afterschool.ActivityAdapter;
import cn.seu.herald_android.mod_afterschool.AfterSchoolActivityItem;

public class ActivitiesFragment extends Fragment {

    // 展示活动列表
    @BindView(R.id.recyclerview_afterschoolactivity)
    RefreshRecyclerView recyclerView;

    // 下拉刷新控件
    @BindView(R.id.swipe_container)
    CustomSwipeRefreshLayout srl;

    // ButterKnife 所需
    private Unbinder unbinder;

    // 适配器
    ActivityAdapter activityAdapter;

    // 当前展示的页
    int page = 1;

    // 标识刷新状态，如果还在刷新就拒绝刷新请求
    boolean isRefreshing = false;

    public static ActivitiesFragment getInstance() {
        return new ActivitiesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.app_main__fragment_activities, container, false);
        unbinder = ButterKnife.bind(this, contentView);
        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 加载活动列表
        init();
        loadActivityList();
    }

    private void init() {
        // 活动列表初始化
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CustomDividerItemDecoration(getContext()));

        // 初始化适配器
        activityAdapter = new ActivityAdapter(
                getContext(),
                new ArrayList<>());

        // 绑定适配器跟上拉加载监听函数
        recyclerView.setAdapter(activityAdapter);
        recyclerView.setOnFooterListener((footerposition) -> new ApiRequest()
                .url("http://115.28.27.150/herald/api/v1/huodong/get?page=" + (page + 1))
                .get()
                .onFinish((success, code, response) -> {
                    if (success) {
                        //每次上拉记载时，页数都加1
                        page += 1;
                        addNewItemWithData(response);
                    } else {
                        AppContext.showMessage("加载失败，请重试");
                    }
                }).run());

        // 设置下拉刷新
        srl.setOnRefreshListener(this::refreshCache);
    }

    public void loadActivityList(){
        //如果缓存不为空的话先加载第一页的缓存
        String cache = CacheHelper.get("herald_afterschoolactivity");
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
        new ApiRequest()
                .get()
                .url("http://115.28.27.150/herald/api/v1/huodong/get?page="+page)
                .toCache("herald_afterschoolschool", o -> o)
                .onFinish((success, code, response) -> {
                    // AppContext.showMessage("刷新成功");
                    isRefreshing = false;
                    if ( srl != null) srl.setRefreshing(false);
                    //成功则
                    if (success){
                        activityAdapter.removeAll();
                        addNewItemWithData(response);
                    } else {
                        AppContext.showMessage("刷新失败，请重试");
                    }
                }).run();
    }

    public void addNewItemWithData(String data){
        //根据所给数据(格式为服务器中返回的数据格式)将活动添加到列表中
        try {
            JSONArray array = new JSONObject(data).getJSONArray("content");
            if(array.length() == 0) {
                activityAdapter.setLoadFinished(true);
                AppContext.showMessage("没有更多数据");
            } else {
                //新一页的内容
                ArrayList<AfterSchoolActivityItem> newcontent =
                        AfterSchoolActivityItem.transformJSONArrayToArrayList(array);
                for(AfterSchoolActivityItem item : newcontent){
                    //逐项加入列表中
                    activityAdapter.addItem(item);
                }
            }
            activityAdapter.notifyDataSetChanged();
        }catch (JSONException e){
            AppContext.showMessage("解析失败，请刷新");
            e.printStackTrace();
        }
    }
}
