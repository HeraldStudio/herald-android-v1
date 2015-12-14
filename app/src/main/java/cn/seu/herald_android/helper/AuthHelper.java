package cn.seu.herald_android.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.NetworkOnMainThreadException;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;


import cn.seu.herald_android.ApiClient.ApiClient;
import cn.seu.herald_android.exception.AuthException;

/**
 * Created by heyon on 2015/12/8.
 */
public class AuthHelper {
    private Context context;
    public static String APPID = "34cc6df78cfa7cd457284e4fc377559e";
    public static String url = "http://115.28.27.150/api/";
    public AuthHelper(Context context) {
        this.context = context;
    }

    public void checkAuth()throws AuthException,NetworkOnMainThreadException{
        //检查uuid的正确情况，如果正确则更新个人信息
        String uuid = getUUID();
        if (uuid.equals(""))
            throw new AuthException("未登录",AuthException.HAVE_NOT_LOGIN);
        try{
            ApiClient apiClient = new ApiClient();
            String result = apiClient.doRequest(ApiClient.API_USER,uuid);
            Log.d("checkAuth",result);
            if(new JSONObject(result).getInt("code")==200)
            {
                JSONObject content = new JSONObject(result).getJSONObject("content");
                String cardnum = content.getString("cardnum");
                String schoolnum = content.getString("schoolnum");
                String name = content.getString("name");
                String sex = content.getString("sex");
                SharedPreferences.Editor editor  = context.getSharedPreferences("Auth",context.MODE_PRIVATE).edit();
                editor.putString("cardnum",cardnum);
                editor.putString("schoolnum",schoolnum);
                editor.putString("name",name);
                editor.putString("sex",sex);
                editor.commit();
            }
        }catch (AuthException e){
            throw e;
        }catch (JSONException e){
            e.printStackTrace();
            throw new AuthException("刷新个人信息时遇到了未知错误",AuthException.UNKONW_ERROR);
        }
    }

    public String doLogin(String cardnum,String pwd)throws NetworkOnMainThreadException,AuthException{
        //联网登录，获取并保存uuid,然后返回
        ApiClient apiClient = new ApiClient();
        String uuid = apiClient.doAuth(cardnum,pwd);
        if(setAuthCache("uuid",uuid))
        //更新个人信息
            checkAuth();
        return uuid;
    }

    public void doLogout() {
        setAuthCache("uuid", "");
        setAuthCache("cardnum", "");
        setAuthCache("schoolnum", "");
        setAuthCache("name", "");
        setAuthCache("sex", "");
    }

    public boolean isLogin(){
        //判断是否已登录
        SharedPreferences pref = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        String uuid = pref.getString("uuid","");
        if (uuid.equals("")){
            return false;
        }
        return true;
    }

    public String getUUID(){
        //获得存储的uuid
        SharedPreferences pref = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
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
        SharedPreferences pref = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        String authCache = pref.getString(cacheName,"");
        return authCache;
    }

    public boolean setAuthCache(String cacheName,String cacheValue){
        //用于更新存储的某项信息
        SharedPreferences.Editor editor= context.getSharedPreferences("Auth",context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }





}
