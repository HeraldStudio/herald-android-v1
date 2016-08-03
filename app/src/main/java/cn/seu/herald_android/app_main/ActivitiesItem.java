package cn.seu.herald_android.app_main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;

public class ActivitiesItem {
    String title;
    String desc;
    String startTime;
    String endTime;
    String activityTime;
    String assoc;
    String location;
    String detailUrl;
    String picUrl;
    // 标识活动是否已开始等信息
    String tag;
    // 标识活动开始信息的颜色
    int tagColorID;

    public ActivitiesItem(String title, String desc, String startTime, String endTime, String activityTime, String detailUrl, String assoc, String location, String picUrl) {
        this.title = title;
        this.desc = desc;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityTime = activityTime;
        this.detailUrl = detailUrl;
        this.assoc = assoc;
        this.location = location;
        this.picUrl = picUrl;

        // 判断活动是否开始
        long now_time = CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis();
        long start_time_value = getStartCalendar().getTimeInMillis();
        long end_time_value = getEndCalendar().getTimeInMillis();
        if (now_time < start_time_value){
            tag = "即将开始";
            tagColorID =  R.color.colorSecondaryText;
        } else if (now_time > end_time_value) {
            tag = "已结束";
            tagColorID =  R.color.colorSecondaryText;
        } else {
            tag = "进行中";
            tagColorID =  R.color.relaxGreen;
        }
    }

    public Calendar getStartCalendar(){
        Calendar dst = Calendar.getInstance();
        dst.set(
                Integer.parseInt(startTime.split("-")[0]),
                Integer.parseInt(startTime.split("-")[1])-1,
                Integer.parseInt(startTime.split("-")[2])
                );
        return CalendarUtils.toSharpDay(dst);
    }



    public Calendar getEndCalendar(){
        Calendar dst = Calendar.getInstance();
        dst.set(
                Integer.parseInt(endTime.split("-")[0]),
                Integer.parseInt(endTime.split("-")[1])-1,
                Integer.parseInt(endTime.split("-")[2])
        );
        return CalendarUtils.toSharpDay(dst);
    }

    public String getDetailUrl(){
        return detailUrl;
    }

    public String getPicUrl(){
        return picUrl;
    }

    public String getTag(){
        return tag;
    }

    public int getTagColorId(){
        return tagColorID;
    }

    public static ArrayList<ActivitiesItem> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<ActivitiesItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            list.add(new ActivitiesItem(
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


}