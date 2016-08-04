package cn.seu.herald_android.app_module.grade;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.CacheHelper;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableHeaderAdapter;

public class GradeActivity extends BaseActivity {

    public static ApiSimpleRequest remoteRefreshNotifyDotState() {
        return new ApiSimpleRequest(Method.POST).api("gpa").addUuid()
                .toCache("herald_grade_gpa",
                        /** notifyModuleIfChanged: */Module.grade);
    }

    @BindView(R.id.tableview_grade)
    SortableTableView<GradeModel> tableViewGrade;
    @BindView(R.id.tv_grade_gpawithoutrevamp)
    TextView tv_gpa;
    @BindView(R.id.tv_grade_gpa)
    TextView tv_gpa2;
    @BindView(R.id.tv_grade_time)
    TextView tv_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_grade);
        ButterKnife.bind(this);

        // 设置表头
        TableHeaderAdapter tableHeaderAdapter = new TableHeaderAdapter(this) {
            @Override
            public View getHeaderView(int columnIndex, ViewGroup parentView) {
                String headers[] = {"课程", "学期", "成绩", "类型", "学分"};
                TextView tv_header = new TextView(getContext());
                tv_header.setText(headers[columnIndex]);
                tv_header.setTextSize(13f);
                tv_header.setGravity(Gravity.CENTER);
                tv_header.setTextColor(ContextCompat.getColor(GradeActivity.this, R.color.colorIcons));
                return tv_header;
            }
        };
        tableViewGrade.setHeaderBackgroundColor(ContextCompat.getColor(GradeActivity.this, R.color.colorGradePrimary));
        tableViewGrade.setHeaderAdapter(tableHeaderAdapter);

        loadCache();
    }


    private void loadCache() {
        try {
            // 测试数据测试
            String cache = CacheHelper.get("herald_grade_gpa");
            if (!cache.equals("")) {
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                // 获取计算后的绩点
                if (jsonArray.getJSONObject(0).has("gpa")) {
                    tv_gpa.setText(jsonArray.getJSONObject(0).getString("gpa without revamp"));
                    tv_gpa2.setText(jsonArray.getJSONObject(0).getString("gpa without revamp"));
                    tv_time.setText("最后计算时间:" + jsonArray.getJSONObject(0).get("calculate time"));
                }
                // 数据类型转换
                GradeAdapter gradeAdapter = new GradeAdapter(this, GradeModel.transformJSONArrayToArrayList(jsonArray));
                // 设置成绩表单数据
                tableViewGrade.setDataAdapter(gradeAdapter);
                // 设置行颜色变化
                tableViewGrade.setDataRowColoriser(new GradeAdapter.GradeRowColorizer(getBaseContext()));
            } else {
                refreshCache();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grade, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_grade_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiSimpleRequest(Method.POST).api("gpa").addUuid()
                .toCache("herald_grade_gpa")
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
}

