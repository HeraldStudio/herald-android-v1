package cn.seu.herald_android.mod_query.exam;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import cn.seu.herald_android.R;
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
        String cache = getCacheHelper().getCache("herald_exam");
        if (cache.equals("")) {
            refreshCache();
            return;
        }

        try {
            List<ExamItem> exams = ExamItem.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new ExamAdapter(exams));
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
        String cache = new CacheHelper(host.getContext()).getCache("herald_exam");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            List<ExamItem> examList = new ArrayList<>();
            List<ExamItem> temp = ExamItem.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            for (ExamItem examItem : temp) {
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
}
