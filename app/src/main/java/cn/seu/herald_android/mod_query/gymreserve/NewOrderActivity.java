package cn.seu.herald_android.mod_query.gymreserve;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.custom.ListViewUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;

public class NewOrderActivity extends BaseActivity {
    public static void startNewOrderActivity(Activity activity, SportTypeItem item, String dayInfo, String avaliableTime){
        Intent intent = new Intent(activity,NewOrderActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gymItem",item);
        bundle.putString("dayInfo",dayInfo);
        bundle.putString("avaliableTime",avaliableTime);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    //预约的体育项目
    SportTypeItem gymItem;
    //预约的日期
    String dayinfo;
    //预约的时间段
    String avaliableTime;
    //预约的是全场还是半场，半场则为true，全场为false
    boolean half = false;

    //使用时间
    TextView tv_time;
    //使用类型
    Spinner spinner;
    //联系电话
    EditText et_phone;
    //已邀请的好友列表
    ListView list_invitedfriend;
    //适配器
    InvitedFriendAdapter invitedFriendAdapter;
    //已邀请的好友数组
    ArrayList<Friend> invitedFriends;
    //最近联系人列表
    ListView list_recentlyfriend;
    //适配器
    RecentlyFriendAdapter recentlyFriendAdapter;
    //最近联系人数组
    ArrayList<Friend> recentlyFriends;
    //添加近联系人按钮
    Button btn_addfriend;
    //已邀请好友数标签提示
    TextView tv_tipsOfInvitedNums;

    boolean isOrdeing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);
        init();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ok) {
            if(!refreshTipsOfInvitedNum()){
                //检查预约人数是否达到要求
                showSnackBar("预约人数不满足要求");
                return false;
            }
            if(et_phone.getText().toString().equals("")){
                //检查手机号是否为空
                showSnackBar("联系人不能为空");
                return false;
            }
            sendNewOrder();
        }
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        Bundle bundle = getIntent().getExtras();
        gymItem = (SportTypeItem) bundle.getSerializable("gymItem");
        dayinfo = bundle.getString("dayInfo");
        avaliableTime = bundle.getString("avaliableTime");


        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
                finish();
            });
        }

        //沉浸式
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorGymReserveprimary));
        enableSwipeBack();

        //设置标题
        setTitle(gymItem.name + "新增预约");

        //初始化
        tv_time = (TextView)findViewById(R.id.tv_time);
        spinner = (Spinner)findViewById(R.id.spiner_type);
        et_phone = (EditText)findViewById(R.id.et_phone);
        tv_tipsOfInvitedNums = (TextView)findViewById(R.id.tv_tipofinvitedfriends);

        //列表初始化
        list_invitedfriend = (ListView) findViewById(R.id.listview_invitedfriend);
        list_recentlyfriend = (ListView) findViewById(R.id.listview_recentlyfriend);
        if (list_recentlyfriend != null) {
            list_recentlyfriend.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        }
        list_invitedfriend.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        //初始化数组
        invitedFriends = new ArrayList<>();
        recentlyFriends = new ArrayList<>();

        //初始化添加新朋友按钮
        btn_addfriend = (Button)findViewById(R.id.btn_add_friend);
        if (btn_addfriend != null) {
            btn_addfriend.setOnClickListener(
                    o-> startActivity(new Intent(NewOrderActivity.this,SearchFriendActivity.class)));
        }

        //加载跟所选项目和用户有关的、一般不变的信息
        setupItemInfo();
        //加载全场半场使用的下拉框，并设置提示信息
        setupSpinner();

        //加载最近邀请的好友
        refreshRecentlyFriend();

    }

    private void setupItemInfo(){
        //预约时间
        tv_time.setText(dayinfo + " " + avaliableTime);

        //此处手机号已在GymReserveActivity中预获取，如果获取失败了那么取到的字符串是空，设置的text也为空，在提交时会提示用户输入手机号
        et_phone.setText(CacheHelper.get("herald_gymreserve_phone"));

    }

    public void setupSpinner(){
        //下拉框设置
        String[] list;
        if ( gymItem.allowHalf == 1){
            list = new String[]{"全场","半场"};
        }else{
            list = new String[]{"全场"};
        }
        ArrayAdapter<String> spinnerSimpleAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinneritem_gym_half, list);

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

    //刷新邀请好友数提示
    boolean refreshTipsOfInvitedNum(){
        int min = (half ? gymItem.halfMinUsers : gymItem.fullMinUsers) - 1;
        int max = (half ? gymItem.halfMaxUsers : gymItem.fullMaxUsers) - 1;
        tv_tipsOfInvitedNums.setText(String.format("已邀请好友：%d (可邀请好友数：%d 到 %d )",invitedFriends.size(),min,max));
        //如果满足预约人数要求则返回true，反之返回false
        return invitedFriends.size() >= min && invitedFriends.size() <= max;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRecentlyFriend();
    }

    void sendNewOrder(){
        //发起新预约
        //检查自己的userId是否为空
        if (isOrdeing)
            return;
        isOrdeing = true;
        showProgressDialog();
        String userId = CacheHelper.get("herald_gymreserve_userid");
        //如果为空则需要继续获取userId
        if (userId.equals("")){
            new ApiRequest()
                    .api("yuyue")
                    .addUUID()
                    .post("method", "getFriendList")
                    .post("cardNo", ApiHelper.getUserName())
                    .toCache("herald_gymreserve_userid", o -> o.getJSONArray("content").getJSONObject(0).getString("userId"))
                    .onFinish((success, code, response) -> {
                        isOrdeing = false;
                        if (success){
                            //如果成功获取了自己的userId后继续尝试发送
                            sendNewOrder();
                        }else{
                            //没有成功则显示错误信息
                            showSnackBar("用户信息获取失败，请退出场馆预约后重试进入");
                        }
                    })
                    .run();
            return;
        }

        //构造发送请求
        ArrayList<String> userIds = new ArrayList<>();
        for ( Friend friend :invitedFriends){
            userIds.add("\""+friend.userId+"\"");
        }


        String itemId = gymItem.sportId + "";
        String useTime = dayinfo.split(" ")[0] + " " + avaliableTime;//参数形式为 ‘2016-05-15 12:00-13:00’
        //客户端这里发的1是全场，2是半场，在服务端会改掉这个
        String useMode = half?"2":"1";
        String phone = et_phone.getText().toString();
        String useUserIds = userIds.toString();
        new ApiRequest()
                .api("yuyue")
                .addUUID()
                .post("method", "new")
                .post("orderVO.itemId",itemId)
                .post("orderVO.useTime",useTime)
                .post("orderVO.useMode",useMode)
                .post("orderVO.phone",phone)
                .post("orderVO.remark","remark")//随便评论点内容
                .post("useUserIds",useUserIds)
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    isOrdeing = true;
                    Handler handler = new Handler();
                    try {
                        if (success){
                            int rescode = new JSONObject(response).getJSONObject("content").getInt("code");

                            switch (rescode){
                                case 0:
                                    showSnackBar("预约成功");
                                    handler.postDelayed(() -> {
                                        startActivity(new Intent(NewOrderActivity.this, MyOrderActivity.class));
                                        finish();
                                    },500);
                                    break;
                                default:
                                    showSnackBar(new JSONObject(response).getJSONObject("content").getString("msg"));
                                    //预约失败会重新选择时间段
                                    handler.postDelayed(this::finish,500);
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        showSnackBar("判断预约结果失败，请手动查询");
                        //跳转至我的预约列表，方便查看预约结果
                        handler.postDelayed(() -> {
                            startActivity(new Intent(NewOrderActivity.this, MyOrderActivity.class));
                            finish();
                        },500);
                    }
                })
                .run();

    }

    //加载最近的好友列表
    void refreshRecentlyFriend(){
        //重新获取最近好友列表
        recentlyFriends = getFriendArrayList();
        recentlyFriendAdapter = new RecentlyFriendAdapter(getBaseContext(),R.layout.listviewitem_gym_recentlyfriend,recentlyFriends);
        list_recentlyfriend.setAdapter(recentlyFriendAdapter);
        ListViewUtils.setHeightWithContent(list_recentlyfriend);
    }


    void refreshInvitedFriend(){
        //适配器数据改变
        invitedFriendAdapter = new InvitedFriendAdapter(getBaseContext(),R.layout.listviewitem_gym_invitedfriend,invitedFriends);
        list_invitedfriend.setAdapter(invitedFriendAdapter);
        ListViewUtils.setHeightWithContent(list_invitedfriend);
        refreshTipsOfInvitedNum();
    }

    public  ArrayList<Friend> getFriendArrayList(){
        try{
            JSONArray array = getFriendJSONArray();
            ArrayList<Friend> list = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                list.add(new Friend(array.getJSONObject(i)));
            }
            return list;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public  JSONArray getFriendJSONArray(){
        String cache = CacheHelper.get("herald_gymreserve_recentfriendlist");
        try{
            if(!cache.equals("")){
                return new JSONArray(cache);
            }
        }catch (JSONException e){
            e.printStackTrace();
            CacheHelper.set("herald_gymreserve_recentfriendlist","");
        }
        return new JSONArray();
    }

    public void removeFriend(Friend friend){
        try{
            JSONArray array = getFriendJSONArray();
            JSONArray result = new JSONArray();
            for(int i=0;i<array.length();i++){
                if(friend.getJSONObject().toString().equals(array.getJSONObject(i).toString()))
                    continue;
                result.put(array.getJSONObject(i));
            }
            CacheHelper.set("herald_gymreserve_recentfriendlist",result.toString());
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    //最近联系人展示用Adapter
    public class RecentlyFriendAdapter extends ArrayAdapter<Friend> {
        int resource;
        public RecentlyFriendAdapter(Context context, int resource, List<Friend> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Friend friend = getItem(position);
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitem_gym_recentlyfriend,null);

            TextView tv_name = (TextView)convertView.findViewById(R.id.tv_friendname);
            TextView tv_department = (TextView)convertView.findViewById(R.id.tv_frienddepartment);
            try{
                tv_name.setText(friend.nameDepartment.split("\\(")[0]);
                tv_department.setText(friend.nameDepartment.split("\\(")[1].split("\\)")[0]);
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
                tv_name.setText(friend.nameDepartment);
            }


            ImageView imgv_add = (ImageView)convertView.findViewById(R.id.ibtn_add);
            //如果朋友不存在于已邀请朋友中，则加入
            imgv_add.setOnClickListener(o->{
                if (!invitedFriends.contains(friend)){
                    invitedFriends.add(friend);
                    refreshInvitedFriend();
                }
            });
            //从最近朋友中删除
            ImageView imgv_delete = (ImageView)convertView.findViewById(R.id.ibtn_delete);
            imgv_delete.setOnClickListener(o->{
                removeFriend(friend);
                refreshRecentlyFriend();
            });
            return convertView;
        }
    }

    //已邀请好友展示用Adapter
    public  class InvitedFriendAdapter extends ArrayAdapter<Friend> {
        int resource;
        public InvitedFriendAdapter(Context context, int resource, List<Friend> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Friend friend = getItem(position);
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitem_gym_invitedfriend,null);
            TextView tv_name = (TextView)convertView.findViewById(R.id.tv_friendname);
            tv_name.setText(friend.nameDepartment.split("\\(")[0]);
            ImageView imgv_sub = (ImageView)convertView.findViewById(R.id.ibtn_sub);
            imgv_sub.setOnClickListener(o->{
                invitedFriends.remove(friend);
                refreshInvitedFriend();
            });
            return convertView;
        }
    }



}
