package cn.seu.herald_android.mode_query;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ModuleHelper;

public class QueryActivity extends AppCompatActivity {
    public static int[] res_queryicons = {
            R.drawable.ic_extension_24dp,
            R.drawable.ic_account_box_24dp,
            R.drawable.ic_info_black_24dp
    };


    private GridLayout gridLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        initView();
    }

    public void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动跑操模块的一个Demo
                ModuleHelper.launchModuleActivity(QueryActivity.this, ModuleHelper.MODULE_PAOCAO, null);
            }
        });

        //获取gridLayout容器
        gridLayout =(GridLayout)findViewById(R.id.query_grid_layout);


        //获取设备的长宽
        WindowManager windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();
        int padding  = width /12;
        int icon_max_w = width / 3;
        int icon_min_w = icon_max_w * 3 /4;

        //加载已安装的查询模块
        for(int i = 0; i < 3; i++ ){
            ImageView imgv = new ImageView(getBaseContext());
            imgv.setBackground(ContextCompat.getDrawable(getBaseContext(), res_queryicons[i]));
            imgv.setPadding(padding, padding, padding, padding);
            imgv.setMaxHeight(icon_max_w);
            imgv.setMaxWidth(icon_max_w);
            imgv.setMinimumWidth(icon_min_w);
            imgv.setMinimumHeight(icon_min_w);
            gridLayout.addView(imgv);
        }
    }
}
