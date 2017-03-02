package cn.seu.herald_android.app_secondary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_module.cardextra.CardActivity;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.custom.ProgressWebView;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.helper.ShareHelper;

public class WebModuleActivity extends BaseActivity {
    @BindView(R.id.webv_root)
    ProgressWebView webView_root;

    String tag_url;
    String title;

    public static void startWebModuleActivity(String title, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        Intent intent = new Intent(AppContext.instance, WebModuleActivity.class);
        intent.putExtras(bundle);
        AppContext.startActivitySafely(intent);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeCreate();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_sec__web);
        ButterKnife.bind(this);

        // 设置标题
        setTitle(title);
        webView_root.getSettings().setJavaScriptEnabled(true);
        openUrl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            // 点击刷新按钮时进行刷新
            webView_root.reload();
        } else if (id == R.id.action_share) {
            // 分享网页
            ShareHelper.share("[分享自小猴偷米App] " + webView_root.getTitle() + " " + webView_root.getUrl());
        }
        return super.onOptionsItemSelected(item);
    }

    private void beforeCreate(){
        Bundle bundle = getIntent().getExtras();
        tag_url = bundle.getString("url");
        title = bundle.getString("title");
    }

    public void openUrl(){
        if (tag_url != null) {
            webView_root.loadUrl(tag_url);
        }
    }
}
