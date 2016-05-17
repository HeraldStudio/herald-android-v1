package cn.seu.herald_android.mod_afterschool;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;

/**
 * Created by heyon on 2016/5/9.
 */
public class AfterSchoolActivityItem {
    String title;
    String introduciton;
    String start_time;
    String end_time;
    String activity_time;
    String assiciation;
    String location;
    String detail_url;
    String pic_url;
    //标识活动是否已开始等信息
    String tag;
    //标识活动开始信息的颜色
    int tagColorID;
    public AfterSchoolActivityItem(String title, String introduciton, String start_time, String end_time, String activity_time, String detail_url, String assiciation, String location,String pic_url) {
        this.title = title;
        this.introduciton = introduciton;
        this.start_time = start_time;
        this.end_time = end_time;
        this.activity_time = activity_time;
        this.detail_url = detail_url;
        this.assiciation = assiciation;
        this.location = location;
        this.pic_url = pic_url;

        //判断活动是否开始
        long now_time = CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis();
        long start_time_value = getStartCalendar().getTimeInMillis();
        long end_time_value = getEndCalendar().getTimeInMillis();
        if(now_time < start_time_value){
            tag = "即将开始";
            tagColorID =  R.color.colorSecondaryText;
        } else if (now_time > end_time_value) {
            tag = "已结束";
            tagColorID =  R.color.colorSecondaryText;
        }else {
            tag = "进行中";
            tagColorID =  R.color.relaxGreen;
        }
    }

    public Calendar getStartCalendar(){
        Calendar dst = Calendar.getInstance();
        dst.set(
                Integer.parseInt(start_time.split("-")[0]),
                Integer.parseInt(start_time.split("-")[1])-1,
                Integer.parseInt(start_time.split("-")[2])
                );
        return CalendarUtils.toSharpDay(dst);
    }



    public Calendar getEndCalendar(){
        Calendar dst = Calendar.getInstance();
        dst.set(
                Integer.parseInt(end_time.split("-")[0]),
                Integer.parseInt(end_time.split("-")[1])-1,
                Integer.parseInt(end_time.split("-")[2])
        );
        return CalendarUtils.toSharpDay(dst);
    }

    public Uri getDetailUri(){
        Uri uri = Uri.parse(this.detail_url);
        return uri;
    }

    public Uri getPicUrl(){
        Uri uri = Uri.parse(this.pic_url);
        return uri;
    }

    public String getTag(){
        return tag;
    }

    public int getTagColorId(){
        return tagColorID;
    }



    public static ArrayList<AfterSchoolActivityItem> transfromJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<AfterSchoolActivityItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            list.add(new AfterSchoolActivityItem(
                    jsonItem.getString("title"),
                    jsonItem.getString("introduction"),
                    jsonItem.getString("start_time"),
                    jsonItem.getString("end_time"),
                    jsonItem.getString("activity_time"),
                    jsonItem.getString("detail_url"),
                    jsonItem.getString("association"),
                    jsonItem.getString("location"),
                    jsonItem.getString("pic_url")
            ));
        }
        return list;
    }

    //获取最新热门活动
    public static ApiRequest remoteRefreshCache(Context context) {
        return new ApiRequest(context)
                .get()
                .url(ApiHelper.getLiveApiUrl(ApiHelper.API_LIVE_HOTAFTERSCHOOLACTIVITY))
                .toCache("herald_afterschoolschool_hot", o -> o);
    }

    /**
     * 读取热门活动缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getAfterSchoolActivityItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_afterschoolschool_hot");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            List<AfterSchoolActivityItem> afterSchoolActivityItems = AfterSchoolActivityItem.transfromJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            if (afterSchoolActivityItems.size() == 0) {
                return new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                        now, TimelineItem.NO_CONTENT, "最近没有热门活动");
            } else {
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                        now, TimelineItem.CONTENT_NOTIFY, "最近有" + afterSchoolActivityItems.size() + "个热门活动");
                for (AfterSchoolActivityItem afterSchoolActivityItem : afterSchoolActivityItems) {
                    item.attachedView.add(new AfterSchoolActivityBlockLayout(host.getContext(), afterSchoolActivityItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            new CacheHelper(host.getContext()).setCache("herald_afterschoolschool_hot", "");
            return new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                    now, TimelineItem.NO_CONTENT, "热门活动数据加载失败，请手动刷新"
            );
        }
    }


}
