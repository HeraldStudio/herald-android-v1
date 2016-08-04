package cn.seu.herald_android.custom;

import android.content.Context;
import android.util.AttributeSet;

import com.kyleduo.switchbutton.SwitchButton;

import cn.seu.herald_android.framework.AppContext;

public class CustomSwitchButton extends SwitchButton {
    public CustomSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTintColor(AppContext.getColorPrimary(context));
    }
}
