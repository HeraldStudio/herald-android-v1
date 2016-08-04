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
import cn.seu.herald_android.app_secondary.AboutUsActivity;
import cn.seu.herald_android.app_secondary.FeedbackActivity;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.SystemUtil;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;

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

    @OnClick(R.id.tv_aboutus)
    void aboutUs() {
        AppContext.startActivitySafely(AboutUsActivity.class);
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

    @OnClick(R.id.check_update)
    void checkUpdate() {
        // 如果版本有更新则提示更新版本
        int versionCode = SystemUtil.getAppVersionCode();
        int newestCode = ServiceHelper.getNewestVersionCode();

        if (versionCode < newestCode) {
            CacheHelper.set("herald_new_version_ignored", "");

            // 如果当前版本号小于最新版本，则提示更新
            String tip = "小猴偷米" + ServiceHelper.getNewestVersionName() + "更新说明\n"
                    + ServiceHelper.getNewestVersionDesc().replaceAll("\\\\n", "\n");

            // 显示对话框
            new AlertDialog.Builder(getContext())
                    .setTitle("发现新版本")
                    .setMessage(tip)
                    .setPositiveButton("赶紧下载", (dialog, which) -> {
                        AppContext.openUrlInBrowser("http://android.heraldstudio.com/download");
                    })
                    .setNegativeButton("残忍拒绝", (dialog, which) -> {}).show();
        } else {
            AppContext.showMessage("当前版本已经是最新版本");
        }
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
