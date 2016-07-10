package cn.seu.herald_android.mod_achievement;

public class Achievement {
    //成就标题
    private String name;
    //成就描述
    private String des;
    //成就达成时间
    private String time;

    public Achievement(String name, String des, String time) {
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
        return (this.name.equals(achievement.name));
    }
}
