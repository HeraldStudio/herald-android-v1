package cn.seu.herald_android.mod_auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import cn.seu.herald_android.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.MainActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;
import okhttp3.Call;

public class LoginActivity extends BaseAppCompatActivity {
    TextView tv_card;
    TextView tv_pwd;
    Button btn_login;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.notoolbar);
        setSupportActionBar(toolbar);
        init();
    }


    public void init(){
        //空间初始化
        tv_card = (TextView)findViewById(R.id.tv_login_cardnum);
        tv_pwd = (TextView)findViewById(R.id.tv_login_pwd);

        //进度进度条
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("登录中");

        //绑定登录按钮点击函数
        btn_login = (Button)findViewById(R.id.btn_login_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //运行请求前先清除旧的uuid
                doLogin();
            }
        });


    }

    @Override
    public void onBackPressed() {
        finish();
        Toast.makeText(getApplicationContext(), "退出先声网客户端",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    private void doLogin(){
        //登录函数
        progressDialog.show();
        btn_login.setEnabled(false);
        OkHttpUtils
                .post()
                .url(getApiHepler().auth_url)
                .addParams("user", tv_card.getText().toString())
                .addParams("password",tv_pwd.getText().toString())
                .addParams("appid", getApiHepler().APPID)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        progressDialog.dismiss();
                        btn_login.setEnabled(true);
                        //处理Api错误
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
                        String uuid = response;
                        getApiHepler().setAuthCache("uuid", uuid);
                        NetworkLoginHelper.getInstance(LoginActivity.this)
                                .setAuth(tv_card.getText().toString(), tv_pwd.getText().toString());
                        checkUUID();
                    }
                });
    }


    public void checkUUID(){
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_USER))
                .addParams("uuid",getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        //错误检测
                        e.printStackTrace();
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
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                //如果返回的状态码是200则说明uuid正确，则说明密码正确
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                                finish();
                            } else {
                                //如果返回的状态码不是200则说明uuid不对，需要重新输入账号密码
                                showMsg("密码错误请重新登录");
                                getApiHepler().doLogout();
                                NetworkLoginHelper.getInstance(LoginActivity.this)
                                        .setAuth("", "");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }
}
