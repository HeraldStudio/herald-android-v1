package cn.seu.herald_android.app_module.lecture;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.BaseActivity;

public class LectureActivity extends BaseActivity {

    // 容纳讲座预告卡片布局的RecyclerView
    @BindView(R.id.recyclerview_lecture_notice)
    RecyclerView recyclerView_notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_lecture);
        ButterKnife.bind(this);

        recyclerView_notice.setLayoutManager(new LinearLayoutManager(this));

        refreshCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lecture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_lecturerecord) {
            // 展示刷卡记录
            displayLectureRecords();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNoticeCache() {
        // 尝试从缓存加载讲座预告
        String cache = Cache.lectureNotices.getValue();
        if (!cache.equals("")) {
            try {
                // 数据解析
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                // json数组转化并且构造adapter
                LectureNoticeAdapter lectureNoticeAdapter = new LectureNoticeAdapter(getBaseContext(),
                        LectureNoticeModel.transformJSONArrayToArrayList(jsonArray));
                // 设置adapter
                recyclerView_notice.setAdapter(lectureNoticeAdapter);
                // 刷新打卡记录缓存
            } catch (JSONException e) {
                e.printStackTrace();
                showSnackBar("解析失败，请刷新");
            }
        } else {
            showSnackBar("解析失败，请刷新");
        }
    }

    private void refreshCache() {
        showProgressDialog();

        // 获取讲座预告
        Cache.lectureNotices.refresh((success, code) -> {
            hideProgressDialog();
            if (success) {
                loadNoticeCache();
            } else {
                showSnackBar("刷新失败，请重试");
            }
        });
    }

    private void displayLectureRecords() {
        // 加载打卡记录对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog_lecture_records = builder.create();
        // show函数需要在getWindow前调用
        dialog_lecture_records.show();
        // 对话框窗口设置布局文件
        Window window = dialog_lecture_records.getWindow();
        window.setContentView(R.layout.mod_que_lecture__dialog_lecture_record);

        // 获取对话窗口中的listview
        ListView list_record = (ListView) window.findViewById(R.id.list_lecture_record);
        // 获得对话框中的打卡次数textview
        TextView tv_count = (TextView) window.findViewById(R.id.tv_recordcount);

        // 加载讲座记录时显示刷新框
        showProgressDialog();
        // 获取已听讲座
        Cache.lectureRecords.refresh((success, code) -> {
                    hideProgressDialog();
                    if (success) {
                        String cache = Cache.lectureRecords.getValue();
                        if (!cache.equals("")) {
                            try {
                                // 设置打卡次数
                                int count = new JSONObject(cache).getJSONObject("content").getInt("count");
                                tv_count.setText(count + "");
                                // 设置列表
                                JSONArray jsonArray = new JSONObject(cache).getJSONObject("content").getJSONArray("detial");
                                list_record.setAdapter(new LectureRecordAdapter(
                                        getBaseContext(),
                                        R.layout.mod_que_lecture__dialog_lecture_record__item,
                                        LectureRecordModel.transformJSONArrayToArrayList(jsonArray)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showSnackBar("解析失败，请刷新");
                            }
                        }
                        // showSnackBar("获取讲座记录成功");
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
        });
    }
}
