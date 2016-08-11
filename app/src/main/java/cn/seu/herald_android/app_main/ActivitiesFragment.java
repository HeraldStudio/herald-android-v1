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
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.custom.CustomSwipeRefreshLayout;
import cn.seu.herald_android.custom.refreshrecyclerview.RefreshRecyclerView;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

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
    ActivitiesAdapter activitiesAdapter;

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
        loadCache();
        refreshCache();
    }

    private void init() {
        // 活动列表初始化
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // 初始化适配器
        activitiesAdapter = new ActivitiesAdapter(
                getContext(),
                new ArrayList<>());

        // 绑定适配器跟上拉加载监听函数
        recyclerView.setAdapter(activitiesAdapter);
        recyclerView.setOnFooterListener((footerPos) -> loadNextPage());

        // 设置下拉刷新
        srl.setOnRefreshListener(this::refreshCache);
    }

    public void refreshCache() {
        // 刷新第一页的缓存,刷新后载入最新的第一页，同时重置page为第一页
        if (isRefreshing)
            return;
        isRefreshing = true;
        Cache.activities.refresh((success, code) -> {
            if (srl != null) srl.setRefreshing(false);
            loadCache();

            if (!success) {
                AppContext.showMessage("刷新失败，请重试");
            }
            isRefreshing = false;
        });
    }

    // 载入缓存内容
    public void loadCache() {

        // 首先清空数据
        activitiesAdapter.removeAll();

        // 设置当前页数为0，以便在没有得到数据时，上拉加载仍加载第1页内容而不是第2页
        page = 0;

        try {
            JSONArray array = new JSONObject(Cache.activities.getValue()).getJSONArray("content");

            // 新一页的内容
            ArrayList<ActivitiesItem> newContent =
                    ActivitiesItem.transformJSONArrayToArrayList(array);

            // 如果有数据，逐条添加数据，并设置当前页数为1
            for (ActivitiesItem item : newContent) {
                // 逐项加入列表中
                activitiesAdapter.addItem(item);
                page = 1;
            }

            // 恢复上拉加载控件的可用性
            activitiesAdapter.setLoadFinished(false);

            // 显式重载列表内容
            activitiesAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            AppContext.showMessage("解析失败，请刷新");
            e.printStackTrace();
        }
    }

    // 联网加载下一页内容，若成功，加入列表并自增一页；否则显示错误信息
    public void loadNextPage() {
        new ApiSimpleRequest(Method.GET).url("http://www.heraldstudio.com/herald/api/v1/huodong/get?page=" + (page + 1))
                .onResponse((success, code, response) -> {
                    if (srl != null) srl.setRefreshing(false);

                    if (success) {
                        page++;

                        try {
                            JSONArray array = new JSONObject(response).getJSONArray("content");
                            if (array.length() == 0) {
                                activitiesAdapter.setLoadFinished(true);
                            } else {
                                // 新一页的内容
                                ArrayList<ActivitiesItem> newContent =
                                        ActivitiesItem.transformJSONArrayToArrayList(array);
                                for (ActivitiesItem item : newContent) {
                                    // 逐项加入列表中
                                    activitiesAdapter.addItem(item);
                                }
                            }
                            activitiesAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            AppContext.showMessage("解析失败，请刷新");
                            e.printStackTrace();
                        }
                    }
                }).run();
    }
}
