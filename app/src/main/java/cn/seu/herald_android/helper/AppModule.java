package cn.seu.herald_android.helper;

import android.content.Intent;

import cn.seu.herald_android.app_framework.$;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.mod_webmodule.WebModuleActivity;

/**
 * AppModule | 应用模块
 *
 * 注意：这里允许伪模块的存在，真的模块作为常量保存在 SettingsHelper 中，
 *      而伪模块可以使用构造函数动态创建，用于临时打开某个界面或转到某个 Web 页等。
 */
public class AppModule {

    /// 模块 ID，如果是真模块，注意要与 Module 枚举类中的顺序一致；伪模块用 -1 即可
    public int id;

    /// 模块名称，这里用英文，以便作为存储数据等的键值
    public String name;

    /// 模块显示名称
    public String nameTip;

    /// 模块描述
    public String desc;

    /// 模块Action名称，也可以是网址或TABn（n表示要跳转到的Tab index）
    public String controller;

    /// 模块图标id
    public int icon;

    public boolean hasCard;

    /// 构造函数
    public AppModule(int id, String name, String nameTip, String desc,
                     String controller, int icon, boolean hasCard) {
        this.id = id;
        this.name = name;
        this.nameTip = nameTip;
        this.desc = desc;
        this.controller = controller;
        this.icon = icon;
        this.hasCard = hasCard;
    }

    /// 创建一个基于webview的页面，注意这里url中必须含有http
    public AppModule(String title, String url) {
        this(-1, "", title, "", url, 0, false);
    }

    /// 卡片是否开启
    public $<Boolean> cardEnabled = new $<>(() -> {
        String cache = SettingsHelper.get("herald_settings_module_cardenabled_" + name);
        return hasCard && !cache.equals("0");
    }, value -> {
        if (!hasCard) {
            return;
        }
        // flag为true则设置为选中，否则设置为不选中
        SettingsHelper.set("herald_settings_module_cardenabled_" + name, value ? "1" : "0");
        SettingsHelper.notifyModuleSettingsChanged();
    });

    /// 快捷方式是否开启
    public $<Boolean> shortcutEnabled = new $<>(() -> {
        String cache = SettingsHelper.get("herald_settings_module_shortcutenabled_" + name);

        // 默认只开启无卡片的模块
        if (cache.equals("")) {
            return !hasCard;
        }
        return !cache.equals("0");
    }, value -> {
        // flag为true则设置为选中，否则设置为不选中
        SettingsHelper.set("herald_settings_module_shortcutenabled_" + name, value ? "1" : "0");
        SettingsHelper.notifyModuleSettingsChanged();
    });

    /// 用来标识一个不带卡片的模块数据是否有更新
    public $<Boolean> hasUpdates = new $<>(() -> {
        String cache = SettingsHelper.get("herald_settings_module_hasupdates_" + name);
        return !hasCard && cache.equals("1");
    }, value -> {
        // flag为true则设置为选中，否则设置为不选中
        SettingsHelper.set("herald_settings_module_hasupdates_" + name, value ? "1" : "0");
        SettingsHelper.notifyModuleSettingsChanged();
    });

    /// 打开模块
    public void open() {
        // 空模块不做任何事
        if (controller.equals("")) { return; }

        // Web 页面，交给 WebModule 打开
        if (controller.startsWith("http")) {
            WebModuleActivity.startWebModuleActivity(AppContext.currentContext.$get(), nameTip, controller);
        } else if (controller.startsWith("TAB")) {
            int tab = Integer.valueOf(controller.replaceAll("TAB", ""));
            MainActivity.sendChangeMainFragmentBroadcast(AppContext.currentContext.$get(), tab);
        } else {
            Intent intent = new Intent();
            intent.setAction("cn.seu.herald_android." + controller);
            AppContext.currentContext.$get().startActivity(intent);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AppModule && controller.equals(((AppModule) o).controller);
    }
}
