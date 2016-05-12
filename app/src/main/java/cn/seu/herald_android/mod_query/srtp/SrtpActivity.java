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
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

public class SrtpActivity extends BaseAppCompatActivity {

    private RecyclerView recyclerView_srtp;
    private TextView tv_total_credit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srtp);
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
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });


        //设置伸缩标题禁用
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitleEnabled(false);
        //适配4.4的沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorSrtpprimary));
        enableSwipeBack();
    }

    private void loadCache() {
        String cache = getCacheHelper().getCache("herald_srtp");
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
                showSnackBar("缓存解析失败，请刷新后再试");
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
        new ApiRequest(this).api(ApiHelper.API_SRTP).addUUID()
                .post("schoolnum", getApiHelper().getSchoolnum())
                .toCache("herald_srtp", o -> {
                    if (o.getJSONArray("content").length() == 1) {
                        showSnackBar("暂无Srtp信息，赶紧去参加课外研学活动吧");
                    } else {
                        showSnackBar("已获取最新srtp信息");
                    }
                    return o;
                })
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) loadCache();
                }).run();
    }
}
