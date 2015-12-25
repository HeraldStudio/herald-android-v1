package cn.seu.herald_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import cn.seu.herald_android.exception.AuthException;
import cn.seu.herald_android.helper.AuthHelper;
import cn.seu.herald_android.mod_auth.LoginActivity;
import cn.seu.herald_android.mod_query.QueryActivity;
import cn.seu.herald_android.mod_settings.SysSettingsActivity;
import cn.seu.herald_android.mod_wifi.NetworkService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private AuthHelper authHelper;
    private Handler initHandler;
    private NavigationView navigationView;
    private TextView tv_hello;
    private TextView tv_nav_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tv_hello = (TextView)findViewById(R.id.tv_main_hello);

        //获取侧边栏布局
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tv_nav_user = (TextView)headerLayout.findViewById(R.id.tv_nav_username);

        //启动自动登录服务

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(sp.getBoolean("autoLogin", SysSettingsActivity.DEFAULT_AUTO_LOGIN))
            startService(new Intent(this, NetworkService.class));

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
        } else if(id == R.id.action_logout){
            authHelper.doLogout();
            checkAuth();
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
            Intent intent = new Intent(MainActivity.this, QueryActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SysSettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);
        } else if (id == R.id.nav_join) {

        } else if (id == R.id.nav_send) {

        }else{

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void init(){
        authHelper = new AuthHelper(MainActivity.this);
        checkAuth();
        refreshUI();
    }

    public void checkAuth(){
        initHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.obj instanceof AuthException)
                {
                    switch (((AuthException) msg.obj).getCode())
                    {
                        case AuthException.ERROR_UUID:
                            Toast.makeText(MainActivity.this,"认证信息已无效请重新登陆",Toast.LENGTH_SHORT).show();
                            break;
                        case AuthException.HAVE_NOT_LOGIN:
                            Toast.makeText(MainActivity.this,"请登录",Toast.LENGTH_SHORT).show();
                            break;
                    }
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }else{
                    refreshUI();
                    Log.d("MainActivity","refresh");
                }
                return false;
            }
        });
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    authHelper.checkAuth();
                } catch (AuthException e) {
                    e.printStackTrace();
                    msg.obj = e;
                }finally {
                    initHandler.sendMessage(msg);
                }
            }
        });
        thread.start();
    }

    public void refreshUI(){
        //设置首页欢迎信息
        tv_hello.setText("你好！" + authHelper.getAuthCache("name") + "同学");
        //设置侧边栏上个人信息
        tv_nav_user.setText(authHelper.getAuthCache("name"));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkAuth();
        refreshUI();
    }
}
