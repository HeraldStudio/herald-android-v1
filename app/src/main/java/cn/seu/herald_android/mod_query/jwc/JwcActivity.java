package cn.seu.herald_android.mod_query.jwc;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import okhttp3.Call;

public class JwcActivity extends BaseAppCompatActivity {

    //教务通知类型列表
    private ExpandableListView expandableListView;

    public static void remoteRefreshCache(Context context, Runnable doAfter) {
        ApiHelper apiHelper = new ApiHelper(context);
        CacheHelper cacheHelper = new CacheHelper(context);
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_JWC))
                .addParams("uuid", apiHelper.getUUID())
                .build()
                .readTimeOut(10000).connTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        apiHelper.dealApiExceptionSilently(e);
                        doAfter.run();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                cacheHelper.setCache("herald_jwc", response);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        doAfter.run();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwc);
        init();
        loadCache();
    }

    private void init() {
        //toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorJwcprimary));
        enableSwipeBack();

        //教务通知类型列表加载
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache() {
        //如果缓存不为空则加载缓存，反之刷新缓存
        String cache = getCacheHelper().getCache("herald_jwc");
        if (!cache.equals("")) {
            try {
                JSONObject json_content = new JSONObject(cache).getJSONObject("content");
                //父view和子view数据集合
                ArrayList<String> parentArray = new ArrayList<>();
                ArrayList<ArrayList<JwcItem>> childArray = new ArrayList<>();
                //根据每种集合加载不同的子view
                for (int i = 0; i < json_content.length(); i++) {
                    // 跳过最新动态
                    if (json_content.names().getString(i).equals("最新动态")) continue;

                    String jsonArray_str = json_content.getString(json_content.names().getString(i));
                    if (!jsonArray_str.equals("")) {
                        //如果有教务通知则加载数据和子项布局
                        JSONArray jsonArray = new JSONArray(jsonArray_str);
                        //根据数组长度获得教务通知的Item集合
                        ArrayList<JwcItem> item_list = JwcItem.transformJSONArrayToArrayList(jsonArray);
                        //加入到list中
                        parentArray.add(json_content.names().getString(i));
                        childArray.add(item_list);
                    }
                }
                //设置伸缩列表
                JwcExpandAdapter jwcExpandAdapter = new JwcExpandAdapter(getBaseContext(), parentArray, childArray);
                expandableListView.setAdapter(jwcExpandAdapter);

                if (jwcExpandAdapter.getGroupCount() > 0)
                    expandableListView.expandGroup(0);

            } catch (JSONException e) {
                showMsg("缓存解析失败，请刷新后再试");
                e.printStackTrace();
            }
        } else {
            refreshCache();
        }
    }

    private void refreshCache() {
        showProgressDialog();
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_JWC))
                .addParams("uuid", getApiHelper().getUUID())
                .build()
                .readTimeOut(10000).connTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getApiHelper().dealApiException(e);
                        hideProgressDialog();
                    }

                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                getCacheHelper().setCache("herald_jwc", response);
                                loadCache();
                                showMsg("刷新成功");
                            } else {
                                showMsg("服务器遇到了一些问题，不妨稍后再试试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("数据解析失败，请重试");
                        }
                    }
                });
    }
}
