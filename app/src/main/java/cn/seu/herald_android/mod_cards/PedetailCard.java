package cn.seu.herald_android.mod_cards;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.pedetail.CardsPedetailRowLayout;
import cn.seu.herald_android.mod_query.pedetail.PedetailActivity;

public class PedetailCard {

    public static ApiRequest[] getRefresher() {
        return new ApiRequest[]{
                new ApiRequest().api("pc").addUUID().noCheck200()// 201也属于成功
                        .toCache("herald_pc_forecast", o -> o.getString("content"))
                        .onFinish((success, code, response) -> {
                    long today = CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis();
                    if (success) {
                        CacheHelper.set("herald_pc_date", String.valueOf(today));
                    } else if (code == 201) { // 今天还没有预告
                        CacheHelper.set("herald_pc_date", String.valueOf(today));
                        // 覆盖旧的预告信息
                        CacheHelper.set("herald_pc_forecast", "refreshing");
                    }
                }),
                new ApiRequest().api("pedetail").addUUID()
                        .toCache("herald_pedetail", o -> o.getJSONArray("content")),
                new ApiRequest().api("pe").addUUID()
                        .toCache("herald_pe_count", o -> o.getString("content"))
                        .toCache("herald_pe_remain", o -> o.getString("remain"))
        };
    }

    /**
     * 读取跑操预报缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        final long now = Calendar.getInstance().getTimeInMillis();

        String date = CacheHelper.get("herald_pc_date");
        String forecast = CacheHelper.get("herald_pc_forecast");
        String record = CacheHelper.get("herald_pedetail");

        try {
            int count = Integer.valueOf(CacheHelper.get("herald_pe_count"));
            int remain = Integer.valueOf(CacheHelper.get("herald_pe_remain"));

            Calendar nowCal = Calendar.getInstance();
            long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
            long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;
            long endTime = today + PedetailActivity.FORECAST_TIME_PERIOD[1] * 60 * 1000;

            String todayStamp = new SimpleDateFormat("yyyy-MM-dd").format(nowCal.getTime());

            if (record.contains(todayStamp)) {
                CardsModel item = new CardsModel(SettingsHelper.Module.pedetail,
                        CardsModel.Priority.CONTENT_NOTIFY,
                        "你今天的跑操已经到账。" + getRemainNotice(count, remain, false)
                );

                item.attachedView.add(new CardsPedetailRowLayout(AppContext.currentContext.$get(), count, Math.max(0, 45 - count), remain));

                return item;
            }

            if (now >= startTime && !date.equals(String.valueOf(CalendarUtils.toSharpDay(nowCal).getTimeInMillis()))) {
                return new CardsModel(SettingsHelper.Module.pedetail,
                        CardsModel.Priority.CONTENT_NOTIFY, "跑操预告数据为空，请尝试刷新"
                );
            }

            if (now < startTime) {
                // 跑操时间没到
                CardsModel item = new CardsModel(SettingsHelper.Module.pedetail,
                        CardsModel.Priority.CONTENT_NO_NOTIFY, "小猴会在早上跑操时间实时显示跑操预告\n"
                        + getRemainNotice(count, remain, false)
                );

                item.attachedView.add(new CardsPedetailRowLayout(AppContext.currentContext.$get(), count, Math.max(0, 45 - count), remain));

                return item;
            } else if (now >= endTime) {
                // 跑操时间已过

                if (!forecast.contains("跑操")) {
                    // 没有跑操预告信息
                    CardsModel item = new CardsModel(SettingsHelper.Module.pedetail,
                            CardsModel.Priority.CONTENT_NO_NOTIFY, "今天没有跑操预告信息\n"
                            + getRemainNotice(count, remain, false)
                    );

                    item.attachedView.add(new CardsPedetailRowLayout(AppContext.currentContext.$get(), count, Math.max(0, 45 - count), remain));

                    return item;
                } else {
                    // 有跑操预告信息但时间已过
                    CardsModel item = new CardsModel(SettingsHelper.Module.pedetail,
                            CardsModel.Priority.CONTENT_NO_NOTIFY, forecast + "(已结束)\n"
                            + getRemainNotice(count, remain, false)
                    );

                    item.attachedView.add(new CardsPedetailRowLayout(AppContext.currentContext.$get(), count, Math.max(0, 45 - count), remain));

                    return item;
                }
            } else {
                // 还没有跑操预告信息
                if (!forecast.contains("跑操")) {
                    CardsModel item = new CardsModel(SettingsHelper.Module.pedetail,
                            CardsModel.Priority.CONTENT_NO_NOTIFY, "目前暂无跑操预报信息，过一会再来看吧~\n"
                            + getRemainNotice(count, remain, false)
                    );

                    item.attachedView.add(new CardsPedetailRowLayout(AppContext.currentContext.$get(), count, Math.max(0, 45 - count), remain));

                    return item;
                }

                // 有跑操预告信息
                CardsModel item = new CardsModel(SettingsHelper.Module.pedetail,
                        CardsModel.Priority.CONTENT_NOTIFY, "小猴预测" + forecast + "\n"
                        + getRemainNotice(count, remain, forecast.contains("今天正常跑操"))
                );

                item.attachedView.add(new CardsPedetailRowLayout(AppContext.currentContext.$get(), count, Math.max(0, 45 - count), remain));

                return item;
            }
        } catch (Exception e) {
            return new CardsModel(SettingsHelper.Module.pedetail,
                    CardsModel.Priority.CONTENT_NOTIFY, "跑操数据为空，请尝试刷新"
            );
        }
    }

    private static String getRemainNotice(int count, int remain, boolean todayAvailable) {

        if (count == 0) return "你这学期还没有跑操，如果是需要跑操的同学要加油咯~";
        if (count >= 45) {
            return "已经跑够次数啦，" + (remain > 0 && remain >= 50 - count ?
                    "你还可以再继续加餐，多多益善哟~" : "小猴给你个满分~");
        }
        int offset =  remain - (45 - count);
        if (offset >= 20) {
            return "时间似乎比较充裕，但还是要加油哟~";
        } else if (offset >= 10) {
            return "时间比较紧迫了，" + (todayAvailable ? "赶紧加油出门跑操吧~" : "还需要继续加油哟~");
        } else if (offset >= 0) {
            return "没时间解释了，" + (todayAvailable ? "赶紧出门补齐跑操吧~" : "赶紧找机会补齐跑操吧~");
        } else {
            return "似乎没什么希望了，小猴为你感到难过，不如参加一些加跑操的活动试试？";
        }
    }
}
