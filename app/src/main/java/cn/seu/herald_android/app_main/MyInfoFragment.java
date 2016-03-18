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

public class MyInfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_info, null, false);
    }

    private TextView tv_checkupdate;
    private TextView tv_nowversion;
    private SwitchButton swith_seu;
    private ServiceHelper serviceHelper;

    @Override
    public void onStart() {
        super.onStart();
        serviceHelper = new ServiceHelper(getContext());
        //如果缓存存在的话先设置欢迎信息和侧边栏信息
        TextView tv_username = (TextView) getView().findViewById(R.id.tv_username);
        tv_username.setText(new ApiHelper(getContext()).getAuthCache("name"));

        tv_checkupdate = (TextView) getView().findViewById(R.id.tv_checkupdate);
        tv_checkupdate.setOnClickListener(v -> checkUpdate());

        tv_nowversion = (TextView) getView().findViewById(R.id.tv_now_version);
        tv_nowversion.setText("当前版本： " + ServiceHelper.getAppVersionName(getContext()));

        getView().findViewById(R.id.tv_aboutus).setOnClickListener((v) -> {
            startActivity(new Intent(getContext(), AboutusActivity.class));
        });

        getView().findViewById(R.id.tv_feedback).setOnClickListener((v) -> {
            startActivity(new Intent(getContext(), FeedbackActivity.class));
        });

        getView().findViewById(R.id.tv_logout).setOnClickListener((v) -> {
            new ApiHelper(getContext()).doLogout();
        });

        swith_seu = (SwitchButton) getView().findViewById(R.id.switchseuauto);
        swith_seu.setCheckedImmediately(new SettingsHelper(getContext()).getWifiAutoLogin());
        swith_seu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new SettingsHelper(getContext()).setWifiAutoLogin(isChecked);
            ContextUtils.showMessage(getContext(), "更改将在下次启动时生效");
        });

        getView().findViewById(R.id.switch_container).setOnClickListener((v) -> {
            swith_seu.toggle();
        });

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
}
