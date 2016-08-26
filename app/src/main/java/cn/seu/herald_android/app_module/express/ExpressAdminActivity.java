package cn.seu.herald_android.app_module.express;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.User;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;

import static cn.seu.herald_android.helper.LogUtils.makeLogTag;

/**
 * Created by corvo on 8/23/16.
 *
 * 此Activity默认不会被普通用户使用, 只提供给快递模块管理员使用, 为了调用方便,
 * 所有数据均不在本地存储, 因此用户一定要保证自己有充足的流量, 每作一次请求,
 * 界面都会重新布置
 */

public class ExpressAdminActivity extends BaseActivity {
    private String TAG = makeLogTag(ExpressAdminActivity.class);

    private String queryAll = "http://139.129.4.159/kuaidi/queryAll";
    private String pageCount = "http://139.129.4.159/kuaidi/queryCount";
    private String modifyState = "http://139.129.4.159/kuaidi/modifyState";
    private Spinner pageSpinner;
    private Button modifyButton;

    private RecyclerView recyclerView;
    private List<ExpressInfo> infoList;

    private List<String> pageList;

    private int maxNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express__admin);

        recyclerView = (RecyclerView) findViewById(R.id.express_admin_view);
        pageSpinner = (Spinner) findViewById(R.id.express_admin_spinner_page);
        modifyButton = (Button) findViewById(R.id.express_admin_button_modify);

        pageSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                infoList.clear();
                getAll(position*5, position*5+5);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getAll(0, 5);

            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject modifyJson = new JSONObject();
                try {
                    JSONArray array = new JSONArray();
                    Log.d(TAG, String.valueOf(infoList));
                    for (ExpressInfo info : infoList) {

                        JSONObject obj = new JSONObject();
                        obj.put("user_phone", info.getUserphone());
                        obj.put("sub_time", String.valueOf(info.getSubmitTime() / 1000));
                        obj.put("finish", info.isFetched()? "1":"0");
                        obj.put("received", info.isReceived()? "1":"0");
                        array.put(obj);
                    }
                    modifyJson.put("content", array);
                    modifyJson.put("user_id", ApiHelper.getCurrentUser().userName);

                    Log.d(TAG, modifyJson.toString());
                    new ApiSimpleRequest(Method.POST)
                            .url(modifyState)
                            .post("state_json", modifyJson.toString())
                            .onResponse(((success, code, response) -> {
                                if (success) {
                                    try {
                                        JSONObject res = new JSONObject(response);
                                        showSnackBar(res.getString("content"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        showSnackBar("操作失败");
                                    }

                                }
                            })).run();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        infoList = new ArrayList<>();
        pageList = new ArrayList<>();
    }

    public void refreshUI() {
       // Log.d(TAG, "refreshUI");
        ExpressAdminAdapter adminAdapter = new ExpressAdminAdapter(infoList, refresh);
        recyclerView.setAdapter(adminAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    public void refreshPage() {

        new ApiSimpleRequest(Method.POST)
                .url(pageCount)
                .onResponse(((success, code, response) -> {
                    if (success) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.getInt("code") == 200) {
                                maxNum = res.getInt("content");
                            }

                            pageList = getPageList(maxNum);
                            pageSpinner.setAdapter(new ArrayAdapter<String>(
                                    this, R.layout.mod_que_express__spin_item, pageList
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                })).run();
    }

    public OnRefresh refresh = new OnRefresh() {
        @Override
        public void makeSubmit(List<ExpressInfo> infoList) {

        }
    };

    /**
     * 生成 1~5, 5~10等数组
     * @param maxNum
     * @return
     */
    public List<String> getPageList(int maxNum) {
        List<String> retList = new ArrayList<>();
        for (int i = 0; i < maxNum; i+=5) {
            String cur = String.valueOf(i) + "~" + String.valueOf(i+5);
            retList.add(cur);
            Log.d(TAG, cur);
        }

        return retList;
    }

    public void getAll(int start, int end) {
       // Log.d(TAG, "getAll");
        new ApiSimpleRequest(Method.POST)
                .url(queryAll)
                .post("start", String.valueOf(start))
                .post("end", String.valueOf(end))
                .onResponse(((success, code, response) -> {
                    if (success) {
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.getInt("code") == 200) {
                                JSONArray array = res.getJSONArray("content");

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject data = array.getJSONObject(i);
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
                                    infoList.add(info);
                                }
                                refreshUI();
                            } else {
                                showSnackBar(res.getString("content"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackBar("查询失败");
                        }
                    }

                })).run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPage();
    }

    public interface OnRefresh {

        /**
         * 回调接路, 提交更改
         * @param infoList
         */
        public void makeSubmit(List<ExpressInfo> infoList);
    }
}
