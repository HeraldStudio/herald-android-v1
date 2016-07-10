package cn.seu.herald_android.mod_query.experiment;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.mod_achievement.AchievementViewPagerAdapter;

public class ExperimentActivity extends BaseActivity {

    // 每节实验开始的时间，以(Hour * 60 + Minute)形式表示
    // 本程序假定每节实验都是3小时
    public static final int[] EXPERIMENT_BEGIN_TIME = {
            9 * 60 + 45, 13 * 60 + 45, 18 * 60 + 15
    };
    //实验类型列表
    private ExpandableListView expandableListView;
    //成就墙展示View
    private ViewPager viewPager;
    //成就列表
    private ArrayList<Achievement> achievementArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_experiment);
        init();
        loadCache();
    }

    private void init() {
        //toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
                finish();
            });
        }

        //禁用collapsingToolbarLayout的伸缩标题
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitleEnabled(false);
        }

        //沉浸式
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorExperimentprimary));
        enableSwipeBack();

        //实验类型列表加载
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);

        //成就墙viewPager
        viewPager = (ViewPager) findViewById(R.id.chengjiu_viewpager);
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

    private void loadCache() {
        //如果缓存不为空则加载缓存，反之刷新缓存
        String cache = CacheHelper.get("herald_experiment");
        if (!cache.equals("")) {
            try {
                JSONObject json_content = new JSONObject(cache).getJSONObject("content");
                //父view和子view数据集合
                ArrayList<String> parentArray = new ArrayList<>();
                ArrayList<ArrayList<ExperimentItem>> childArray = new ArrayList<>();
                achievementArrayList = new ArrayList<>();
                //根据每种集合加载不同的子view
                for (int i = 0; i < json_content.length(); i++) {
                    String jsonArray_str = json_content.getString(json_content.names().getString(i));
                    if (!jsonArray_str.equals("")) {
                        //如果有实验则加载数据和子项布局
                        JSONArray jsonArray = new JSONArray(jsonArray_str);
                        //根据数组长度获得实验的Item集合
                        ArrayList<ExperimentItem> item_list = ExperimentItem.transformJSONArrayToArrayList(jsonArray);
                        //加入到list中
                        parentArray.add(json_content.names().getString(i));
                        childArray.add(item_list);
                        //加入到成就列表中
                        achievementArrayList.addAll(AchievementFactory.getExperimentAchievement(item_list));
                    }
                }
                //设置成就列表
                setupAchievementWall();
                //设置伸缩列表
                ExperimentExpandAdapter experimentExpandAdapter = new ExperimentExpandAdapter(getBaseContext(), parentArray, childArray);
                expandableListView.setAdapter(experimentExpandAdapter);

                if (experimentExpandAdapter.getGroupCount() > 0)
                    expandableListView.expandGroup(0);

            } catch (JSONException e) {
                showSnackBar("解析失败，请刷新");
                e.printStackTrace();
            }
        } else {
            refreshCache();
        }
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest().api("phylab").addUUID()
                .toCache("herald_experiment", o -> o)
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                        // showSnackBar("刷新成功");
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    private void setupAchievementWall() {
        //设置成就数目
        TextView tv_numofAchievement = (TextView) findViewById(R.id.num);
        if (tv_numofAchievement != null) {
            tv_numofAchievement.setText(String.format("成就墙(%d/%d)", achievementArrayList.size(), 20));
        }
        if(0 == achievementArrayList.size()){
            //无成就时添加提示
            Achievement achievementTip = new Achievement("暂无成就","你还没有获得任何实验成就，赶紧参加实验，寻找神秘的成就碎片吧！","");
            achievementArrayList.add(achievementTip);
        }
        //设置适配器
        viewPager.setAdapter(new AchievementViewPagerAdapter(getBaseContext(), achievementArrayList));
    }
}
