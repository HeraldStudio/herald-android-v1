package cn.seu.herald_android.mod_query.lecture;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

public class LectureActivity extends BaseAppCompatActivity {

    //容纳讲座预告卡片布局的RecyclerView
    RecyclerView recyclerView_notice;
    //打卡记录对话框
    AlertDialog.Builder builder;
    AlertDialog dialog;
    //记录列表
    ListView list_record;
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
        setStatusBarColor(this,getResources().getColor(R.color.colorLectureprimary));

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

    private void loadCache(){

    }

    public void refreshCache(){
        progressDialog.show();
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
                    }

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject json_res = new JSONObject(response);
                            //数据解析
                            JSONArray jsonArray = json_res.getJSONObject("content").getJSONArray("detial");
                            //json数组转化并且构造adapter
                            LectureAdapter lectureAdapter = new LectureAdapter(getBaseContext(),
                                    LectureItem.transfromJSONArrayToArrayList(jsonArray));
                            //设置adapter
                            recyclerView_notice.setAdapter(lectureAdapter);
                            //刷新打卡记录缓存
                            getCacheHelper().setCache("herald_lecture_records", jsonArray.toString());
                            showMsg("已获取最新讲座预告");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("数据解析出错");
                        }
                    }
                });
    }

    public void displayLectureRecords(){
        //打卡记录对话框加载
        builder = new AlertDialog.Builder(this);
        dialog = builder.create();
        dialog.show();

        //对话框窗口设置布局文件
        Window window = dialog.getWindow();
        window.setContentView(R.layout.content_dialog_lecture_record);

        //获取对话窗口中的listview
        list_record = (ListView)window.findViewById(R.id.list_lecture_record);

        //获取缓存记录
        String cache = getCacheHelper().getCache("herald_lecture_records");
        if(!cache.equals("")){
            try {
                JSONArray jsonArray = new JSONArray(cache);
                list_record.setAdapter(new LectureRecordAdapter(
                        getBaseContext(),
                        R.layout.listviewitem_lecture_record,
                        LectureItem.transfromJSONArrayToArrayList(jsonArray)));
            }catch (JSONException e){
                e.printStackTrace();
                showMsg("缓存解析失败，请重试。");
            }

        }
    }



}
