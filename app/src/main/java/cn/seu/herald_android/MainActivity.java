package cn.seu.herald_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import cn.seu.herald_android.exception.AuthException;
import cn.seu.herald_android.helper.AuthHelper;
import cn.seu.herald_android.mode_auth.LoginActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private AuthHelper authHelper;
    private Handler initHandler;
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            return true;
        }else if(id == R.id.action_logout){
            authHelper.doLogout();
            checkAuth();
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

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }else{

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void init(){
        authHelper = new AuthHelper(MainActivity.this);
        refreshUI();
        checkAuth();
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
        TextView tv_hello = (TextView)findViewById(R.id.tv_main_hello);
        tv_hello.setText("你好！"+authHelper.getAuthCache("name")+"同学");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshUI();
    }
}
