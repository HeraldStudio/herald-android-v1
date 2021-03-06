package cn.seu.herald_android.factory;

import android.view.LayoutInflater;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.AppModule;

public class ShortcutCard {

    /**
     * 原快捷栏变成了一个卡片
     **/
    public static CardsModel getCard() {
        CardsModel model = new CardsModel(new AppModule("",
                "常用模块", "", "MODULE_MANAGER", R.mipmap.ic_fav, R.mipmap.ic_fav, true, false),
                CardsModel.Priority.CONTENT_NO_NOTIFY, "点我管理常用模块，让小猴更懂你"
        );
        model.attachedView = new ArrayList<>();
        model.attachedView.add(LayoutInflater.from(AppContext.getCurrentContext())
                .inflate(R.layout.app_main__fragment_cards__item_shortcut_box, null));
        return model;
    }
}
