package cn.seu.herald_android.mod_communicate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_auth.LoginActivity;

public class WelcomeActivity extends Activity {
    private SettingsHelper settingsHelper;
    private ApiHelper apiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        settingsHelper = new SettingsHelper(this);
        apiHelper = new ApiHelper(this);

        Handler handler = new Handler();
        handler.post(() -> {
            //欢迎页：如果已登录则跳转到MainActivity
            //判断是否是首次启动
            int launchtimes = settingsHelper.getLaunchTimes();
            if (0 == launchtimes) {
                //是第一次启动则启用默认设置
                settingsHelper.setDefaultConfig();
            }
            //启动次数递增
            settingsHelper.updateLaunchTimes(launchtimes + 1);
            if (apiHelper.isLogin()) {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
