package cn.seu.herald_android.app_module.express;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;

/**
 * Created by corvo on 8/6/16.
 */
public class ExpressHistoryActivity extends BaseActivity{


    private RecyclerView historyRecyclerView;
    private ExpressHistoryAdapter historyAdapter;
    private List<ExpressInfo> expressInfoList;

    private ExpressDatabaseContent dbContent;

    @Override
    public void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express__history);
        historyRecyclerView = (RecyclerView) findViewById(R.id.express_view_history);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        expressInfoList = new ArrayList<>();
        dbContent = new ExpressDatabaseContent(this);

        /*
        ExpressInfo info = new ExpressInfo();
        info.setSmsInfo("这是一条测试短信, 可以看得到吗?");
        info.setArrival("中午12:20~12:40");
        info.setDest("梅九");
        info.setLocate("东门");
        info.setUsername("No One");
        info.setUserphone("15189800598");
        info.setWeight("小于4斤");
        info.setFetched(true);
        info.setSubmitTime(Long.valueOf(1234445));
        expressInfoList.add(info);

        info = new ExpressInfo();
        info.setSmsInfo("这是第二条测试短信, 可以看得到吗?");
        info.setArrival("中午12:20~12:40");
        info.setDest("梅九");
        info.setLocate("东门");
        info.setUsername("No One");
        info.setUserphone("15189800598");
        info.setWeight("小于4斤");
        info.setFetched(false);
        info.setSubmitTime(Long.valueOf(1234445));
        expressInfoList.add(info);
        */

        expressInfoList = dbContent.dbQuery();

        historyAdapter = new ExpressHistoryAdapter(expressInfoList);
        historyRecyclerView.setAdapter(historyAdapter);

    }
}
