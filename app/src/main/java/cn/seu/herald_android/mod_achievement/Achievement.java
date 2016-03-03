package cn.seu.herald_android.mod_achievement;

/**
 * Created by heyon on 2016/3/3.
 */
public class Achievement {
    public static final int HERALD = 0;
    public static final int SHIYAN = 1;
    public static final int PAOCAO = 2;
    //成就类型
    int type;
    //成就标题
    String name;
    //成就描述
    String des;
    //成就达成时间
    String time;
    public Achievement(int type, String name, String des, String time) {
        this.type = type;
        this.name = name;
        this.des = des;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDes() {
        return des;
    }

    public String getTime() {
        return time;
    }
}
