package cn.seu.herald_android.factory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.app_module.pedetail.PedetailActivity;
import cn.seu.herald_android.app_module.pedetail.PedetailBlockLayout;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;

public class PedetailCard {

    public static ApiRequest getRefresher() {
        return (
                Cache.pcForecast.getRefresher()
        ).parallel(
                Cache.peDetail.getRefresher()
        ).parallel(
                Cache.peCount.getRefresher()
        );
    }

    /**
     * 读取跑操预报缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        if (!ApiHelper.isLogin()) {
            return new CardsModel(Module.pedetail,
                    CardsModel.Priority.NO_CONTENT, "登录即可使用跑操查询、跑操预告功能"
            );
        }

        final long now = Calendar.getInstance().getTimeInMillis();

        String date = CacheHelper.get("herald_pc_date");
        String forecast = Cache.pcForecast.getValue();
        String record = Cache.peDetail.getValue();

        int count, remain;
        try {
            count = Integer.valueOf(Cache.peCount.getValue());
        } catch (NumberFormatException e) {
            count = 0;
        }

        try {
            remain = Integer.valueOf(Cache.peRemain.getValue());
        } catch (NumberFormatException e) {
            remain = 0;
        }

        Calendar nowCal = Calendar.getInstance();
        long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
        long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;
        long endTime = today + PedetailActivity.FORECAST_TIME_PERIOD[1] * 60 * 1000;

        String todayStamp = new SimpleDateFormat("yyyy-MM-dd").format(nowCal.getTime());

        if (record.contains(todayStamp)) {
            CardsModel item = new CardsModel(Module.pedetail,
                    CardsModel.Priority.CONTENT_NOTIFY,
                    "你今天的跑操已经到账。" + getRemainNotice(count, remain, false)
            );

            item.attachedView.add(new PedetailBlockLayout(AppContext.instance, count, Math.max(0, 45 - count), remain));

            return item;
        }

        if (now >= startTime && !date.equals(String.valueOf(CalendarUtils.toSharpDay(nowCal).getTimeInMillis()))) {
            return new CardsModel(Module.pedetail,
                    CardsModel.Priority.CONTENT_NOTIFY, "跑操预告数据为空，请尝试刷新"
            );
        }

        if (now < startTime) {
            // 跑操时间没到
            CardsModel item = new CardsModel(Module.pedetail,
                    CardsModel.Priority.CONTENT_NO_NOTIFY, "小猴会在早上跑操时间实时显示跑操预告\n"
                    + getRemainNotice(count, remain, false)
            );

            item.attachedView.add(new PedetailBlockLayout(AppContext.instance, count, Math.max(0, 45 - count), remain));

            return item;
        } else if (now >= endTime) {
            // 跑操时间已过

            if (!forecast.contains("跑操")) {
                // 没有跑操预告信息
                CardsModel item = new CardsModel(Module.pedetail,
                        CardsModel.Priority.CONTENT_NO_NOTIFY, "今天没有跑操预告信息\n"
                        + getRemainNotice(count, remain, false)
                );

                item.attachedView.add(new PedetailBlockLayout(AppContext.instance, count, Math.max(0, 45 - count), remain));

                return item;
            } else {
                // 有跑操预告信息但时间已过
                CardsModel item = new CardsModel(Module.pedetail,
                        CardsModel.Priority.CONTENT_NO_NOTIFY, forecast + "(已结束)\n"
                        + getRemainNotice(count, remain, false)
                );

                item.attachedView.add(new PedetailBlockLayout(AppContext.instance, count, Math.max(0, 45 - count), remain));

                return item;
            }
        } else {
            // 还没有跑操预告信息
            if (!forecast.contains("跑操")) {
                CardsModel item = new CardsModel(Module.pedetail,
                        CardsModel.Priority.CONTENT_NO_NOTIFY, "目前暂无跑操预报信息，过一会再来看吧~\n"
                        + getRemainNotice(count, remain, false)
                );

                item.attachedView.add(new PedetailBlockLayout(AppContext.instance, count, Math.max(0, 45 - count), remain));

                return item;
            }

            // 有跑操预告信息
            CardsModel item = new CardsModel(Module.pedetail,
                    CardsModel.Priority.CONTENT_NOTIFY, "小猴预测" + forecast + "\n"
                    + getRemainNotice(count, remain, forecast.contains("今天正常跑操"))
            );

            item.attachedView.add(new PedetailBlockLayout(AppContext.instance, count, Math.max(0, 45 - count), remain));

            return item;
        }
    }

    private static String getRemainNotice(int count, int remain, boolean todayAvailable) {

        if (count == 0) return "你这学期还没有跑操，如果是需要跑操的同学要加油咯~";
        if (count >= 45) {
            return "已经跑够次数啦，" + (remain > 0 && remain >= 50 - count ?
                    "你还可以再继续加餐，多多益善哟~" : "小猴给你个满分~");
        }
        int offset = remain - (45 - count);
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
