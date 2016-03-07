package cn.seu.herald_android.mod_auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.MainActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;
import okhttp3.Response;

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
        if(getApiHepler().isLogin()){
            startActivity(new Intent(getBaseContext(),MainActivity.class));
            finish();
        }
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

        //判断是否是首次启动
        int launchtimes = getSettingsHelper().getLaunchTimes();
        if(0==launchtimes){
            //是第一次启动则启用默认设置
            getSettingsHelper().setDefaultConfig();
        }

        //启动次数递增
        getSettingsHelper().updateLanuchTimes(launchtimes+1);
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
                        getApiHepler().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        btn_login.setEnabled(true);
                        String uuid = response;
                        getApiHepler().setAuthCache("uuid", uuid);
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
                        getApiHepler().dealApiException(e);
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
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }





}
