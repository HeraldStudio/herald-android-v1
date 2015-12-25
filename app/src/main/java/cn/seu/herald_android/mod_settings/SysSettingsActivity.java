package cn.seu.herald_android.mod_settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Switch;

import cn.seu.herald_android.R;

public class SysSettingsActivity extends AppCompatActivity {

    private SharedPreferences sp;

    private Switch autoLogin;

    // 各项设置的默认值存为常量，以防止设置中显示的默认值与相应功能状态的默认值不一致
    public static boolean DEFAULT_AUTO_LOGIN = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_settings);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        autoLogin = (Switch) findViewById(R.id.autoLogin);
        autoLogin.setChecked(sp.getBoolean("autoLogin", DEFAULT_AUTO_LOGIN));
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("autoLogin", autoLogin.isChecked());
        editor.apply();
        super.onPause();
    }
}
