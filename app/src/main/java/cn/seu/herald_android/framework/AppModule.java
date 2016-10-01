package cn.seu.herald_android.framework;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.app_secondary.WebModuleActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.SettingsHelper;

/**
 * AppModule | 应用模块
 *
 * 注意：这里允许伪模块的存在，真的模块作为常量保存在 SettingsHelper 中，
 *      而伪模块可以使用构造函数动态创建，用于临时打开某个界面或转到某个 Web 页等。
 */
public class AppModule {

    // 模块名称，这里用英文，以便作为存储数据等的键值
    public String name;

    // 模块显示名称
    public String nameTip;

    // 模块描述
    public String desc;

    // 模块Action名称，也可以是网址或TABn（n表示要跳转到的Tab index）
    private String mDestination;

    public String getDestination() {
        return mDestination.replaceAll("\\[uuid]", ApiHelper.getCurrentUser().uuid);
    }

    // 模块图标id
    public int icon, invertIcon;

    // 是否有卡片
    public boolean hasCard;

    public boolean needLogin;

    // 构造函数
    public AppModule(String name, String nameTip, String desc,
                     String destination, int icon, int invertIcon, boolean hasCard, boolean needLogin) {
        this.name = name;
        this.nameTip = nameTip;
        this.desc = desc;
        this.mDestination = destination;
        this.icon = icon;
        this.invertIcon = invertIcon;
        this.hasCard = hasCard;
        this.needLogin = needLogin;
    }

    // 创建一个基于 WebView 的页面，注意这里url中必须含有http
    public AppModule(String title, String url) {
        this("", title, "", url, 0, 0, false, false);
    }

    // 卡片是否开启
    public boolean getCardEnabled() {
        String cache = SettingsHelper.get("herald_settings_module_card_enabled_" + name);
        return hasCard && !cache.equals("0");
    }

    public void setCardEnabled(boolean newValue) {
        if (!hasCard) {
            return;
        }
        // flag为true则设置为选中，否则设置为不选中
        SettingsHelper.set("herald_settings_module_card_enabled_" + name, newValue ? "1" : "0");
        SettingsHelper.notifyModuleSettingsChanged();
    }

    // 快捷方式是否开启
    public boolean getShortcutEnabled() {
        String cache = SettingsHelper.get("herald_settings_module_shortcut_enabled_" + name);

        // 默认只开启无卡片的模块
        if (cache.equals("")) {
            return !hasCard;
        }
        return !cache.equals("0");
    }

    public void setShortcutEnabled(boolean newValue) {
        // flag为true则设置为选中，否则设置为不选中
        SettingsHelper.set("herald_settings_module_shortcut_enabled_" + name, newValue ? "1" : "0");
        SettingsHelper.notifyModuleSettingsChanged();
    }

    // 用来标识一个不带卡片的模块数据是否有更新
    public boolean getHasUpdates() {
        String cache = SettingsHelper.get("herald_settings_module_has_updates_" + name);
        return !hasCard && cache.equals("1");
    }

    public void setHasUpdates(boolean newValue) {
        // flag为true则设置为选中，否则设置为不选中
        SettingsHelper.set("herald_settings_module_has_updates_" + name, newValue ? "1" : "0");
        SettingsHelper.notifyModuleSettingsChanged();
    }

    // 打开模块
    public void open() {

        // 空模块不做任何事
        if (getDestination().equals("")) {
            return;
        }

        setHasUpdates(false);

        if (needLogin && !ApiHelper.isLogin()) {
            AppContext.showMessage(nameTip + "功能需要登录使用", "立即登录", AppContext::showLogin);
            return;
        }

        // Web 页面，交给 WebModule 打开
        if (getDestination().startsWith("http")) {
            WebModuleActivity.startWebModuleActivity(nameTip, getDestination());
        } else if (getDestination().startsWith("TAB")) {
            int tab = Integer.valueOf(getDestination().replaceAll("TAB", ""));
            MainActivity.sendChangeMainFragmentBroadcast(tab);
        } else {
            Intent intent = new Intent();
            intent.setAction("cn.seu.herald_android." + getDestination());
            AppContext.startActivitySafely(intent);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AppModule && getDestination().equals(((AppModule) o).getDestination());
    }

    private int mainColor = 0;

    public int getIconMainColor() {
        if (mainColor != 0) {
            return mainColor;
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(AppContext.getCurrentContext().getResources(), icon);

            for (int i = 0; i < bitmap.getWidth(); i++) {
                for (int j = 0; j < bitmap.getHeight(); j++) {
                    int pixel = bitmap.getPixel(i, j);
                    if (Color.alpha(pixel) >= 240) {
                        float hsv[] = new float[3];
                        Color.colorToHSV(pixel, hsv);
                        return mainColor = Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel));
                    }
                }
            }
            return mainColor = Color.GRAY;
        }
    }
}
