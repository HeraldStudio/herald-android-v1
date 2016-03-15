package cn.seu.herald_android.mod_settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Switch;
import android.widget.TextView;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ServiceHelper;


public class SysSettingsActivity extends BaseAppCompatActivity {

    TextView tv_checkupdate;
    TextView tv_nowversion;
    Switch swith_seu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_settings);
        init();
    }

    public void init(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        tv_checkupdate = (TextView)findViewById(R.id.tv_checkupdate);
        tv_checkupdate.setOnClickListener(v -> checkUpdate());

        tv_nowversion = (TextView)findViewById(R.id.tv_now_version);
        tv_nowversion.setText("当前版本： " + ServiceHelper.getAppVersionName(this));

        swith_seu = (Switch)findViewById(R.id.switchseuauto);
        swith_seu.setChecked(getSettingsHelper().getWifiAutoLogin());
        swith_seu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSettingsHelper().setWifiAutoLogin(isChecked);
            if(isChecked){
                showMsg("已打开seu-wlan自动登录");
            }else{
                showMsg("已关闭seu-wlan自动登录");
            }
        });

    }

    public void checkUpdate(){
        //如果版本有更新则提示更新版本
        int versionCode = ServiceHelper.getAppVersionCode(this);
        int newestCode = getServiceHelper().getNewestVesionCode();

        if(versionCode < newestCode){
            //如果当前版本号小于最新版本，则提示更新
            String tip = String.format("小猴已经发布%s版本啦,您的当前版本为%s,赶紧下载新版本吧",
                    getServiceHelper().getNewestVesionName(),
                    ServiceHelper.getAppVersionName(this));
            //显示对话框
            new AlertDialog.Builder(this)
                    .setTitle("发现新版本")
                    .setMessage(tip)
                    .setPositiveButton("赶紧下载",(dialog, which) -> {
                        try{
                            Uri uri = Uri.parse(ServiceHelper.getServiceUrl(ServiceHelper.SERVICE_DOWNLOAD));
                            Intent intent = new  Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }catch (Exception e){
                            showMsg("打开下载页失败，请联系管理员~");
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("残忍拒绝", (dialog, which) -> {
                    }).show();
        } else {
            showMsg("当前版本已经是最新版本");
        }
    }

}
