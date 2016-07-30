package cn.seu.herald_android.factory;

import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.SystemUtil;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ServiceHelper;

public class ServiceCard {

    public static ApiRequest getRefresher() {
        return new ApiSimpleRequest(Method.POST).url("http://android.heraldstudio.com/checkversion").addUuid()
                .post("schoolnum", ApiHelper.getCurrentUser().schoolNum,
                        "versioncode", String.valueOf(SystemUtil.getAppVersionCode()),
                        "versionname", SystemUtil.getAppVersionName(),
                        "versiontype", "Android")
                .toServiceCache("versioncheck_cache");
    }

    public static CardsModel getPushMessageCard() {
        String cache = ServiceHelper.get("versioncheck_cache");
        String pushMessage = "", pushMessageUrl = "";

        try {
            pushMessage = new JSONObject(cache)
                    .getJSONObject("content").getJSONObject("message").getString("content");
            pushMessageUrl = new JSONObject(cache)
                    .getJSONObject("content").getJSONObject("message").getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!pushMessage.equals("")) {
            CardsModel item = new CardsModel("小猴提示", pushMessage,
                    CardsModel.Priority.CONTENT_NOTIFY, R.mipmap.ic_pushmsg);

            final String finalUrl = pushMessageUrl;
            item.setOnClickListener(v -> {
                if (!finalUrl.equals("") && !finalUrl.equals("null")) {
                    new AppModule("小猴提示", finalUrl).open();
                }
            });
            return item;
        }

        return null;
    }

    public static CardsModel getCheckVersionCard() {

        // 如果版本有更新则提示更新版本
        int versionCode = SystemUtil.getAppVersionCode();

        if (versionCode < ServiceHelper.getNewestVersionCode()) {
            // 如果当前版本号小于最新版本，且用户没有忽略此版本，则提示更新
            String tip = "小猴偷米" + ServiceHelper.getNewestVersionName() + "更新说明\n"
                    + ServiceHelper.getNewestVersionDesc().replaceAll("\\\\n", "\n") + "\n\n点我下载新版本吧";
            CardsModel item = new CardsModel("版本升级", tip,
                    CardsModel.Priority.CONTENT_NOTIFY, R.mipmap.ic_update);

            item.setOnClickListener((v) ->
                    AppContext.openUrlInBrowser("http://android.heraldstudio.com/download"));

            return item;
        }

        return null;
    }
}
