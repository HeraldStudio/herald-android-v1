package cn.seu.herald_android.app_module.cardextra;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiRequest;

public class CardActivity extends BaseActivity {

    public static final String chargeUrl = "http://58.192.115.47:8088/wechat-web/login/initlogin.html";

    @BindView(R.id.recyclerview_card)
    RecyclerView recyclerViewCard;

    @BindView(R.id.tv_extra)
    TextView tv_extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_card);
        ButterKnife.bind(this);

        // 设置布局
        recyclerViewCard.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCard.setHasFixedSize(true);

        String cache = Cache.card.getValue();
        if (!cache.equals("")) {
            loadCache();
        }
        refreshCache();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_card_sync) {
            // 点击刷新按钮时进行刷新
            refreshCache();
        } else if (id == R.id.action_card_chongzhi) {
            new AlertDialog.Builder(this)
                    .setMessage("注意：\\n1、一卡通充值由东南大学学生处官方开发，具有一定的权威性和可靠性；\\n2、充值之后需要在食堂刷卡机上刷卡，充值金额才能到账。")
                    .setPositiveButton("确定", (d, w) -> {
                        new AppModule("一卡通充值", chargeUrl).open();
                    }).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCache() {
        // 尝试加载缓存
        String cache = Cache.card.getValue();
        String todayCache = Cache.cardToday.getValue();
        ArrayList<CardRecordModel> list = new ArrayList<>();

        if (!todayCache.equals("")) {
            JObj json_cache = new JObj(todayCache).$o("content");
            // 获取消费记录
            JArr jsonArray = json_cache.$a("detial");
            // 获取余额并且设置
            String extra = new JObj(todayCache).$o("content").$s("left");
            tv_extra.setText(extra);
            // 数据类型转换
            list.addAll(CardRecordModel.transformJArrToArrayList(jsonArray));
        }

        if (!cache.equals("")) {
            JObj json_cache = new JObj(cache).$o("content");
            // 获取消费记录
            JArr jsonArray = json_cache.$a("detial");
            // 数据类型转换
            list.addAll(CardRecordModel.transformJArrToArrayList(jsonArray));
        }

        CardAdapter cardAdapter = new CardAdapter(getBaseContext(), list);
        // 设置消费记录数据适配器
        recyclerViewCard.setAdapter(cardAdapter);
    }

    private void refreshCache() {
        showProgressDialog();

        // 先加入刷新余额的请求
        ApiRequest request = Cache.cardToday.getRefresher();

        // 如果今天还没刷新过, 加入刷新流水的请求
        if (!todayHasRefreshed()) {
            request = request.parallel(Cache.card.getRefresher());
        }

        // 刷新完毕后登记刷新日期
        request.onFinish((success, code) -> {
            hideProgressDialog();
            loadCache();
            if (success) {
                // showSnackBar("刷新成功");
                Cache.cardDate.setValue(getDayStamp());
            } else {
                showSnackBar("刷新失败，请重试或到充值页面查询");
            }
        }).run();
    }

    public String getDayStamp() {
        return new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    }

    public boolean todayHasRefreshed() {
        return Cache.cardDate.getValue().equals(getDayStamp());
    }
}
