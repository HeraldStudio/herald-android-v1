package cn.seu.herald_android.mod_query.gymreserve;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.ApiThreadManager;

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
        showProgressDialog();

        new ApiRequest(this).api(ApiHelper.API_GYMRESERVE).uuid().post("method", "getDate")
                .toCache("herald_gymreserve_timelist", o -> o.getJSONObject("content").getJSONArray("timeList"))
                .onFinish((success, code, response) -> {
                    if (success) {
                        refreshCacheStep2();
                    } else {
                        hideProgressDialog();
                    }
                }).run();
    }

    private void refreshCacheStep2() {
        try {
            JSONArray array = new JSONArray(getCacheHelper().getCache("herald_gymreserve_timelist"));
            ApiThreadManager manager = new ApiThreadManager();

            // 枚举所有可预约日期
            for (int i = 0; i < array.length(); i++) {
                JSONObject timeListObject = array.getJSONObject(i);
                Hashtable<Integer, JSONArray> listOfSports = new Hashtable<>();

                // 枚举所有运动项目
                for (int j = 7; j <= 14; j++) {
                    final int sportNum = j;
                    manager.add(
                            new ApiRequest(this).api(ApiHelper.API_GYMRESERVE).uuid().post("method", "getOrder")
                                    .post("itemId", String.valueOf(j), "dayInfo", timeListObject.getString("dayInfo"))
                                    .onFinish((success, code, response) -> {
                                        if (success) try {
                                            JSONArray orderIndexs = new JSONObject(response)
                                                    .getJSONObject("content").getJSONArray("orderIndexs");
                                            listOfSports.put(sportNum, orderIndexs);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            new ApiHelper(this).dealApiException(e);
                                        }
                                    })
                    );
                }
            }
        } catch (JSONException e) {
            hideProgressDialog();
            showSnackBar("数据解析失败，请重试");
        }
    }
}
