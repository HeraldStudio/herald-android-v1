package cn.seu.herald_android.mod_communicate;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

public class FeedbackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_com__feedback);
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
            EditText et_contact = (EditText)findViewById(R.id.tv_contact);
            String content = null;
            if (et != null && et_contact != null) {
                content = "[来自Android版] " + et.getText().toString() + " [联系方式：" + et_contact.getText() + "]";
            }

            showProgressDialog();
            new ApiRequest().url(ApiHelper.feedback_url)
                    .post("cardnum", ApiHelper.getUserName(),
                            "content", content)
                    .onFinish((success, code, response) -> {
                        hideProgressDialog();
                        if (success) {
                            showSnackBar("您的反馈已发送，小猴将尽快处理，感谢支持！");
                            //延时展示信息然后退出
                            Handler handler = new Handler();
                            handler.postDelayed(this::finish,2000);
                        } else {
                            showSnackBar("发送失败，请重试");
                        }
                    }).run();
        }
        return super.onOptionsItemSelected(item);
    }
}
