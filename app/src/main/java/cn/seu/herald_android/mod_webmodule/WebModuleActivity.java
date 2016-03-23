package cn.seu.herald_android.mod_webmodule;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class WebModuleActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openUrl();
    }

    private void openUrl() {
        String Action = getIntent().getAction();
        String tag_url = null;
        switch (Action) {
            case "cn.seu.herald_android.WEBMODULE_GYMORDER":
                //打开场馆预约
                tag_url = "http://115.28.27.150/heraldapp/#/yuyue/home";
                break;
            case "cn.seu.herald_android.WEBMODULE_EMPTYROOM":
                //打开空教室
                tag_url = "http://115.28.27.150/queryEmptyClassrooms/m";
                break;
            case "cn.seu.herald_android.WEBMODULE_QUANYI":
                //打开权益服务
                tag_url = "https://jinshuju.net/f/By3aTK";
                break;
        }
        if(tag_url != null) {
            Uri uri = Uri.parse(tag_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        finish();
    }
}
