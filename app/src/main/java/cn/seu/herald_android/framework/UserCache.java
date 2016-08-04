package cn.seu.herald_android.framework;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class UserCache {

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public UserCache(String namespace) {
        preferences = AppContext.instance.getSharedPreferences(namespace, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public String get(String key) {
        return preferences.getString(key, "");
    }

    public void set(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }
}
