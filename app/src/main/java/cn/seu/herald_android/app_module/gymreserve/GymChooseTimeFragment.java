package cn.seu.herald_android.app_module.gymreserve;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.EmptyTipArrayAdapter;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

public class GymChooseTimeFragment extends Fragment {
    // 展示可预约时间段的列表
    private ListView listView;
    ProgressBar progressBar;

    public GymChooseTimeFragment() {

    }

    // 该项目可预约时间
    String dayInfo;
    // 项目
    GymSportModel gymSportModel;
    // fragment所在activity
    BaseActivity baseActivity;
    // 标识是否处于判断预约状态
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
        // 控件初始化
        listView = ButterKnife.findById(view, R.id.listview_orderitembytime);
        progressBar = new ProgressBar(getContext());
        // 设定适配器
        refreshOrderItem();
        return view;
    }

    public void refreshOrderItem() {
        baseActivity.showProgressDialog();
        new ApiSimpleRequest(Method.POST)
                .api("yuyue")
                .addUuid()
                .post("method", "getOrder")
                .post("itemId", gymSportModel.sportId + "")
                .post("dayInfo", dayInfo)
                .onResponse((success, code, response) -> {
                    if (success) {
                        loadOrderItemTimes(response);
                    } else {
                        baseActivity.hideProgressDialog();
                        baseActivity.showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    // 根据数据来刷新当前项目在当天的各个时间段可预约情况
    public void loadOrderItemTimes(String response) {
        ArrayList<OrderItemTime> list = transformJSONtoArrayList(new JObj(response).$o("content").$a("orderIndexs"));
        listView.setAdapter(new OrderItemTimeAdapter(getContext(), R.layout.mod_que_gymreserve__order_time__fragment__item, list));
        baseActivity.hideProgressDialog();
    }

    public void judgeOrder(OrderItemTime time) {
        if (isJudging)
            return;
        // 判断是否可以预约，如果可以则打开预约界面
        new ApiSimpleRequest(Method.POST)
                .api("yuyue")
                .addUuid()
                .post("method", "judgeOrder")
                .post("itemId", gymSportModel.sportId + "")
                .post("dayInfo", dayInfo)
                .post("time", time.availableTime)
                .onResponse((success, code, response) -> {
                    isJudging = false;
                    String judgeRes = "获取失败，请重试";
                    if (success) {
                        String rescode = new JObj(response).$o("content").$s("code");
                        String msg = new JObj(response).$o("content").$s("msg");
                        if (rescode.equals("0")) {
                            // 可以进行预约
                            GymNewOrderActivity.startWithData(gymSportModel, dayInfo, time.availableTime);
                        } else {
                            judgeRes = msg;
                            baseActivity.showSnackBar(judgeRes);
                        }
                    }
                })
                .run();
    }

    class OrderItemTime {
        // 某项目在当天的各个时间段可预约情况,标识是否可以预约
        boolean enable;
        // 剩余可预约数量
        int surplus;
        // 可用时间段
        String availableTime;

        public OrderItemTime(boolean enable, int surplus, String availableTime) {
            this.enable = enable;
            this.surplus = surplus;
            this.availableTime = availableTime;
        }
    }

    public ArrayList<OrderItemTime> transformJSONtoArrayList(JArr array) {
        ArrayList<OrderItemTime> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JObj obj = array.$o(i);
            list.add(new OrderItemTime(
                    obj.$b("enable"),
                    obj.$i("surplus"),
                    obj.$s("avaliableTime")
            ));
        }
        return list;
    }

    class OrderItemTimeAdapter extends EmptyTipArrayAdapter<OrderItemTime> {

        class ViewHolder {
            @BindView(R.id.availableTime)
            TextView availableTime;
            @BindView(R.id.surplus)
            TextView surplus;
            @BindView(R.id.btn)
            Button btn;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }

        int resource;

        public OrderItemTimeAdapter(Context context, int resource, List<OrderItemTime> objects) {
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

            final OrderItemTime time = getItem(position);

            holder.availableTime.setText(time.availableTime);
            holder.surplus.setText(String.valueOf(time.surplus));
            holder.btn.setEnabled(time.enable);

            // 为预约按钮设定点击事件
            holder.btn.setOnClickListener(o -> {
                if (!isJudging)//如果没有处于判断状态则开始判断
                    judgeOrder(time);
            });
            return convertView;
        }
    }

}