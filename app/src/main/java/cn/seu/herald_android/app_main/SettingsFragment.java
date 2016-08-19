package cn.seu.herald_android.app_main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
                        AppContext.openUrlInBrowser("http://app.heraldstudio.com/download");
                    })
                    .setNegativeButton("残忍拒绝", (dialog, which) -> {}).show();
        } else {
            AppContext.showMessage("当前版本已经是最新版本");
        }
    }

    @OnClick(R.id.tv_share_app)
    void shareApp() {
        // 分享App
        ShareHelper.share("我在使用小猴偷米App，它是东南大学本科生必备的校园生活助手，你也来试试吧：http://app.heraldstudio.com/");
    }
}
