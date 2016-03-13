package cn.seu.herald_android.mod_query.lecture;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import okhttp3.Call;

public class LectureActivity extends BaseAppCompatActivity {

    //容纳讲座预告卡片布局的RecyclerView
    RecyclerView recyclerView_notice;
    //打卡记录对话框
    AlertDialog.Builder builder;
    AlertDialog dialog_lecture_records;
    //打卡记录列表
    ListView list_record;
    //打卡记录次数
    TextView tv_count;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);
        init();
        //由于此模块实时性要求较高，每次打开实时刷新
        refreshCache();
    }

    private void init(){
        //设置工具栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });


        //沉浸式工具栏
        setStatusBarColor(this, getResources().getColor(R.color.colorLectureprimary));

        //设置伸缩标题禁用
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);


        //RecyclerView加载
        recyclerView_notice = (RecyclerView)findViewById(R.id.recyclerview_lecture_notice);
        recyclerView_notice.setLayoutManager(new LinearLayoutManager(this));

        //加载对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("最新讲座消息获取中");
        progressDialog.setMessage("请稍后...");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lecture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_lecturerecord){
            //展示刷卡记录
            displayLectureRecords();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNoticeCache(){
        //尝试从缓存加载讲座预告
        String cache =  getCacheHelper().getCache("herald_lecture_notices");
        if(!cache.equals("")){
            try{
                //数据解析
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                //json数组转化并且构造adapter
                LectureNoticeAdapter lectureNoticeAdapter = new LectureNoticeAdapter(getBaseContext(),
                        LectureNoticeItem.transfromJSONArrayToArrayList(jsonArray));
                //设置adapter
                recyclerView_notice.setAdapter(lectureNoticeAdapter);
                //刷新打卡记录缓存
            }catch (JSONException e){
                e.printStackTrace();
                showMsg("缓存解析出错，请刷新后再试。");
            }

        }else{
            showMsg("暂无缓存或者缓存已失效，请重新刷新。");
        }

    }

    public void refreshCache(){
        progressDialog.show();
        //获取讲座预告
        OkHttpUtils
                .post()
                .url(ApiHelper.wechat_lecture_notice_url)
                .addParams("uuid", getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getApiHepler().dealApiException(e);
                        progressDialog.dismiss();
                        showMsg("由于网络错误获取最新讲座预告失败，已加载缓存。");
                        loadNoticeCache();
                    }

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200){
                                getCacheHelper().setCache("herald_lecture_notices", json_res.toString());
                                if(json_res.getJSONArray("content").length() == 0) {
                                    showMsg("最近暂无讲座预告信息");
                                } else {
                                    showMsg("已获取最新讲座预告");
                                }
                                loadNoticeCache();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("数据解析出错");
                        }
                    }
                });
    }

    public static void remoteRefreshCache(Context context, Runnable doAfter){
        ApiHelper apiHelper = new ApiHelper(context);
        CacheHelper cacheHelper = new CacheHelper(context);
        //获取讲座预告
        OkHttpUtils
                .post()
                .url(ApiHelper.wechat_lecture_notice_url)
                .addParams("uuid", apiHelper.getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        apiHelper.dealApiException(e);
                        doAfter.run();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200){
                                cacheHelper.setCache("herald_lecture_notices", json_res.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        doAfter.run();
                    }
                });
    }

    public void displayLectureRecords(){
        //加载打卡记录对话框
        builder = new AlertDialog.Builder(this);
        dialog_lecture_records = builder.create();
        //show函数需要在getWindow前调用
        dialog_lecture_records.show();
        //对话框窗口设置布局文件
        Window window = dialog_lecture_records.getWindow();
        window.setContentView(R.layout.content_dialog_lecture_record);

        //获取对话窗口中的listview
        list_record = (ListView)window.findViewById(R.id.list_lecture_record);
        //获得对话框中的打卡次数textview
        tv_count = (TextView)window.findViewById(R.id.tv_recordcount);

        //加载讲座记录时显示刷新框
        progressDialog.show();
        //获取已听讲座
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_LECTURE))
                .addParams("uuid", getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getApiHepler().dealApiException(e);
                        progressDialog.dismiss();
                        showMsg("由于网络错误，获取最新讲座记录失败");
                    }

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200){
                                getCacheHelper().setCache("herald_lecture_records", json_res.toString());
                                loadRecordCache();
                                showMsg("获取讲座记录成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("数据解析出错");
                        }
                    }
                });
    }

    public void loadRecordCache(){
        String cache = getCacheHelper().getCache("herald_lecture_records");
        if(!cache.equals("")){
            try {
                //设置打卡次数
                int count = new JSONObject(cache).getJSONObject("content").getInt("count");
                tv_count.setText(count+"");
                //设置列表
                JSONArray jsonArray = new JSONObject(cache).getJSONObject("content").getJSONArray("detial");
                list_record.setAdapter(new LectureRecordAdapter(
                        getBaseContext(),
                        R.layout.listviewitem_lecture_record,
                        LectureRecordItem.transfromJSONArrayToArrayList(jsonArray)));
            }catch (JSONException e){
                e.printStackTrace();
                showMsg("缓存解析失败，请刷新后再试。");
            }
        }
    }



}
