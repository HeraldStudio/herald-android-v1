package cn.seu.herald_android.app_module.gymreserve;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;

public class GymReserveActivity extends BaseActivity {

    public static ApiSimpleRequest remoteRefreshNotifyDotState() {
        return new ApiSimpleRequest(Method.POST).api("yuyue").addUuid().post("method", "myOrder")
                .toCache("herald_gymreserve_myorder",
                        /** notifyModuleIfChanged: */Module.gymreserve);
    }

    @BindView(R.id.listview_reserve)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_gymreserve);
        ButterKnife.bind(this);

        // 启用刷新
        refreshItemListAndTimeList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_record) {
            AppContext.startActivitySafely(GymMyOrderActivity.class);
        } else if (id == R.id.action_sync) {
            refreshItemListAndTimeList();
        }
        return super.onOptionsItemSelected(item);
    }


    private void refreshItemListAndTimeList() {
        showProgressDialog();
        // 获取项目和时间列表
        new ApiSimpleRequest(Method.POST).api("yuyue")
                .addUuid()
                .post("method", "getDate")
                .toCache("herald_gymreserve_timelist_and_itemlist")
                .onResponse((success, code, response) -> {
                    if (success) {
                        loadItemList();
                    } else {
                        showSnackBar("刷新失败，请重试");
                        hideProgressDialog();
                    }
                }).run();

        // 如果用户手机号为空时，同时预获取用户手机号
        new ApiSimpleRequest(Method.POST).api("yuyue")
                .addUuid().post("method", "getPhone")
                .toCache("herald_gymreserve_phone", o -> o.getJSONObject("content").getString("phone"))
                .run();

        // 如果用户自己的信息未完善，则同时预查询自己的ID
        if (CacheHelper.get("herald_gymreserve_userid").equals("")) {
            new ApiSimpleRequest(Method.POST).api("yuyue")
                    .addUuid()
                    .post("method", "getFriendList")
                    .post("cardNo", ApiHelper.getCurrentUser().userName)
                    .toCache("herald_gymreserve_userid", o -> o.getJSONArray("content").getJSONObject(0).getString("userId"))
                    .run();
        }
    }

    private void loadItemList() {
        try {
            JSONArray itemArray = new JSONObject(CacheHelper.get("herald_gymreserve_timelist_and_itemlist")).getJSONObject("content").getJSONArray("itemList");
            // 活动项目列表
            ArrayList<GymSportModel> list = GymSportModel.transformJSONtoArrayList(itemArray);
            // 获取日期列表
            JSONArray timeArray =  new JSONObject(CacheHelper.get("herald_gymreserve_timelist_and_itemlist")).getJSONObject("content").getJSONArray("timeList");
            String[] dayInfos = GymSportModel.transformJSONtoStringArray(timeArray);
            // 设置适配器同时设置点击函数
            GymReserveSportAdapter adapter = new GymReserveSportAdapter(getBaseContext(), list);
            // 点击时创建相应运动预约界面
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                // 打开相对应的预约界面
                GymChooseTimeActivity.startWithData(list.get(position), dayInfos);
            });

            hideProgressDialog();
        } catch (JSONException e) {
            hideProgressDialog();
            showSnackBar("数据解析失败，请重试");
        }
    }
}
