package cn.seu.herald_android.mod_communicate;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class AboutusActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //设置状态栏颜色
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimary));
        enableSwipeBack();
    }
}
