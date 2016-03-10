package cn.seu.herald_android.mod_query.experiment;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.mod_achievement.Achievement;
import okhttp3.Call;

public class ExperimentActivity extends BaseAppCompatActivity {

    //实验类型列表
    ExpandableListView expandableListView;
    //成就墙展示View
    ViewPager viewPager;
    //成就墙数目
    TextView tv_numofAchievement;
    //成就列表
    ArrayList<Achievement> achievementArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        init();
        loadCache();

    }

    public void init(){
        //toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //设置按钮
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        //沉浸式
        setStatusBarColor(this,getResources().getColor(R.color.colorExperimentprimary));

        //实验类型列表加载
        expandableListView = (ExpandableListView)findViewById(R.id.expandableListView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            expandableListView.setNestedScrollingEnabled(true);
        }

        //成就墙viewPager
        viewPager = (ViewPager)findViewById(R.id.chengjiu_viewpager);

        //成就列表
        achievementArrayList = new ArrayList<>();
        achievementArrayList.add(new Achievement(Achievement.HERALD,"先与声","参加海螺工作室客户端内测的首批用户的标志。","2016-3-3"));
        achievementArrayList.add(new Achievement(Achievement.SHIYAN,"挑战物理女王的勇士","选择了周立新老师的实验，最终得分低于60。","2016-3-3"));

        //设置成就数目
        tv_numofAchievement = (TextView)findViewById(R.id.tv_num);
        tv_numofAchievement.setText(String.format("成就墙(%d/%d)",achievementArrayList.size(),20));

        //设置适配器
        viewPager.setAdapter(new AchievementViewPagerAdapter(getBaseContext(), achievementArrayList));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
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

    public void loadCache(){
        //如果缓存不为空则加载缓存，反之刷新缓存
        String cache = getCacheHelper().getCache("herald_experiment");
        if(!cache.equals("")){
            try{
                JSONObject json_content = new JSONObject(cache).getJSONObject("content");
                //实验种类集合
                String[] titles = {"文科及医学实验选做","文科及医学实验",
                        "基础性实验(上)","基础性实验(上)选做",
                        "基础性实验(下)","基础性实验(下)选做"};
                //父view和子view数据集合
                ArrayList<String> parentArray = new ArrayList<>();
                ArrayList<ArrayList<ExperimentItem>> childArray = new ArrayList<>();
                //根据每种集合加载不同的子view
                for(int i=0;i<titles.length;i++){
                    if(!json_content.has(titles[i])) continue;
                    String jsonArray_str = json_content.getString(titles[i]);
                    if(!jsonArray_str.equals("")){
                        //如果有实验则加载数据和子项布局
                        JSONArray jsonArray = new JSONArray(jsonArray_str);
                        //根据数组长度获得实验的Item集合
                        ArrayList<ExperimentItem> item_list = ExperimentItem.transfromJSONArrayToArrayList(jsonArray);
                        //加入到list中
                        parentArray.add(titles[i]);
                        childArray.add(item_list);
                    }
                }
                ExprimentExpandAdapter exprimentExpandAdapter = new ExprimentExpandAdapter(getBaseContext(),parentArray,childArray);
                expandableListView.setAdapter(exprimentExpandAdapter);
                if(exprimentExpandAdapter.getGroupCount() > 0)
                    expandableListView.expandGroup(0);
            }catch (JSONException e){
                showMsg("缓存解析失败，请刷新后再试");
                e.printStackTrace();
            }
        }else {
            refreshCache();
        }
    }


    public void refreshCache(){
        getProgressDialog().show();
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_PHYLAB))
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
                                showMsg("刷新成功");
                                getCacheHelper().setCache("herald_experiment",response);
                            }
                            loadCache();
                        }catch (JSONException e){
                            e.printStackTrace();
                            showMsg("数据解析失败");
                        }
                    }
                });
    }
}
