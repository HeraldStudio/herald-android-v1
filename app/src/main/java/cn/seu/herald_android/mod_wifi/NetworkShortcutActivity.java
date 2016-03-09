package cn.seu.herald_android.mod_wifi;

import android.app.Activity;
import android.os.Bundle;

public class NetworkShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkLoginHelper.getInstance(this).checkAndLogin();
        finish();
    }
}
