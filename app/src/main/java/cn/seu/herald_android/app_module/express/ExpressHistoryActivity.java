package cn.seu.herald_android.app_module.express;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
public class ExpressHistoryActivity extends BaseActivity {
    private static String TAG = makeLogTag(ExpressHistoryActivity.class);

    String queryState = "http://139.129.4.159/kuaidi/queryState";
    String queryByCard = "http://139.129.4.159/kuaidi/queryByCard";
    String deleteRecord = "http://139.129.4.159/kuaidi/deleteRecord";


    private RecyclerView historyRecyclerView;
    private ExpressHistoryAdapter historyAdapter;
    private List<ExpressInfo> expressInfoList;

    private ExpressDatabaseContent dbContent;

    private ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express__history);
        historyRecyclerView = (RecyclerView) findViewById(R.id.express_view_history);
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
     * 请求与一卡通关联的所有数据, 仅请求一次
     */
    private void getAllData() {
        if (CacheHelper.get("express_sync").equals("")) {
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
        expressInfoList = dbContent.dbQuery();
        historyAdapter = new ExpressHistoryAdapter(expressInfoList, onDelete, this);
        historyRecyclerView.setAdapter(historyAdapter);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    private void refreshState() {

        for (ExpressInfo info : expressInfoList) {
            Log.d(TAG, "refresh state");

            if (!info.isReceived()) {        // 只对没有接单的进行查询
                new ApiSimpleRequest(Method.POST)
                        .url(queryState)
                        .post("user_phone", info.getUserphone())
                        .post("sub_time", String.valueOf(info.getSubmitTime() / 1000))
                        .onResponse(((success, code, response) -> {
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

    private OnDelete onDelete = new OnDelete() {
        @Override
        public void deleteItem(ExpressInfo info) {

            mProgress = ProgressDialog.show(ExpressHistoryActivity.this, "正在提交", "请稍后", true);

            new ApiSimpleRequest(Method.POST)
                .url(deleteRecord)
                .addUuid()
                .post("user_phone", info.getUserphone())
                .post("sub_time", String.valueOf(info.getSubmitTime() / 1000)) // 时间戳转换
                .onResponse(((success, code, response) -> {
                    if (success) {
                        Log.d(TAG, response);
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.getInt("code") == 200) {
                                dbContent.dbDelete(info.getSubmitTime());
                                refreshUI();
                                mProgress.dismiss();
                                showSnackBar("删除记录成功");
                            } else {
                                mProgress.dismiss();
                                showSnackBar(res.getString("content"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mProgress.dismiss();
                            showSnackBar("删除失败");
                        }
                    }
                })).run();
        }
    };

    /**
     * 回调接口, 删除快递记录
     */
    public interface OnDelete {
        public void deleteItem(ExpressInfo info);
    }
}
