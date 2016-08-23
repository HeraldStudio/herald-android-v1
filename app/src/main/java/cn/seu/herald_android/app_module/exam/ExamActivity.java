package cn.seu.herald_android.app_module.exam;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class ExamActivity extends BaseActivity {

    @BindView(R.id.recyclerview_exam)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_exam);
        ButterKnife.bind(this);
        loadCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exam, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            refreshCache();
        } else if (id == R.id.action_add) {
            AppContext.startActivitySafely(AddExamActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCache();
    }

    private void loadCache() {
        // 加载缓存中的考试
        String cache = Cache.exam.getValue();
        if (cache.equals("")) {
            refreshCache();
            return;
        }

        List<ExamModel> exams = ExamModel.transformJArrToArrayList(new JObj(cache).$a("content"));
        List<ExamModel> customExams = ExamModel.transformJArrToArrayList(AddExamActivity.getCustomExamJArr());
        for (ExamModel examModel : customExams) {
            examModel.isCustom = true;
            exams.add(examModel);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ExamAdapter adapter = new ExamAdapter(exams);
        // 绑定自定义考试可以删除的响应函数
        adapter.setOnItemClickListener((item, position) -> {
            if (item.isCustom) {
                // 如果是自定义的考试就弹出删除询问
                new AlertDialog.Builder(this)
                        .setMessage("确定删除这个自定义考试吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            deleteDefinedExam(item);
                        })
                        .setNegativeButton("取消", (dialog1, which1) -> {
                        }).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void refreshCache() {
        showProgressDialog();
        Cache.exam.refresh((success, code) -> {
            hideProgressDialog();
            if (success) {
                loadCache();
                // showSnackBar("刷新成功");
            } else {
                showSnackBar("刷新失败，请重试");
            }
        });
    }

    void deleteDefinedExam(ExamModel item) {
        JArr array_old = AddExamActivity.getCustomExamJArr();
        JArr array_new = new JArr();
        boolean deleted = false;
        for (int i = 0; i < array_old.size(); i++) {
            JObj obj = array_old.$o(i);
            if (item.equals(new ExamModel(obj)) && !deleted) {
                deleted = true;
                continue;
            }
            array_new.put(obj);
        }
        Cache.examCustom.setValue(array_new.toString());
        showSnackBar("删除成功");
        loadCache();
    }
}
