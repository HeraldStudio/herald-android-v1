package cn.seu.herald_android.mod_query.express;

import android.os.Bundle;
import android.view.Menu;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;

/**
 * Created by corvo on 7/28/16.
 */
public class ExpressActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu);
        return super.onCreateOptionsMenu(menu);
    }
}
