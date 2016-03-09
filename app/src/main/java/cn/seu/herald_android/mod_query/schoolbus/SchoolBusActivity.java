package cn.seu.herald_android.mod_query.schoolbus;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;


import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

/**
 * 2016/2/22 By heyongdong
 * 校车时刻表查询
 */
public class SchoolBusActivity extends BaseAppCompatActivity {
    //工作日和双休日切换的Tablayout
    TabLayout tabLayout;
    //工作日和双休日切换ViewPager
    ViewPager viewPager;
    //工作日显示的Fragment
    SchoolBusFragment weekdayFragment;
    //周末显示的Fragment
    SchoolBusFragment weekendFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_bus);
        //初始化函数
        init();
        //加载校车数据
        loadListWithCace();
    }

    public void init() {
        //沉浸式布局
        setStatusBarColor(this, getResources().getColor(R.color.colorSchoolBusprimary));
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        //初始化控件
        tabLayout = (TabLayout)findViewById(R.id.tablayout_schoolbus);
        viewPager = (ViewPager)findViewById(R.id.schoolbus_viewpager);
        //设置Tab当前页标志的颜色
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorSchoolBusprimary));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_sync){
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshCache(){
        getProgressDialog().show();
        //通过联网刷新数据
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_SCHOOLBUS))
                .addParams("uuid",getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getApiHepler().dealApiException(e);
                        getProgressDialog().dismiss();
                    }

                    @Override
                    public void onResponse(String response) {
                        getProgressDialog().dismiss();
                        try{
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200){
                                getCacheHelper().setCache("herald_schoolbus_cache",json_res.toString());
                                loadListWithCace();
                            }
                        }catch (JSONException e2){
                            e2.printStackTrace();
                            showMsg("数据解析错误。");
                        }

                    }
                });
    }

    public void loadListWithCace(){
        //尝试加载缓存
        String cache = getCacheHelper().getCache("herald_schoolbus_cache");
        if(!cache.equals("")){
            try{
                JSONObject cache_json = new JSONObject(getCacheHelper().getCache("herald_schoolbus_cache")).getJSONObject("content");
                JSONArray weekend_tosubway = cache_json.getJSONObject("weekend").getJSONArray("前往地铁站");
                JSONArray weekend_toschool = cache_json.getJSONObject("weekend").getJSONArray("返回九龙湖");
                JSONArray weekday_tosubway = cache_json.getJSONObject("weekday").getJSONArray("前往地铁站");
                JSONArray weekday_toschool = cache_json.getJSONObject("weekday").getJSONArray("返回九龙湖");
                //生成周末前往地铁站列表
                ArrayList<SchoolBusItem> list_weekend_tosubway = SchoolBusItem.transfromtransfromJSONtoArrayList(weekend_toschool);
                //生成周末去学校列表
                ArrayList<SchoolBusItem> list_weekend_toschool = SchoolBusItem.transfromtransfromJSONtoArrayList(weekend_tosubway);
                //平时前往地铁站的列表
                ArrayList<SchoolBusItem> list_weekday_tosubway = SchoolBusItem.transfromtransfromJSONtoArrayList(weekday_tosubway);
                //平时前往学校的列表
                ArrayList<SchoolBusItem> list_weekday_toschool = SchoolBusItem.transfromtransfromJSONtoArrayList(weekday_toschool);
                //加载WeekDay，工作日的Fragment
                ArrayList<ArrayList<SchoolBusItem>> weekdayList = new ArrayList<>();
                weekdayList.add(list_weekday_tosubway);
                weekdayList.add(list_weekday_toschool);
                weekdayFragment = SchoolBusFragment.newInstance(new String[]{"前往地铁站","返回九龙湖"},weekdayList);

                //加载WeekEnd，双休日的Fragment
                ArrayList<ArrayList<SchoolBusItem>> weekendList = new ArrayList<>();
                weekendList.add(list_weekend_tosubway);
                weekendList.add(list_weekend_toschool);
                weekendFragment = SchoolBusFragment.newInstance(new String[]{"前往地铁站","返回九龙湖"},weekendList);


                //添加到viewpager中
                SchoolBusViewPagerAdapter schoolBusViewPagerAdapter = new SchoolBusViewPagerAdapter(getSupportFragmentManager());
                schoolBusViewPagerAdapter.add(weekdayFragment, "周一至周五");
                schoolBusViewPagerAdapter.add(weekendFragment, "周末");

                viewPager.setAdapter(schoolBusViewPagerAdapter);

                //关联ViewPager和Tablayout
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setTabsFromPagerAdapter(schoolBusViewPagerAdapter);

            }catch (JSONException e){
                showMsg("缓存加载失败，请尝试重新刷新");
            }
        }else {
            refreshCache();
        }
    }
}





