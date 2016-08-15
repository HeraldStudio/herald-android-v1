package cn.seu.herald_android.app_module.grade;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableHeaderAdapter;

public class GradeActivity extends BaseActivity {

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
        String cache = Cache.grade.getValue();
        if (!cache.equals("")) {
            JArr jsonArray = new JObj(cache).$a("content");
            // 获取计算后的绩点
            if (jsonArray.$o(0).has("gpa")) {
                tv_gpa.setText(jsonArray.$o(0).$s("gpa without revamp"));
                tv_gpa2.setText(jsonArray.$o(0).$s("gpa without revamp"));
                tv_time.setText("最后计算时间:" + jsonArray.$o(0).$s("calculate time"));
            }
            // 数据类型转换
            GradeAdapter gradeAdapter = new GradeAdapter(this, GradeModel.transformJArrToArrayList(jsonArray));
            // 设置成绩表单数据
            tableViewGrade.setDataAdapter(gradeAdapter);
            // 设置行颜色变化
            tableViewGrade.setDataRowColoriser(new GradeAdapter.GradeRowColorizer(getBaseContext()));
        } else {
            refreshCache();
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
        Cache.grade.refresh((success, code) -> {
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

