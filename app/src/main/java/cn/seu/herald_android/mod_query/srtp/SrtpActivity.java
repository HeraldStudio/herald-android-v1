package cn.seu.herald_android.mod_query.srtp;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;

public class SrtpActivity extends BaseActivity {

    private RecyclerView recyclerView_srtp;
    private TextView tv_total_credit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_srtp);
        init();
        loadCache();
    }

    private void init() {
        setupToolBar();
        //回收列表
        recyclerView_srtp = (RecyclerView) findViewById(R.id.recyclerview_srtp);
        //总学分
        tv_total_credit = (TextView) findViewById(R.id.tv_totalcredit);
    }

    private void setupToolBar() {
        //设置toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
                finish();
            });
        }


        //设置伸缩标题禁用
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitleEnabled(false);
        }
        //适配4.4的沉浸式
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorSrtpprimary));
        enableSwipeBack();
    }

    private void loadCache() {
        String cache = CacheHelper.get("herald_srtp");
        if (!cache.equals("")) {
            try {
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                //获得总学分
                String total = jsonArray.getJSONObject(0).getString("total");
                tv_total_credit.setText(total);
                //加载列表
                ArrayList<SrtpItem> arrayList = SrtpItem.transformJSONArrayToArrayList(jsonArray);
                //适配器
                SrtpAdapter srtpAdapter = new SrtpAdapter(this, arrayList);
                recyclerView_srtp.setLayoutManager(new LinearLayoutManager(this));
                recyclerView_srtp.setAdapter(srtpAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
                showSnackBar("解析失败，请刷新");
            }
        } else {
            refreshCache();
        }
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

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest().api("srtp").addUUID()
                .post("schoolnum", ApiHelper.getSchoolnum())
                .toCache("herald_srtp", o -> {
                    if (o.getJSONArray("content").length() == 1) {
                        showSnackBar("你还没有参加课外研学项目");
                    }
                    return o;
                })
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }
}
