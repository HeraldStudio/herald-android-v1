package cn.seu.herald_android.ApiClient;

import android.content.SharedPreferences;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.seu.herald_android.exception.AuthException;
import cn.seu.herald_android.helper.AuthHelper;

/**
 * Created by heyon on 2015/12/8.
 */
public class ApiClient {
    public static final int API_USER=0;
    public static final int API_PE=1;
    public static final int API_SRTP=2;

    private static String[] apiNames = new String[]{
            "user",
            "pe",
            "srtp"
    };

    public ApiClient(){

    }

    public String doRequest(int apiname ,String uuid)throws NetworkOnMainThreadException,AuthException{
        String result = "";
        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new NetworkOnMainThreadException();
            //抛出在主线程中操作网络的异常
        }else{
            String url = AuthHelper.url + apiNames[apiname];
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody body = new FormEncodingBuilder()
                    .add("uuid", uuid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try {
                Response response  = okHttpClient.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    public String doAuth(String cardnum,String pwd )throws NetworkOnMainThreadException,AuthException{
        //如果认证成功返回uuid
        String uuid = "";
        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new NetworkOnMainThreadException();
            //抛出在主线程中操作网络的异常
        }else{
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody body = new FormEncodingBuilder()
                    .add("user", cardnum)
                    .add("password", pwd)
                    .add("appid", AuthHelper.APPID)
                    .build();
            Request request = new Request.Builder()
                    .url("http://115.28.27.150/uc/auth")
                    .post(body)
                    .build();
            try {
                Log.d("api", "doAuth: "+request.toString());
                Response response  = okHttpClient.newCall(request).execute();
                if(response.code()==401)
                    throw new AuthException("账户信息出错，请核实一卡通号或者统一认证密码",AuthException.ERROR_PWD);
                //返回uuid
                uuid = response.body().string().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uuid;
    }
}
