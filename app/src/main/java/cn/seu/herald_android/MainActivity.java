package cn.seu.herald_android;

import android.content.Intent;
import android.os.Bundle;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.QueryActivity;
import cn.seu.herald_android.mod_query.cardextra.CardActivity;
import cn.seu.herald_android.mod_query.emptyroom.EmptyRoomActivity;
import cn.seu.herald_android.mod_query.experiment.ExperimentActivity;
import cn.seu.herald_android.mod_query.grade.GradeActivity;
import cn.seu.herald_android.mod_query.lecture.LectureActivity;
import cn.seu.herald_android.mod_query.schoolbus.SchoolBusActivity;
import cn.seu.herald_android.mod_settings.SysSettingsActivity;
import cn.seu.herald_android.mod_shortcut.ShortCutBoxGridViewAdapter;
import okhttp3.Call;
public class MainActivity extends BaseAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private NavigationView navigationView;
    //显示欢迎信息的tv
    private TextView tv_hello;
    private TextView tv_nav_user;

    //主页轮播栏插件
    private SliderLayout sliderLayout;

    //显示推送消息的WebView
    private WebView webView;

    //查询助手快捷方式
    private GridView gv_ShortCutBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检查登录状态，如果未登录则跳转到
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
            Intent intent = new Intent(MainActivity.this, SchoolBusActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            //打开设置
            Intent intent = new Intent(MainActivity.this, GradeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_join) {
            //显示加入我们对话框

        } else if (id == R.id.nav_send) {
            //打开给我们发送建议的窗口

        }else{

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void init(){

        //切换动画
        overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);

        //设置侧边栏
        setupDrawer();

        //刷新个人信息显示的UI
        refreshUI();

        //轮播栏加载
        setupSliderLayout();

        //webview加载
        //setupWebView();

        //快捷盒子加载
        setupGridViewShortCutBox();
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

        tv_hello = (TextView)findViewById(R.id.tv_main_hello);

        //获取侧边栏布局
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        tv_nav_user = (TextView)headerLayout.findViewById(R.id.tv_nav_username);
    }

    public void setupSliderLayout(){
        sliderLayout = (SliderLayout)findViewById(R.id.sliderlayout_main);
        HashMap<String,String> url_maps = new HashMap<>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        //加载图片
        for(String name : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            sliderLayout.addSlider(textSliderView);
        }
        //设置轮播选项
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        //圆点位置
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        //描述动画
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        //切换间隔
        sliderLayout.setDuration(4000);
    }

    public void setupGridViewShortCutBox(){
        //加载用户自己设置的快捷方式
        gv_ShortCutBox = (GridView)findViewById(R.id.gridview_main_shortcutbox);
        //加载适配器
        //获取设置为快捷方式的查询模块
        ArrayList<SettingsHelper.ShortCutSetting> settingArrayList = getSettingsHelper().getShortCutSettingList();
        ArrayList<HashMap<String, Object>> shorcutHashMapArrayList = new ArrayList<>();
        for(int i=0;i<settingArrayList.size();i++)
        {
            SettingsHelper.ShortCutSetting shortCutSettingItem = settingArrayList.get(i);
            HashMap<String, Object> map = new HashMap<>();
            if(shortCutSettingItem.isEnabled()){
                map.put("ItemIcon", shortCutSettingItem.getIc_id());//添加图标
                map.put("ItemName", shortCutSettingItem.getName());//添加按钮文字
                map.put("Aciton",shortCutSettingItem.getActions());//添加打开模块的动作
                shorcutHashMapArrayList.add(map);
            }
        }
        //生成适配器的元素
        SimpleAdapter saImageItems = new SimpleAdapter(this, shorcutHashMapArrayList,
                R.layout.gridviewitem_shortcut, new String[] {"ItemIcon","ItemName"},
                new int[] {R.id.ic_shortcut,R.id.tv_shortcut});
        //添加并且显示
        gv_ShortCutBox.setAdapter(saImageItems);
        //添加响应
        gv_ShortCutBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获得点击项的map
                HashMap<String, Object> clickItemMap =(HashMap<String, Object>)parent.getItemAtPosition(position);
                Intent intent = new Intent();
                intent.setAction(clickItemMap.get("Aciton").toString());
                startActivity(intent);
            }
        });

}

    public void setupWebView(){
//        webView = (WebView) findViewById(R.id.inform_webview);
//        //WebView加载web资源
//        webView.loadUrl("http://my404.club:3000");
//        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                // TODO Auto-generated method stub
//                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
//                view.loadUrl(url);
//                return true;
//            }
//        });
    }


    public void refreshUI(){
        //如果缓存存在的话先设置欢迎信息和侧边栏信息
        tv_hello.setText("你好！" + getApiHepler().getAuthCache("name") + "同学");
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
                                getApiHepler().setAuthCache("name",json_content.getString("name"));
                                getApiHepler().setAuthCache("sex",json_content.getString("sex"));
                                getApiHepler().setAuthCache("cardnum",json_content.getString("cardnum"));
                                getApiHepler().setAuthCache("schoolnum",json_content.getString("schoolnum"));
                                tv_hello.setText("你好！" + getApiHepler().getAuthCache("name") + "同学");
                                tv_nav_user.setText(getApiHepler().getAuthCache("name"));
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

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshUI();
    }


    //以下是所用轮播栏插件的相关接口
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //轮播栏滑动时的动作监听
    }

    @Override
    public void onPageSelected(int position) {
        //轮播栏被选中时的动作
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }
}
