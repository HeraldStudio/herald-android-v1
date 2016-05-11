package cn.seu.herald_android.mod_afterschool;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.custom.CalendarUtils;

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
    private String detail_url;
    private String pic_url;
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


}
