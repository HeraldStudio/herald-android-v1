package cn.seu.herald_android.app_module.experiment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.CacheHelper;

public class ExperimentActivity extends BaseActivity {

    @BindView(R.id.expandableListView)
    ExpandableListView expandableListView;
    @BindView(R.id.chengjiu_viewpager)
    ViewPager viewPager;

    // 每节实验开始的时间，以(Hour * 60 + Minute)形式表示
    // 本程序假定每节实验都是3小时
    public static final int[] EXPERIMENT_BEGIN_TIME = {
            9 * 60 + 45, 13 * 60 + 45, 18 * 60 + 15
    };

    // 成就列表
    private ArrayList<AchievementModel> achievements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_experiment);
        ButterKnife.bind(this);
        loadCache();
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
        // 如果缓存不为空则加载缓存，反之刷新缓存
        String cache = CacheHelper.get("herald_experiment");
        if (!cache.equals("")) {
            try {
                JSONObject json_content = new JSONObject(cache).getJSONObject("content");
                // 父view和子view数据集合
                ArrayList<String> parentArray = new ArrayList<>();
                ArrayList<ArrayList<ExperimentModel>> childArray = new ArrayList<>();
                achievements = new ArrayList<>();
                // 根据每种集合加载不同的子view
                for (int i = 0; i < json_content.length(); i++) {
                    String jsonArray_str = json_content.getString(json_content.names().getString(i));
                    if (!jsonArray_str.equals("")) {
                        // 如果有实验则加载数据和子项布局
                        JSONArray jsonArray = new JSONArray(jsonArray_str);
                        // 根据数组长度获得实验的Item集合
                        ArrayList<ExperimentModel> item_list = ExperimentModel.transformJSONArrayToArrayList(jsonArray);
                        // 加入到list中
                        parentArray.add(json_content.names().getString(i));
                        childArray.add(item_list);
                        // 加入到成就列表中
                        achievements.addAll(AchievementFactory.getExperimentAchievement(item_list));
                    }
                }
                // 设置成就列表
                setupAchievementWall();
                // 设置伸缩列表
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
        new ApiSimpleRequest(Method.POST).api("phylab").addUuid()
                .toCache("herald_experiment")
                .onFinish((success, code) -> {
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
        // 设置成就数目
        TextView tv_numOfAchievement = (TextView) findViewById(R.id.num);
        if (tv_numOfAchievement != null) {
            tv_numOfAchievement.setText(String.format("成就墙(%d/%d)", achievements.size(), 20));
        }
        if (0 == achievements.size()) {
            // 无成就时添加提示
            AchievementModel tipModel = new AchievementModel("暂无成就", "你还没有获得任何实验成就，赶紧参加实验，寻找神秘的成就碎片吧！", "");
            achievements.add(tipModel);
        }
        // 设置适配器
        viewPager.setAdapter(new AchievementViewPagerAdapter(getBaseContext(), achievements));
    }
}
