package cn.seu.herald_android.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;


import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;


import java.net.ConnectException;
import java.net.SocketTimeoutException;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.mod_auth.LoginActivity;
import okhttp3.Call;

/**
 * Created by heyon on 2015/12/8.
 */
public class ApiHelper {
    private Context context;
    private Activity activity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public static String APPID = "34cc6df78cfa7cd457284e4fc377559e";
    private static String url = "http://115.28.27.150/api/";
    public static String auth_url = "http://115.28.27.150/uc/auth";
    //微信端的接口url
    /**
     * 微信端接口主要为讲座预告的接口，由于服务器端一些转发的问题，url为wechat2前缀
     */
    public static String wechat_lecture_notice_url = "http://115.28.27.150/wechat2/lecture";
    public static String android_service_url = "http://192.168.1.109:/uc/auth";
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

    public ApiHelper(Activity activity){
        context = activity;
        this.activity = activity;
        this.pref = context.getSharedPreferences("herald", Context.MODE_PRIVATE);
        this.editor = context.getSharedPreferences("herald", Context.MODE_PRIVATE).edit();
    }

    public static String getApiUrl(int api){
        return ApiHelper.url+ApiHelper.apiNames[api];
    }


    public void checkAuth(){
        //检查uuid的正确情况，如果正确则更新个人信息
        String uuid = getUUID();
        if(uuid == ""){
            Toast.makeText(context, "请登录", Toast.LENGTH_SHORT).show();
            doLogout();
        }
    }


    public void dealApiException(Exception e){
        if (e instanceof SocketTimeoutException) {
            Toast.makeText(context, "抱歉，学校服务器又出问题了T.T咱也是无能为力呀", Toast.LENGTH_SHORT).show();
        } else if (e instanceof ConnectException) {
            Toast.makeText(context, "网络连接错误，请检查您的网络连接~", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "很抱歉，发生了未知的错误T.T", Toast.LENGTH_SHORT).show();
        }
    }



    public void doLogout() throws ClassCastException{
        //清除授权信息
        setAuthCache("uuid", "");
        setAuthCache("cardnum", "");
        setAuthCache("schoolnum", "");
        setAuthCache("name", "");
        setAuthCache("sex", "");
        //清除模块缓存
        CacheHelper cacheHelper = new CacheHelper(context);
        cacheHelper.clearAllModuleCache();
        //跳转到登录页
        Intent intent = new Intent(context,LoginActivity.class);
        if(activity instanceof BaseAppCompatActivity){
            ((BaseAppCompatActivity) activity).startActivityAndFinish(intent);
        }else{
            throw new ClassCastException();
        }
    }



    public boolean isLogin(){
        //判断是否已登录
        SharedPreferences pref = context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE);
        String uuid = pref.getString("uuid","");
        if (uuid.equals("")){
            return false;
        }
        return true;
    }

    public String getUUID(){
        //获得存储的uuid
        SharedPreferences pref = context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE);
        String uuid = pref.getString("uuid","");
        return uuid;
    }

    public String getAuthCache(String cacheName){
        //可用
        /**
         * uuid         认证用uuid
         * cardnuim     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别
         */
        //获得存储的某项信息
        SharedPreferences pref = context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE);
        String authCache = pref.getString(cacheName,"");
        return authCache;
    }

    public boolean setAuthCache(String cacheName,String cacheValue){
        //用于更新存储的某项信息
        SharedPreferences.Editor editor= context.getSharedPreferences("herald_auth",context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }

}
