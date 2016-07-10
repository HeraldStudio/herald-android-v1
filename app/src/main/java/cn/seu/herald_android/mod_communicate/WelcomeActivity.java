package cn.seu.herald_android.mod_communicate;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.SettingsHelper;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_com__welcome);

        Handler handler = new Handler();
        handler.post(() -> {
            //欢迎页：如果已登录则跳转到MainActivity
            //判断是否是首次启动
            int launchtimes = SettingsHelper.launchTimes.get();
            //启动次数递增
            SettingsHelper.launchTimes.set(launchtimes + 1);
            if (ApiHelper.isLogin()) {
                AppContext.showMain();
            } else {
                AppContext.showLogin();
            }
        });
    }
}
