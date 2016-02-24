package cn.seu.herald_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import cn.seu.herald_android.helper.ApiHelper;

public class BaseAppCompatActivity extends AppCompatActivity {
    private ApiHelper apiHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.apiHelper = new ApiHelper(this);
    }

    public void showMsg(String msg){
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }
    public ApiHelper getApiHepler(){
        return apiHelper;
    }

    public void startActivityAndFinish(Intent intent){
        startActivity(intent);
        finish();
    }
}