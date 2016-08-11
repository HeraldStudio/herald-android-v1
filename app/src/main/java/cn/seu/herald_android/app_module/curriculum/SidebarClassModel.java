package cn.seu.herald_android.app_module.curriculum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.consts.Cache;

public class SidebarClassModel {

    private String className;

    private String teacher;

    private String week;

    private String credits;

    public SidebarClassModel(JSONObject json) {

        try {
            className = json.getString("course");
        } catch (JSONException e) {
            className = "未知课程";
        }

        try {
            teacher = json.getString("lecturer");
        } catch (JSONException e) {
            teacher = "未知教师";
        }

        try {
            credits = json.getString("credit");
        } catch (JSONException e) {
            credits = "未知";
        }

        try {
            week = json.getString("week");
        } catch (JSONException e) {
            week = "未知";
        }
    }

    public String getClassName() {
        return className;
    }

    public String getDesc() {
        return teacher + " " + week + "周 " + credits + "学分";
    }

    public boolean isAdded() {
        String data = Cache.curriculum.getValue();

        // 读取json内容
        try {
            JSONObject content = new JSONObject(data);

            for (String weekNum : CurriculumScheduleLayout.WEEK_NUMS) {
                JSONArray array = content.getJSONArray(weekNum);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        ClassModel model = new ClassModel(array.getJSONArray(i));
                        if (model.getClassName().equals(className)) {
                            return true;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
