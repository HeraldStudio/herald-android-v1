package cn.edu.seu.herald.myseu;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Vhyme on 2015/10/18.
 */
public class CurriculumCardLayout extends CardLayout {

    public CurriculumCardLayout(Context c, AttributeSet a){
        super(c, a);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setVisibility(View.GONE);

    }

}
