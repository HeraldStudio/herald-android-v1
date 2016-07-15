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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.custom.ListViewUtils;
import cn.seu.herald_android.helper.ApiRequest;

public class GymChooseTimeFragment extends Fragment {
    //展示可预约时间段的列表
    private ListView listView;
    ProgressBar progressBar;

    public GymChooseTimeFragment() {

    }

    //该项目可预约时间
    String dayInfo;
    //项目
    GymSportModel gymSportModel;
    //fragment所在activity
    BaseActivity baseActivity;
    //标识是否处于判断预约状态
    boolean isJudging = false;

    public static GymChooseTimeFragment newInstance(String timeItem, GymSportModel gymSportModel, BaseActivity baseActivity) {
        GymChooseTimeFragment fragment = new GymChooseTimeFragment();
        fragment.dayInfo = timeItem;
        fragment.gymSportModel = gymSportModel;
        fragment.baseActivity = baseActivity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mod_que_gymreserve__order_time__fragment, container, false);
        //控件初始化
        listView = (ListView) view.findViewById(R.id.listview_orderitembytime);
        progressBar = new ProgressBar(getContext());
        //设定适配器
        refreshOrderItem();
        return view;
    }

    public void refreshOrderItem(){
        baseActivity.showProgressDialog();
        ApiRequest apiRequest = new ApiRequest();
        apiRequest
                .api("yuyue")
                .addUUID()
                .post("method", "getOrder")
                .post("itemId", gymSportModel.sportId + "")
                .post("dayInfo", dayInfo)
                .onFinish((success, code, response) -> {
                    if (success) {
                        loadOrderItemTimes(response);
                    } else {
                        baseActivity.hideProgressDialog();
                        baseActivity.showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    //根据数据来刷新当前项目在当天的各个时间段可预约情况
    public void loadOrderItemTimes(String response){
        try {
            ArrayList<OrderItemTime> list = transformJSONtoArrayList(new JSONObject(response).getJSONObject("content").getJSONArray("orderIndexs"));
            listView.setAdapter(new OrderItemTimeAdapter(getContext(),R.layout.mod_que_gymreserve__order_time__fragment__item,list));
            if (listView.getCount() == 0){
                ListViewUtils.addDefaultEmptyTipsView(getContext(),listView,"暂无可用预约场地");
            }
            baseActivity.hideProgressDialog();
        }catch (JSONException e){
            //数据解析错误
            baseActivity.showSnackBar("数据解析错误，请稍后再试");
        }
    }

    public void judgeOrder(OrderItemTime time){
        if (isJudging)
            return;
        //判断是否可以预约，如果可以则打开预约界面
        new ApiRequest()
                .api("yuyue")
                .addUUID()
                .post("method", "judgeOrder")
                .post("itemId", gymSportModel.sportId + "")
                .post("dayInfo",dayInfo)
                .post("time",time.avaliableTime)
                .onFinish((success, code, response) -> {
                    isJudging = false;
                    try{
                        String judgeRes = "获取失败，请重试";
                        if (success){
                            String rescode = new JSONObject(response).getJSONObject("content").getString("code");
                            String msg = new JSONObject(response).getJSONObject("content").getString("msg");
                            if (rescode.equals("0")){
                                //可以进行预约
                                GymNewOrderActivity.startNewOrderActivity(baseActivity, gymSportModel, dayInfo, time.avaliableTime);
                            }else {
                                judgeRes = msg;
                                baseActivity.showSnackBar(judgeRes);
                            }
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                        baseActivity.showSnackBar("预约失败");
                    }
                })
                .run();
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
            final OrderItemTime time = getItem(position);
            TextView tv_avaliabletime = (TextView)convertView.findViewById(R.id.tv_avaliableyime);
            TextView tv_surplus = (TextView)convertView.findViewById(R.id.tv_surplus);
            Button btn = (Button) convertView.findViewById(R.id.btn_order);
            tv_avaliabletime.setText(time.avaliableTime);
            tv_surplus.setText("" + time.surplus);
            btn.setEnabled(time.enable);

            //为预约按钮设定点击事件
            btn.setOnClickListener(o->{
                if (!isJudging)//如果没有处于判断状态则开始判断
                    judgeOrder(time);
            });
            return convertView;
        }
    }

}