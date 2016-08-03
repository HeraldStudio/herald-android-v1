package cn.seu.herald_android.factory;

import org.json.JSONObject;

import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.app_module.cardextra.CardActivity;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;

public class CardCard {

    public static ApiRequest getRefresher() {
        return new ApiSimpleRequest(Method.POST).api("card").addUuid().post("timedelta", "1")
                .toCache("herald_card_today");
    }

    /**
     * 读取一卡通缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        if (!ApiHelper.isLogin()) {
            return new CardsModel(Module.card,
                    CardsModel.Priority.NO_CONTENT, "登录可使用消费查询、充值、余额提醒功能"
            );
        }

        String cache = CacheHelper.get("herald_card_today");
        try {
            JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
            // 获取余额并且设置
            String left = json_cache.getString("left").replaceAll(",", "");
            float extra = Float.valueOf(left);

            if (extra < 20) {
                CardsModel item = new CardsModel(Module.card,
                        CardsModel.Priority.CONTENT_NOTIFY, "一卡通余额还有" + left + "元，快点我充值~\n如果已经充值过了，需要在食堂刷卡一次才会更新哦~"
                );
                item.setOnClickListener(v ->
                        new AppModule("一卡通充值", CardActivity.chargeUrl).open());
                return item;
            } else {
                return new CardsModel(Module.card,
                        CardsModel.Priority.CONTENT_NO_NOTIFY, "你的一卡通余额还有" + left + "元"
                );
            }
        } catch (Exception e) {
            return new CardsModel(Module.card,
                    CardsModel.Priority.CONTENT_NOTIFY, "一卡通数据为空，请尝试刷新"
            );
        }
    }
}