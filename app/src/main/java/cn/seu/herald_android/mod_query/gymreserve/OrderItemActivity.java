package cn.seu.herald_android.mod_query.gymreserve;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class OrderItemActivity extends BaseAppCompatActivity {

    public static void startOrderItemActivity(Activity activity,GymReserveItem item, String[] timeItems){
        Intent intent = new Intent(activity,OrderItemActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gymItem",item);
        bundle.putStringArray("timeItem",timeItems);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    GymReserveItem gymItem;
    String[] timeItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_item);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {

        }
        return super.onOptionsItemSelected(item);
    }

    public void init(){
        Bundle bundle = getIntent().getExtras();
        gymItem = (GymReserveItem) bundle.getSerializable("gymItem");
        timeItems = bundle.getStringArray("timeItems");

        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorGymReserveprimary));
        enableSwipeBack();

        //设置标题
        setTitle(gymItem.name);

    }


}
