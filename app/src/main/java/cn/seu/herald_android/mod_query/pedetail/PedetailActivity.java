package cn.seu.herald_android.mod_query.pedetail;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import okhttp3.Call;

public class PedetailActivity extends BaseAppCompatActivity {

    public static final int[] FORECAST_TIME_PERIOD = {
            6 * 60 + 20, 7 * 60 + 20
    };
    // 左右滑动分页的日历容器
    private ViewPager pager;
    // 跑操次数数字
    private TextView count, monthCount;

    public static void remoteRefreshCache(Context context, Runnable doAfter) {
        ApiHelper apiHelper = new ApiHelper(context);
        CacheHelper cacheHelper = new CacheHelper(context);
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_PC))
                .addParams("uuid", apiHelper.getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        apiHelper.dealApiException(e);
                        doAfter.run();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            long today = CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis();
                            cacheHelper.setCache("herald_pc_date", String.valueOf(today));
                            cacheHelper.setCache("herald_pc_forecast", new JSONObject(response).getString("content"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        OkHttpUtils
                                .post()
                                .url(ApiHelper.getApiUrl(ApiHelper.API_PEDETAIL))
                                .addParams("uuid", apiHelper.getUUID())
                                .build()
                                .readTimeOut(5000).connTimeOut(5000)
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                        apiHelper.dealApiException(e);
                                        doAfter.run();
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONArray array = new JSONObject(response).getJSONArray("content");
                                            cacheHelper.setCache("herald_pedetail", array.toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        doAfter.run();
                                    }
                                });
                    }
                });
    }

    /********************************
     * 初始化
     *********************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedetail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //禁用collapsingToolbarLayout的伸缩标题
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        //沉浸式
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPedetailprimary));

        pager = (ViewPager) findViewById(R.id.calendarPager);

        // 设置下拉刷新控件的进度条颜色
        count = (TextView) findViewById(R.id.tv_fullcount);
        monthCount = (TextView) findViewById(R.id.tv_monthcount);

        // 首先加载一次缓存数据（如未登录则弹出登陆窗口）
        readLocal();

        // 检查是否需要联网刷新，如果需要则刷新，不需要则取消
        if (isRefreshNeeded()) refreshCache();
    }

    /*************************
     * 实现::联网环节::获取学期
     *************************/

    private void refreshCache() {

        // 先显示刷新控件
        showProgressDialog();

        // 读取uuid
        String uuid = getApiHelper().getUUID();
        if (uuid == null) return;


        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_PEDETAIL))
                .addParams("uuid", getApiHelper().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        handleException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray array = new JSONObject(response).getJSONArray("content");
                            getCacheHelper().setCache("herald_pedetail", array.toString());
                            // 下一环节
                            readLocal();
                            // 隐藏刷新控件，为了美观，先延时0.5秒
                            hideProgressDialog();
                        } catch (JSONException e) {
                            handleException(e);
                        }
                    }
                });
    }

    /*************************
     * 实现::联网环节::工具函数
     *************************/

    private boolean isRefreshNeeded() {
        return (pager.getAdapter() == null) || (pager.getAdapter().getCount() == 0);
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
     * 实现::本地读取
     *****************************/

    private void readLocal() {
        try {
            // 读取本地保存的跑操数据
            JSONArray array = new JSONArray(getCacheHelper().getCache("herald_pedetail"));

            // 用户有数据
            // 有效跑操计数器，用于显示每一个跑操是第几次
            int exerciseCount = 0;

            // 创建一个包含所有有效跑操记录的列表（单重列表结构）
            List<ExerciseInfo> infoList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                // 将跑操数据倾倒到列表
                ExerciseInfo info = new ExerciseInfo(obj, exerciseCount + 1);
                if (info.getValid()) {
                    infoList.add(info);
                    exerciseCount++;
                }
            }
            showCount(exerciseCount);

            // 用年月时间戳（年*12+自然月-1）比较器进行排序以防万一
            Collections.sort(infoList, ExerciseInfo.yearMonthComparator);

            // 当前所在月的年月戳
            Calendar cal = Calendar.getInstance();
            int curMonth = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH);

            // 起始月为最早记录所在月
            int startMonth = infoList.size() > 0 ? infoList.get(0).getYearMonth() : curMonth;

            // 终止月为最晚记录所在月
            int endMonth = infoList.size() > 0 ?
                    Math.max(infoList.get(infoList.size() - 1).getYearMonth(), curMonth) : curMonth;

            // 创建一个键值对结构，键为年月戳，值为该月的跑操记录列表
            Map<Integer, List<ExerciseInfo>> pages = new HashMap<>();
            for (int i = startMonth; i <= endMonth; i++) {
                pages.put(i, new ArrayList<>());
            }

            // 将单重列表的每个元素倾倒到双重列表中对应的位置
            for (ExerciseInfo info : infoList) {
                pages.get(info.getYearMonth()).add(info);
            }

            // 删除空白月（当前月除外）
            for (int i = startMonth; i <= endMonth; i++) {
                if (pages.get(i).size() == 0 && infoList.size() > 0)
                    pages.remove(i);
            }

            // 设置水平滚动分页的适配器，负责将双重列表中每一个子列表的数据转换为视图，供水平滚动分页控件调用
            final PagesAdapter adapter = new PagesAdapter(pages, this, this::refreshCache);
            pager.setAdapter(adapter);

            // 根据实际需要，显示时应首先滑动到末页
            pager.setCurrentItem(adapter.getCount() - 1);

            // 初始化当月跑操次数的值
            int monthlyCountNum = adapter.getSubCount(pager.getCurrentItem());
            monthCount.setText(String.valueOf(monthlyCountNum));

            // 水平滚动分页控件的事件监听器
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                // 用户滚动到某页时，更改标题和当月跑操次数的值
                public void onPageSelected(int position) {
                    // 动画切换当月跑操次数数字
                    AlphaAnimation aa1 = new AlphaAnimation(0, 1);
                    aa1.setDuration(250);
                    monthCount.startAnimation(aa1);

                    int monthlyCountNum = adapter.getSubCount(pager.getCurrentItem());
                    monthCount.setText(String.valueOf(monthlyCountNum));
                }

                // 在页面左右滑动过程中临时屏蔽下拉刷新控件
                public void onPageScrollStateChanged(int state) {
                    //srl.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
                }
            });

            if (infoList.size() == 0) {
                showErrorMessage("本学期暂时没有跑操记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*****************************
     * 实现::跑操计数
     *****************************/

    private void showCount(int countNum) {
        count.setText(String.valueOf(countNum));
    }

    /*****************************
     * 实现::错误处理
     *****************************/

    private void handleException(Exception e) {
        runOnUiThread(() -> {
            e.printStackTrace();

            // 显示对应的错误信息，并要求重新登录
            showErrorMessage(e);

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
