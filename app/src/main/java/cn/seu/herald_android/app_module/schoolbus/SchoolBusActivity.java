package cn.seu.herald_android.app_module.schoolbus;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class SchoolBusActivity extends BaseActivity {

    // 适配器
    @BindView(R.id.tablayout_schoolbus)
    TabLayout tabLayout;
    @BindView(R.id.schoolbus_viewpager)
    ViewPager viewPager;

    private SchoolBusViewPagerAdapter schoolBusViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_schoolbus);
        ButterKnife.bind(this);

        // 适配器初始化
        // 添加到viewpager中
        schoolBusViewPagerAdapter = new SchoolBusViewPagerAdapter(getSupportFragmentManager());

        if (viewPager != null) {
            viewPager.setAdapter(schoolBusViewPagerAdapter);

            // 关联ViewPager和Tablayout
            if (tabLayout != null) {
                tabLayout.setupWithViewPager(viewPager);
            }
        }
        // 加载校车数据
        loadListWithCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshCache() {
        showProgressDialog();
        Cache.schoolbus.refresh((success, code) -> {
            hideProgressDialog();
            if (success) {
                loadListWithCache();
                // showSnackBar("刷新成功");
            } else {
                showSnackBar("刷新失败");
            }
        });
    }

    private void loadListWithCache() {
        // 尝试加载缓存
        String cache = Cache.schoolbus.getValue();
        if (!cache.equals("")) {
            JObj cache_json = new JObj(Cache.schoolbus.getValue()).$o("content");
            JArr weekend_tosubway = cache_json.$o("weekend").$a("前往地铁站");
            JArr weekend_toschool = cache_json.$o("weekend").$a("返回九龙湖");
            JArr weekday_tosubway = cache_json.$o("weekday").$a("前往地铁站");
            JArr weekday_toschool = cache_json.$o("weekday").$a("返回九龙湖");
            // 生成周末前往地铁站列表
            ArrayList<SchoolBusModel> list_weekend_tosubway = SchoolBusModel.transformJSONtoArrayList(weekend_toschool);
            // 生成周末去学校列表
            ArrayList<SchoolBusModel> list_weekend_toschool = SchoolBusModel.transformJSONtoArrayList(weekend_tosubway);
            // 平时前往地铁站的列表
            ArrayList<SchoolBusModel> list_weekday_tosubway = SchoolBusModel.transformJSONtoArrayList(weekday_tosubway);
            // 平时前往学校的列表
            ArrayList<SchoolBusModel> list_weekday_toschool = SchoolBusModel.transformJSONtoArrayList(weekday_toschool);
            // 加载WeekDay，工作日的Fragment
            ArrayList<ArrayList<SchoolBusModel>> weekdayList = new ArrayList<>();
            weekdayList.add(list_weekday_tosubway);
            weekdayList.add(list_weekday_toschool);
            SchoolBusFragment weekdayFragment = SchoolBusFragment.newInstance(new String[]{"前往地铁站", "返回九龙湖"}, weekdayList);

            // 加载WeekEnd，双休日的Fragment
            ArrayList<ArrayList<SchoolBusModel>> weekendList = new ArrayList<>();
            weekendList.add(list_weekend_tosubway);
            weekendList.add(list_weekend_toschool);
            SchoolBusFragment weekendFragment = SchoolBusFragment.newInstance(new String[]{"前往地铁站", "返回九龙湖"}, weekendList);

            schoolBusViewPagerAdapter.removeAll();
            schoolBusViewPagerAdapter.add(weekdayFragment, "周一至周五");
            schoolBusViewPagerAdapter.add(weekendFragment, "周末");
            schoolBusViewPagerAdapter.notifyDataSetChanged();
        } else {
            refreshCache();
        }
    }
}





