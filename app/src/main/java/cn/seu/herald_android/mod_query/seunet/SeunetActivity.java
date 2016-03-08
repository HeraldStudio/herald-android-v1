package cn.seu.herald_android.mod_query.seunet;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;
import okhttp3.Call;

public class SeunetActivity extends BaseAppCompatActivity {
    //显示已用流量比例的饼状图
    PieChartView pieChartView_wlan;
    //钱包余额
    TextView tv_leftmoney;
    //已用流量
    TextView tv_used;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seunet);
        init();
    }
    public void init(){
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        //禁用collapsingToolbarLayout的伸缩标题
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        //沉浸式
        setStatusBarColor(this,getResources().getColor(R.color.colorSeuNetprimary));
        //初始化流量显示饼状图
        pieChartView_wlan = (PieChartView)findViewById(R.id.chartwlan);
        //余额显示的tv
        tv_leftmoney = (TextView)findViewById(R.id.tv_extra_money);
        //已用流量
        tv_used = (TextView)findViewById(R.id.tv_used);

        //先尝试加载缓存再刷新
        loadCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }


    public void loadCache(){
        //尝试加载缓存
        String cache = getCacheHelper().getCache("herald_nic");
        if(!cache.equals("")){
            //如果缓存不为空
            try {
                JSONObject json_cache = new JSONObject(cache);
                //设置余额显示
                String leftmoney = json_cache.getJSONObject("content").getString("left");
                tv_leftmoney.setText(leftmoney);

                //设置已用流量显示
                String used = json_cache.getJSONObject("content").getJSONObject("web").getString("used");
                if(used.equals("暂无流量信息")){
                    //说明已欠费或者还未使用
                    used = "0.00 B";
                }
                tv_used.setText(used);
                //设置统计饼状图
                setupChart(json_cache.getJSONObject("content"));
            }catch (JSONException e){
                e.printStackTrace();
                showMsg("缓存解析错误，请重新刷新后再试");
            }
        }
    }

    public void refreshCache(){
        getProgressDialog().show();
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_NIC))
                .addParams("uuid", getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                             @Override
                             public void onError(Call call, Exception e) {
                                //如果加载错误，显示错误信息
                                 showMsg("网络错误，请稍后再试");
                             }

                             @Override
                             public void onResponse(String response) {
                                 getProgressDialog().dismiss();
                                 try{
                                     JSONObject json_res = new JSONObject(response);
                                     if(json_res.getInt("code")==200) {
                                         getCacheHelper().setCache("herald_nic",response);
                                         showMsg("刷新成功");
                                         loadCache();
                                     }
                                 }catch (JSONException e){
                                     e.printStackTrace();
                                     showMsg("数据解析失败");
                                 }
                             }
                         }
                );
    }
    public void setupChart(JSONObject json){
        if(json == null)
            return;
        try{
            //seu-wlan
            if(json.getJSONObject("web").getString("state").equals("未开通")) {
                List<SliceValue> values = new ArrayList<SliceValue>();
                //设置饼状图总的值为1，即不分块，因为未开通
                SliceValue sliceValue = new SliceValue(1f);
                sliceValue.setLabel("未开通");
                values.add(new SliceValue(1f));
                PieChartData pieChartData = new PieChartData(values);
                pieChartView_wlan.setPieChartData(pieChartData);
            }else{
                String str_use = json.getJSONObject("web").getString("used");
                setUsedPercentage(str_use, pieChartView_wlan);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
    }


    private void setUsedPercentage(String used_str,PieChartView pieChartView){
        //根据获取的已用流量加载下面的饼状图，展示已用百分比
        float used = Float.parseFloat(used_str.split(" ")[0]);
        String unit = used_str.split(" ")[1];
        if(unit.equals("B"))used = 0f;
        float used_per=0;
        if(unit.equals("KB"))used_per = used/(1024f * 5f *1024f);
        if(unit.equals("MB"))used_per = used/(1024f * 5f );
        if(unit.equals("GB")){
            used_per = used/5f;
        }
        if (used_per < 0.1f)used_per=0.01f;
        List<SliceValue> values = new ArrayList<SliceValue>();
        SliceValue sliceValue_used = new SliceValue(used_per);
        SliceValue sliceValue_left = new SliceValue(1f-used_per);
        //设置已用的流量部分的颜色
        sliceValue_used.setLabel(used_per * 100 + "%");
        sliceValue_used.setColor(getResources().getColor(R.color.colorSeuNetprimary));
        //设置未使用的流量部分的颜色
        sliceValue_left.setLabel((1f - used_per) * 100 + "%");
        sliceValue_left.setTarget(used_per * 100);
        sliceValue_left.setColor(getResources().getColor(R.color.colorSeuNetaccent));
        values.add(sliceValue_used);
        values.add(sliceValue_left);
        PieChartData pieChartData = new PieChartData(values);
        pieChartData.setHasLabels(true);
        pieChartView.setPieChartData(pieChartData);
    }
}
