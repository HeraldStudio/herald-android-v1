package cn.seu.herald_android.mod_webmodule;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.custom.ContextUtils;

/**
 * Created by heyon on 2016/5/11.
 */
public class WebShowActivity extends BaseAppCompatActivity {
    ProgressWebView webView_root;
    String tag_url;
    String title;
    int theme;
    public static void startWebShowActivity(Context context,String title,String url){
        startWebShowActivity(context,title,url,R.style.WebShowTheme);
    }

    public static void startWebShowActivity(Context context,String title, Uri uri,int themeId){
        startWebShowActivity(context,title,uri.toString(),themeId);
    }

    public static void startWebShowActivity(Context context,String title,String url,int themeId){
        Bundle bundle = new Bundle();
        bundle.putString("title",title);
        bundle.putString("url",url);
        bundle.putInt("theme",themeId);
        Intent intent = new Intent(context,WebShowActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeCreate();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_show);
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

    private void init() {
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //设置标题
        setTitle(title);
        //设置根布局参数
        enableSwipeBack();
        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, ContextUtils.getColorPrimary(this)));
        webView_root = (ProgressWebView)findViewById(R.id.webv_root);
        webView_root.getSettings().setJavaScriptEnabled(true);
    }

    public void openUrl(){
        if(tag_url!=null)
            webView_root.loadUrl(tag_url);
    }
}
