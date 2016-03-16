package cn.seu.herald_android.mod_webmodule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import cn.seu.herald_android.R;

public class WebModuleActivity extends AppCompatActivity {

    private String tag_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_module);
        setupTagUrl();
        setupWebView();
    }

    private void setupTagUrl() {
        String Action = getIntent().getAction();
        switch (Action) {
            case "cn.seu.herald_android.WEBMODULE_GYMORDER":
                //打开场馆预约
                tag_url = "http://115.28.27.150/heraldapp/#/yuyue/home";
                setTitle("场馆预约");
                break;
            case "cn.seu.herald_android.WEBMODULE_EMPTYROOM":
                //打开空教室
                tag_url = "http://115.28.27.150/queryEmptyClassrooms/m";
                setTitle("空教室查询");
                break;
            case "cn.seu.herald_android.WEBMODULE_QUANYI":
                //打开权益服务
                tag_url = "https://jinshuju.net/f/By3aTK";
                setTitle("权益服务");
                break;
        }
    }

    private void setupWebView() {
        WebView webView = (WebView) findViewById(R.id.webview_webmodule);
        webView.loadUrl(tag_url);
    }
}
