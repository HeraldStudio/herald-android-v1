package cn.seu.herald_android.app_main;

import android.animation.ArgbEvaluator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.squareup.seismic.ShakeDetector;

import java.util.Arrays;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_modulemanager.ModuleManageActivity;
import cn.seu.herald_android.helper.NetworkLoginHelper;
import me.majiajie.pagerbottomtabstrip.Controller;
import me.majiajie.pagerbottomtabstrip.PagerBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.TabItemBuilder;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectListener;

public class MainActivity extends BaseActivity {

    CardsFragment cardsFragment = new CardsFragment();

    ActivityFragment afterSchoolFragment = new ActivityFragment();

    ModuleListFragment moduleListFragment = new ModuleListFragment();

    SettingsFragment myInfoFragment = new SettingsFragment();

    //首页tab和viewPager
    ViewPager viewPager;
    PagerBottomTabLayout pagerBottomTabLayout;
    TabLayout topTabLayout;

    //用来接收需要切换首页fragment的广播
    BroadcastReceiver changeMainFragmentReceiver;

    //首页的假toolbar
    RelativeLayout main_toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);
        init();
    }

    private boolean preventShake = false;

    private ShakeDetector shakeDetector = new ShakeDetector(() -> {
        //摇一摇自动登陆
        if (SettingsHelper.wifiAutoLogin.get() && !preventShake) {
            preventShake = true;
            new NetworkLoginHelper(this).checkAndLogin();
            new Handler().postDelayed(() -> preventShake = false, 2000);
        }
    });

    @Override
    protected void onPause() {
        super.onPause();
        shakeDetector.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //设置摇晃力度检测阈值
        shakeDetector.setSensitivity(ShakeDetector.SENSITIVITY_LIGHT);
        shakeDetector.start(sensorManager);
    }

    private void init() {
        //Toolbar初始化
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        setTitle(" " + getTitle().toString().trim());

        //设置状态栏颜色
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        //控件初始化
        viewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        pagerBottomTabLayout = (PagerBottomTabLayout)findViewById(R.id.main_tabs);
        topTabLayout = (TabLayout)findViewById(R.id.main_toptabs);


        setupPagerBottomTabLayout();

        //放置三个Tab页面
        attachFragments();

        //注册主页页面变化广播接收器
        setupChangeMainFragmentReceiver();

        //设置右上角加号按钮
        setupMoreButton();

        runMeasurementDependentTask(() -> {
            //刷新卡片视图
            cardsFragment.loadTimelineView(true);
            //刷新模块视图
            moduleListFragment.loadModuleList();
        });


    }

    private void setupMoreButton(){
        //右上角加号按钮弹出
        ImageButton ibtn = (ImageButton)findViewById(R.id.ibtn_add);
        if (ibtn != null) {
            ibtn.setOnClickListener(v -> {

                float dp = getResources().getDisplayMetrics().density;

                //快捷方式
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog dialog_more = builder.setCancelable(true).create();
                //点击外部时关闭
                dialog_more.setCanceledOnTouchOutside(true);
                //show函数需要在getWindow前调用
                dialog_more.show();
                //对话框窗口设置布局文件
                Window window = dialog_more.getWindow();
                //自定义背景半透明深度
                window.setDimAmount(0.25f);
                //防止某些机型对话框显示背景色
                window.setBackgroundDrawable(null);
                WindowManager.LayoutParams wmlp = window.getAttributes();
                wmlp.gravity = Gravity.RIGHT | Gravity.TOP;
                //此处容易引发布局的ClassCastException，需要注意
                //所有写死的尺寸一定要乘以dp，否则会出现兼容问题
                wmlp.x = (int)(3 * dp);
                wmlp.y = ibtn.getLayoutParams().height;
                wmlp.width = (int)(140 * dp);
                window.setAttributes(wmlp);
                window.setContentView(R.layout.app_main__dialog_more);
                //设置点击项
                window.findViewById(R.id.content_wifi).setOnClickListener(v1 -> {
                    //设置登录校园网
                    new NetworkLoginHelper(this).checkAndLogin();
                });
                window.findViewById(R.id.content_module_manage).setOnClickListener(v1 -> {
                    //设置打开模块管理
                    startActivity(new Intent(MainActivity.this, ModuleManageActivity.class));
                });
                window.findViewById(R.id.content_charge).setOnClickListener(v1 -> {
                    //打开充值页面
                    Uri uri = Uri.parse("http://58.192.115.47:8088/wechat-web/login/initlogin.html");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });


            });
        }
    }

    private void setupPagerBottomTabLayout(){
        if (SettingsHelper.bottomTabEnabled.get()){
            pagerBottomTabLayout.setVisibility(View.VISIBLE);
            topTabLayout.setVisibility(View.GONE);
        }else {
            pagerBottomTabLayout.setVisibility(View.GONE);
            topTabLayout.setVisibility(View.VISIBLE);
        }
    }

    private FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        List<Fragment> fragments = Arrays.asList(
                cardsFragment,afterSchoolFragment,moduleListFragment, myInfoFragment
        );

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    };

    private void attachFragments() {
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);//三个页面都不回收，提高流畅性
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin((int) (3 * getResources().getDisplayMetrics().density));

        topTabLayout.setupWithViewPager(viewPager);


        //各碎片设置状态栏颜色渐变
        int[] statusColors = new int[]{
                ContextCompat.getColor(getBaseContext(),R.color.colorFragmentCards),
                ContextCompat.getColor(getBaseContext(),R.color.colorFragmentActivitys),
                ContextCompat.getColor(getBaseContext(),R.color.colorFragmentModules),
                ContextCompat.getColor(getBaseContext(),R.color.colorFragmentSettings)};


        //各页面Tab设置
        TabItemBuilder tabItemBuilder0 = new TabItemBuilder(this).create()
                .setDefaultIcon(R.drawable.ic_home_24dp).setText("首页").setSelectedColor(statusColors[0]).build();
        TabItemBuilder tabItemBuilder1 = new TabItemBuilder(this).create()
                .setDefaultIcon(R.drawable.ic_explore).setText("发现").setSelectedColor(statusColors[1]).build();
        TabItemBuilder tabItemBuilder2 = new TabItemBuilder(this).create()
                .setDefaultIcon(R.drawable.ic_view_module_24dp).setText("模块").setSelectedColor(statusColors[2]).build();
        TabItemBuilder tabItemBuilder3 = new TabItemBuilder(this).create()
                .setDefaultIcon(R.drawable.ic_person_24dp).setText("我的").setSelectedColor(statusColors[3]).build();

        //创建控制器
        Controller controller = pagerBottomTabLayout
                .builder()
                .addTabItem(tabItemBuilder0)
                .addTabItem(tabItemBuilder1)
                .addTabItem(tabItemBuilder2)
                .addTabItem(tabItemBuilder3)
                .build();

        controller.addTabItemClickListener(new OnTabItemSelectListener() {
            @Override
            public void onSelected(int index, Object tag) {
                viewPager.setCurrentItem(index,true);
            }

            @Override
            public void onRepeatClick(int index, Object tag) {

            }
        });

        //viewpage滑动时设置底部tab
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //设置滑动时状态栏颜色渐变
                ArgbEvaluator evaluator = new ArgbEvaluator();
                int colorOld= statusColors[position];
                int colorNew = statusColors[(position+1)%statusColors.length];
                int evaluate = (Integer) evaluator.evaluate(positionOffset, colorOld,colorNew);
                setNavigationColor(evaluate);
            }

            @Override
            public void onPageSelected(int position) {
                controller.setSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public static int MAIN_FRAGMENT_CARDS = 0;
    public static int MAIN_FRAGMENT_ACTIVITYS = 1;
    public static int MAIN_FRAGMENT_MODULES = 2;
    public static int MAIN_FRAGMENT_SETTTINGS = 3;

    public static void sendChangeMainFragmentBroadcast(Context context,int fragmentCode){
        Intent intent = new Intent();
        intent.putExtra("fragmentCode",fragmentCode);
        intent.setAction("android.intent.action.MAIN.changeMainFragment");
        context.sendBroadcast(intent);
    }

    public void setupChangeMainFragmentReceiver(){
        changeMainFragmentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.MAIN.changeMainFragment")){
                    int page = intent.getIntExtra("fragmentCode",0);
                    viewPager.setCurrentItem(page,true);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MAIN.changeMainFragment");
        registerReceiver(changeMainFragmentReceiver,intentFilter);
    }

    public void syncModuleSettings() {
        cardsFragment.loadTimelineView(false);
        moduleListFragment.loadModuleList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(changeMainFragmentReceiver);
    }

    protected void setNavigationColor(int color) {
        setStatusBarColor(color);
        main_toolbar = (RelativeLayout)findViewById(R.id.main_toolbar);
        if (main_toolbar != null) {
            main_toolbar.setBackgroundColor(color);
        }
    }
}
