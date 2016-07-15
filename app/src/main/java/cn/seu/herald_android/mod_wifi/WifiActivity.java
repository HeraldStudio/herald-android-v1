package cn.seu.herald_android.mod_wifi;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.NetworkLoginHelper;
import cn.seu.herald_android.mod_query.seunet.SeunetActivity;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;
import okhttp3.Call;

public class WifiActivity extends BaseActivity {
    Vibrator vibrator;
    @BindView(R.id.btn_connect_wifi)
    Button btnConnect;
    @BindView(R.id.btn_disconnect_wifi)
    Button btnDisconnect;
    @BindView(R.id.btn_refresh_wifi)
    Button btnRefresh;
    @BindView(R.id.chartwlan)
    PieChartView pieChartView;
    @BindView(R.id.tv_user)
    TextView tv_user;//用户一卡通
    @BindView(R.id.tv_ip)
    TextView tv_ip;//用户ip显示
    @BindView(R.id.tv_index)
    TextView tv_index;//用户已登录设备数
    @BindView(R.id.tv_expire)
    TextView tv_expire;//剩余天数
    @BindView(R.id.layout_info)
    LinearLayout info_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_wifi);
        ButterKnife.bind(this);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        //打开后自动连接
        loginToService();
    }

    @OnClick(R.id.btn_connect_wifi)
    void loginToService() {
        btnConnect.setEnabled(false);
        btnConnect.setText("正在登录中");
        AppContext.showMessage("正在登录校园网");
        //登陆网络服务
        String username = ApiHelper.getWifiUserName();
        String password = ApiHelper.getWifiPassword();
        OkHttpUtils.post().url("http://w.seu.edu.cn/portal/login.php")
                .addParams("username", username)
                .addParams("password", password).build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                vibrator.vibrate(50);
                btnConnect.setEnabled(true);
                btnConnect.setText("连接校园网");
                AppContext.showMessage("似乎信号有点差，不妨换个姿势试试？");
            }

            @Override
            public void onResponse(String response) {
                btnConnect.setEnabled(true);
                btnConnect.setText("连接校园网");
                try {
                    // 登陆成功状态
                    JSONObject info = new JSONObject(response);
                    String[] infoStr = {
                            info.getString("login_username"),
                            info.getString("login_index"),
                            info.getString("login_ip"),
                            NetworkLoginHelper.unicodeToString(info.getString("login_location")),
                            info.getString("login_expire"),
                            info.getString("login_remain"),
                            NetworkLoginHelper.formatTime(info.getString("login_time"))
                    };
                    info_layout.setVisibility(View.VISIBLE);
                    vibrator.vibrate(50);
                    tv_user.setText(String.format("当前用户: %s",infoStr[0]));
                    tv_index.setText(String.format("在线设备数: %s",infoStr[1]));
                    tv_ip.setText(String.format("当前ip: %s",infoStr[2]));
                    tv_expire.setText(String.format("剩余天数: %s",infoStr[4]));
                    AppContext.showMessage("小猴已经成功帮你登陆seu网络啦", "退出登陆",
                            () -> logoutFromService());
                } catch (JSONException e) {
                    info_layout.setVisibility(View.GONE);
                    try {
                        String error = new JSONObject(response).getString("error");
                        vibrator.vibrate(50);
                        AppContext.showMessage("登陆失败，" + error);
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                        vibrator.vibrate(50);
                        AppContext.showMessage("登陆失败，出现未知错误");
                    }
                }
            }
        });
    }

    @OnClick(R.id.btn_disconnect_wifi)
    void logoutFromService() {
        btnDisconnect.setEnabled(false);
        btnDisconnect.setText("正在断开连接");
        OkHttpUtils.post().url("http://w.seu.edu.cn/portal/logout.php").build()
                .connTimeOut(5000).readTimeOut(5000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                btnDisconnect.setEnabled(true);
                btnDisconnect.setText("断开连接");
                vibrator.vibrate(50);
                AppContext.showMessage("校园网退出登录失败，请重试");
            }

            @Override
            public void onResponse(String response) {
                btnDisconnect.setEnabled(true);
                btnDisconnect.setText("断开连接");
                info_layout.setVisibility(View.GONE);
                vibrator.vibrate(50);
                AppContext.showMessage("校园网退出登录成功");
            }
        });
    }

    private void loadCache() {
        //尝试加载缓存
        String cache = CacheHelper.get("herald_nic");
        if (!cache.equals("")) {
            //如果缓存不为空
            try {
                JSONObject json_cache = new JSONObject(cache);
                //设置统计饼状图
                SeunetActivity.setupChart(json_cache.getJSONObject("content"), pieChartView);
                AppContext.showMessage("刷新成功");
            } catch (JSONException e) {
                e.printStackTrace();
                AppContext.showMessage("解析失败，请刷新");
            }
        } else {
            List<SliceValue> values = new ArrayList<>();
            //暂时用一个完整的饼代替默认图
            SliceValue sliceValue = new SliceValue(1f);
            sliceValue.setColor(Color.rgb(220, 220, 220));
            values.add(sliceValue);
            PieChartData pieChartData = new PieChartData(values);
            //为控件设置数据
            pieChartView.setPieChartData(pieChartData);
            refreshCache();
        }
    }

    @OnClick(R.id.btn_refresh_wifi)
    void refreshCache() {
        btnRefresh.setEnabled(false);
        btnRefresh.setText("正在刷新流量使用情况");
        new ApiRequest().api("nic").addUUID()
                .toCache("herald_nic", o -> o)
                .onFinish((success, code, response) -> {
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("刷新流量使用情况");
                    if (success) {
                        loadCache();
                    } else {
                        AppContext.showMessage("刷新失败，请重试");
                    }
                }).run();
    }
}
