package cn.seu.herald_android.mod_local.todo;

import org.json.JSONObject;

public abstract class TodoItem {

    private String name;

    private long createTime;

    public TodoItem(String name) {
        setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract JSONObject toJSON();

    public abstract TodoItem createFromJSON(JSONObject fromJSON);
}
