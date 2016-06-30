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

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_communicate.AboutusActivity;
import cn.seu.herald_android.mod_communicate.FeedbackActivity;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;

public class MyInfoFragment extends Fragment {

    private View contentView;

    private TextView tv_nowversion;
    private SwitchButton swith_seu;
    private SwitchButton swith_bottomtab;
    private ServiceHelper serviceHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_main_my_info, null, false);

        serviceHelper = new ServiceHelper(getContext());

        View check_update = contentView.findViewById(R.id.check_update);
        check_update.setOnClickListener(v -> checkUpdate());

        View custom_account = contentView.findViewById(R.id.custom_account);
        custom_account.setOnClickListener(v -> setCustomAccount());

        tv_nowversion = (TextView) contentView.findViewById(R.id.tv_now_version);
        tv_nowversion.setText("当前版本： " + ServiceHelper.getAppVersionName(getContext()));

        contentView.findViewById(R.id.tv_aboutus).setOnClickListener((v) -> {
            startActivity(new Intent(getContext(), AboutusActivity.class));
        });

        contentView.findViewById(R.id.tv_feedback).setOnClickListener((v) -> {
            startActivity(new Intent(getContext(), FeedbackActivity.class));
        });

        contentView.findViewById(R.id.tv_logout).setOnClickListener((v) -> {
            new AlertDialog.Builder(getContext()).setMessage("退出后将自动清除模块缓存，确定退出吗？")
                    .setPositiveButton("退出", (d, w) -> new ApiHelper(getContext()).doLogout())
                    .setNegativeButton("取消", null)
                    .show();
        });

        contentView.findViewById(R.id.create_shortcut_wifi).setOnClickListener(v -> {
            //创捷快捷方式的Intent
            Intent addIntent=new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            Parcelable icon=Intent.ShortcutIconResource.fromContext(getContext(), R.mipmap.ic_wifi); //获取快捷键的图标
            //用于触发自动登录服务的Intent
            Intent seuloginIntent = new Intent();
            seuloginIntent.setAction("cn.seu.herald_android.WIFI_AUTOLOGIN_ACTIVITY");
            addIntent.putExtra("duplicate", false);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "SEU快捷登录");//快捷方式的标题
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);//快捷方式的图标
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, seuloginIntent);//快捷方式的动作
            getContext().sendBroadcast(addIntent);//发送广播
            ContextUtils.showMessage(getContext(),"已将快捷方式放置至桌面");
        });

        //seu登录模块设置
        setupSeuSettings();

        //个性化设置
        setupPersonalSettings();

        //刷新用户名
        refreshUsername();

        return contentView;
    }

    private void setupPersonalSettings(){
        //底部菜单设置
        swith_bottomtab = (SwitchButton) contentView.findViewById(R.id.switchbottomtab);
        swith_bottomtab = (SwitchButton) contentView.findViewById(R.id.switchbottomtab);
        swith_bottomtab.setCheckedImmediately(!new SettingsHelper(getContext()).getBottomTabEnabled());
        swith_bottomtab.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new SettingsHelper(getContext()).setBottomTabEnabled(!isChecked);
        });
        contentView.findViewById(R.id.switch_container).setOnClickListener(v -> swith_bottomtab.toggle());
    }


    private void setupSeuSettings(){
        //seu登录模块设置
        swith_seu = (SwitchButton) contentView.findViewById(R.id.switchseuauto);
        swith_seu.setCheckedImmediately(new SettingsHelper(getContext()).getWifiAutoLogin());
        swith_seu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new SettingsHelper(getContext()).setWifiAutoLogin(isChecked);
        });
        contentView.findViewById(R.id.switch_container).setOnLongClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setMessage("长按摇一摇设置项代替摇一摇登录，属于测试功能，" +
                            "该功能未来可能保留，也可能取消，请不要过分依赖此功能。")
                    .setPositiveButton("使用此功能", (d, w) -> {
                        new NetworkLoginHelper(getContext(), false).checkAndLogin();
                    }).show();
            return true;
        });
        contentView.findViewById(R.id.switch_container).setOnClickListener(v -> swith_seu.toggle());
    }

    public void refreshUsername() {
        // 刷新用户名显示
        if (contentView != null) {
            TextView tv_username = (TextView) contentView.findViewById(R.id.tv_username);
            tv_username.setText(new ApiHelper(getContext()).getAuthCache("name"));
        }
    }

    private void checkUpdate() {
        //如果版本有更新则提示更新版本
        int versionCode = ServiceHelper.getAppVersionCode(getContext());
        int newestCode = serviceHelper.getNewestVersionCode();

        if (versionCode < newestCode) {
            new CacheHelper(getContext()).setCache("herald_new_version_ignored", "");

            //如果当前版本号小于最新版本，则提示更新
            String tip = "小猴偷米" + serviceHelper.getNewestVersionName() + "更新说明\n"
                    + serviceHelper.getNewestVersionDesc().replaceAll("\\\\n", "\n");

            //显示对话框
            new AlertDialog.Builder(getContext())
                    .setTitle("发现新版本")
                    .setMessage(tip)
                    .setPositiveButton("赶紧下载", (dialog, which) -> {
                        try {
                            Uri uri = Uri.parse(ServiceHelper.getServiceUrl(ServiceHelper.SERVICE_DOWNLOAD));
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } catch (Exception e) {
                            ContextUtils.showMessage(getContext(), "打开下载页失败，请联系管理员~");
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("残忍拒绝", (dialog, which) -> {
                    }).show();
        } else {
            ContextUtils.showMessage(getContext(), "当前版本已经是最新版本");
        }
    }

    private void setCustomAccount() {
        ApiHelper helper = new ApiHelper(getContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wifi_setauth, null);
        EditText et = (EditText) v.findViewById(R.id.et_username);
        et.setText(helper.getWifiUserName());
        EditText pw = (EditText) v.findViewById(R.id.et_pwd);//密码框不设置初始值，防止密码位数泄露

        new AlertDialog.Builder(getContext()).setTitle("设置校园网独立账号")
                .setView(v)
                .setPositiveButton("保存", (dialog, which) -> {
                    if (!et.getText().toString().equals("") && !pw.getText().toString().equals("")) {
                        helper.setWifiAuth(et.getText().toString(), pw.getText().toString());
                        ContextUtils.showMessage(getContext(), "已保存为校园网独立账号，建议手动摇一摇测试账号是否有效~");
                    } else {
                        ContextUtils.showMessage(getContext(), "你没有更改设置");
                    }
                })
                .setNeutralButton("恢复默认", (dialog, which) -> {
                    helper.clearWifiAuth();
                    ContextUtils.showMessage(getContext(), "已恢复默认校园网账号设置");
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
