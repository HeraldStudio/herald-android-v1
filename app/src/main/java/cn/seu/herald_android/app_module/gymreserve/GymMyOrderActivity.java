package cn.seu.herald_android.app_module.gymreserve;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.custom.EmptyTipArrayAdapter;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

public class GymMyOrderActivity extends BaseActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_gymreserve__my_order);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            refreshMyOrder();
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        // 列表初始化
        listView = (ListView) findViewById(R.id.listview_gym_myorder);

        refreshMyOrder();
    }

    private void refreshMyOrder() {
        showProgressDialog();
        Cache.gymReserveMyOrder.refresh((success, code) -> {
            hideProgressDialog();
            if (success) {
                loadMyOrder();
            } else {
                showSnackBar("刷新失败，请重试");
            }
        });
    }

    public void loadMyOrder() {
        String cache = Cache.gymReserveMyOrder.getValue();
        if (!cache.equals("")) {
            JArr array = new JObj(cache).$o("content").$a("rows");
            ArrayList<MyOrder> list = MyOrder.transformJSONtoArrayList(array);
            listView.setAdapter(new MyOrderAdapter(getBaseContext(), R.layout.mod_que_gymreserve__my_order__item, list));
            return;
        }
        refreshMyOrder();
    }

    public void cancelOrder(int id) {
        showProgressDialog();
        new ApiSimpleRequest(Method.POST)
                .addUuid()
                .api("yuyue")
                .post("method", "cancelUrl")
                .post("id", id + "")
                .onResponse((success, code, response) -> {
                    String res = new JObj(response).$o("content").$s("msg");
                    if (success && res.equals("success")) {
                        showSnackBar("取消预约成功");
                        refreshMyOrder();
                    } else {
                        showSnackBar("取消预约失败，请重试");
                    }
                }).run();
    }

    public static class MyOrder {
        public static final int STATE_SUCCESS = 2;//预约成功，已通过
        public static final int STATE_FINISHED = 4;//已完成
        public static final int STATE_BREAK = 5;//失约
        public static final int STATE_CANCEL = 6;//已取消预约
        // 运动名称
        String itemName;
        // 开始时间
        String startTime;
        // 结束时间
        String endTime;
        // 场地名
        String floorName;
        // 使用时间
        String useDate;
        // 使用人数
        int peoplenum;
        // 标识预约的id
        int id;
        // 标识预约状态的state
        int state;

        public MyOrder(String startTime, String endTime, String itemName, String floorName, String useDate, int id, int peoplenum, int state) {
            this.endTime = endTime;
            this.floorName = floorName;
            this.id = id;
            this.itemName = itemName;
            this.peoplenum = peoplenum;
            this.startTime = startTime;
            this.useDate = useDate;
            this.state = state;
        }

        public static ArrayList<MyOrder> transformJSONtoArrayList(JArr array) {
            ArrayList<MyOrder> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JObj obj = array.$o(i);
                list.add(new MyOrder(
                        obj.$s("useBeginTime"),
                        obj.$s("useEndTime"),
                        obj.$s("itemName"),
                        obj.$s("floorName"),
                        obj.$s("useDate"),
                        obj.$i("id"),
                        obj.$i("usePeoples"),
                        obj.$i("state")
                ));
            }
            return list;
        }


    }

    public class MyOrderAdapter extends EmptyTipArrayAdapter<MyOrder> {

        class ViewHolder {
            @BindView(R.id.tv_name_and_floor)
            TextView tv_nameandfloor;
            @BindView(R.id.tv_usedate)
            TextView tv_useDate;
            @BindView(R.id.tv_usetime)
            TextView tv_useTime;
            @BindView(R.id.tv_usepeoplenum)
            TextView tv_peoplenum;
            @BindView(R.id.tv_state)
            TextView tv_state;
            @BindView(R.id.btn_cancelorder)
            Button btn_cancleorder;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }

        int resource;

        public MyOrderAdapter(Context context, int resource, List<MyOrder> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(resource, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            MyOrder item = getItem(position);
            holder.tv_nameandfloor.setText(String.format("%s (%s)", item.itemName, item.floorName));
            holder.tv_useDate.setText(item.useDate);
            holder.tv_useTime.setText(String.format("%s-%s", item.startTime, item.endTime));
            holder.tv_peoplenum.setText(item.peoplenum + "人");

            switch (item.state) {
                case MyOrder.STATE_SUCCESS:
                    holder.btn_cancleorder.setVisibility(View.VISIBLE);
                    holder.btn_cancleorder.setOnClickListener(v -> {
                        // 取消预约
                        cancelOrder(item.id);
                    });
                    holder.tv_state.setText("已通过");
                    holder.tv_state.setTextColor(ContextCompat.getColor(getContext(), R.color.relaxGreen));
                    break;
                case MyOrder.STATE_BREAK:
                    holder.btn_cancleorder.setVisibility(View.INVISIBLE);
                    holder.tv_state.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
                    holder.tv_state.setText("失约");
                    break;
                case MyOrder.STATE_FINISHED:
                    holder.btn_cancleorder.setVisibility(View.INVISIBLE);
                    holder.tv_state.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
                    holder.tv_state.setText("已完成");
                    break;
                default:
                case MyOrder.STATE_CANCEL:
                    holder.btn_cancleorder.setVisibility(View.INVISIBLE);
                    holder.tv_state.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
                    holder.tv_state.setText("已取消");
            }
            return convertView;
        }
    }
}
