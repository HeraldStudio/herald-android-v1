package cn.edu.seu.herald.myseu;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Created by Vhyme on 2015/10/18.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        /*
        * 沉浸顶栏实现
        * 参考：http://blog.csdn.net/jdsjlzx/article/details/41643587
        * */
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.drawable.bg_actionbar);
    }
}
