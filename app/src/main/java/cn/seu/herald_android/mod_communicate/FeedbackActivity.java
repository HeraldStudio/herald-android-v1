package cn.seu.herald_android.mod_communicate;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

public class FeedbackActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorFeedbackprimary));
        enableSwipeBack();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            EditText et = (EditText) findViewById(R.id.editText);
            String content = et.getText().toString();

            showProgressDialog();
            new ApiRequest(this).url(ApiHelper.feedback_url)
                    .post("cardnum", getApiHelper().getUserName(), "content", content)
                    .onFinish((success, code, response) -> {
                        hideProgressDialog();
                        if (success) {
                            showSnackBar("您的反馈已发送，小猴将尽快处理，感谢支持！");
                        }
                    }).run();
        }
        return super.onOptionsItemSelected(item);
    }
}
