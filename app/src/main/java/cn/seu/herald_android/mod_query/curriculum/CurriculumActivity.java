package cn.seu.herald_android.mod_query.curriculum;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

/******************************************************************************
 * CurriculumActivity | 主程序
 * 实现课表查询模块主要功能
 ******************************************************************************/

public class CurriculumActivity extends BaseAppCompatActivity {

    // 水平分页控件
    private ViewPager pager;

    /********************************
     * 初始化
     *********************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorCurriculumPrimary));
        enableSwipeBack();

        pager = (ViewPager) findViewById(R.id.pager);

        //异步加载背景图
        final ImageView iv = (ImageView) findViewById(R.id.curriculum_bg);
        runMeasurementDependentTask(() -> {
            Picasso.with(this)
                    .load(R.drawable.curriculum_bg)
                    .resize(iv.getWidth(), iv.getHeight())
                    .centerCrop().into(iv);
        });

        readLocal();
    }

    /*************************
     * 实现::联网环节::获取课表
     *************************/

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest(this).api(ApiHelper.API_SIDEBAR).uuid()
                .toCache("herald_sidebar", o -> o.getJSONArray("content"))
                .onFinish((success, response) -> {
                    if (success) {
                        refreshCacheStep2();
                    } else {
                        hideProgressDialog();
                    }
                }).run();
    }

    private void refreshCacheStep2() {
        new ApiRequest(this).api(ApiHelper.API_CURRICULUM).uuid()
                .toCache("herald_curriculum", o -> o.getJSONObject("content"))
                .onFinish((success, response) -> {
                    hideProgressDialog();
                    if (success) readLocal();
                }).run();
    }

    public static ApiRequest[] remoteRefreshCache(Context context) {
        return new ApiRequest[]{
                new ApiRequest(context).api(ApiHelper.API_SIDEBAR).uuid()
                        .toCache("herald_sidebar", o -> o.getJSONArray("content")),

                new ApiRequest(context).api(ApiHelper.API_CURRICULUM).uuid()
                        .toCache("herald_curriculum", o -> o.getJSONObject("content"))
        };
    }

    /*****************************
     * 实现::本地读取
     *****************************/

    private void readLocal() {
        String data = getCacheHelper().getCache("herald_curriculum");
        String sidebar = getCacheHelper().getCache("herald_sidebar");
        if (data.equals("")) {
            refreshCache();
            return;
        }

        PagesAdapter adapter = new PagesAdapter(this, data, sidebar);
        pager.setAdapter(adapter);
        pager.setCurrentItem(adapter.getCurrentPage());
        setTitle("第" + (adapter.getCurrentPage() + 1) + "周");

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int p1, float p2, int p3) {
            }

            @Override
            public void onPageSelected(int position) {
                setTitle("第" + (position + 1) + "周");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    /*****************************
     * 实现::错误处理
     *****************************/

    private void handleException(Exception e) {
        runOnUiThread(() -> {
            e.printStackTrace();

            // 显示对应的错误信息，并要求重新登录
            showErrorMessage(e);

            // 隐藏刷新控件，为了美观，先延时0.5秒
            hideProgressDialog();
        });
    }

    // 根据Exception的类型，显示一个错误信息。将根据课表显示状态自动选择SnackBar或对话框形式
    private void showErrorMessage(Exception e) {
        String message;
        if (e instanceof NumberFormatException || e instanceof JSONException) {
            message = "暂时无法获取数据，请重试";
        } else if (e instanceof ConnectException || e instanceof SocketException) {
            message = "暂时无法连接网络，请重试";
        } else if (e instanceof SocketTimeoutException) {
            // 服务器端出错
            message = "学校网络设施出现故障，暂时无法刷新";
        } else {
            message = "出现未知错误，请重试";
        }
        showErrorMessage(message);
    }

    // 显示一个错误信息。将根据课表显示状态自动选择SnackBar或对话框形式
    private void showErrorMessage(String message) {
        showSnackBar(message);
        hideProgressDialog();
    }
}
