package cn.seu.herald_android.mod_query.seunet;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class SeunetActivity extends BaseActivity {

    @BindView(R.id.chartwlan)
    PieChartView pieChartView_wlan;
    @BindView(R.id.tv_extra_money)
    TextView tv_money_left;
    @BindView(R.id.tv_used)
    TextView tv_used;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_seunet);
        ButterKnife.bind(this);

        pieChartView_wlan.setChartRotationEnabled(false);

        //先尝试加载缓存再刷新
        loadCache();
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
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadCache() {
        //尝试加载缓存
        String cache = CacheHelper.get("herald_nic");
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
                setupChart(json_cache.getJSONObject("content"),pieChartView_wlan);
            } catch (JSONException e) {
                e.printStackTrace();
                showSnackBar("解析失败，请刷新");
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
        new ApiRequest().api("nic").addUUID()
                .toCache("herald_nic", o -> o)
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                        // showSnackBar("刷新成功");
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    public static void setupChart(JSONObject json, PieChartView pieChartView) {
        if (json == null)
            return;
        try {
            //seu-wlan
            if (json.getJSONObject("web").getString("state").equals("未开通")) {
                setEmptyPie(pieChartView);
            } else {
                String str_use = json.getJSONObject("web").getString("used");
                setUsedPercentage(str_use, pieChartView);
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
            setEmptyPie(pieChartView);
        }
    }

    public static void setEmptyPie(PieChartView pieChartView) {
        List<SliceValue> values = new ArrayList<>();
        SliceValue sliceValue = new SliceValue(1f);
        sliceValue.setColor(Color.rgb(220, 220, 220));
        values.add(sliceValue);
        PieChartData pieChartData = new PieChartData(values);
        //为控件设置数据
        pieChartView.setPieChartData(pieChartData);
    }

    public static void setUsedPercentage(String used_str, PieChartView pieChartView) {
        //根据获取的已用流量加载下面的饼状图，展示已用百分比
        double used = Double.parseDouble(used_str.split(" ")[0]);
        String unit = used_str.split(" ")[1];
        if (unit.equals("B")) used = 0d;
        double used_per = 0;

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
        sliceValue_used.setColor(ContextCompat.getColor(pieChartView.getContext(), R.color.colorSeuNetaccent));
        //设置未使用的流量部分的颜色
        sliceValue_left.setLabel(df.format((1f - used_per) * 100) + "%");
        sliceValue_left.setTarget((float)used_per * 100);
        sliceValue_left.setColor(ContextCompat.getColor(pieChartView.getContext(), R.color.colorSeuNetprimary));
        values.add(sliceValue_used);
        values.add(sliceValue_left);
        PieChartData pieChartData = new PieChartData(values);
        pieChartData.setHasLabels(true);
        pieChartView.setPieChartData(pieChartData);
    }
}
