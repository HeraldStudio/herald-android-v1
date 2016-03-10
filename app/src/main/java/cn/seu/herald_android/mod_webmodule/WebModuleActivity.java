package cn.seu.herald_android.mod_webmodule;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.seu.herald_android.R;

public class WebModuleActivity extends AppCompatActivity {

    String tag_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_module);
        setupTagUrl();
        setupWebview();
    }
    private void setupTagUrl(){
        String Action = getIntent().getAction();
        if(Action.equals("cn.seu.herald_android.WEBMODULE_GYMORDER")){
            //打开场馆预约
            tag_url = "http://115.28.27.150/heraldapp/#/yuyue/home";
            setTitle("场馆预约");
        }else if(Action.equals("cn.seu.herald_android.WEBMODULE_EMPTYROOM")){
            //打开空教室
            tag_url = "http://115.28.27.150/queryEmptyClassrooms/m";
            setTitle("空教室查询");
        }else if(Action.equals("cn.seu.herald_android.WEBMODULE_QUANYI")){
            //打开权益服务
            tag_url = "https://jinshuju.net/f/By3aTK";
            setTitle("权益服务");
        }
    }
    private void setupWebview(){
        WebView webView = (WebView)findViewById(R.id.webview_webmodule);
        webView.loadUrl(tag_url);}
}
