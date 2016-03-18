package cn.seu.herald_android.mod_modulemanager;

import cn.seu.herald_android.helper.SettingsHelper;

/**
 * 这个类的对象代表客户端中各个子模块
 * Created by heyon on 2016/3/8.
 */
public class SeuModule {
    //模块ID
    private int moduleId;
    //图片资源
    private int ic_id;
    //是否被选为快捷方式
    private boolean enabledShortCut;
    //是否提供并显示卡片
    private boolean enabledCard;
    //启动的activity的action
    private String actions;
    //名字
    private String name;
    //描述
    private String description;

    public SeuModule(int moduleId, boolean enabledShortCut, boolean enabledCard, String actions) {
        this.moduleId = moduleId;
        this.enabledShortCut = enabledShortCut;
        this.actions = actions;
        this.ic_id = SettingsHelper.moduleIconsId[moduleId];
        this.name = SettingsHelper.moduleNamesTips[moduleId];
        this.description = SettingsHelper.moduleDescriptions[moduleId];
        this.enabledCard = SettingsHelper.moduleHasCard[moduleId] && enabledCard;
    }

    public int getModuleId() {
        return moduleId;
    }

    public boolean isEnabledShortCut() {
        return enabledShortCut;
    }

    public boolean isEnabledCard() {
        return enabledCard;
    }

    public String getActions() {
        return actions;
    }

    public int getIc_id() {
        return ic_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
