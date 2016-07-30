package cn.seu.herald_android.app_main;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_module.cardextra.CardActivity;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.custom.TabEntity;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.helper.WifiLoginHelper;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_tabs_pager)
    ViewPager viewPager;
    @BindView(R.id.main_tabs)
    CommonTabLayout bottomTabLayout;
    @BindView(R.id.main_toolbar)
    RelativeLayout mainToolbar;
    @BindView(R.id.tv_login)
    TextView loginBtn;
    @BindView(R.id.blurView)
    BlurView blurView;

    CardsFragment cardsFragment = new CardsFragment();
    ActivitiesFragment afterSchoolFragment = new ActivitiesFragment();
    ModuleListFragment moduleListFragment = new ModuleListFragment();
    SettingsFragment myInfoFragment = new SettingsFragment();

    // 用来接收需要切换首页fragment的广播
    BroadcastReceiver changeMainFragmentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);
        ButterKnife.bind(this);

        // 放置三个Tab页面
        attachFragments();

        // 注册主页页面变化广播接收器
        setupChangeMainFragmentReceiver();

        loadLoginButton();
        ApiHelper.addUserChangedListener(this::loadLoginButton);
    }

    private void loadLoginButton() {
        loginBtn.setOnClickListener(v -> {
            AppContext.showLogin();
        });
        if (ApiHelper.isLogin()) {
            loginBtn.setVisibility(View.GONE);
        } else {
            loginBtn.setVisibility(View.VISIBLE);
        }
    }

    private boolean preventShake = false;

    private ShakeDetector shakeDetector = new ShakeDetector(() -> {
        // 摇一摇自动登陆
        if (SettingsHelper.getWifiAutoLogin() && !preventShake) {
            preventShake = true;
            new WifiLoginHelper(this).checkAndLogin();
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
        // 设置摇晃力度检测阈值
        shakeDetector.setSensitivity(ShakeDetector.SENSITIVITY_LIGHT);
        shakeDetector.start(sensorManager);
    }

    @OnClick(R.id.ibtn_add)
    void setupMoreButton(View btn) {
        float dp = getResources().getDisplayMetrics().density;

        // 快捷方式
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog dialog_more = builder.setCancelable(true).create();

        // 点击外部时关闭
        dialog_more.setCanceledOnTouchOutside(true);
        // show函数需要在getWindow前调用
        dialog_more.show();
        // 对话框窗口设置布局文件
        Window window = dialog_more.getWindow();
        // 自定义背景半透明深度
        window.setDimAmount(0.25f);
        // 防止某些机型对话框显示背景色
        window.setBackgroundDrawable(null);
        WindowManager.LayoutParams wmlp = window.getAttributes();
        wmlp.gravity = Gravity.RIGHT | Gravity.TOP;
        // 此处容易引发布局的ClassCastException，需要注意
        // 所有写死的尺寸一定要乘以dp，否则会出现兼容问题
        wmlp.x = (int) (3 * dp);
        wmlp.y = btn.getLayoutParams().height;
        wmlp.width = (int) (140 * dp);
        window.setAttributes(wmlp);
        window.setContentView(R.layout.app_main__dialog_more);
        // 设置点击项
        window.findViewById(R.id.content_wifi).setOnClickListener(v1 -> {
            // 设置登录校园网
            new WifiLoginHelper(this).checkAndLogin();
        });
        window.findViewById(R.id.content_module_manage).setOnClickListener(v1 ->
                Module.moduleManager.open());
        window.findViewById(R.id.content_charge).setOnClickListener(v1 ->
                new AppModule("一卡通充值", CardActivity.chargeUrl).open());
    }

    private FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        List<Fragment> fragments = Arrays.asList(
                cardsFragment, afterSchoolFragment, moduleListFragment, myInfoFragment
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

        int[] selectedIcons = {
                R.drawable.ic_home_selected,
                R.drawable.ic_activity_selected,
                R.drawable.ic_view_module_selected,
                R.drawable.ic_person_selected
        };

        int[] unselectedIcons = {
                R.drawable.ic_home_unselected,
                R.drawable.ic_activity_unselected,
                R.drawable.ic_view_module_unselected,
                R.drawable.ic_person_unselected
        };

        ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            tabEntities.add(new TabEntity(null, selectedIcons[i], unselectedIcons[i]));
        }

        bottomTabLayout.setTabData(tabEntities);
        for (int i = 0; i < adapter.getCount(); i++) {
            bottomTabLayout.getTitleView(i).setVisibility(View.GONE);
        }
        bottomTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override public void onTabReselect(int position) {}
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override public void onPageSelected(int position) {
                bottomTabLayout.setCurrentTab(position);

                MainActivity.this.setNavigationColorRes(
                        position == 1 ? R.color.colorPrimary : R.color.colorActivitiesPrimary);
            }

            @Override public void onPageScrollStateChanged(int state) {}
        });

        final View decorView = getWindow().getDecorView();
        // Activity's root View. Can also be root View of your layout
        final View rootView = decorView.findViewById(android.R.id.content);
        // set background, if your root layout doesn't have one
        final Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView)
                .windowBackground(windowBackground).blurAlgorithm(new RenderScriptBlur(this, true))
                .blurRadius(16);
    }

    public static void sendChangeMainFragmentBroadcast(int fragmentCode) {
        Intent intent = new Intent();
        intent.putExtra("fragmentCode", fragmentCode);
        intent.setAction("android.intent.action.MAIN.changeMainFragment");
        AppContext.instance.sendBroadcast(intent);
    }

    public void setupChangeMainFragmentReceiver() {
        changeMainFragmentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.MAIN.changeMainFragment")) {
                    int page = intent.getIntExtra("fragmentCode", 0);
                    viewPager.setCurrentItem(page, true);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MAIN.changeMainFragment");
        registerReceiver(changeMainFragmentReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(changeMainFragmentReceiver);
    }

    protected void setNavigationColor(@ColorInt int color) {
        setStatusBarColor(color);
        mainToolbar.setBackgroundColor(color);
    }

    protected void setNavigationColorRes(@ColorRes int color) {
        setNavigationColor(ContextCompat.getColor(this, color));
    }
}
