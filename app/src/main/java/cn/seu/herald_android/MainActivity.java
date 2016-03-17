package cn.seu.herald_android;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.mod_communicate.AboutusActivity;
import cn.seu.herald_android.mod_communicate.FeedbackActivity;
import cn.seu.herald_android.mod_query.QueryActivity;
import cn.seu.herald_android.mod_settings.SysSettingsActivity;
import cn.seu.herald_android.mod_timeline.TimelineView;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;
import okhttp3.Call;

public class MainActivity extends BaseAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //显示侧边栏欢迎信息的tv
    private TextView tv_nav_user;

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
        } else if (id == R.id.action_logout) {
            getApiHelper().doLogout();
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
            //注销登录
            getApiHelper().doLogout();
        } else if (id == R.id.nav_assistant) {
            //打开查询助手
            Intent intent = new Intent(MainActivity.this, QueryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            //打开设置
            Intent intent = new Intent(MainActivity.this, SysSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_aboutus) {
            //显示关于我们
            Intent intent = new Intent(MainActivity.this, AboutusActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            //打开用户反馈
            Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gymorder) {
            //场馆预约
            Uri uri = Uri.parse("http://115.28.27.150/heraldapp/#/yuyue/home");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.nav_quanyi) {
            //权益部门
            Uri uri = Uri.parse("https://jinshuju.net/f/By3aTK");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void init() {

        //登陆校园wifi
        if (getSettingsHelper().getWifiAutoLogin()) {
            checkAndLoginWifi();
        }


        //切换动画
        overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);

        //设置侧边栏
        setupDrawer();

        //刷新个人信息显示的UI
        refreshWelcome();
        // 在线刷新时间轴及快捷方式
        loadTimelineView(true);
        //检查版本更新，获取推送消息
        checkVersionAndPushMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWelcome();
        // 本地重载时间轴及快捷方式
        loadTimelineView(false);
    }

    private void checkAndLoginWifi() {
        NetworkLoginHelper.getInstance(this).checkAndLogin();
    }

    private void setupDrawer() {
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //抽屉布局
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //获取侧边栏布局
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tv_nav_user = (TextView) headerLayout.findViewById(R.id.tv_nav_username);

        //tv_nav_cardnum = (TextView) headerLayout.findViewById(R.id.tv_nav_usercard);
    }

    private void refreshWelcome() {
        //如果缓存存在的话先设置欢迎信息和侧边栏信息
        tv_nav_user.setText(getApiHelper().getAuthCache("name"));
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_USER))
                .addParams("uuid", getApiHelper().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        //错误检测
                        getApiHelper().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                //如果返回的状态码是200则说明uuid正确，则更新各类个人信息
                                //更新首页欢迎信息和侧边栏信息
                                JSONObject json_content = json_res.getJSONObject("content");
                                getApiHelper().setAuthCache("name", json_content.getString("name"));
                                getApiHelper().setAuthCache("sex", json_content.getString("sex"));
                                getApiHelper().setAuthCache("cardnum", json_content.getString("cardnum"));
                                getApiHelper().setAuthCache("schoolnum", json_content.getString("schoolnum"));
                                tv_nav_user.setText(getApiHelper().getAuthCache("name"));
                                //tv_nav_cardnum.setText(getApiHelper().getAuthCache("cardnum"));
                            } else {
                                //如果返回的状态码不是200则说明uuid不对，需要重新授权,则注销当前登录
                                showMsg("登录信息已失效，请重新登录");
                                getApiHelper().doLogout();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    // 刷新时间轴和快捷方式
    // refresh 是否联网刷新
    public void loadTimelineView(boolean refresh) {
        SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        srl.setColorSchemeResources(R.color.colorPrimary);
        TimelineView view = (TimelineView) findViewById(R.id.timeline);
        view.setActivity(this);
        srl.setOnRefreshListener(() -> view.loadContent(true));
        view.setHideRefresh(() -> new Handler().postDelayed(() -> srl.setRefreshing(false), 1000));
        if (refresh)
            runMeasurementDependentTask(() -> srl.setRefreshing(true));
        // 快捷方式刷新在这里
        view.loadContent(refresh);
    }


    private void checkVersionAndPushMessage() {
        //拉取最新版本信息、轮播图url和推送消息
        getServiceHelper().refreshVersionCache();
    }
}
