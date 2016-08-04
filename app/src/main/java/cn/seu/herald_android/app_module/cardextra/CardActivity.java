package cn.seu.herald_android.app_module.cardextra;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.CacheHelper;

public class CardActivity extends BaseActivity {

    public static final String chargeUrl = "http://58.192.115.47:8088/wechat-web/login/initlogin.html";

    @BindView(R.id.recyclerview_card)
    RecyclerView recyclerViewCard;

    @BindView(R.id.tv_extra)
    TextView tv_extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_card);
        ButterKnife.bind(this);

        // 设置布局
        recyclerViewCard.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCard.setHasFixedSize(true);

        String cache = CacheHelper.get("herald_card");
        if (!cache.equals("")) {
            loadCache();
        }
        refreshCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_card_sync) {
            // 点击刷新按钮时进行刷新
            refreshCache();
        } else if (id == R.id.action_card_chongzhi) {
            new AlertDialog.Builder(this)
                    .setMessage("注意：充值之后需要在食堂刷卡机上刷卡，充值金额才能到账哦")
                    .setPositiveButton("确定", (d, w) -> {
                        new AppModule("一卡通充值", chargeUrl).open();
                    }).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache() {
        try {
            // 尝试加载缓存
            String cache = CacheHelper.get("herald_card");
            String todayCache = CacheHelper.get("herald_card_today");
            ArrayList<CardRecordModel> list = new ArrayList<>();

            if (!todayCache.equals("")) {
                JSONObject json_cache = new JSONObject(todayCache).getJSONObject("content");
                // 获取消费记录
                JSONArray jsonArray = json_cache.getJSONArray("detial");
                // 获取余额并且设置
                String extra = new JSONObject(todayCache).getJSONObject("content").getString("left");
                tv_extra.setText(extra);
                // 数据类型转换
                list.addAll(CardRecordModel.transformJSONArrayToArrayList(jsonArray));
            }

            if (!cache.equals("")) {
                JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
                // 获取消费记录
                JSONArray jsonArray = json_cache.getJSONArray("detial");
                // 数据类型转换
                list.addAll(CardRecordModel.transformJSONArrayToArrayList(jsonArray));
            }

            CardAdapter cardAdapter = new CardAdapter(getBaseContext(), list);
            // 设置消费记录数据适配器
            recyclerViewCard.setAdapter(cardAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
            showSnackBar("解析失败，请刷新");
        }
    }

    private void refreshCache() {
        showProgressDialog();

        // 先加入刷新余额的请求
        ApiRequest request = new ApiSimpleRequest(Method.POST)
                .api("card").addUuid().post("timedelta", "1")
                .toCache("herald_card_today");

        // 如果今天还没刷新过, 加入刷新流水的请求
        if (!todayHasRefreshed()) {
            request = request.parallel(new ApiSimpleRequest(Method.POST)
                    .api("card").addUuid().post("timedelta", "31")
                    .toCache("herald_card"));
        }

        // 刷新完毕后登记刷新日期
        request.onFinish((success, code) -> {
            hideProgressDialog();
            loadCache();
            if (success) {
                // showSnackBar("刷新成功");
                CacheHelper.set("herald_card_date", getDayStamp());
            } else {
                showSnackBar("刷新失败，请重试或到充值页面查询");
            }
        }).run();
    }

    public String getDayStamp() {
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }

    public boolean todayHasRefreshed() {
        return CacheHelper.get("herald_card_date").equals(getDayStamp());
    }
}
