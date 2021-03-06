package cn.seu.herald_android.app_main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_secondary.FeedbackActivity;
import cn.seu.herald_android.app_secondary.WebModuleActivity;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.SystemUtil;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.helper.ShareHelper;

public class SettingsFragment extends Fragment implements ApiHelper.OnUserChangeListener {

    @BindView(R.id.tv_now_version)
    TextView nowVersion;

    @BindView(R.id.tv_login_or_logout)
    TextView loginOrLogoutText;

    private View contentView;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.app_main__fragment_settings, null, false);
        unbinder = ButterKnife.bind(this, contentView);
        nowVersion.setText("当前版本： " + SystemUtil.getAppVersionName());

        loadData();

        // 注册用户改变事件
        ApiHelper.registerOnUserChangeListener(this);
        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 防泄漏
        ApiHelper.unregisterOnUserChangeListener(this);
        unbinder.unbind();
    }

    @Override
    public void onUserChange() {
        loadData();
    }

    private void loadData(){

        // 初始化登录按钮文字
        loginOrLogoutText.setText(ApiHelper.isLogin() ? "退出登录" : "登录");

        SwitchButton switch_seu = ButterKnife.findById(contentView, R.id.switchseuauto);
        switch_seu.setCheckedImmediately(SettingsHelper.getWifiAutoLogin());
        switch_seu.setOnCheckedChangeListener((buttonView, isChecked) ->
                SettingsHelper.setWifiAutoLogin(isChecked));
    }

    @OnClick(R.id.aboutus)
    void aboutUs() {
        WebModuleActivity.startWebModuleActivity("关于小猴",
                "http://app.heraldstudio.com/about.htm?type=android");
    }

    @OnClick(R.id.tv_feedback)
    void feedback() {
        AppContext.startActivitySafely(FeedbackActivity.class);
    }

    @OnClick(R.id.tv_login_or_logout)
    void loginOrLogout() {
        if (ApiHelper.isLogin()) {

            // 退出登录
            new AlertDialog.Builder(getContext()).setMessage("确定要退出登录吗？")
                    .setPositiveButton("退出", (d, w) -> ApiHelper.doLogout(null))
                    .setNegativeButton("取消", null)
                    .show();
        } else {

            // 登录
            AppContext.showLogin();
        }
    }

    @OnClick(R.id.tv_share_app)
    void shareApp() {
        // 分享App
        ShareHelper.share("我在使用小猴偷米App，它是东南大学本科生必备的校园生活助手，你也来试试吧：http://app.heraldstudio.com/");
    }

    @OnClick(R.id.custom_account)
    void setCustomAccount() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_settings__dialog_wifi_set_auth, null);
        EditText et = (EditText) v.findViewById(R.id.et_username);
        et.setText(ApiHelper.getWifiUserName());
        EditText pw = (EditText) v.findViewById(R.id.et_pwd);//密码框不设置初始值，防止密码位数泄露

        new AlertDialog.Builder(getContext()).setTitle("设置校园网独立账号")
                .setView(v)
                .setPositiveButton("保存", (dialog, which) -> {
                    if (!et.getText().toString().equals("") && !pw.getText().toString().equals("")) {
                        ApiHelper.setWifiAuth(et.getText().toString(), pw.getText().toString());
                        AppContext.showMessage("已保存为校园网独立账号，建议手动测试账号是否有效~");
                    } else {
                        AppContext.showMessage("你没有更改设置");
                    }
                })
                .setNeutralButton("恢复默认", (dialog, which) -> {
                    ApiHelper.clearWifiAuth();
                    AppContext.showMessage("已恢复默认校园网账号设置");
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
