package cn.seu.herald_android.mod_local.todo;

import org.json.JSONObject;

public class SimpleTodoItem extends TodoItem {

    public SimpleTodoItem(String name) {
        super(name);
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public TodoItem createFromJSON(JSONObject fromJSON) {
        return null;
    }
}
