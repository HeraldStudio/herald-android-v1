package cn.seu.herald_android.mod_query.gymreserve;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class OrderItemActivity extends BaseAppCompatActivity {

    public static void startOrderItemActivity(Activity activity, SportTypeItem item, String[] dayInfos){
        Intent intent = new Intent(activity,OrderItemActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gymItem",item);
        bundle.putStringArray("dayInfos",dayInfos);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    //预约的体育项目
    SportTypeItem gymItem;
    //可预约的时间
    String[] dayinfos;

    //Tab
    TabLayout tabLayout;
    ViewPager viewPager;

    //适配器
    OrderItemTimeFragmentAdapter orderItemTimeFragmentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_item);
        init();
        loadOrderItemByTime();
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
            OrderItemTimeFragment fragment =(OrderItemTimeFragment) orderItemTimeFragmentAdapter.getItem(viewPager.getCurrentItem());
            fragment.refreshOrderItem();
        }
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        Bundle bundle = getIntent().getExtras();
        gymItem = (SportTypeItem) bundle.getSerializable("gymItem");
        dayinfos = bundle.getStringArray("dayInfos");

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

        //设置标题
        setTitle(gymItem.name + "场馆预约");

        //初始化
        tabLayout = (TabLayout)findViewById(R.id.tablayout_orderitem);
        viewPager = (ViewPager)findViewById(R.id.viewpager_orderitem);
        //设置只存在一页缓存
        if (viewPager!=null)viewPager.setOffscreenPageLimit(1);

    }

    public void loadOrderItemByTime(){
        orderItemTimeFragmentAdapter = new OrderItemTimeFragmentAdapter(getSupportFragmentManager());
        for(String dayinfo : dayinfos){
            //只保留日期的月份和日期、周数
            String timeTitle = dayinfo.split("-")[1] + "-" + dayinfo.split("-")[2];
            OrderItemTimeFragment fragment = OrderItemTimeFragment.newInstance(dayinfo,gymItem,this);
            orderItemTimeFragmentAdapter.add(fragment,timeTitle);
        }
        viewPager.setAdapter(orderItemTimeFragmentAdapter);
        //关联tablayout和viewpager
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(orderItemTimeFragmentAdapter);
    }


}
