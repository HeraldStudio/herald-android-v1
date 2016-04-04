package cn.seu.herald_android.app_main;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_my_info, null, false);
    }

    private View check_update;
    private View custom_account;
    private TextView tv_nowversion;
    private SwitchButton swith_seu;
    private ServiceHelper serviceHelper;

    @Override
    public void onStart() {
        super.onStart();
        serviceHelper = new ServiceHelper(getContext());

        check_update = getView().findViewById(R.id.check_update);
        check_update.setOnClickListener(v -> checkUpdate());

        custom_account = getView().findViewById(R.id.custom_account);
        custom_account.setOnClickListener(v -> setCustomAccount());

        tv_nowversion = (TextView) getView().findViewById(R.id.tv_now_version);
        tv_nowversion.setText("当前版本： " + ServiceHelper.getAppVersionName(getContext()));

        getView().findViewById(R.id.tv_aboutus).setOnClickListener((v) -> {
            startActivity(new Intent(getContext(), AboutusActivity.class));
        });

        getView().findViewById(R.id.tv_feedback).setOnClickListener((v) -> {
            startActivity(new Intent(getContext(), FeedbackActivity.class));
        });

        getView().findViewById(R.id.tv_logout).setOnClickListener((v) -> {
            new AlertDialog.Builder(getContext()).setMessage("退出后将自动清除模块缓存，确定退出吗？")
                    .setPositiveButton("退出", (d, w) -> new ApiHelper(getContext()).doLogout())
                    .setNegativeButton("取消", null)
                    .show();
        });

        swith_seu = (SwitchButton) getView().findViewById(R.id.switchseuauto);
        swith_seu.setCheckedImmediately(new SettingsHelper(getContext()).getWifiAutoLogin());
        swith_seu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new SettingsHelper(getContext()).setWifiAutoLogin(isChecked);
        });
        getView().findViewById(R.id.switch_container).setOnLongClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setMessage("长按摇一摇设置项代替摇一摇登录，属于测试功能，" +
                            "该功能未来可能保留，也可能取消，请不要过分依赖此功能。")
                    .setPositiveButton("使用此功能", (d, w) -> {
                        new NetworkLoginHelper(getContext(), false).checkAndLogin();
                    }).show();
            return true;
        });

        getView().findViewById(R.id.switch_container).setOnClickListener((v) -> {
            swith_seu.toggle();
        });

    }

    public void refreshUsername() {
        //刷新用户名显示
        TextView tv_username = (TextView) getView().findViewById(R.id.tv_username);
        tv_username.setText(new ApiHelper(getContext()).getAuthCache("name"));
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
