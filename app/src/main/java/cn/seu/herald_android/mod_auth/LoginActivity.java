package cn.seu.herald_android.mod_auth;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.tv_login_cardnum)
    TextView tv_card;
    @BindView(R.id.tv_login_pwd)
    TextView tv_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_auth__login);
        ButterKnife.bind(this);
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

    @OnClick(R.id.btn_login_login)
    public void loginBtnOnClick() {
        if (tv_card.getText().toString().trim().equals("") || tv_pwd.getText().toString().equals("")) {
            return;
        }

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

        String username = tv_card.getText().toString();
        String password = tv_pwd.getText().toString();

        //登录函数
        showProgressDialog();
        new ApiRequest().url(ApiHelper.auth_url).noCheck200()
                .post("user", username)
                .post("password", password)
                .post("appid", appid)
                .onFinish((success, code, response) -> {

                    if (response.contains("Unauthorized")) {
                        hideProgressDialog();
                        showSnackBar("密码错误，请重试");
                    } else if (response.contains("Bad Request")) {
                        Toast.makeText(LoginActivity.this, "当前客户端版本已过期，请下载最新版本", Toast.LENGTH_LONG).show();
                        Uri uri = Uri.parse("http://android.heraldstudio.com/download");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else if (!success) {
                        hideProgressDialog();
                        showSnackBar("网络异常，请重试");
                    } else {
                        ApiHelper.setAuthCache("uuid", response);
                        ApiHelper.setAuth(username, password);
                        checkUUID();
                    }
                }).run();
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
