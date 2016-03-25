package cn.seu.herald_android.mod_auth;

import android.app.ProgressDialog;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import okhttp3.Call;

public class LoginActivity extends BaseAppCompatActivity {
    private TextView tv_card;
    private TextView tv_pwd;
    private Button btn_login;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }


    private void init() {
        //空间初始化
        tv_card = (TextView) findViewById(R.id.tv_login_cardnum);
        tv_pwd = (TextView) findViewById(R.id.tv_login_pwd);

        //进度进度条
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("登录中");

        //绑定登录按钮点击函数
        btn_login = (Button) findViewById(R.id.btn_login_login);
        btn_login.setOnClickListener(v -> {
            if (tv_card.getText().toString().trim().length() > 0 && tv_pwd.getText().toString().length() > 0) {
                doLogin();
            }
        });
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
        String appid = ApiHelper.getAppId();
        String godModePrefix = "IAmTheGodOfHerald|OverrideAppidWith:";

        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
            boolean GODMODE = manager.getPrimaryClip().getItemAt(0).getText().toString().startsWith(godModePrefix);
            if (GODMODE) {
                appid = manager.getPrimaryClip().getItemAt(0).getText().toString().replace(godModePrefix, "");
                showMsg("进入上帝登陆模式");
            }
        }

        //登录函数
        progressDialog.show();
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
                        progressDialog.dismiss();
                        btn_login.setEnabled(true);
                        //处理Api错误
                        if (e instanceof SocketTimeoutException) {
                            showMsg("抱歉，学校服务器又出问题了T.T咱也是无能为力呀");
                        } else if (e instanceof ConnectException) {
                            showMsg("网络连接错误，请检查您的网络连接");
                        } else if (e.toString().contains("Bad Request")) {
                            Toast.makeText(LoginActivity.this, "当前客户端版本已过期，请下载最新版本", Toast.LENGTH_LONG).show();
                            Uri uri = Uri.parse(ServiceHelper.getServiceUrl(ServiceHelper.SERVICE_DOWNLOAD));
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } else {
                            showMsg("一卡通和统一身份认证密码不匹配，请核对后再试");
                        }
                    }

                    @Override
                    public void onResponse(String response) {
                        //以下两句是登陆时对话框提前消失的罪魁祸首
                        //progressDialog.dismiss();
                        //btn_login.setEnabled(true);

                        //showMsg("dologin"+response);
                        getApiHelper().setAuthCache("uuid", response);
                        //保存用户密码
                        getApiHelper().setAuth(tv_card.getText().toString(), tv_pwd.getText().toString());
                        checkUUID();
                    }
                });
    }


    private void checkUUID() {
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_USER))
                .addParams("uuid", getApiHelper().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        progressDialog.dismiss();
                        btn_login.setEnabled(true);
                        //错误检测
                        e.printStackTrace();
                        //showMsg("checkuuid+"+e.toString());
                        if (e instanceof SocketTimeoutException) {
                            showMsg("抱歉，学校服务器又出问题了T.T咱也是无能为力呀");
                        } else if (e instanceof ConnectException) {
                            showMsg("网络连接错误，请检查您的网络连接");
                        } else {
                            showMsg("一卡通和统一查询密码不匹配，请核对后再试");
                        }
                    }

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        btn_login.setEnabled(true);
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                //如果返回的状态码是200则说明uuid正确，则说明密码正确
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                                finish();
                            } else {
                                //如果返回的状态码不是200则说明uuid不对，需要重新输入账号密码
                                showMsg("一卡通和统一查询密码不匹配，请核对后再试");
                                getApiHelper().doLogout();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("连接登录服务器失败，请检查网络状况，或者核对用户名密码后再试。");
                            getApiHelper().doLogout();
                        }
                    }
                });

    }
}
