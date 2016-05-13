package cn.seu.herald_android.mod_query.gymreserve;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;


/**
 * Created by heyon on 2016/5/13.
 */
public class OrderItemTimeFragment extends Fragment {
    //展示可预约时间段的列表
    private ListView listView;
    ProgressBar progressBar;

    public OrderItemTimeFragment() {

    }

    //该项目可预约时间
    String dayInfo;
    //项目
    GymReserveItem gymReserveItem;
    //fragment所在activity
    BaseAppCompatActivity baseAppCompatActivity;

    public static OrderItemTimeFragment newInstance(String timeItem, GymReserveItem gymReserveItem, BaseAppCompatActivity baseAppCompatActivity) {
        OrderItemTimeFragment fragment = new OrderItemTimeFragment();
        fragment.dayInfo = timeItem;
        fragment.gymReserveItem = gymReserveItem;
        fragment.baseAppCompatActivity = baseAppCompatActivity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orderitembytime, container, false);
        //控件初始化
        listView = (ListView) view.findViewById(R.id.listview_orderitembytime);
        progressBar = new ProgressBar(getContext());
        //设定适配器
        refreshOrderItem();
        return view;
    }

    public void refreshOrderItem(){
        baseAppCompatActivity.showProgressDialog();
        ApiRequest apiRequest = new ApiRequest(getContext());
        apiRequest
                .api(ApiHelper.API_GYMRESERVE)
                .addUUID()
                .post("method", "getOrder")
                .post("itemId",gymReserveItem.sportId+"")
                .post("dayInfo", dayInfo)
                .onFinish((success, code, response) -> {
                    if (success) {
                        loadOrderItemTimes(response);
                    } else {
                        baseAppCompatActivity.hideProgressDialog();
                        baseAppCompatActivity.showSnackBar("获取可预约时间段失败，请检查网络");
                    }
                }).run();
    }

    //根据数据来刷新当前项目在当天的各个时间段可预约情况
    public void loadOrderItemTimes(String response){
        try {
            ArrayList<OrderItemTime> list = transformJSONtoArrayList(new JSONObject(response).getJSONObject("content").getJSONArray("orderIndexs"));
            listView.setAdapter(new OrderItemTimeAdapter(getContext(),R.layout.listviewitem_orderitemtime,list));
            baseAppCompatActivity.hideProgressDialog();
        }catch (JSONException e){
            //数据解析错误
            baseAppCompatActivity.showSnackBar("数据解析错误，请稍后再试");
        }
    }

    class OrderItemTime {
        //某项目在当天的各个时间段可预约情况,标识是否可以预约
        boolean enable;
        //剩余可预约数量
        int surplus;
        //可用时间段
        String avaliableTime;

        public OrderItemTime(boolean enable, int surplus, String avaliableTime) {
            this.enable = enable;
            this.surplus = surplus;
            this.avaliableTime = avaliableTime;
        }
    }

    public ArrayList<OrderItemTime> transformJSONtoArrayList(JSONArray array)throws JSONException{
        ArrayList<OrderItemTime> list = new ArrayList<>();
        for(int i = 0;i<array.length();i++){
            JSONObject obj = array.getJSONObject(i);
            list.add(new OrderItemTime(
                    obj.getBoolean("enable"),
                    obj.getInt("surplus"),
                    obj.getString("avaliableTime")
            ));
        }
        return list;
    }

    class OrderItemTimeAdapter extends ArrayAdapter<OrderItemTime>{
        int resoure;
        public OrderItemTimeAdapter(Context context, int resource, List<OrderItemTime> objects) {
            super(context, resource, objects);
            this.resoure = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(resoure,null);
            OrderItemTime time = getItem(position);
            TextView tv_avaliabletime = (TextView)convertView.findViewById(R.id.tv_avaliableyime);
            TextView tv_surplus = (TextView)convertView.findViewById(R.id.tv_surplus);
            Button btn = (Button) convertView.findViewById(R.id.btn_order);
            tv_avaliabletime.setText(time.avaliableTime);
            tv_surplus.setText("" + time.surplus);
            btn.setEnabled(time.enable);
            return convertView;
        }
    }

}