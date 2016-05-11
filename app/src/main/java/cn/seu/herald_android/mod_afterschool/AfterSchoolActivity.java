package cn.seu.herald_android.mod_afterschool;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.custom.CustomDividerItemDecoration;
import cn.seu.herald_android.custom.refreshrecyclerview.RefreshRecyclerView;
import cn.seu.herald_android.custom.refreshrecyclerview.onFooterListener;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.ApiThreadManager;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;
import okhttp3.Call;


public class AfterSchoolActivity extends BaseAppCompatActivity {

    //展示活动列表
    RefreshRecyclerView recyclerView;
    //适配器
    AfterSchoolActivityAdapter afterSchoolActivityAdapter;
    //当前展示的页
    int page = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_school);
        init();
        String cache = getCacheHelper().getCache("herald_afterschoolactivity");
        if (!cache.equals("")) {
            loadCache();
        }
        refreshCache();
    }

    private void init(){
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });
        //设置根布局参数
        enableSwipeBack();
        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorAfterSchoolPrimary));
        //活动列表初始化
        recyclerView = (RefreshRecyclerView) findViewById(R.id.recyclerview_afterschoolactivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CustomDividerItemDecoration(
                getBaseContext()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            //点击刷新按钮时进行刷新
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache(){
        try {
            //尝试加载缓存
            String cache = getCacheHelper().getCache("herald_afterschoolschool");
            if (!cache.equals("")) {
                //获取活动列表
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                //数据类型转换
                afterSchoolActivityAdapter = new AfterSchoolActivityAdapter(
                        AfterSchoolActivity.this,
                        AfterSchoolActivityItem.transfromJSONArrayToArrayList(jsonArray));
                //设置消费记录数据适配器
                recyclerView.setAdapter(afterSchoolActivityAdapter);
                recyclerView.setOnFooterListener((footerposition)-> {
                        page += 1;
                        OkHttpUtils
                                .get()
                                .url(ApiHelper.getLiveApiUrl(ApiHelper.API_LIVE_AFTERSCHOOLACTIVITY)+"?page="+page)
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                        getApiHelper().dealApiException(e);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            if(new JSONObject(response).getInt("code") == 200){
                                                JSONArray array = new JSONObject(response).getJSONArray("content");
                                                if(array.length() == 0)
                                                {
                                                    afterSchoolActivityAdapter.setLoadFinished(true);
                                                    showSnackBar("已无更多内容");
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
                                            }else {
                                                showSnackBar("刷新失败");
                                            }
                                        }catch (JSONException e){
                                            showSnackBar("数据解析失败，请重试");
                                        }
                                    }
                                });
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showSnackBar("缓存解析出错，请点击刷新按钮重新获取数据");
        }
    }

    private void refreshCache(){
        showProgressDialog();

        //刷新缓存
        ApiThreadManager manager = new ApiThreadManager();
        page = 1;
        manager.add(new ApiRequest(this)
                .get()
                .url(ApiHelper.getLiveApiUrl(ApiHelper.API_LIVE_AFTERSCHOOLACTIVITY)+"?page="+page)
                .toCache("herald_afterschoolschool", o -> o))
                .onFinish((success) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                        showSnackBar("已获取最新活动");
                    } else {
                        showSnackBar("刷新失败");
                    }
                })
                .runWithGetMethod();

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
                return new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                        now, TimelineItem.NO_CONTENT, "最近没有热门活动");
            } else {
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                        now, TimelineItem.CONTENT_NOTIFY, "最近有" + afterSchoolActivityItems.size() + "个热门活动");
                for (AfterSchoolActivityItem afterSchoolActivityItem : afterSchoolActivityItems) {
                    item.attachedView.add(new AfterSchoolActivityBlockLayout(host.getContext(), afterSchoolActivityItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            new CacheHelper(host.getContext()).setCache("herald_afterschoolschool_hot", "");
            return new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                    now, TimelineItem.NO_CONTENT, "热门活动数据加载失败，请手动刷新"
            );
        }
    }
}

