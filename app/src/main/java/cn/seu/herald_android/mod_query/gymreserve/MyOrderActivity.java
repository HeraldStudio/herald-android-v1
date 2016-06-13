package cn.seu.herald_android.mod_query.gymreserve;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.custom.ListViewUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

public class MyOrderActivity extends BaseAppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        init();
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
            refreshMyOrder();
        }
        return super.onOptionsItemSelected(item);
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
        setTitle("我的预约");

        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorGymReserveprimary));
        enableSwipeBack();
        //列表初始化
        listView = (ListView) findViewById(R.id.listview_gym_myorder);

        refreshMyOrder();
    }


    private void refreshMyOrder() {
        showProgressDialog();
        new ApiRequest(this)
                .addUUID()
                .api(ApiHelper.API_GYMRESERVE)
                .post("method", "myOrder")
                .toCache("herald_gymreserve_myorder", o -> o)
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadMyOrder();
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                })
                .run();
    }

    public void loadMyOrder(){
        String cache = getCacheHelper().getCache("herald_gymreserve_myorder");
        if(!cache.equals("")){
            try{
                JSONArray array = new JSONObject(cache).getJSONObject("content").getJSONArray("rows");
                ArrayList<MyOrder> list = MyOrder.transfromJSONtoArrayList(array);
                listView.setAdapter(new MyOrderAdapter(getBaseContext(),R.layout.listviewitem_gym_myorder,list));
                if (listView.getCount() == 0)
                    ListViewUtils.addDefaultEmptyTipsView(getBaseContext(),listView,"暂无场馆预约记录");
            }catch (JSONException e){
                e.printStackTrace();
            }
            return;
        }
        refreshMyOrder();
    }

    public void cancelOrder(int id){
        showProgressDialog();
        new ApiRequest(this)
                .addUUID()
                .api(ApiHelper.API_GYMRESERVE)
                .post("method","cancelUrl")
                .post("id",id+"")
                .onFinish((success, code, response) -> {
                    try {
                        String res = new JSONObject(response).getJSONObject("content").getString("msg");
                        if (success && res.equals("success")){
                            showSnackBar("取消预约成功");
                            refreshMyOrder();
                        } else {
                            showSnackBar("取消预约失败，请重试");
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }).run();
    }

    public static class MyOrder{
        public static final int STATE_SUCCESS = 2;//预约成功，已通过
        public static final int STATE_FINISHED =4;//已完成
        public static final int STATE_BREAK = 5;//失约
        public static final int STATE_CANCEL = 6;//已取消预约
        //运动名称
        String itemName;
        //开始时间
        String startTime;
        //结束时间
        String endTime;
        //场地名
        String floorName;
        //使用时间
        String useDate;
        //使用人数
        int peoplenum;
        //标识预约的id
        int id;
        //标识预约状态的state
        int state;

        public MyOrder(String startTime, String endTime, String itemName, String floorName, String useDate,int id ,int peoplenum,int state) {
            this.endTime = endTime;
            this.floorName = floorName;
            this.id = id;
            this.itemName = itemName;
            this.peoplenum = peoplenum;
            this.startTime = startTime;
            this.useDate = useDate;
            this.state = state;
        }

        public static ArrayList<MyOrder> transfromJSONtoArrayList(JSONArray array)throws JSONException{
            ArrayList<MyOrder> list =new ArrayList<>();
            for(int i= 0;i<array.length();i++){
                JSONObject obj = array.getJSONObject(i);
                list.add(new MyOrder(
                        obj.getString("useBeginTime"),
                        obj.getString("useEndTime"),
                        obj.getString("itemName"),
                        obj.getString("floorName"),
                        obj.getString("useDate"),
                        obj.getInt("id"),
                        obj.getInt("usePeoples"),
                        obj.getInt("state")
                ));
            }
            return list;
        }


    }

    public class MyOrderAdapter extends ArrayAdapter<MyOrder>{
        int resource;
        public MyOrderAdapter(Context context, int resource, List<MyOrder> objects) {
            super(context, resource,objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyOrder item = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(resource,null);
            TextView tv_nameandfloor = (TextView)convertView.findViewById(R.id.tv_name_and_floor);
            TextView tv_useDate = (TextView)convertView.findViewById(R.id.tv_usedate);
            TextView tv_useTime = (TextView)convertView.findViewById(R.id.tv_usetime);
            TextView tv_peoplenum = (TextView)convertView.findViewById(R.id.tv_usepeoplenum);
            TextView tv_state = (TextView)convertView.findViewById(R.id.tv_state);
            Button btn_cancleorder = (Button) convertView.findViewById(R.id.btn_cancelorder);

            tv_nameandfloor.setText(String.format("%s (%s)",item.itemName,item.floorName));
            tv_useDate.setText(item.useDate);
            tv_useTime.setText(String.format("%s-%s",item.startTime,item.endTime));
            tv_peoplenum.setText(item.peoplenum + "人");

            switch (item.state){
                case MyOrder.STATE_SUCCESS:
                    btn_cancleorder.setVisibility(View.VISIBLE);
                    btn_cancleorder.setOnClickListener(v -> {
                        //取消预约
                        cancelOrder(item.id);
                    });
                    tv_state.setText("已通过");
                    tv_state.setTextColor(ContextCompat.getColor(getContext(),R.color.relaxGreen));
                    break;
                case MyOrder.STATE_BREAK:
                    btn_cancleorder.setVisibility(View.INVISIBLE);
                    tv_state.setTextColor(ContextCompat.getColor(getContext(),R.color.colorSecondaryText));
                    tv_state.setText("失约");
                    break;
                case MyOrder.STATE_FINISHED:
                    btn_cancleorder.setVisibility(View.INVISIBLE);
                    tv_state.setTextColor(ContextCompat.getColor(getContext(),R.color.colorSecondaryText));
                    tv_state.setText("已完成");
                    break;
                default:
                case MyOrder.STATE_CANCEL:
                    btn_cancleorder.setVisibility(View.INVISIBLE);
                    tv_state.setTextColor(ContextCompat.getColor(getContext(),R.color.colorSecondaryText));
                    tv_state.setText("已取消");
            }
            return convertView;
        }
    }
}
