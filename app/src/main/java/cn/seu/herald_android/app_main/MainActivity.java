package cn.seu.herald_android.app_main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;
import okhttp3.Call;

public class MainActivity extends BaseAppCompatActivity {

    CardsFragment cardsFragment = new CardsFragment();

    ModuleListFragment moduleListFragment = new ModuleListFragment();

    MyInfoFragment myInfoFragment = new MyInfoFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(" " + getTitle().toString().trim());

        //登陆校园wifi
        if (getSettingsHelper().getWifiAutoLogin()) {
            checkAndLoginWifi();
        }

        //切换动画
        overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);

        //检查个人信息
        checkAuth();

        //放置三个Tab页面
        attachFragments();

        runMeasurementDependentTask(() -> {
            //刷新卡片视图
            cardsFragment.loadTimelineView(true);

            //刷新模块视图
            moduleListFragment.loadModuleList();
        });

        //检查版本更新，获取推送消息
        checkVersionAndPushMessage();
    }


    private void checkAndLoginWifi() {
        NetworkLoginHelper.getInstance(this).checkAndLogin();
    }

    private void checkAuth() {
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

    private FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        List<Fragment> fragments = Arrays.asList(
                cardsFragment, moduleListFragment, myInfoFragment
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
            return new String[]{"首页", "模块", "我的"}[position];
        }
    };

    private void attachFragments() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void checkVersionAndPushMessage() {
        //拉取最新版本信息、轮播图url和推送消息
        getServiceHelper().refreshVersionCache();
    }

    public void syncModuleSettings() {
        cardsFragment.loadTimelineView(false);
        moduleListFragment.loadModuleList();
    }
}
