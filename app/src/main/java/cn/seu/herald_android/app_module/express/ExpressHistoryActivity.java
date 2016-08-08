package cn.seu.herald_android.app_module.express;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import static cn.seu.herald_android.helper.LogUtils.makeLogTag;
import okhttp3.Call;

/**
 * Created by corvo on 8/6/16.
 */
public class ExpressHistoryActivity extends BaseActivity{
    private static String TAG = makeLogTag(ExpressHistoryActivity.class);

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
        refreshState();
        refreshUI();
    }

    private void refreshUI() {
        Log.d(TAG, "refreshUI");
        historyAdapter = new ExpressHistoryAdapter(expressInfoList);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void refreshState() {

        String queryState = "http://192.168.1.105:8080/kuaidi/queryState";

        expressInfoList = dbContent.dbQuery();
        for (ExpressInfo info : expressInfoList) {

            if (!info.isFetched()) {
                OkHttpUtils.post()
                        .url(queryState)
                        .addParams("user_phone", info.getUserphone())
                        .addParams("sub_time", String.valueOf(info.getSubmitTime()))
                        .build()
                        .connTimeOut(20000)
                        .readTimeOut(20000)
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject res = new JSONObject(response);
                                    if (res.getInt("code") == 200) {
                                        JSONObject content = res.getJSONObject("content");
                                        boolean isFetch = content.getBoolean("finish");

                                        info.setFetched(isFetch); // 更新此次记录
                                        Log.d(TAG, "更新记录成功");

                                        dbContent.dbRefresh(info.getUserphone(), // 刷新数据库
                                                        info.getSubmitTime(),
                                                        isFetch);

                                        refreshUI();            // 刷新UI
                                    } else {
                                        Log.d(TAG, res.getString("content"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        }
    }
}
