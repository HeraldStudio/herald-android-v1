package cn.seu.herald_android.mod_query.cardextra;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.ApiThreadManager;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;

public class CardActivity extends BaseAppCompatActivity {


    //消费记录详情列表
    private RecyclerView recyclerViewCard;
    //余额
    private TextView tv_extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        init();

        String cache = getCacheHelper().getCache("herald_card");
        if (!cache.equals("")) {
            loadCache();
        }
        refreshCache();
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

        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorCardprimary));
        enableSwipeBack();
        //禁用collapsingToolbarLayout的伸缩标题
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

        //RecyclerView初始化
        recyclerViewCard = (RecyclerView) findViewById(R.id.recyclerview_card);
        //设置布局
        recyclerViewCard.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCard.setHasFixedSize(true);
        tv_extra = (TextView) findViewById(R.id.tv_extra);
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
            //点击刷新按钮时进行刷新
            refreshCache();
        }else if(id == R.id.action_card_chongzhi) {
            new AlertDialog.Builder(this)
                    .setMessage("注意：充值之后需要在食堂刷卡机上刷卡，充值金额才能到账哦")
                    .setPositiveButton("确定", (d, w) -> {
                        Uri uri = Uri.parse("http://58.192.115.47:8088/wechat-web/login/initlogin.html");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache() {
        try {
            //尝试加载缓存
            String cache = getCacheHelper().getCache("herald_card");
            String todayCache = getCacheHelper().getCache("herald_card_today");
            ArrayList<CardItem> list = new ArrayList<>();

            if (!todayCache.equals("")) {
                JSONObject json_cache = new JSONObject(todayCache).getJSONObject("content");
                //获取消费记录
                JSONArray jsonArray = json_cache.getJSONArray("detial");
                //获取余额并且设置
                String extra = new JSONObject(todayCache).getJSONObject("content").getString("left");
                tv_extra.setText(extra);
                //数据类型转换
                list.addAll(CardItem.transfromJSONArrayToArrayList(jsonArray));
            }

            if (!cache.equals("")) {
                JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
                //获取消费记录
                JSONArray jsonArray = json_cache.getJSONArray("detial");
                //数据类型转换
                list.addAll(CardItem.transfromJSONArrayToArrayList(jsonArray));
            }

            CardAdapter cardAdapter = new CardAdapter(getBaseContext(), list);
            //设置消费记录数据适配器
            recyclerViewCard.setAdapter(cardAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
            showSnackBar("解析失败，请刷新");
        }
    }

    private void refreshCache() {
        showProgressDialog();

        // 先加入刷新余额的请求
        ApiThreadManager manager = new ApiThreadManager();
        manager.add(new ApiRequest(this).api(ApiHelper.API_CARD).addUUID().post("timedelta", "1")
                .toCache("herald_card_today", o -> o));

        // 如果今天还没刷新过,加入刷新流水的请求
        if (!todayHasRefreshed()) {
            manager.add(new ApiRequest(this).api(ApiHelper.API_CARD).addUUID()
                    .post("timedelta", "31").toCache("herald_card", o -> o));
        }

        // 刷新完毕后登记刷新日期
        manager.onFinish((success) -> {
            hideProgressDialog();
            if (success) {
                loadCache();
                // showSnackBar("刷新成功");
                getCacheHelper().setCache("herald_card_date", getDayStamp());
            } else {
                showSnackBar("刷新失败，请重试或到充值页面查询");
            }
        }).runWithPostMethod();
    }

    public String getDayStamp() {
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }

    public boolean todayHasRefreshed() {
        return getCacheHelper().getCache("herald_card_date").equals(getDayStamp());
    }

    public static ApiRequest remoteRefreshCache(Context context) {
        return new ApiRequest(context).api(ApiHelper.API_CARD).addUUID().post("timedelta", "1")
                .toCache("herald_card_today", o -> o);
    }

    /**
     * 读取一卡通缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getCardItem(TimelineView host) {
        CacheHelper helper = new CacheHelper(host.getContext());
        String cache = helper.getCache("herald_card_today");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
            //获取余额并且设置
            String left = json_cache.getString("left").replaceAll(",", "");
            float extra = Float.valueOf(left);

            if (extra < 20) {
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CARDEXTRA,
                        now, TimelineItem.CONTENT_NOTIFY, "一卡通余额还有" + left + "元，快点我充值~\n如果已经充值过了，需要在食堂刷卡一次才会更新哦~"
                );
                item.setOnClickListener(v -> {
                    Uri uri = Uri.parse("http://58.192.115.47:8088/wechat-web/login/initlogin.html");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    host.getContext().startActivity(intent);
                });
                return item;
            } else {
                return new TimelineItem(SettingsHelper.MODULE_CARDEXTRA,
                        now, TimelineItem.CONTENT_NO_NOTIFY, "你的一卡通余额还有" + left + "元"
                );
            }
        } catch (Exception e) {
            return new TimelineItem(SettingsHelper.MODULE_CARDEXTRA,
                    now, TimelineItem.CONTENT_NOTIFY, "一卡通数据为空，请尝试刷新"
            );
        }
    }
}
