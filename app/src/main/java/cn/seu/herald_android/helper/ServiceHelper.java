package cn.seu.herald_android.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.framework.UserCache;

public class ServiceHelper {

    private ServiceHelper() {}

    public static UserCache serviceCache = new UserCache("herald_service");

    public static String get(String key) {
        return serviceCache.get(key);
    }

    public static void set(String key, String value) {
        serviceCache.set(key, value);
    }

    public static ArrayList<SliderView.SliderViewItem> getSliderViewItemArray() {
        // 获得服务器端的推送内容，这里是获得轮播栏各项设置并且返回数组
        String cache = get("versioncheck_cache");
        ArrayList<SliderView.SliderViewItem> list = new ArrayList<>();
        try {
            JSONArray slideViewArray = new JSONObject(cache).getJSONObject("content").getJSONArray("sliderviews");
            for (int i = 0; i < slideViewArray.length(); i++) {
                JSONObject jsonItem = slideViewArray.getJSONObject(i);
                list.add(new SliderView.SliderViewItem(
                        jsonItem.getString("title"),
                        jsonItem.getString("imageurl"),
                        jsonItem.getString("url")
                ));
            }
        } catch (JSONException e) {
            list = null;
        }
        return list;
    }

    public static int getNewestVersionCode() {
        String cache = ServiceHelper.get("versioncheck_cache");

        try {
            return new JSONObject(cache)
                    .getJSONObject("content").getJSONObject("version").getInt("code");
        } catch (JSONException e) {
            // 此处为服务器判断应用版本已为最新，返回值中没有带version，属正常现象，直接返回0
            return 0;
        }
    }

    public static String getNewestVersionName() {
        String cache = ServiceHelper.get("versioncheck_cache");

        try {
            return new JSONObject(cache)
                    .getJSONObject("content").getJSONObject("version").getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getNewestVersionDesc() {
        String cache = ServiceHelper.get("versioncheck_cache");

        try {
            return new JSONObject(cache)
                    .getJSONObject("content").getJSONObject("version").getString("des");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}

    /*
    private Context context;
    private ApiHelper apiHelper;
    // 安卓服务端的接口
    private static String android_service_url = "http://app.heraldstudio.com/";
    // public static String android_service_url = "http://192.168.1.109:3000/";
    private static final int SERVICE_VERSION = 0;
    public static final int SERVICE_DOWNLOAD = 1;
    private static String[] serviceNames = new String[]{
            "checkversion",
            "download"
    };

    public static String getServiceUrl(int service) {
        return ServiceHelper.android_service_url + ServiceHelper.serviceNames[service];
    }

    public ServiceHelper(Context context) {
        this.context = context;
        this.apiHelper = new ApiHelper(context);
    }

    public String getPushMessageContent() {
        // 获得服务器端的推送消息
        String cache = getServiceCache("versioncheck_cache");
        String message;
        try {
            JSONObject jsonObject = new JSONObject(cache).getJSONObject("content").getJSONObject("message");
            message = jsonObject.getString("content");
            if (message.equals("null"))message="";
        } catch (JSONException e) {
            message = "";
        }
        return message;
    }

    public String getPushMessageUrl() {
        // 获得服务器端的推送消息链接
        String cache = getServiceCache("versioncheck_cache");
        String messageUrl;
        try {
            JSONObject jsonObject = new JSONObject(cache).getJSONObject("content").getJSONObject("message");
            messageUrl = jsonObject.getString("url");
        } catch (JSONException e) {
            // 如果异常则指调为空
            messageUrl = "";
        }
        return messageUrl;
    }




    private String getServiceCache(String cacheName) {
        // 可用
        /
         * uuid         认证用uuid
         * cardnuim     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别

        // 获得存储的某项信息
        SharedPreferences pref = context.getSharedPreferences("herald_service", Context.MODE_PRIVATE);
        return pref.getString(cacheName, "");
    }

    public void setServiceCache(String cacheName, String cacheValue) {
        // 用于更新存储的某项信息
        SharedPreferences.Editor editor = context.getSharedPreferences("herald_service", Context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        editor.apply();
    }



}*/
