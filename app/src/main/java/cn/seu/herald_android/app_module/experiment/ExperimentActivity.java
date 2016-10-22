package cn.seu.herald_android.app_module.experiment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class ExperimentActivity extends BaseActivity {

    @BindView(R.id.expandableListView)
    ExpandableListView expandableListView;

    // 每节实验开始的时间，以(Hour * 60 + Minute)形式表示
    // 本程序假定每节实验都是3小时
    public static final int[] EXPERIMENT_BEGIN_TIME = {
            9 * 60 + 45, 13 * 60 + 45, 18 * 60 + 15
    };

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
        String cache = Cache.experiment.getValue();
        if (!cache.equals("")) {
            JObj json_content = new JObj(cache).$o("content");
            // 父view和子view数据集合
            ArrayList<String> parentArray = new ArrayList<>();
            ArrayList<ArrayList<ExperimentModel>> childArray = new ArrayList<>();
            // 根据每种集合加载不同的子view
            for (String key : json_content.keySet()) {
                String jsonArray_str = json_content.$s(key);
                if (!jsonArray_str.equals("")) {
                    // 如果有实验则加载数据和子项布局
                    JArr jsonArray = new JArr(jsonArray_str);
                    // 根据数组长度获得实验的Item集合
                    ArrayList<ExperimentModel> item_list = ExperimentModel.transformJArrToArrayList(jsonArray);
                    // 加入到list中
                    parentArray.add(key);
                    childArray.add(item_list);
                }
            }
            // 设置伸缩列表
            ExperimentExpandAdapter experimentExpandAdapter = new ExperimentExpandAdapter(getBaseContext(), parentArray, childArray);
            expandableListView.setAdapter(experimentExpandAdapter);

            if (experimentExpandAdapter.getGroupCount() > 0)
                expandableListView.expandGroup(0);
        } else {
            refreshCache();
        }
    }

    private void refreshCache() {
        showProgressDialog();
        Cache.experiment.refresh((success, code) -> {
            hideProgressDialog();
            if (success) {
                loadCache();
                // showSnackBar("刷新成功");
            } else {
                showSnackBar("刷新失败，请重试");
            }
        });
    }
}
