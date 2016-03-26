package cn.seu.herald_android.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;

/**
 * Created by heyon on 2016/3/14.
 */
public class ServiceHelper {
    private Context context;
    private ApiHelper apiHelper;
    //安卓服务端的接口
    private static String android_service_url = "http://android.heraldstudio.com/";
    //public static String android_service_url = "http://192.168.1.109:3000/";
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

    public static ApiRequest refreshVersionCache(Context context) {
        return new ApiRequest(context).url(getServiceUrl(SERVICE_VERSION)).uuid()
                .post("schoolnum", new ApiHelper(context).getAuthCache("schoolnum"),
                        "versioncode", String.valueOf(getAppVersionCode(context)))
                .toServiceCache("versioncheck_cache", o -> o);
    }

    public String getPushMessageContent() {
        //获得服务器端的推送消息
        String cache = getServiceCache("versioncheck_cache");
        String message = "";
        try {
            JSONObject jsonObject = new JSONObject(cache).getJSONObject("content").getJSONObject("message");
            message = jsonObject.getString("content");
        } catch (JSONException e) {
            message = "";
        }
        return message;
    }

    public String getPushMessageUrl() {
        //获得服务器端的推送消息链接
        String cache = getServiceCache("versioncheck_cache");
        String messageUrl = "";
        try {
            JSONObject jsonObject = new JSONObject(cache).getJSONObject("content").getJSONObject("message");
            messageUrl = jsonObject.getString("url");
        } catch (JSONException e) {
            //如果异常则指调为空
            messageUrl = "";
        }
        return messageUrl;
    }

    public String getNewestVersionName() {
        //获得最新版本名字
        String cache = getServiceCache("versioncheck_cache");
        String name = getAppVersionName(this.context);
        try {
            JSONObject jsonObject = new JSONObject(cache).getJSONObject("content").getJSONObject("version");
            name = jsonObject.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    public String getNewestVersionDesc() {
        //获得最新版本更新说明
        String cache = getServiceCache("versioncheck_cache");
        String desc = getAppVersionName(this.context);
        try {
            JSONObject jsonObject = new JSONObject(cache).getJSONObject("content").getJSONObject("version");
            desc = jsonObject.getString("des");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return desc;
    }

    public int getNewestVersionCode() {
        //获得最新版本号
        String cache = getServiceCache("versioncheck_cache");
        int code = getAppVersionCode(this.context);
        try {
            JSONObject jsonObject = new JSONObject(cache).getJSONObject("content").getJSONObject("version");
            code = jsonObject.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    public ArrayList<SliderView.SliderViewItem> getSliderViewItemArray() {
        //获得服务器端的推送内容，这里是获得轮播栏各项设置并且返回数组
        String cache = getServiceCache("versioncheck_cache");
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


    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }


    public static int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private String getServiceCache(String cacheName) {
        //可用
        /**
         * uuid         认证用uuid
         * cardnuim     一卡通号
         * schoolnum    学号
         * name         名字
         * sex          性别
         */
        //获得存储的某项信息
        SharedPreferences pref = context.getSharedPreferences("herald_service", Context.MODE_PRIVATE);
        return pref.getString(cacheName, "");
    }

    public boolean setServiceCache(String cacheName, String cacheValue) {
        //用于更新存储的某项信息
        SharedPreferences.Editor editor = context.getSharedPreferences("herald_service", Context.MODE_PRIVATE).edit();
        editor.putString(cacheName, cacheValue);
        return editor.commit();
    }


    public static TimelineItem getPushMessageItem(TimelineView host) {
        ServiceHelper helper = new ServiceHelper(host.getContext());
        final long now = Calendar.getInstance().getTimeInMillis();

        //获取推送消息
        String pushMessage = helper.getPushMessageContent();
        if (!pushMessage.equals("")) {
            TimelineItem item = new TimelineItem("小猴提示", pushMessage,
                    now, TimelineItem.CONTENT_NOTIFY, R.mipmap.ic_pushmsg);

            String pushMessageUrl = helper.getPushMessageUrl();
            if (!pushMessageUrl.equals("") && !pushMessageUrl.equals("null")) {
                Uri uri = Uri.parse(pushMessageUrl);
                item.addButton(host.getContext(), "查看详情", (v) ->
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri)));
                item.setOnClickListener((v) ->
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri)));
            }
            return item;
        }

        return null;
    }

    public static TimelineItem getCheckVersionItem(TimelineView host) {
        ServiceHelper serviceHelper = new ServiceHelper(host.getContext());
        CacheHelper cacheHelper = new CacheHelper(host.getContext());
        final long now = Calendar.getInstance().getTimeInMillis();
        //如果版本有更新则提示更新版本
        int versionCode = ServiceHelper.getAppVersionCode(host.getContext());
        int newestCode = serviceHelper.getNewestVersionCode();

        if (versionCode < newestCode
                && !cacheHelper.getCache("herald_new_version_ignored").equals(String.valueOf(newestCode))) {
            //如果当前版本号小于最新版本，且用户没有忽略此版本，则提示更新
            String tip = "小猴偷米" + serviceHelper.getNewestVersionName() + "更新说明\n"
                    + serviceHelper.getNewestVersionDesc().replaceAll("\\\\n", "\n");
            TimelineItem item = new TimelineItem("版本升级", tip,
                    now, TimelineItem.CONTENT_NOTIFY, R.mipmap.ic_update);

            item.addButton(host.getContext(), "下载", (v) -> {
                Uri uri = Uri.parse(ServiceHelper.getServiceUrl(ServiceHelper.SERVICE_DOWNLOAD));
                host.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
            });

            item.addButton(host.getContext(), "忽略此版本", (v) -> {
                ContextUtils.showMessage(host.getContext(), "已忽略此版本，你仍可在系统设置中找到此更新");
                cacheHelper.setCache("herald_new_version_ignored", String.valueOf(newestCode));
                host.loadContent(false);
            });

            return item;
        }

        return null;
    }
}
