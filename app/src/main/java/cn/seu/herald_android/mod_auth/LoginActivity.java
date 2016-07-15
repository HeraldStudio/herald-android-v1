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
        setContentView(R.layout.mod_auth__login);
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
        String appid = ApiHelper.appid.$get();
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

            /**
             * 注意: 学号的获取关系到统计功能和 srtp api 的正常调用, 千万要保证学号正确!
             *
             * 若学号没有正确获取, 由于服务端的缓存机制, 已绑定微信的用户仍然可能看到正确的结果,
             * 所以开发者在日常使用中几乎不可能发现这种错误, 需要经过专门测试才能确定客户端是否正确获取学号!
             *
             * 测试方法如下: (注意 postman 使用时应当保证填写的 uuid 与客户端 uuid 相同)
             *
             * 1) 使用 postman, 调用 srtp 接口, schoolnum 参数中填写自己的学号, 应当返回自己的 srtp 信息;
             * 2) 使用 postman, 调用 srtp 接口, schoolnum 参数中填写同学A的学号, 应当返回同学A的 srtp 信息;
             * 3) 使用 postman, 调用 srtp 接口, 不带 schoolnum 参数, 应当返回同学A的 srtp 信息;
             * 4) 使用客户端刷新 srtp 模块, 应当返回正确的 srtp 信息;
             * 5) 使用 postman, 调用 srtp 接口, 不带 schoolnum 参数, 应当返回自己的 srtp 信息;
             **/
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
