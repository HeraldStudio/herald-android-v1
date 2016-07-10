package cn.seu.herald_android.mod_query.exam;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;

public class ExamActivity extends BaseActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
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


        //沉浸式
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorExamprimary));
        enableSwipeBack();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_exam);
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
        }else if (id == R.id.action_add){
            startActivity(new Intent(ExamActivity.this,AddExamActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCache();
    }

    private void loadCache() {
        //加载缓存中的考试
        String cache = CacheHelper.get("herald_exam");
        if (cache.equals("")) {
            refreshCache();
            return;
        }

        try {
            List<ExamItem> exams = ExamItem.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            List<ExamItem> definedexams = ExamItem.transformJSONArrayToArrayList(getDefinedExamsJSONArray());
            for(ExamItem examItem : definedexams){
                examItem.isdefined = true;
                exams.add(examItem);
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ExamAdapter adapter = new ExamAdapter(exams);
            //绑定自定义考试可以删除的响应函数
            adapter.setOnItemClickListner((item, position) -> {
                if (item.isdefined){
                    //如果是自定义的考试就弹出删除询问
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
        } catch (JSONException e) {
            e.printStackTrace();
            showSnackBar("解析失败，请刷新");
        }
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest().api("exam").addUUID()
                .toCache("herald_exam", o -> o)
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

    JSONArray getDefinedExamsJSONArray(){
        String cache = CacheHelper.get("herald_exam_definedexam");
        try{
            return new JSONArray(cache);
        }catch (JSONException e){
            e.printStackTrace();
            CacheHelper.set("herald_exam_definedexam",new JSONArray().toString());
        }
        return new JSONArray();
    }

    void deleteDefinedExam(ExamItem item){
        try{
            JSONArray array_old = getDefinedExamsJSONArray();
            JSONArray array_new = new JSONArray();
            for(int i = 0;i< array_old.length() ; i++){
                JSONObject obj = array_old.getJSONObject(i);
                if (item.equals(new ExamItem(obj))) {
                    continue;
                }
                array_new.put(obj);
            }
            CacheHelper.set("herald_exam_definedexam",array_new.toString());
            showSnackBar("删除成功");
            loadCache();
        }catch (JSONException e){
            e.printStackTrace();
            showSnackBar("删除失败");
        }

    }
}
