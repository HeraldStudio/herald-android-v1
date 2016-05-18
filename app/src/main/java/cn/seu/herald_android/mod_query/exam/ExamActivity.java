package cn.seu.herald_android.mod_query.exam;

import android.app.AlertDialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;

public class ExamActivity extends BaseAppCompatActivity {

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
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });


        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorExamprimary));
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
        String cache = getCacheHelper().getCache("herald_exam");
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
            showSnackBar("数据解析失败，请重试");
        }
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest(this).api(ApiHelper.API_EXAM).addUUID()
                .toCache("herald_exam", o -> o)
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                        showSnackBar("刷新成功");
                    }
                }).run();
    }

    public static ApiRequest remoteRefreshCache(Context context) {
        return new ApiRequest(context).api(ApiHelper.API_EXAM).addUUID()
                .toCache("herald_exam", o -> o);
    }

    /**
     * 读取考试缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getExamItem(TimelineView host) {
        //教务处考试缓存
        String cache = new CacheHelper(host.getContext()).getCache("herald_exam");
        //自定义考试缓存
        String definedcache = new CacheHelper(host.getContext()).getCache("herald_exam_definedexam");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            List<ExamItem> examList = new ArrayList<>();
            List<ExamItem> temp = ExamItem.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            List<ExamItem> defined = ExamItem.transformJSONArrayToArrayList(new JSONArray(definedcache));
            //加入教务处的考试
            for (ExamItem examItem : temp) {
                if (examItem.getRemainingDays() >= 0) {
                    examList.add(examItem);
                }
            }
            //加入本地自定义的考试
            for (ExamItem examItem : defined) {
                if (examItem.getRemainingDays() >= 0) {
                    examList.add(examItem);
                }
            }

            if (examList.size() == 0) {
                return new TimelineItem(SettingsHelper.MODULE_EXAM,
                        now, TimelineItem.NO_CONTENT, "最近没有新的考试安排");
            } else {
                Collections.sort(examList, (e1, e2) -> {
                    int remainingDays1 = 0, remainingDays2 = 0;
                    try {
                        remainingDays1 = e1.getRemainingDays();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        remainingDays2 = e2.getRemainingDays();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return remainingDays1 - remainingDays2;
                });
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_EXAM,
                        now, TimelineItem.CONTENT_NOTIFY, "你最近有" + examList.size() + "场考试，抓紧时间复习吧");
                for (ExamItem examItem : examList) {
                    item.attachedView.add(new ExamBlockLayout(host.getContext(), examItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            new CacheHelper(host.getContext()).setCache("herald_exam", "");
            return new TimelineItem(SettingsHelper.MODULE_EXAM,
                    now, TimelineItem.NO_CONTENT, "考试数据加载失败，请手动刷新"
            );
        }
    }

    JSONArray getDefinedExamsJSONArray(){
        String cache = getCacheHelper().getCache("herald_exam_definedexam");
        try{
            JSONArray array = new JSONArray(cache);
            return array;
        }catch (JSONException e){
            e.printStackTrace();
            getCacheHelper().setCache("herald_exam_definedexam",new JSONArray().toString());
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
            getCacheHelper().setCache("herald_exam_definedexam",array_new.toString());
            showSnackBar("删除成功");
            loadCache();
        }catch (JSONException e){
            e.printStackTrace();
            showSnackBar("删除失败");
        }

    }
}
