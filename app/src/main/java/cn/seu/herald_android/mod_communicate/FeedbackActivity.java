package cn.seu.herald_android.mod_communicate;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

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
            OkHttpUtils
                    .post()
                    .url(ApiHelper.feedback_url)
                    .addParams("cardnum", getApiHelper().getUserName())
                    .addParams("content", content)
                    .build()
                    .readTimeOut(10000).connTimeOut(10000)
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            hideProgressDialog();
                            getApiHelper().dealApiException(e);
                        }

                        @Override
                        public void onResponse(String response) {
                            hideProgressDialog();
                            try {
                                JSONObject json_res = new JSONObject(response);
                                if (json_res.getInt("code") == 200) {
                                    showMsg("您的反馈已发送，小猴将尽快处理，感谢支持！");
                                } else {
                                    showMsg("服务器遇到了一些问题，不妨稍后再试试");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showMsg("服务器遇到了一些问题，不妨稍后再试试");
                            }
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }
}
