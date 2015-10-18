package cn.edu.seu.herald.myseu;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * UI中各种卡片式模块的父类
 * Created by Vhyme on 2015/10/18.
 */
public class CardLayout extends LinearLayout {

    private View contentView = null;
    private String titleText = null;
    private String titleLink = null;
    private Intent titleLinkIntent = null;
    private boolean attached = false;
    private boolean hasTitle = false;

    /*
    * 构造函数
    * */
    public CardLayout(Context context){
        super(context);
        init();
    }
    public CardLayout(Context c, AttributeSet a){
        super(c, a);
        init();
    }

    /*
    * 初始化（在Attach之前）
    * */
    private void init(){
        setOrientation(VERTICAL);

    }

    public void setContentView(View v){
        contentView = v;
    }
    public void setTitleText(String s){
        titleText = s;
    }
    public void setTitleLink(String s){
        titleLink = s;
    }
    public void setTitleLinkIntent(Intent i){

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;


    }
}
