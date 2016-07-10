package cn.seu.herald_android.mod_auth;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import okhttp3.Call;

public class LoginActivity extends BaseActivity {
    private TextView tv_card;
    private TextView tv_pwd;
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_auth);
        init();
    }


    private void init() {
        //空间初始化
        tv_card = (TextView) findViewById(R.id.tv_login_cardnum);
        tv_pwd = (TextView) findViewById(R.id.tv_login_pwd);

        //绑定登录按钮点击函数
        btn_login = (Button) findViewById(R.id.btn_login_login);
        if (btn_login != null) {
            btn_login.setOnClickListener(v -> {
                if (tv_card.getText().toString().trim().length() > 0 && tv_pwd.getText().toString().length() > 0) {
                    doLogin();
                }
            });
        }

        progressDialog.setCancelable(false);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    private void doLogin() {
        String appid = ApiHelper.appid.get();
        String godModePrefix = "IAmTheGodOfHerald|OverrideAppidWith:";

        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
            boolean GODMODE = manager.getPrimaryClip().getItemAt(0).getText().toString().startsWith(godModePrefix);
            if (GODMODE) {
                appid = manager.getPrimaryClip().getItemAt(0).getText().toString().replace(godModePrefix, "");
                showSnackBar("进入上帝登陆模式");
            }
        }

        //登录函数
        showProgressDialog();
        btn_login.setEnabled(false);
        OkHttpUtils
                .post()
                .url(ApiHelper.auth_url)
                .addParams("user", tv_card.getText().toString())
                .addParams("password", tv_pwd.getText().toString())
                .addParams("appid", appid)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        hideProgressDialog();
                        btn_login.setEnabled(true);
                        //处理Api错误
                        if (e instanceof SocketTimeoutException) {
                            showSnackBar("抱歉，学校服务器又出问题了T.T咱也是无能为力呀");
                        } else if (e instanceof ConnectException) {
                            showSnackBar("网络连接错误，请检查您的网络连接");
                        } else if (e.toString().contains("Bad Request")) {
                            Toast.makeText(LoginActivity.this, "当前客户端版本已过期，请下载最新版本", Toast.LENGTH_LONG).show();
                            Uri uri = Uri.parse("http://android.heraldstudio.com/download");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } else {
                            showSnackBar("一卡通和统一身份认证密码不匹配，请核对后再试");
                        }
                    }

                    @Override
                    public void onResponse(String response) {
                        //以下两句是登陆时对话框提前消失的罪魁祸首
                        //progressDialog.dismiss();
                        //btn_login.setEnabled(true);

                        //showSnackBar("dologin"+response);
                        ApiHelper.setAuthCache("uuid", response);
                        //保存用户密码
                        ApiHelper.setAuth(tv_card.getText().toString(), tv_pwd.getText().toString());
                        checkUUID();
                    }
                });
    }


    private void checkUUID() {
        new ApiRequest().api("user").addUUID().toAuthCache("schoolnum", json ->
            json.getJSONObject("content").getString("schoolnum")
        ).onFinish((success, code, response) -> {
            if (success && ApiHelper.getAuthCache("schoolnum").length() == 8) {
                hideProgressDialog();
                AppContext.showMain();
            } else {
                hideProgressDialog();
                ApiHelper.doLogout("用户不存在或网络异常, 请重试");
            }
        }).run();
    }
}
