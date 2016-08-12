package cn.seu.herald_android.app_module.express;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import static cn.seu.herald_android.helper.LogUtils.makeLogTag;

import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import okhttp3.Call;

/**
 * Created by corvo on 8/6/16.
 */
public class ExpressHistoryActivity extends BaseActivity{
    private static String TAG = makeLogTag(ExpressHistoryActivity.class);

    String queryState = "http://139.129.4.159/kuaidi/queryState";
    String queryByCard = "http://139.129.4.159/kuaidi/queryByCard";

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllData();
        refreshState();
        refreshUI();
    }

    /**
     * 请求与一卡通关联的所有数据
     */
    private void getAllData() {
        if (!CacheHelper.get("express_sync").equals("1")) {
            Log.d(TAG, "getAllData");
            new ApiSimpleRequest(Method.POST)
                    .url(queryByCard)
                    .post("card_num", ApiHelper.getCurrentUser().userName)
                    .onResponse(((success, code, response) -> {
                        if (success) {
                            try {
                                JSONObject res = new JSONObject(response);
                                JSONArray orders = res.getJSONArray("content");
                                for (int i = 0; i < orders.length(); i++) {
                                    JSONObject data = orders.getJSONObject(i);
                                    ExpressInfo info = new ExpressInfo(
                                            data.getString("user"),
                                            data.getString("phone"),
                                            data.getString("sms"),
                                            data.getString("dest"),
                                            data.getString("arrival"),
                                            data.getString("locate"),
                                            data.getString("weight"),
                                            data.getLong("sub_time") * 1000,
                                            data.getBoolean("finish"),
                                            data.getBoolean("receiving"));

                                    dbContent.dbInsert(info);
                                }
                                CacheHelper.set("express_sync", "1");
                                refreshUI();
                            } catch (JSONException e) {
                                showSnackBar("出现错误");
                                e.printStackTrace();
                            }
                        } else {
                            showSnackBar("初始化数据失败");
                        }
                    })).run();
        }
    }

    private void refreshUI() {
        Log.d(TAG, "refreshUI");
        historyAdapter = new ExpressHistoryAdapter(expressInfoList);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void refreshState() {

        expressInfoList = dbContent.dbQuery();
        for (ExpressInfo info : expressInfoList) {

            if (!info.isFetched()) {        // 只对没有结束的快递查询
                new ApiSimpleRequest(Method.POST)
                        .url(queryState)
                        .post("user_phone", info.getUserphone())
                        .post("sub_time", String.valueOf(info.getSubmitTime() / 1000))
                        .onResponse(((success, code, response) ->{
                            if (success) {
                                try {
                                    JSONObject res = new JSONObject(response);
                                    if (res.getInt("code") == 200) {
                                        JSONObject content = res.getJSONObject("content");
                                        boolean isFetched = content.getBoolean("finish");
                                        boolean isReceived = content.getBoolean("receiving");

                                        info.setFetched(isFetched); // 更新此次记录
                                        info.setReceived(isReceived);
                                        Log.d(TAG, "更新记录成功");

                                        dbContent.dbRefresh(info.getUserphone(), // 刷新数据库
                                                info.getSubmitTime(),
                                                isFetched, isReceived);

                                        refreshUI();            // 刷新UI
                                    } else {
                                        Log.d(TAG, res.getString("content"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showSnackBar("获取信息出现问题");
                            }
                        })).run();
            }
        }
    }
}
