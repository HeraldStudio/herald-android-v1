package cn.seu.herald_android.mod_query.srtp;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;

public class SrtpActivity extends BaseActivity {

    public static ApiRequest remoteRefreshNotifyDotState() {
        return new ApiRequest().api("srtp").addUUID().post("schoolnum", ApiHelper.getSchoolnum())
                .toCache("herald_srtp",
                        /** notifyModuleIfChanged: */SettingsHelper.Module.srtp);
    }

    @BindView(R.id.recyclerview_srtp)
    RecyclerView recyclerView_srtp;
    @BindView(R.id.tv_totalcredit)
    TextView tv_total_credit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_srtp);
        ButterKnife.bind(this);
        loadCache();
    }

    private void loadCache() {
        String cache = CacheHelper.get("herald_srtp");
        if (!cache.equals("")) {
            try {
                JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
                //获得总学分
                String total = jsonArray.getJSONObject(0).getString("total");
                tv_total_credit.setText(total);
                //加载列表
                ArrayList<SrtpModel> arrayList = SrtpModel.transformJSONArrayToArrayList(jsonArray);
                //适配器
                SrtpAdapter srtpAdapter = new SrtpAdapter(this, arrayList);
                recyclerView_srtp.setLayoutManager(new LinearLayoutManager(this));
                recyclerView_srtp.setAdapter(srtpAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
                showSnackBar("解析失败，请刷新");
            }
        } else {
            refreshCache();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            //点击刷新按钮时进行刷新
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest().api("srtp").addUUID()
                .post("schoolnum", ApiHelper.getSchoolnum())
                .toCache("herald_srtp", o -> {
                    if (o.getJSONArray("content").length() == 1) {
                        showSnackBar("你还没有参加课外研学项目");
                    }
                    return o;
                })
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        loadCache();
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }
}
