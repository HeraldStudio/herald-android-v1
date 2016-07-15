package cn.seu.herald_android.mod_webmodule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.SettingsHelper;

public class WebModuleActivity extends BaseActivity {
    @BindView(R.id.webv_root)
    ProgressWebView webView_root;

    String tag_url;
    String title;
    int theme;
    public static void startWebModuleActivity(Context context, String title, String url){

        int theme = R.style.WebShowTheme;
        if (url.equals(SettingsHelper.Module.schedule.controller)) {
            theme = R.style.ScheduleTheme;
        } else if (url.equals(SettingsHelper.Module.quanyi.controller)) {
            theme = R.style.QuanYiTheme;
        } else if (url.equals(SettingsHelper.Module.emptyroom.controller)) {
            theme = R.style.EmptyRoomTheme;
        }

        startWebModuleActivity(context, title, url, theme);
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeCreate();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_web);
        ButterKnife.bind(this);

        //设置标题
        setTitle(title);
        webView_root.getSettings().setJavaScriptEnabled(true);
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

    public void openUrl(){
        if (tag_url != null) {
            webView_root.loadUrl(tag_url);
        }
    }
}
