package cn.seu.herald_android.mod_webmodule;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class WebModuleActivity extends BaseAppCompatActivity {

    String tag_url;
    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_module);
        init();
    }

    private void init() {
        String Action = getIntent().getAction();
        switch (Action) {
            case "cn.seu.herald_android.WEBMODULE_GYMRESERVE":
                //打开场馆预约
                tag_url = "http://115.28.27.150/heraldapp/#/yuyue/home";
                title = "场馆预约";
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(tag_url));
                startActivity(intent);
                break;
            case "cn.seu.herald_android.WEBMODULE_EMPTYROOM":
                //打开空教室
                tag_url = "http://115.28.27.150/queryEmptyClassrooms/m";
                title = "空教室查询";
                WebShowActivity.startWebShowActivity(WebModuleActivity.this,title,tag_url);
                break;
            case "cn.seu.herald_android.WEBMODULE_QUANYI":
                //打开权益服务
                tag_url = "https://jinshuju.net/f/By3aTK";
                title ="权益服务";
                WebShowActivity.startWebShowActivity(WebModuleActivity.this,title,tag_url);
                break;
        }
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        //切换动画
        overridePendingTransition(R.anim.activity_right_in, R.anim.activity_out_left);
    }

}
