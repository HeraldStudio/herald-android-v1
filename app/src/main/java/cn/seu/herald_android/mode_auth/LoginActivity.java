package cn.seu.herald_android.mode_auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import cn.seu.herald_android.MainActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.exception.AuthException;
import cn.seu.herald_android.helper.AuthHelper;

public class LoginActivity extends AppCompatActivity {
    AuthHelper authHelper;
    Handler loginHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.notoolbar);
        setSupportActionBar(toolbar);
        authHelper = new AuthHelper(this);
        init();
    }

    public void init(){
        final TextView tv_card = (TextView)findViewById(R.id.tv_login_cardnum);
        final TextView tv_pwd = (TextView)findViewById(R.id.tv_login_pwd);
        Button btn_login = (Button)findViewById(R.id.btn_login_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //运行请求前先清除旧的uuid
                authHelper.setAuthCache("uuid","");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String card  = tv_card.getText().toString();
                        String pwd = tv_pwd.getText().toString();
                        Log.d("login", "run: card"+card);
                        Log.d("login", "run: pwd"+pwd);
                        Message msg = new Message();
                        try {
                            authHelper.doLogin(card,pwd);
                        } catch (AuthException e) {
                            e.printStackTrace();
                            msg.obj = e;
                        } finally {
                            loginHandler.sendMessage(msg);
                        }
                    }
                }).start();

            }
        });
        loginHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.obj instanceof AuthException) {
                    Toast.makeText(getApplication(),((AuthException) msg.obj).getMsg(),Toast.LENGTH_SHORT).show();
                    return false;
                }
                toHomePage();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        Toast.makeText(getApplicationContext(),"退出先声网客户端",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void toHomePage(){
        if(authHelper.isLogin())
        super.onBackPressed();
    }


}
