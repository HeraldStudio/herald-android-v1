package cn.seu.herald_android.mod_query.gymreserve;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

public class GymReserveActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gymreserve);
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

        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorGymReserveprimary));
        enableSwipeBack();
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


    private void refreshCache() {

        // 先显示刷新控件
        showProgressDialog();

        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_GYMRESERVE))
                .addParams("uuid", getApiHelper().getUUID())
                .addParams("method", "getDate")
                .build()
                .readTimeOut(10000).connTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        hideProgressDialog();
                        getApiHelper().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                JSONArray array = json_res.getJSONObject("content").getJSONArray("timeList");

                                getCacheHelper().setCache("herald_gymreserve_timelist", array.toString());
                                refreshCacheStep2();
                            } else {
                                hideProgressDialog();
                                showMsg("数据解析失败，请重试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideProgressDialog();
                            showMsg("数据解析失败，请重试");
                        }
                    }
                });
    }

    private void refreshCacheStep2() {
        try {
            JSONArray array = new JSONArray(getCacheHelper().getCache("herald_gymreserve_timelist"));
            Vector<Object> threads = new Vector<>();

            // 枚举所有可预约日期
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);

                // 枚举所有运动项目
                for (int j = 7; j <= 14; j++) {
                    threads.add(new Object());
                    OkHttpUtils
                            .post()
                            .url(ApiHelper.getApiUrl(ApiHelper.API_GYMRESERVE))
                            .addParams("uuid", getApiHelper().getUUID())
                            .addParams("method", "getOrder")
                            .addParams("itemId", String.valueOf(j))
                            .addParams("dayInfo", object.getString("dayInfo"))
                            .build()
                            .readTimeOut(10000).connTimeOut(10000)
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                    hideProgressDialog();
                                    getApiHelper().dealApiException(e);
                                }

                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject json_res = new JSONObject(response);
                                        if (json_res.getInt("code") == 200) {
                                            JSONArray array = json_res.getJSONObject("content").getJSONArray("timeList");

                                            getCacheHelper().setCache("herald_gymreserve_timelist", array.toString());
                                            refreshCacheStep2();
                                        } else {
                                            hideProgressDialog();
                                            showMsg("数据解析失败，请重试");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        hideProgressDialog();
                                        showMsg("数据解析失败，请重试");
                                    }
                                }
                            });
                }
            }
        } catch (JSONException e) {
            hideProgressDialog();
            showMsg("数据解析失败，请重试");
        }
    }
}
