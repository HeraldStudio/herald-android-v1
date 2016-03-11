package cn.seu.herald_android.mod_query.cardextra;

import android.app.ProgressDialog;
import android.content.Context;
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

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.mod_query.grade.GradeItem;
import cn.seu.herald_android.mod_query.grade.GradeItemDataAdapter;
import okhttp3.Call;

public class CardActivity extends BaseAppCompatActivity {


    //消费记录详情列表
    RecyclerView recyclerViewCard;
    //余额
    TextView tv_extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        init();
        loadCache();
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

        //沉浸式
        setStatusBarColor(this,getResources().getColor(R.color.colorCardprimary));
        //禁用collapsingToolbarLayout的伸缩标题
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

        //recyclerview初始化
        recyclerViewCard = (RecyclerView)findViewById(R.id.recyclerview_card);
        //设置布局
        recyclerViewCard.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCard.setHasFixedSize(true);
        tv_extra = (TextView)findViewById(R.id.tv_extra);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_card_sync){
            //点击刷新按钮时进行刷新
            refreshCache();
        }else if(id == R.id.action_card_settings){

        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache(){
        try {
            //尝试加载缓存
            String cache = getCacheHelper().getCache("herald_card");
            if(!cache.equals("")){
                JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
                //获取消费记录
                JSONArray jsonArray = json_cache.getJSONArray("detial");
                //获取余额并且设置
                String extra = json_cache.getString("left");
                tv_extra.setText(extra);
                //数据类型转换
                CardAdapter cardAdapter = new CardAdapter(getBaseContext(),CardItem.transfromJSONArrayToArrayList(jsonArray));
                //设置消费记录数据适配器
                recyclerViewCard.setAdapter(cardAdapter);
                showMsg("刷新成功");

            }else{
                refreshCache();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showMsg("缓存解析出错，请点击刷新按钮重新获取数据");
        }
    }

    private void refreshCache(){
        getProgressDialog().show();
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_CARD))
                .addParams("uuid",getApiHepler().getUUID())
                .addParams("timedelta","7")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getProgressDialog().dismiss();
                        getApiHepler().dealApiException(e);
                        loadCache();
                    }

                    @Override
                    public void onResponse(String response) {
                        getProgressDialog().dismiss();
                        try{
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200) {
                                getCacheHelper().setCache("herald_card",response);
                                showMsg("刷新成功");
                                loadCache();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            showMsg("数据解析失败");
                        }

                    }
                });
    }

    public static void remoteRefreshCache(Context context, Runnable onFinish){
        ApiHelper apiHelper = new ApiHelper(context);
        CacheHelper cacheHelper = new CacheHelper(context);
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_CARD))
                .addParams("uuid", apiHelper.getUUID())
                .addParams("timedelta", "7")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        apiHelper.dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200) {
                                cacheHelper.setCache("herald_card",response);
                            }
                            onFinish.run();
                        }catch (JSONException e){
                            e.printStackTrace();
                            onFinish.run();
                        }

                    }
                });
    }
}
