package cn.seu.herald_android.app_framework;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class UserCache {

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public UserCache(String namespace) {
        preferences = AppContext.currentContext.$get().getSharedPreferences(namespace, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public String get(String key) {
        return preferences.getString(key, "");
    }

    public void set(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public $$<String> forKey(String key) {
        return new $$<>(
                () -> UserCache.this.get(key),
                (value) -> UserCache.this.set(key, value)
        );
    }

    public $$<Boolean> booleanForKey(String key, Boolean defaultValue) {
        return new $$<>(/** $get = */() -> {
            if (defaultValue) {
                return !(UserCache.this.get(key).equals("0"));
            } else {
                return UserCache.this.get(key).equals("1");
            }
        }, /** $set = */(value) -> {
            UserCache.this.set(key, value ? "1" : "0");
        });
    }
}
