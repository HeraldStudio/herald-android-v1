package cn.seu.herald_android.mod_achievement;

public class Achievement {
    public static final int HERALD = 0;
    public static final int EXPERIMENT = 1;
    public static final int PAOCAO = 2;
    //成就类型
    private int type;
    //成就标题
    private String name;
    //成就描述
    private String des;
    //成就达成时间
    private String time;

    public Achievement(int type, String name, String des, String time) {
        this.type = type;
        this.name = name;
        this.des = des;
        this.time = time;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Achievement)) return false;
        Achievement achievement = (Achievement) o;
        return (this.type == achievement.type && this.name.equals(achievement.name));
    }
}
