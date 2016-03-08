package cn.seu.herald_android.mod_settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.seu.herald_android.R;

public class SysSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_settings);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
