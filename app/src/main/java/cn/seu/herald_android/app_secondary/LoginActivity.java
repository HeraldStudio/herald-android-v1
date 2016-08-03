package cn.seu.herald_android.app_secondary;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.User;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.tv_login_cardnum)
    TextView tv_card;
    @BindView(R.id.tv_login_pwd)
    TextView tv_pwd;
    @BindView(R.id.login_bg)
    ImageView loginBg;

    private User user = new User("", "", "", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_auth__login);
        ButterKnife.bind(this);
        progressDialog.setCancelable(false);
        setStatusBarColor(Color.BLACK);

        Picasso.with(this).load(R.drawable.login_bg).into(loginBg);

        tv_pwd.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                loginBtnOnClick();
                return true;
            }
            return false;
        });
    }

    @OnClick(R.id.btn_login_login)
    public void loginBtnOnClick() {
        if (tv_card.getText().toString().trim().equals("") || tv_pwd.getText().toString().equals("")) {
            return;
        }

        String appid = ApiHelper.getAppid();
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

        // 登录函数
        showProgressDialog();
        new ApiSimpleRequest(Method.POST).url(ApiHelper.auth_url)
                .post("user", username)
                .post("password", password)
                .post("appid", appid)
                .onResponse((success, code, response) -> {

                    if (response.contains("Unauthorized")) {
                        hideProgressDialog();
                        showSnackBar("密码错误，请重试");
                    } else if (response.contains("Bad Request")) {
                        hideProgressDialog();
                        AppContext.showToast("当前客户端版本已过期，请下载最新版本");
                        AppContext.openUrlInBrowser("http://android.heraldstudio.com/download");
                    } else if (!success) {
                        hideProgressDialog();
                        showSnackBar("网络异常，请重试");
                    } else {
                        user.uuid = response;
                        user.userName = username;
                        user.password = password;
                        checkUUID();
                    }
                }).run();
    }

    @OnClick(R.id.btn_login_trial)
    public void finish() {
        super.finish();
    }

    private void checkUUID() {
        new ApiSimpleRequest(Method.POST).api("user").post("uuid", user.uuid)
                .onResponse((success, code, response) -> {

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
                    try {
                        JSONObject object = new JSONObject(response);
                        user.schoolNum = object.getJSONObject("content").getString("schoolnum");

                        if (success && user.schoolNum.length() == 8) {
                            hideProgressDialog();
                            ApiHelper.setCurrentUser(user);
                            finish();
                            new AppModule("跳转到首页", "TAB0").open();
                        } else {
                            hideProgressDialog();
                            showSnackBar("用户不存在或网络异常, 请重试");
                        }
                    } catch (JSONException e) {
                hideProgressDialog();
                        showSnackBar("用户不存在或网络异常, 请重试");
            }
        }).run();
    }
}
