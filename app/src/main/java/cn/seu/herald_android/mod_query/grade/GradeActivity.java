package cn.seu.herald_android.mod_query.grade;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableHeaderAdapter;

public class GradeActivity extends BaseActivity {

    public static ApiRequest remoteRefreshNotifyDotState() {
        return new ApiRequest().api("gpa").addUUID()
                .toCache("herald_grade_gpa",
                        /** notifyModuleIfChanged: */SettingsHelper.Module.grade);
    }

    private SortableTableView<GradeModel> tableViewGrade;
    private ProgressDialog progressDialog;
    //展示首修GPA的TV
    private TextView tv_gpa;
    //展示非首修GPA的TV
    private TextView tv_gpa2;
    //展示最后计算时间的TV
    private TextView tv_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_grade);
        init();
        //尝试加载缓存
        loadCache();
    }

    private void init() {
        //设置toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("成绩查询");
            setSupportActionBar(toolbar);
            //设置点击函数
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
                finish();
            });
        }
        //沉浸式状态栏颜色
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorGradeprimary));
        enableSwipeBack();

        //设置collapsingToolbarLayout标题禁用
//        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
//        collapsingToolbarLayout.setTitleEnabled(false);

        //控件初始化
        //noinspection unchecked
        tableViewGrade = (SortableTableView<GradeModel>) findViewById(R.id.tableview_grade);
        tv_gpa = (TextView) findViewById(R.id.tv_grade_gpawithoutrevamp);
        tv_gpa2 = (TextView) findViewById(R.id.tv_grade_gpa);
        tv_time = (TextView) findViewById(R.id.tv_grade_time);

        //设置表头
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
        tableViewGrade.setHeaderBackgroundColor(ContextCompat.getColor(GradeActivity.this, R.color.colorGradeprimary));
        tableViewGrade.setHeaderAdapter(tableHeaderAdapter);

        //刷新时的进度对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("获取成绩中");
        progressDialog.setMessage("由于教务处网站访问速度较慢，可能有一定延迟，请耐心等待~");
    }


    private void loadCache() {
        try {
            //测试数据测试
            String cache = CacheHelper.get("herald_grade_gpa");
            if (!cache.equals("")) {
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                //获取计算后的绩点
                if (jsonArray.getJSONObject(0).has("gpa")) {
                    tv_gpa.setText(jsonArray.getJSONObject(0).getString("gpa without revamp"));
                    tv_gpa2.setText(jsonArray.getJSONObject(0).getString("gpa without revamp"));
                    tv_time.setText("最后计算时间:" + jsonArray.getJSONObject(0).get("calculate time"));
                }
                //数据类型转换
                GradeAdapter gradeAdapter = new GradeAdapter(this, GradeModel.transformJSONArrayToArrayList(jsonArray));
                //设置成绩表单数据
                tableViewGrade.setDataAdapter(gradeAdapter);
                //设置行颜色变化
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
        progressDialog.show();
        new ApiRequest().api("gpa").addUUID()
                .toCache("herald_grade_gpa")
                .onFinish((success, code, response) -> {
                    progressDialog.hide();
                    if (success) {
                        loadCache();
                        // showSnackBar("刷新成功");
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }
}

