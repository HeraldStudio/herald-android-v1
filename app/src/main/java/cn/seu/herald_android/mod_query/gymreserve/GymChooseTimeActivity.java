package cn.seu.herald_android.mod_query.gymreserve;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_framework.BaseActivity;

public class GymChooseTimeActivity extends BaseActivity {

    public static void startWithData(GymSportModel item, String[] dayInfos) {
        Intent intent = new Intent(AppContext.currentContext.$get(), GymChooseTimeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gymItem",item);
        bundle.putStringArray("dayInfos",dayInfos);
        intent.putExtras(bundle);
        AppContext.currentContext.$get().startActivity(intent);
    }

    GymSportModel gymItem;
    String[] dayinfos;

    @BindView(R.id.tablayout_orderitem)
    TabLayout tabLayout;
    @BindView(R.id.viewpager_orderitem)
    ViewPager viewPager;

    GymChooseTimeAdapter gymChooseTimeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_gymreserve__order_time);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        gymItem = (GymSportModel) bundle.getSerializable("gymItem");
        dayinfos = bundle.getStringArray("dayInfos");
        setTitle(gymItem.name + "场馆预约");

        if (viewPager != null) viewPager.setOffscreenPageLimit(1);
        loadOrderItemByTime();
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
            GymChooseTimeFragment fragment = (GymChooseTimeFragment) gymChooseTimeAdapter.getItem(viewPager.getCurrentItem());
            fragment.refreshOrderItem();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadOrderItemByTime(){
        gymChooseTimeAdapter = new GymChooseTimeAdapter(getSupportFragmentManager());
        for(String dayinfo : dayinfos){
            String timeTitle = dayinfo.split("-")[1] + "-" + dayinfo.split("-")[2];
            GymChooseTimeFragment fragment = GymChooseTimeFragment.newInstance(dayinfo, gymItem, this);
            gymChooseTimeAdapter.add(fragment, timeTitle);
        }
        viewPager.setAdapter(gymChooseTimeAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
