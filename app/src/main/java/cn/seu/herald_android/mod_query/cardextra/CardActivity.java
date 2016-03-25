package cn.seu.herald_android.mod_query.cardextra;

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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

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
        } else {
            refreshCache();
        }
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
            Uri uri = Uri.parse("http://58.192.115.47:8088/wechat-web/login/initlogin.html");
            Intent intent = new  Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache() {
        try {
            //尝试加载缓存
            String cache = getCacheHelper().getCache("herald_card");
            if (!cache.equals("")) {
                JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
                //获取消费记录
                JSONArray jsonArray = json_cache.getJSONArray("detial");
                //获取余额并且设置
                String extra = json_cache.getString("left");
                tv_extra.setText(extra);
                //数据类型转换
                CardAdapter cardAdapter = new CardAdapter(getBaseContext(), CardItem.transfromJSONArrayToArrayList(jsonArray));
                //设置消费记录数据适配器
                recyclerViewCard.setAdapter(cardAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showMsg("缓存解析出错，请点击刷新按钮重新获取数据");
        }
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest(this).api(ApiHelper.API_CARD).uuid()
                .post("timedelta", "31").toCache("herald_card", o -> o)
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                        showMsg("刷新成功");
                    }
                }).run();
    }

    public static ApiRequest remoteRefreshCache(Context context) {
        return new ApiRequest(context).api(ApiHelper.API_CARD).uuid()
                .post("timedelta", "31").toCache("herald_card", o -> o);
    }

}
