package cn.seu.herald_android.mod_achievement;

import java.util.ArrayList;

import cn.seu.herald_android.mod_query.experiment.ExperimentItem;

/**
 * Created by heyon on 2016/3/10.
 */
public class AchievementFactory {
    public static ArrayList<Achievement> getHeraldAchievement(ArrayList<Object> dataList){
        ArrayList<Achievement> list = new ArrayList<>();
        list.add(new Achievement(Achievement.HERALD,"先与声","参加海螺工作室客户端内测的首批用户的标志。","2016-3-3"));
        return list;
    }

    public static ArrayList<Achievement> getExperimentAchievement(ArrayList<ExperimentItem> dataList){
        ArrayList<Achievement> list = new ArrayList<>();
        int ele_nums = 0;//电学实验数目
        int light_nums = 0;
        for(Object item : dataList){
            ExperimentItem experimentItem = (ExperimentItem)item;
            String name = experimentItem.getName();
            Float grade;
            String time = experimentItem.getDate();
            try{

                grade = Float.parseFloat(experimentItem.getGrade());

            }catch (NumberFormatException e){
                grade = 0.0f;
            }
            //文科实验成就
            if(name.contains("文科")&&grade>=80){
                Achievement achievement = new Achievement(
                        Achievement.EXPERIMENT,
                        "文理兼修",
                        String.format("在实验 %s 中获得80分以上的优良成绩",name ),
                        time);
                if(!list.contains(achievement))list.add(achievement);
            }
            //电学实验数量记录
            if(name.contains("电")){
                ele_nums+=1;
            }
            //电学实验成绩超过85，颁发电学达人
            if(name.contains("电")&&grade>=85){
                Achievement achievement = new Achievement(
                        Achievement.EXPERIMENT,
                        "电学达人",
                       String.format("在电学实验 %s 中获得85以上的成绩",name),
                        time);
                if(!list.contains(achievement))list.add(achievement);
            }

            //电学实验成绩超过90，颁发掌控雷电
            if(name.contains("电")&&grade>=90){
                Achievement achievement = new Achievement(
                        Achievement.EXPERIMENT,
                        "掌控雷电",
                        String.format("在电学实验 %s 中获得90以上的优秀成绩",name),
                        time);
                if(!list.contains(achievement))list.add(achievement);
            }


            //光学实验记录
            if(name.contains("光")||name.equals("迈克尔逊干涉仪")){
                light_nums+=1;
            }

            //光学实验成绩超过85
            if((name.contains("光")||name.equals("迈克尔逊干涉仪"))&&grade>=85){
                Achievement achievement = new Achievement(
                        Achievement.EXPERIMENT,
                        "光！",
                        String.format("在光学实验 %s 中获得85以上的优秀成绩",name),
                        time);
                if(!list.contains(achievement))list.add(achievement);
            }

            //光学迈克尔逊干涉仪实验分数高于85
            if((name.contains("光")||name.equals("迈克尔逊干涉仪"))&&grade>=80){
                Achievement achievement = new Achievement(
                        Achievement.EXPERIMENT,
                        "画个一百个红圈诅咒你！",
                        String.format("在光学实验 %s 中获得80以上的成绩",name),
                        time);
                if(!list.contains(achievement))list.add(achievement);
            }

            //选择了物理女王的实验
            if(experimentItem.getTeacher().equals("周立新")){
                Achievement achievement = new Achievement(
                        Achievement.EXPERIMENT,
                        "挑战物理女王的勇士",
                        "选择了某老师的某项实验",
                        time);
                if(!list.contains(achievement))list.add(achievement);
            }
        }
        if(ele_nums >= 4){
            //选择的电学实验超过4门
            Achievement achievement = new Achievement(
                    Achievement.EXPERIMENT,
                    "就喜欢玩电",
                    "在物理实验中总共做过4门或者以上的电学实验",
                    "");
            if(!list.contains(achievement))list.add(achievement);
        }

        if(light_nums >= 4){
            //选择的光学实验超过4门
            Achievement achievement = new Achievement(
                    Achievement.EXPERIMENT,
                    "追逐光",
                    "在物理实验中总共做过4门或者以上的电学实验",
                    "");
            if(!list.contains(achievement))list.add(achievement);
        }
        return list;
    }
}
