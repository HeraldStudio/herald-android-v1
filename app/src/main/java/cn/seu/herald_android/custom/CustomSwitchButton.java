package cn.seu.herald_android.custom;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.kyleduo.switchbutton.SwitchButton;

public class CustomSwitchButton extends SwitchButton {
    public CustomSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTintColor(ContextCompat.getColor(context, ContextUtils.getColorPrimary(context)));
    }
}
