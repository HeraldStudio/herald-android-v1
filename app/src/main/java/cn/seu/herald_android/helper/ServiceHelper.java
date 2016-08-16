package cn.seu.herald_android.helper;

import java.util.ArrayList;

import cn.seu.herald_android.custom.SliderView;
import cn.seu.herald_android.framework.UserCache;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class ServiceHelper {

    private ServiceHelper() {
    }

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
        JArr slideViewArray = new JObj(cache).$o("content").$a("sliderviews");
        for (int i = 0; i < slideViewArray.size(); i++) {
            JObj jsonItem = slideViewArray.$o(i);
            list.add(new SliderView.SliderViewItem(
                    jsonItem.$s("title"),
                    jsonItem.$s("imageurl"),
                    jsonItem.$s("url")
            ));
        }
        return list;
    }

    public static int getNewestVersionCode() {
        String cache = ServiceHelper.get("versioncheck_cache");

        // 若服务器判断应用版本已为最新，返回值中没有带version，属正常现象，在json处理这步会返回0
        return new JObj(cache).$o("content").$o("version").$i("code");
    }

    public static String getNewestVersionName() {
        String cache = ServiceHelper.get("versioncheck_cache");

        return new JObj(cache)
                .$o("content").$o("version").$s("name");
    }

    public static String getNewestVersionDesc() {
        String cache = ServiceHelper.get("versioncheck_cache");

        return new JObj(cache)
                .$o("content").$o("version").$s("des");
    }
}