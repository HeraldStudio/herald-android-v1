package cn.seu.herald_android.app_main;

import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;

import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.helper.CacheHelper;

/**
 * CardsModel | 首页卡片模型
 * 首页卡片列表每一个分区（代表一个卡片）的模型，这里只需要用来存储每一行的数据、
 * 用来排序、以及用来存储点击卡片时打开的页面
 */
public class CardsModel {

    // 表示卡片消息是否重要，不重要的消息总在后面
    public enum Priority {
        CONTENT_NOTIFY,
        CONTENT_NO_NOTIFY,
        NO_CONTENT
    }

    public ArrayList<View> attachedView = new ArrayList<>();
    private String name;
    private String info;
    private Priority contentPriority;
    private View.OnClickListener onClickListener = null;
    private int iconRes;

    public CardsModel(String name, String info, Priority contentPriority, int iconRes) {
        // 非模块部分所调用的timelineItem构造
        this.name = name;
        this.info = info;
        this.contentPriority = contentPriority;
        this.iconRes = iconRes;
    }

    public CardsModel(AppModule module, Priority contentPriority, String info) {
        // 各个模块部分所调用的timelineItem构造
        this.name = module.nameTip;
        this.info = info;
        this.contentPriority = contentPriority;
        this.onClickListener = (v) -> module.open();
        this.iconRes = module.icon;
    }


    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public int getIconRes() {
        return iconRes;
    }

    @Nullable
    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    @Override
    public String toString() {
        String ret = name + "|" + info + "|";
        for (View k : attachedView) {
            ret += k.toString();
        }
        return ret;
    }

    public boolean isRead() {
        String code = String.valueOf(toString().hashCode());

        // 这里用name的哈希值做键, 而不用moduleId, 防止多个moduleId=-1的模块共用一个存储造成冲突
        String key = "herald_cards_read_" + name.hashCode();
        return CacheHelper.get(key).equals(code);
    }

    public Priority getDisplayPriority() {
        if (contentPriority == Priority.CONTENT_NOTIFY && isRead()) {
            return Priority.CONTENT_NO_NOTIFY;
        }
        return contentPriority;
    }

    public void markAsRead() {
        String code = String.valueOf(toString().hashCode());

        // 这里用name的哈希值做键, 而不用moduleId, 防止多个moduleId=-1的模块共用一个存储造成冲突
        String key = "herald_cards_read_" + name.hashCode();
        CacheHelper.set(key, code);
    }
}