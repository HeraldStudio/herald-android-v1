package cn.seu.herald_android.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;

import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.mod_auth.LoginActivity;

public class ApiHelper {
    //可用doRequest调用的API
    //SRTP学分查询
    public static final int API_SRTP = 0;
    //本学期课表学分查询
    public static final int API_SIDEBAR = 1;
    //课表查询
    public static final int API_CURRICULUM = 2;
    //绩点查询(较慢)
    public static final int API_GPA = 3;
    //跑操次数查询
    public static final int API_PE = 4;
    //校园网账户情况
    public static final int API_NIC = 5;
    //一卡通余额
    public static final int API_CARD = 6;
    //人文讲座查询
    public static final int API_LECTURE = 7;
    //物理实验查询
    public static final int API_PHYLAB = 8;
    //跑操预报
    public static final int API_PC = 9;
    //教务处通知
    public static final int API_JWC = 10;
    //校车
    public static final int API_SCHOOLBUS = 11;
    //课程预报（今天剩下的课的消息）
    public static final int API_LEC_NOTICE = 12;
    //个人信息查询
    public static final int API_USER = 13;
    //宿舍查询
    public static final int API_ROOM = 14;
    //跑操详情查询
    public static final int API_PEDETAIL = 15;
    //学期列表查询
    public static final int API_TERM = 16;
    //图书馆藏书搜索
    public static final int API_LIBRARY_SEARCH = 17;
    //图书馆已借图书
    public static final int API_LIBRARY_MYBOOK = 18;
    //热门图书
    public static final int API_LIBRARY_HOTBOOK = 19;
    /**
     * 调戏
     * 参数
     * uuid：要搜索的书名
     * msg：对话内容
     */
    public static final int API_SIMSIMI = 0;
    //暂时无法使用的两个
    public static final int API_RENEW = 1;

    /**
     * 参数
     * book:要搜索的书名
     */
    public static final int API_BOOK_SEARCH = 3;


    //需用其他方式访问的
    public static String auth_url = "http://115.28.27.150/uc/auth";
    public static String auth_update_url = "http://115.28.27.150/uc/update";
    /**
     * 微信端接口主要为讲座预告的接口，由于服务器端一些转发的问题，url为wechat2前缀
     */
    //微信端的接口url
    public static String wechat_lecture_notice_url = "http://115.28.27.150/wechat2/lecture";
    private static String url = "http://115.28.27.150/api/";

    //图书馆藏书搜索
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
            "room",
            "pedetail",
            "term",
            "search",
            "library",
            "library_hot"
    };
    private static String packagePath;
    private Context context;


    //用到logout函数的部分应该调用此函数
    public ApiHelper(Context context) {
        this.context = context;
        packagePath = context.getPackageResourcePath();
    }


    public static String getApiUrl(int api) {
        return ApiHelper.url + ApiHelper.apiNames[api];
    }

    public static String getAppId() {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead;
        MessageDigest md5;
        String test = "";
        try {
            fis = new FileInputStream(packagePath);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            test = HashHelper.toHexString(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 发布流程：
        // 下面第一行去掉注释，第二行加注释，
        // 编译，打包，签名，制成发布版本，
        // 用工具计算md5，在服务器登记appid，
        // 联网测试，上线

        //return test;
        return "34cc6df78cfa7cd457284e4fc377559e";
    }

    public void dealApiException(Exception e) {
        e.printStackTrace();
        if (e instanceof SocketTimeoutException) {
            ContextUtils.showMessage(context, "抱歉，学校服务器又出问题了T.T咱也是无能为力呀");
        } else if (e instanceof ConnectException || e instanceof SocketException) {
            ContextUtils.showMessage(context, "网络连接错误，请检查您的网络连接~");
        } else if (e instanceof RuntimeException && e.toString().contains("Unauthorized")) {
            // uuid过期的处理
            Toast.makeText(context, "账号身份已过期，请重新登录", Toast.LENGTH_LONG).show();
            doLogout();
        } else {
            ContextUtils.showMessage(context, "出现未知错误，请尝试重新登录");
        }
    }


    public void doLogout() {
        //清除授权信息
        setAuthCache("authUser", "");
        setAuthCache("authPwd", "");
        setAuthCache("uuid", "");
        setAuthCache("cardnum", "");
        setAuthCache("schoolnum", "");
        setAuthCache("name", "");
        setAuthCache("sex", "");
        //清除模块缓存
        CacheHelper cacheHelper = new CacheHelper(context);
        cacheHelper.clearAllModuleCache();
        //跳转到登录页

        //如果activity为空会抛出异常
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    }


    public boolean isLogin() {
        //判断是否已登录
        SharedPreferences pref = context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE);
        String uuid = pref.getString("uuid", "");
        return !uuid.equals("");
    }

    public String getUUID() {
        //获得存储的uuid
        SharedPreferences pref = context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE);
        return pref.getString("uuid", "");
    }

    public void setAuth(String username, String password) {
        try {
            String encrypted = new EncryptHelper(username).encrypt(password);
            CacheHelper helper = new CacheHelper(context);
            helper.setCache("authUser", username);
            helper.setCache("authPwd", encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPassword() {
        CacheHelper helper = new CacheHelper(context);
        String username = getUserName();
        EncryptHelper helper1 = new EncryptHelper(username);
        return helper1.decrypt(helper.getCache("authPwd"));
    }

    public String getUserName() {
        CacheHelper helper = new CacheHelper(context);
        return helper.getCache("authUser");
    }

    public String getAuthCache(String cacheName) {
        //可用
        /**
         * uuid         认证用uuid
         * cardnum     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别
         */
        //获得存储的某项信息
        SharedPreferences pref = context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE);
        return pref.getString(cacheName, "");
    }

    public boolean setAuthCache(String cacheName, String cacheValue) {
        //用于更新存储的某项信息
        SharedPreferences.Editor editor = context.getSharedPreferences("herald_auth", Context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }

    public String getSchoolnum() {
        return getAuthCache("schoolnum");
    }
}
