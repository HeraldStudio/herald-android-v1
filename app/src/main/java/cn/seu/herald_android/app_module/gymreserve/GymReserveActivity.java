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
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.BaseActivity;

public class GymReserveActivity extends BaseActivity {

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
        Cache.gymReserveGetDate.refresh((success, code) -> {
            if (success) {
                loadItemList();
            } else {
                showSnackBar("刷新失败，请重试");
                hideProgressDialog();
            }
        });

        // 如果用户手机号为空时，同时预获取用户手机号
        Cache.gymReserveGetPhone.refresh();

        // 如果用户自己的信息未完善，则同时预查询自己的ID
        Cache.gymReserveUserId.refreshIfEmpty();
    }

    private void loadItemList() {
        try {
            JSONArray itemArray = new JSONObject(Cache.gymReserveGetDate.getValue()).getJSONObject("content").getJSONArray("itemList");
            // 活动项目列表
            ArrayList<GymSportModel> list = GymSportModel.transformJSONtoArrayList(itemArray);
            // 获取日期列表
            JSONArray timeArray = new JSONObject(Cache.gymReserveGetDate.getValue()).getJSONObject("content").getJSONArray("timeList");
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
