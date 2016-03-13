package cn.seu.herald_android.mod_query.srtp;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

public class SrtpActivity extends BaseAppCompatActivity {

    RecyclerView recyclerView_srtp;
    TextView tv_totalcredit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srtp);
        init();
        loadCache();
    }

    public void init(){
        setupToolBar();
        //回收列表
        recyclerView_srtp = (RecyclerView)findViewById(R.id.recyclerview_srtp);
        //总学分
        tv_totalcredit = (TextView)findViewById(R.id.tv_totalcredit);
    }

    public void setupToolBar(){
        //设置toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });


        //设置伸缩标题禁用
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitleEnabled(false);
        //适配4.4的沉浸式
        setStatusBarColor(this, getResources().getColor(R.color.colorSrtpprimary));
    }

    public void loadCache(){
        String cache  = getCacheHelper().getCache("herald_srtp");
        if(!cache.equals("")){
            try{
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                //获得总学分
                String total = jsonArray.getJSONObject(0).getString("total");
                tv_totalcredit.setText(total);
                //加载列表
                ArrayList<SrtpItem> arrayList = SrtpItem.transfromJSONArrayToArrayList(jsonArray);
                //适配器
                SrtpAdapter srtpAdapter = new SrtpAdapter(this,arrayList);
                recyclerView_srtp.setLayoutManager(new LinearLayoutManager(this));
                recyclerView_srtp.setAdapter(srtpAdapter);
            }catch (JSONException e){
                e.printStackTrace();
                showMsg("缓存解析失败，请刷新后再试");
            }
        }else{
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
        if(id == R.id.action_sync){
            //点击刷新按钮时进行刷新
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshCache(){
        getProgressDialog().show();
        //获取srtp记录
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_SRTP))
                .addParams("uuid", getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getApiHepler().dealApiException(e);
                        getProgressDialog().dismiss();
                        showMsg("由于网络错误获取课外研学记录失败，已加载缓存。");
                        loadCache();
                    }

                    @Override
                    public void onResponse(String response) {
                        getProgressDialog().dismiss();
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200){
                                getCacheHelper().setCache("herald_srtp", json_res.toString());
                                if(json_res.getJSONArray("content").length() == 1) {
                                    showMsg("暂无Srtp信息，赶紧去参加课外研学活动吧");
                                } else {
                                    showMsg("已获取最新srtp信息");
                                }
                                loadCache();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("数据解析出错");
                        }
                    }
                });
    }
}
