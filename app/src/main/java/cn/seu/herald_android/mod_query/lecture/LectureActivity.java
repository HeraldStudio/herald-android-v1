package cn.seu.herald_android.mod_query.lecture;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

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

    //容纳刷卡记录卡片布局的RecyclerView
    RecyclerView recyclerView_shuaka;
    //用于切换讲座预告和打卡记录的tab
    TabHost tabHost;
    TabWidget tabWidget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);
        init();
        refresh();
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
        //加载tab
        tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        //人文讲座预告的tab
        TabHost.TabSpec specNotice = tabHost.newTabSpec("red");
        //已听讲座的的tab
        TabHost.TabSpec specNums = tabHost.newTabSpec("yellow");
        //tab上的布局加载
        View view_notice = LayoutInflater.from(getBaseContext()).inflate(R.layout.tabitem_lecture_selected,null);
        View view_nums = LayoutInflater.from(getBaseContext()).inflate(R.layout.tabitem_lecture_selected,null);
        TextView tv_tab_notice = (TextView)view_notice.findViewById(R.id.tv_tabitem);
        TextView tv_tab_nums = (TextView)view_nums.findViewById(R.id.tv_tabitem);
        tv_tab_notice.setText("讲座预告");
        tv_tab_nums.setText("刷卡记录");
        specNotice.setIndicator(view_notice);
        specNums.setIndicator(view_nums);

        //设置点击切换
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < tabHost.getTabWidget().getTabCount(); i++) {
                    View child1 = tabHost.getTabWidget().getChildTabViewAt(i);
                    TextView tv_child1 = (TextView) child1.findViewById(R.id.tv_tabitem);
                    tv_child1.setTextColor(getResources().getColor(R.color.colorLecturesecondary_text));
                }
                View child = tabHost.getCurrentTabView();
                TextView tv_child = (TextView) child.findViewById(R.id.tv_tabitem);
                tv_child.setTextColor(getResources().getColor(R.color.colorLectureprimary));
            }
        });
        //设置tab内容
        specNotice.setContent(R.id.widget_layout_red);
        specNums.setContent(R.id.widget_layout_yellow);
        //添加tab
        tabHost.addTab(specNotice);
        tabHost.addTab(specNums);
        //RecyclerView加载
        recyclerView_shuaka = (RecyclerView)findViewById(R.id.recyclerview_lecture_shuaka);
        //设置高度适应设备
        //获取屏幕长度
    }

    public void loadCache(){

    }

    public void refresh(){
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_LECTURE))
                .addParams("uuid",getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getApiHepler().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            //数据解析
                            JSONArray jsonArray = json_res.getJSONObject("content").getJSONArray("detial");
                            //json数组转化并且构造adapter
                            LectureRecordAdapter lectureRecordAdapter = new LectureRecordAdapter(getBaseContext(),
                                    LectureRecordItem.transfromJSONArrayToArrayList(jsonArray));
                            //设置adapter
                            recyclerView_shuaka.setAdapter(lectureRecordAdapter);
                            showMsg("刷卡记录已刷新");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("数据解析出错");
                        }
                    }
                });
    }

}
