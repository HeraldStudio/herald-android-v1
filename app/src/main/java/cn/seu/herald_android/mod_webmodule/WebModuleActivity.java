package cn.seu.herald_android.mod_webmodule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_framework.BaseActivity;

public class WebModuleActivity extends BaseActivity {
    ProgressWebView webView_root;
    String tag_url;
    String title;
    int theme;
    public static void startWebModuleActivity(Context context, String title, String url){
        startWebModuleActivity(context,title,url,R.style.WebShowTheme);
    }

    public static void startWebModuleActivity(Context context, String title, Uri uri, int themeId){
        startWebModuleActivity(context,title,uri.toString(),themeId);
    }

    public static void startWebModuleActivity(Context context, String title, String url, int themeId){
        Bundle bundle = new Bundle();
        bundle.putString("title",title);
        bundle.putString("url",url);
        bundle.putInt("theme",themeId);
        Intent intent = new Intent(context,WebModuleActivity.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeCreate();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_module);
        init();
        openUrl();
    }

    private void beforeCreate(){
        Bundle bundle = getIntent().getExtras();
        tag_url = bundle.getString("url");
        title = bundle.getString("title");
        theme = bundle.getInt("theme");
        //设置主题
        setTheme(theme);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
                finish();
            });
        }

        //设置标题
        setTitle(title);
        //设置根布局参数
        enableSwipeBack();
        //沉浸式
        setStatusBarColor(ContextCompat.getColor(this, AppContext.getColorPrimary()));
        webView_root = (ProgressWebView)findViewById(R.id.webv_root);
        if (webView_root != null) {
            webView_root.getSettings().setJavaScriptEnabled(true);
        }
    }

    public void openUrl(){
        if(tag_url!=null)
            webView_root.loadUrl(tag_url);
    }
}
