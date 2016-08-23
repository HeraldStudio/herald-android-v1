package cn.seu.herald_android.app_module.curriculum;

import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class SidebarClassModel {

    private String className;

    private String teacher;

    private String week;

    private String credits;

    public SidebarClassModel(JObj json) {

        className = json.$s("course");
        if (className.equals("")) {
            className = "未知课程";
        }

        teacher = json.$s("lecturer");
        if (teacher.equals("")) {
            teacher = "未知教师";
        }

        credits = json.$s("credit");
        if (credits.equals("")) {
            credits = "未知";
        }

        week = json.$s("week");
        if (week.equals("")) {
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
        JObj content = new JObj(data);

        for (String weekNum : CurriculumScheduleLayout.WEEK_NUMS) {
            JArr array = content.$a(weekNum);
            for (int i = 0; i < array.size(); i++) {
                try {
                    ClassModel model = new ClassModel(array.$a(i));
                    if (model.getClassName().equals(className)) {
                        return true;
                    }
                } catch (Exception e) {
                }
            }
        }

        return false;
    }
}
