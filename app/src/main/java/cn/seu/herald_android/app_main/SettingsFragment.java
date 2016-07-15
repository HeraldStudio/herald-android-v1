package cn.seu.herald_android.app_main;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
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
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_framework.SystemUtil;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_communicate.AboutusActivity;
import cn.seu.herald_android.mod_communicate.FeedbackActivity;

public class SettingsFragment extends Fragment {

    @BindView(R.id.tv_now_version)
    TextView nowVersion;

    View contentView;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.app_main__fragment_settings, null, false);
        unbinder = ButterKnife.bind(this, contentView);
        nowVersion.setText("当前版本： " + SystemUtil.getAppVersionName());
        // seu登录模块设置
        setupSeuSettings();
        // 个性化设置
        setupPersonalSettings();
        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupPersonalSettings(){
        //底部菜单设置
        SwitchButton swith_bottomtab = ButterKnife.findById(contentView, R.id.switchbottomtab);
        swith_bottomtab.setCheckedImmediately(!SettingsHelper.bottomTabEnabled.$get());
        swith_bottomtab.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsHelper.bottomTabEnabled.$set(!isChecked);
            getActivity().recreate();
        });
    }

    private void setupSeuSettings(){
        //seu登录模块设置
        SwitchButton swith_seu = ButterKnife.findById(contentView, R.id.switchseuauto);
        swith_seu.setCheckedImmediately(SettingsHelper.wifiAutoLogin.$get());
        swith_seu.setOnCheckedChangeListener((buttonView, isChecked) ->
                SettingsHelper.wifiAutoLogin.$set(isChecked));
    }

    @OnClick(R.id.tv_aboutus)
    void aboutUs() {
        startActivity(new Intent(getContext(), AboutusActivity.class));
    }

    @OnClick(R.id.tv_feedback)
    void feedback() {
        startActivity(new Intent(getContext(), FeedbackActivity.class));
    }

    @OnClick(R.id.tv_logout)
    void logout() {
        new AlertDialog.Builder(getContext()).setMessage("退出后将自动清除模块缓存，确定退出吗？")
                .setPositiveButton("退出", (d, w) -> ApiHelper.doLogout(null))
                .setNegativeButton("取消", null)
                .show();
    }

    @OnClick(R.id.create_shortcut_wifi)
    void createShortcut() {
        //创捷快捷方式的Intent
        Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getContext(), R.mipmap.ic_wifi); //获取快捷键的图标
        //用于触发自动登录服务的Intent
        Intent seuloginIntent = new Intent();
        seuloginIntent.setAction("cn.seu.herald_android.WIFI_AUTOLOGIN_ACTIVITY");
        addIntent.putExtra("duplicate", false);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "SEU快捷登录");//快捷方式的标题
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);//快捷方式的图标
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, seuloginIntent);//快捷方式的动作
        getContext().sendBroadcast(addIntent);//发送广播
        AppContext.showMessage("已将快捷方式放置至桌面");
    }

    @OnClick(R.id.check_update)
    void checkUpdate() {
        //如果版本有更新则提示更新版本
        int versionCode = SystemUtil.getAppVersionCode();
        int newestCode = ServiceHelper.getNewestVersionCode();

        if (versionCode < newestCode) {
            CacheHelper.set("herald_new_version_ignored", "");

            //如果当前版本号小于最新版本，则提示更新
            String tip = "小猴偷米" + ServiceHelper.getNewestVersionName() + "更新说明\n"
                    + ServiceHelper.getNewestVersionDesc().replaceAll("\\\\n", "\n");

            //显示对话框
            new AlertDialog.Builder(getContext())
                    .setTitle("发现新版本")
                    .setMessage(tip)
                    .setPositiveButton("赶紧下载", (dialog, which) -> {
                        try {
                            Uri uri = Uri.parse("http://android.heraldstudio.com/download");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } catch (Exception e) {
                            AppContext.showMessage("打开下载页失败，请联系管理员~");
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("残忍拒绝", (dialog, which) -> {
                    }).show();
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
                        AppContext.showMessage("已保存为校园网独立账号，建议手动摇一摇测试账号是否有效~");
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
