package cn.seu.herald_android.app_module.jwc;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

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

public class JwcActivity extends BaseActivity {

    // 教务通知类型列表
    @BindView(R.id.expandableListView)
    ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_jwc);
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
        String cache = CacheHelper.get("herald_jwc");
        if (!cache.equals("")) {
            try {
                JSONObject json_content = new JSONObject(cache).getJSONObject("content");
                // 父view和子view数据集合
                ArrayList<String> parentArray = new ArrayList<>();
                ArrayList<ArrayList<JwcNoticeModel>> childArray = new ArrayList<>();
                // 根据每种集合加载不同的子view
                for (int i = 0; i < json_content.length(); i++) {
                    // 跳过最新动态
                    if (json_content.names().getString(i).equals("最新动态")) continue;

                    String jsonArray_str = json_content.getString(json_content.names().getString(i));
                    if (!jsonArray_str.equals("")) {
                        // 如果有教务通知则加载数据和子项布局
                        JSONArray jsonArray = new JSONArray(jsonArray_str);
                        // 根据数组长度获得教务通知的Item集合
                        ArrayList<JwcNoticeModel> item_list = JwcNoticeModel.transformJSONArrayToArrayList(jsonArray);
                        // 加入到list中
                        parentArray.add(json_content.names().getString(i).replace("教务信息", "核心通知"));
                        childArray.add(item_list);
                    }
                }
                // 设置伸缩列表
                JwcExpandAdapter jwcExpandAdapter = new JwcExpandAdapter(getBaseContext(), parentArray, childArray);
                expandableListView.setAdapter(jwcExpandAdapter);
                expandableListView.setDivider(ContextCompat.getDrawable(this, R.drawable.line_divider));

                if (jwcExpandAdapter.getGroupCount() > 0)
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
        new ApiSimpleRequest(Method.POST).api("jwc").addUuid().toCache("herald_jwc")
                .onResponse((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                        // showSnackBar("刷新成功");
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }
}

