package cn.seu.herald_android.custom;

import android.content.Context;
import android.widget.Button;

public class CustomButton extends Button {
    public CustomButton(Context context) {
        super(context);
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(0);
    }
}
