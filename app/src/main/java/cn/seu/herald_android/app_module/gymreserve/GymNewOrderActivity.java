package cn.seu.herald_android.app_module.gymreserve;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.custom.EmptyTipArrayAdapter;
import cn.seu.herald_android.custom.ListViewUtils;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

public class GymNewOrderActivity extends BaseActivity {

    public static void startWithData(GymSportModel item, String dayInfo, String availableTime) {
        Intent intent = new Intent(AppContext.instance, GymNewOrderActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gymItem", item);
        bundle.putString("dayInfo", dayInfo);
        bundle.putString("availableTime", availableTime);
        intent.putExtras(bundle);
        AppContext.startActivitySafely(intent);
    }

    // 预约的体育项目
    GymSportModel gymItem;
    // 预约的日期
    String dayinfo;
    // 预约的时间段
    String avaliableTime;
    // 预约的是全场还是半场，半场则为true，全场为false
    boolean half = false;

    // 使用时间
    @BindView(R.id.tv_time)
    TextView tv_time;
    // 使用类型
    @BindView(R.id.spiner_type)
    Spinner spinner;
    // 联系电话
    @BindView(R.id.et_phone)
    EditText et_phone;
    // 已邀请的好友列表
    @BindView(R.id.listview_recentlyfriend)
    ListView list_recentlyfriend;
    // 最近联系人列表
    @BindView(R.id.listview_invitedfriend)
    ListView list_invitedfriend;
    // 已邀请好友数标签提示
    @BindView(R.id.tv_tipofinvitedfriends)
    TextView tv_tipsOfInvitedNums;

    @OnClick(R.id.btn_add_friend)
    void addFriendOnClick() {
        AppContext.startActivitySafely(GymAddFriendActivity.class);
    }

    // 适配器
    InvitedFriendAdapter invitedFriendAdapter;
    // 已邀请的好友数组
    ArrayList<FriendModel> invitedFriends;
    // 适配器
    RecentlyFriendAdapter recentlyFriendAdapter;
    // 最近联系人数组
    ArrayList<FriendModel> recentlyFriends;

    boolean isOrdering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_gymreserve__new_order);
        ButterKnife.bind(this);
        init();
    }

    public void init() {
        Bundle bundle = getIntent().getExtras();
        gymItem = (GymSportModel) bundle.getSerializable("gymItem");
        dayinfo = bundle.getString("dayInfo");
        avaliableTime = bundle.getString("availableTime");

        // 设置标题
        setTitle(gymItem.name + "新增预约");

        // 初始化
        if (list_recentlyfriend != null) {
            list_recentlyfriend.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        }
        list_invitedfriend.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        // 初始化数组
        invitedFriends = new ArrayList<>();
        recentlyFriends = new ArrayList<>();

        // 加载跟所选项目和用户有关的、一般不变的信息
        setupItemInfo();
        // 加载全场半场使用的下拉框，并设置提示信息
        setupSpinner();

        // 加载最近邀请的好友
        refreshRecentlyFriend();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_ok) {
            if (!refreshTipsOfInvitedNum()) {
                // 检查预约人数是否达到要求
                showSnackBar("预约人数不满足要求");
                return false;
            }
            if (et_phone.getText().toString().equals("")) {
                // 检查手机号是否为空
                showSnackBar("联系人不能为空");
                return false;
            }
            sendNewOrder();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupItemInfo() {
        // 预约时间
        tv_time.setText(dayinfo + " " + avaliableTime);

        // 此处手机号已在GymReserveActivity中预获取，如果获取失败了那么取到的字符串是空，设置的text也为空，在提交时会提示用户输入手机号
        et_phone.setText(Cache.gymReserveGetPhone.getValue());

    }

    public void setupSpinner() {
        // 下拉框设置
        String[] list;
        if (gymItem.allowHalf == 1) {
            list = new String[]{"全场", "半场"};
        } else {
            list = new String[]{"全场"};
        }
        ArrayAdapter<String> spinnerSimpleAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.mod_que_gymreserve__new_order__item_spinner_half, list);

        spinner.setPrompt("请选择场地类型");
        spinner.setAdapter(spinnerSimpleAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                half = !parent.getItemAtPosition(position).equals("全场");
                refreshTipsOfInvitedNum();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(0);
            }
        });
    }

    // 刷新邀请好友数提示
    boolean refreshTipsOfInvitedNum() {
        int min = (half ? gymItem.halfMinUsers : gymItem.fullMinUsers) - 1;
        int max = (half ? gymItem.halfMaxUsers : gymItem.fullMaxUsers) - 1;
        tv_tipsOfInvitedNums.setText(String.format("已邀请好友：%d (可邀请好友数：%d 到 %d )", invitedFriends.size(), min, max));
        // 如果满足预约人数要求则返回true，反之返回false
        return invitedFriends.size() >= min && invitedFriends.size() <= max;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRecentlyFriend();
    }

    void sendNewOrder() {
        // 发起新预约
        // 检查自己的userId是否为空
        if (isOrdering)
            return;
        isOrdering = true;
        showProgressDialog();
        String userId = Cache.gymReserveUserId.getValue();
        // 如果为空则需要继续获取userId
        if (userId.equals("")) {
            Cache.gymReserveUserId.refresh((success, code) -> {
                isOrdering = false;
                if (success) {
                    // 如果成功获取了自己的userId后继续尝试发送
                    sendNewOrder();
                } else {
                    // 没有成功则显示错误信息
                    showSnackBar("用户信息获取失败，请退出场馆预约后重试进入");
                }
            });
            return;
        }

        // 构造发送请求
        ArrayList<String> userIds = new ArrayList<>();
        for (FriendModel friendModel : invitedFriends) {
            userIds.add("\"" + friendModel.userId + "\"");
        }


        String itemId = gymItem.sportId + "";
        String useTime = dayinfo.split(" ")[0] + " " + avaliableTime;//参数形式为 ‘2016-05-15 12:00-13:00’
        // 客户端这里发的1是全场，2是半场，在服务端会改掉这个
        String useMode = half ? "2" : "1";
        String phone = et_phone.getText().toString();
        String useUserIds = userIds.toString();
        new ApiSimpleRequest(Method.POST)
                .api("yuyue")
                .addUuid()
                .post("method", "new")
                .post("orderVO.itemId", itemId)
                .post("orderVO.useTime", useTime)
                .post("orderVO.useMode", useMode)
                .post("orderVO.phone", phone)
                .post("orderVO.remark", useTime)//随便评论点内容
                .post("useUserIds", useUserIds)
                .onResponse((success, code, response) -> {
                    hideProgressDialog();
                    isOrdering = true;
                    Handler handler = new Handler();
                    if (success) {
                        int rescode = new JObj(response).$o("content").$i("code");

                        switch (rescode) {
                            case 0:
                                showSnackBar("预约成功, 建议手动检查预约是否有效");
                                handler.postDelayed(() -> {
                                    AppContext.startActivitySafely(GymMyOrderActivity.class);
                                    finish();
                                }, 500);
                                break;
                            default:
                                showSnackBar(new JObj(response).$o("content").$s("msg"));
                                // 预约失败会重新选择时间段
                                handler.postDelayed(this::finish, 500);
                        }
                    }
                })
                .run();

    }

    // 加载最近的好友列表
    void refreshRecentlyFriend() {
        // 重新获取最近好友列表
        recentlyFriends = getFriendArrayList();
        recentlyFriendAdapter = new RecentlyFriendAdapter(getBaseContext(), R.layout.mod_que_gymreserve__item_recent_friend, recentlyFriends);
        list_recentlyfriend.setAdapter(recentlyFriendAdapter);
        ListViewUtils.setHeightWithContent(list_recentlyfriend);
    }

    void refreshInvitedFriend() {
        // 适配器数据改变
        invitedFriendAdapter = new InvitedFriendAdapter(getBaseContext(), R.layout.mod_que_gymreserve__new_order__item_invited_friend, invitedFriends);
        list_invitedfriend.setAdapter(invitedFriendAdapter);
        ListViewUtils.setHeightWithContent(list_invitedfriend);
        refreshTipsOfInvitedNum();
    }

    public ArrayList<FriendModel> getFriendArrayList() {
        JArr array = getFriendJArr();
        ArrayList<FriendModel> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(new FriendModel(array.$o(i)));
        }
        return list;
    }

    public JArr getFriendJArr() {
        String cache = Cache.gymReserveFriend.getValue();
        if (!cache.equals("")) {
            return new JArr(cache);
        }
        return new JArr();
    }

    public void removeFriend(FriendModel friendModel) {
        JArr array = getFriendJArr();
        JArr result = new JArr();
        for (int i = 0; i < array.size(); i++) {
            if (friendModel.getJObj().toString().equals(array.$(i).toString()))
                continue;
            result.put(array.$(i));
        }
        Cache.gymReserveFriend.setValue(result.toString());
    }

    // 最近联系人展示用Adapter
    public class RecentlyFriendAdapter extends EmptyTipArrayAdapter<FriendModel> {
        int resource;

        public RecentlyFriendAdapter(Context context, int resource, List<FriendModel> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView) {
            FriendModel friendModel = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_gymreserve__item_recent_friend, null);

            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_friendname);
            TextView tv_department = (TextView) convertView.findViewById(R.id.tv_frienddepartment);
            try {
                tv_name.setText(friendModel.nameDepartment.split("\\(")[0]);
                tv_department.setText(friendModel.nameDepartment.split("\\(")[1].split("\\)")[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                tv_name.setText(friendModel.nameDepartment);
            }


            ImageView imgv_add = (ImageView) convertView.findViewById(R.id.ibtn_add);
            // 如果朋友不存在于已邀请朋友中，则加入
            imgv_add.setOnClickListener(o -> {
                if (!invitedFriends.contains(friendModel)) {
                    invitedFriends.add(friendModel);
                    refreshInvitedFriend();
                }
            });
            // 从最近朋友中删除
            ImageView imgv_delete = (ImageView) convertView.findViewById(R.id.ibtn_delete);
            imgv_delete.setOnClickListener(o -> {
                removeFriend(friendModel);
                refreshRecentlyFriend();
            });
            return convertView;
        }
    }

    // 已邀请好友展示用Adapter
    public class InvitedFriendAdapter extends EmptyTipArrayAdapter<FriendModel> {
        int resource;

        public InvitedFriendAdapter(Context context, int resource, List<FriendModel> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView) {
            FriendModel friendModel = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_gymreserve__new_order__item_invited_friend, null);
            TextView tv_name = (TextView) convertView.findViewById(R.id.tv_friendname);
            tv_name.setText(friendModel.nameDepartment.split("\\(")[0]);
            ImageView imgv_sub = (ImageView) convertView.findViewById(R.id.ibtn_sub);
            imgv_sub.setOnClickListener(o -> {
                invitedFriends.remove(friendModel);
                refreshInvitedFriend();
            });
            return convertView;
        }
    }
}
