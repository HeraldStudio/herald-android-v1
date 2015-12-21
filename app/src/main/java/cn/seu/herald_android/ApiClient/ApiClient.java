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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import cn.seu.herald_android.exception.AuthException;
import cn.seu.herald_android.helper.AuthHelper;

/**
 * Created by heyon on 2015/12/8.
 */
public class ApiClient {
    //可用doRequest调用的API
    //SRTP学分查询
    public static final int API_SRTP=0;
    //本学期课表学分查询
    public static final int API_SIDEBAR=1;
    //课表查询
    public static final int API_CURRICULUM=2;
    //绩点查询(较慢)
    public static final int API_GPA=3;
    //跑操次数查询
    public static final int API_PE=4;
    //校园网账户情况
    public static final int API_NIC=5;
    //一卡通余额
    public static final int API_CARD=6;
    //人文讲座查询
    public static final int API_LECTURE=7;
    //物理实验查询
    public static final int API_PHYLAB=8;
    //跑操预报
    public static final int API_PC=9;
    //教务处通知
    public static final int API_JWC=10;
    //校车
    public static final int API_SCHOOLBUS=11;
    //课程预报（今天剩下的课的消息）
    public static final int API_LEC_NOTICE=12;
    //个人信息查询
    public static final int API_USER=13;
    //宿舍查询
    public static final int API_ROOM=14;


    //需用其他方式访问的
    /**
     * 调戏
     * 参数
     * uuid：要搜索的书名
     * msg：对话内容
     */
    public static final int API_SIMSIMI=0;

    //暂时无法使用的两个
    public static final int API_RENEW=1;
    public static final int API_LIBRARY=2;

    //图书馆藏书搜索
    /**
     * 参数
     * book:要搜索的书名
     */
    public static final int API_BOOK_SEARCH=3;

    private static String[] apiNames = new String[]{
            "srtp",
            "sidebar",
            "curriculum",
            "gpa",
            "pe",
            "nic",
            "card",
            "lecture",
            "phylab",
            "pc",
            "jwc",
            "schoolbus",
            "lecturenotice",
            "user",
            "room"
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
            okHttpClient.setReadTimeout(3000, TimeUnit.MILLISECONDS);//设置超时时间
            RequestBody body = new FormEncodingBuilder()
                    .add("uuid", uuid)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try {
                Response response  = okHttpClient.newCall(request).execute();
                if(response.code()==200)
                    result = response.body().string();
            }catch (SocketTimeoutException e){
                throw new AuthException("网络错误，请检查网络连接",AuthException.NETWORK_ERROR);
            }catch (IOException e) {
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
            okHttpClient.setReadTimeout(3000, TimeUnit.MILLISECONDS);//设置超时时间
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
            }catch (SocketTimeoutException e){
                throw new AuthException("网络错误，请检查网络连接",AuthException.NETWORK_ERROR);
            }catch (ConnectException e) {
                throw new AuthException("网络连接错误，请检查网络连接",AuthException.NETWORK_ERROR);
            }catch (IOException e) {
                throw new AuthException("从服务器拉取认证信息失败,请联系管理员",AuthException.NETWORK_ERROR);
            }
        }
        return uuid;
    }
}
