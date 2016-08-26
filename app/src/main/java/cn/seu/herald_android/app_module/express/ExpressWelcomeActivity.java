package cn.seu.herald_android.app_module.express;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_module.topic.TopicActivity;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.helper.CacheHelper;

/**
 * Created by corvo on 8/24/16.
 */
public class ExpressWelcomeActivity extends BaseActivity implements
        View.OnClickListener{

    private Button btnAccept;
    private Button btnDecline;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express__welcome);

        ExpressWelcomeFragment fragment = new ExpressWelcomeFragment();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.express_txt_declare, (Fragment)fragment);
        fragmentTransaction.commit();

        btnAccept = (Button) findViewById(R.id.express_button_accept);
        btnDecline = (Button) findViewById(R.id.express_button_decline);

        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.express_button_decline) {
            finish();
        } else if (v.getId() == R.id.express_button_accept) {
            if (CacheHelper.get("express_verify").equals("")) {
                CacheHelper.set("express_verify", "1");
            }
            Intent intent = new Intent(this, ExpressActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
