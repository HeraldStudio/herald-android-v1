package cn.seu.herald_android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.mod_query.QueryActivity;
import cn.seu.herald_android.mod_query.grade.GradeActivity;
import cn.seu.herald_android.mod_settings.SysSettingsActivity;

import cn.seu.herald_android.mod_timeline.TimelineView;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;

import okhttp3.Call;
public class MainActivity extends BaseAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navigationView;
    //显示侧边栏欢迎信息的tv
    private TextView tv_nav_user;
    private TextView tv_nav_cardnum;

    //显示推送消息的WebView
    private WebView webView;

    //查询助手快捷方式
    private GridView gv_ShortCutBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SysSettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);
        }else if(id == R.id.action_logout){
            getApiHepler().doLogout();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_info) {
            // Handle the camera action
        } else if (id == R.id.nav_assistant) {
            //打开查询助手
            Intent intent = new Intent(MainActivity.this, QueryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            //打开设置
            Intent intent = new Intent(MainActivity.this, GradeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_join) {
            //显示加入我们对话框

        } else if (id == R.id.nav_send) {
            //打开给我们发送建议的窗口

        } else {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    public void init(){

        //登陆校园wifi
        //
        // checkAndLoginWifi();

        //切换动画
        overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);

        //设置侧边栏
        setupDrawer();

        //刷新个人信息显示的UI
        refreshWelcome();

        // 加载时间轴
        refreshTimelineView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWelcome();
        // 单独重载快捷方式列表
        refreshShortcutBox();
    }

    public void checkAndLoginWifi(){
        NetworkLoginHelper.getInstance(this).checkAndLogin();
        /*if(new CacheHelper(this).getCache("wifi").equals("")){
            new NetworkShortcutHelper(this).addShortcut();
            new CacheHelper(this).setCache("wifi", "created");
        }*/
    }

    public void setupDrawer(){
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //抽屉布局
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //获取侧边栏布局
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tv_nav_user = (TextView)headerLayout.findViewById(R.id.tv_nav_username);

        tv_nav_cardnum = (TextView)headerLayout.findViewById(R.id.tv_nav_usercard);
    }

    public void refreshWelcome(){
        //如果缓存存在的话先设置欢迎信息和侧边栏信息
        tv_nav_user.setText(getApiHepler().getAuthCache("name"));
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_USER))
                .addParams("uuid",getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        //错误检测
                        getApiHepler().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                //如果返回的状态码是200则说明uuid正确，则更新各类个人信息
                                //更新首页欢迎信息和侧边栏信息
                                JSONObject json_content = json_res.getJSONObject("content");
                                getApiHepler().setAuthCache("name", json_content.getString("name"));
                                getApiHepler().setAuthCache("sex", json_content.getString("sex"));
                                getApiHepler().setAuthCache("cardnum", json_content.getString("cardnum"));
                                getApiHepler().setAuthCache("schoolnum", json_content.getString("schoolnum"));
                                tv_nav_user.setText(getApiHepler().getAuthCache("name"));
                                tv_nav_cardnum.setText(getApiHepler().getAuthCache("cardnum"));
                            } else {
                                //如果返回的状态码不是200则说明uuid不对，需要重新授权,则注销当前登录
                                showMsg("登录信息已失效，请重新登录");
                                getApiHepler().doLogout();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void refreshTimelineView(){
        refreshShortcutBox();
        SwipeRefreshLayout srl = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        srl.setColorSchemeResources(R.color.colorPrimary);
        TimelineView view = (TimelineView)findViewById(R.id.timeline);
        view.setActivity(this);
        srl.setOnRefreshListener(() -> {
            view.loadContent(true);
        });
        view.setHideRefresh(() -> new Handler().postDelayed(() -> srl.setRefreshing(false), 1000));
        runMeasurementDependentTask(() -> srl.setRefreshing(true));
        view.loadContent(true);
    }

    private void refreshShortcutBox(){
        TimelineView view = (TimelineView)findViewById(R.id.timeline);
        view.refreshHeaders();
    }
}
