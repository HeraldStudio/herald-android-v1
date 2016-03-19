package cn.seu.herald_android.mod_query.seunet;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;
import okhttp3.Call;

public class SeunetActivity extends BaseAppCompatActivity {
    //显示已用流量比例的饼状图
    private PieChartView pieChartView_wlan;
    //钱包余额
    private TextView tv_money_left;
    //已用流量
    private TextView tv_used;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seunet);
        init();
    }

    private void init() {
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //禁用collapsingToolbarLayout的伸缩标题
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorSeuNetprimary));
        //初始化流量显示饼状图
        pieChartView_wlan = (PieChartView) findViewById(R.id.chartwlan);
        //设置饼图不旋转
        pieChartView_wlan.setChartRotationEnabled(false);
        //余额显示的tv
        tv_money_left = (TextView) findViewById(R.id.tv_extra_money);
        //已用流量
        tv_used = (TextView) findViewById(R.id.tv_used);

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


    private void loadCache() {
        //尝试加载缓存
        String cache = getCacheHelper().getCache("herald_nic");
        if (!cache.equals("")) {
            //如果缓存不为空
            try {
                JSONObject json_cache = new JSONObject(cache);
                //设置余额显示
                String leftmoney = json_cache.getJSONObject("content").getString("left");
                tv_money_left.setText(leftmoney);

                //设置已用流量显示
                String used = json_cache.getJSONObject("content").getJSONObject("web").getString("used");
                if (used.equals("暂无流量信息")) {
                    //说明已欠费或者还未使用
                    used = "0.00 B";
                }
                tv_used.setText(used);
                //设置统计饼状图
                setupChart(json_cache.getJSONObject("content"));
            } catch (JSONException e) {
                e.printStackTrace();
                showMsg("缓存解析错误，请重新刷新后再试");
            }
        } else {
            List<SliceValue> values = new ArrayList<>();
            //暂时用一个完整的饼代替默认图
            SliceValue sliceValue = new SliceValue(1f);
            sliceValue.setColor(Color.rgb(220, 220, 220));
            values.add(sliceValue);
            PieChartData pieChartData = new PieChartData(values);
            //为控件设置数据
            pieChartView_wlan.setPieChartData(pieChartData);

            refreshCache();
        }
    }

    private void refreshCache() {
        showProgressDialog();
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_NIC))
                .addParams("uuid", getApiHelper().getUUID())
                .build()
                .readTimeOut(10000).connTimeOut(10000)
                .execute(new StringCallback() {
                             @Override
                             public void onError(Call call, Exception e) {
                                 //如果加载错误，显示错误信息
                                 showMsg("网络错误，请稍后再试");
                             }

                             @Override
                             public void onResponse(String response) {
                                 hideProgressDialog();
                                 try {
                                     JSONObject json_res = new JSONObject(response);
                                     if (json_res.getInt("code") == 200) {
                                         getCacheHelper().setCache("herald_nic", response);
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
                         }
                );
    }

    private void setupChart(JSONObject json) {
        if (json == null)
            return;
        try {
            //seu-wlan
            if (json.getJSONObject("web").getString("state").equals("未开通")) {
                List<SliceValue> values = new ArrayList<>();
                //设置饼状图总的值为1，即不分块，因为未开通
                SliceValue sliceValue = new SliceValue(1f);
                sliceValue.setLabel("未开通");
                sliceValue.setColor(Color.rgb(220, 220, 220));
                values.add(sliceValue);
                PieChartData pieChartData = new PieChartData(values);
                //为控件设置数据
                pieChartView_wlan.setPieChartData(pieChartData);
            } else {
                String str_use = json.getJSONObject("web").getString("used");
                setUsedPercentage(str_use, pieChartView_wlan);
            }

        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }
    }


    private void setUsedPercentage(String used_str, PieChartView pieChartView) {
        //根据获取的已用流量加载下面的饼状图，展示已用百分比
        double used = Double.parseDouble(used_str.split(" ")[0]);
        String unit = used_str.split(" ")[1];
        if (unit.equals("B")) used = 0d;
        double used_per = 0;
        //TODO 总流量不要写死
        if (unit.equals("KB")) used_per = used / (1024d * 5d * 1024d);
        if (unit.equals("MB")) used_per = used / (1024d * 5d);
        if (unit.equals("GB")) {
            double total = 10d;
            used_per = used / total;
            while (used_per > 1d){
                total += 5d;
                used_per = used / total;
            }
        }
        if (used_per < 0.1d) used_per = 0.01d;
        List<SliceValue> values = new ArrayList<>();
        SliceValue sliceValue_used = new SliceValue((float)used_per);
        SliceValue sliceValue_left = new SliceValue((float)(1d - used_per));
        DecimalFormat df = new DecimalFormat( "#.## ");
        //设置已用的流量部分的颜色
        sliceValue_used.setLabel(df.format(used_per * 100) + "%");
        sliceValue_used.setColor(ContextCompat.getColor(this, R.color.colorSeuNetaccent));
        //设置未使用的流量部分的颜色
        sliceValue_left.setLabel(df.format((1f - used_per) * 100) + "%");
        sliceValue_left.setTarget((float)used_per * 100);
        sliceValue_left.setColor(ContextCompat.getColor(this, R.color.colorSeuNetprimary));
        values.add(sliceValue_used);
        values.add(sliceValue_left);
        PieChartData pieChartData = new PieChartData(values);
        pieChartData.setHasLabels(true);
        pieChartView.setPieChartData(pieChartData);
    }
}
