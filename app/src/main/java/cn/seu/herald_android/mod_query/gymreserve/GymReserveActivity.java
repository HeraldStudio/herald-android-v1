package cn.seu.herald_android.mod_query.gymreserve;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

public class GymReserveActivity extends BaseAppCompatActivity {


    GridView gridView;
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
        //列表初始化
        gridView = (GridView)findViewById(R.id.gridview_reserve);

        //启用刷新
        refreshItemListAndTimeList();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
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
            refreshItemListAndTimeList();
        }
        return super.onOptionsItemSelected(item);
    }


    private void refreshItemListAndTimeList() {
        showProgressDialog();
        //获取项目和时间列表
        new ApiRequest(this).api(ApiHelper.API_GYMRESERVE)
                .addUUID()
                .post("method", "getDate")
                .toCache("herald_gymreserve_timelist_and_itemlist", o -> o)
                .onFinish((success, code, response) -> {
                    if (success) {
                        loadItemList();
                    } else {
                        showSnackBar("获取可预约活动列表失败，请检查网络");
                        hideProgressDialog();
                    }
                }).run();
        //如果用户手机号为空时，同时预获取用户手机号
        if( getCacheHelper().getCache("herald_gymreserve_phone").equals("") || getCacheHelper().getCache("herald_gymreserve_phone").length() > 15){
            new ApiRequest(this).api(ApiHelper.API_GYMRESERVE)
                    .addUUID().post("method", "getPhone")
                    .toCache("herald_gymreserve_phone", o -> o.getJSONObject("content").getString("phone"))
                    .run();
        }
        //如果用户自己的信息未完善，则同时预查询自己的ID
        if( getCacheHelper().getCache("herald_gymreserve_userid").equals("")){
            new ApiRequest(this).api(ApiHelper.API_GYMRESERVE)
                    .addUUID()
                    .post("method", "getFriendList")
                    .post("cardNo",getApiHelper().getAuthCache("cardnum"))
                    .toCache("herald_gymreserve_userid", o -> o.getJSONArray("content").getJSONObject(0).getString("userId"))
                    .run();
        }
    }

    private void loadItemList() {
        try {
            JSONArray itemArray = new JSONObject(getCacheHelper().getCache("herald_gymreserve_timelist_and_itemlist")).getJSONObject("content").getJSONArray("itemList");
            //活动项目列表
            ArrayList<GymReserveItem> list = GymReserveItem.transformJSONtoArrayList(itemArray);
            //获取日期列表
            JSONArray timeArray =  new JSONObject(getCacheHelper().getCache("herald_gymreserve_timelist_and_itemlist")).getJSONObject("content").getJSONArray("timeList");
            String[] dayInfos = GymReserveItem.transformJSONtoStringArray(timeArray);
            //设置适配器
            gridView.setAdapter(new GymReserveItemAdapter(getBaseContext(),R.layout.gridviewitem_gym_reserveitem,list));
            //设置点击函数
            gridView.setOnItemClickListener((parent, view, position, id) -> {
                GymReserveItem item = (GymReserveItem) parent.getItemAtPosition(position);
                //打开相对应的预约界面
                OrderItemActivity.startOrderItemActivity(GymReserveActivity.this,item,dayInfos);
            });
            hideProgressDialog();
        } catch (JSONException e) {
            hideProgressDialog();
            showSnackBar("数据解析失败，请重试");
        }
    }
}
