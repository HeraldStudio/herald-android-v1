package cn.seu.herald_android.mod_query.curriculum;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

/******************************************************************************
 * CurriculumActivity | 主程序
 * 实现课表查询模块主要功能
 ******************************************************************************/

public class CurriculumActivity extends BaseAppCompatActivity {
//
//    // 水平分页控件
//    protected ViewPager pager;
//
//    /********************************
//     * 初始化
//     *********************************/
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_curriculum);
//
//        pager = (ViewPager) findViewById(R.id.pager);
//
//        /************************ 初始化::学期选择 ************************/
//        title = (TextView) findViewById(R.id.action_bar_title);
//        flowLayout = (TagFlowLayout) findViewById(R.id.id_flowlayout);
//        table = (ViewGroup) findViewById(R.id.table);
//
//        // 标题的点击事件，切换学期选择器展开/折叠
//        title.setOnClickListener((v) -> {
//            if (table.getY() == 0) showTags();
//            else hideTags();
//        });
//
//        // 学期选择器绑定到适配器
//        flowLayout.setAdapter(tagAdapter);
//
//        // 学期选择器项目的点击事件，跳转到对应学期
//        flowLayout.setOnTagClickListener((view, position, parent) -> {
//            String tag = ((TextView) view).getText().toString();
//            pager.setCurrentItem(((PagesAdapter) pager.getAdapter()).getPageForTermName(tag));
//            hideTags();
//            return false;
//        });
//
//        // 初始化表格上的蒙版
//        mask = new View(this);
//        mask.setBackgroundColor(ContextCompat.getColor(this, R.color.curriculumcolorMask));
//        mask.setOnClickListener((v) -> hideTags());
//        table.addView(mask);
//        mask.setVisibility(View.GONE);
//
//        /************************ 初始化::课表载入 ************************/
//
//        // 首先加载一次缓存数据（如未登录则弹出登陆窗口）
//        readLocal(false);
//
//        // 检查是否需要联网刷新，如果需要则刷新，不需要则取消
//        if (isRefreshNeeded()) loadData();
//    }
//
//    /*************************
//     * 实现::联网环节::获取学期
//     *************************/
//
//    public void loadData() {
//
//        // 先显示刷新控件
//        getProgressDialog().show();
//
//        // 读取uuid
//        String uuid = getUuid();
//        if (uuid == null) return;
//
//        OkHttpUtils
//                .post()
//                .url(ApiHelper.getApiUrl(ApiHelper.API_TERM))
//                .addParams("uuid",getApiHepler().getUUID())
//                .build()
//                .execute(new StringCallback() {
//                    @Override
//                    public void onError(Call call, Exception e) {
//                        handleException(e);
//                    }
//
//                    @Override
//                    public void onResponse(String response) {
//                        try{
//                            JSONArray array = new JSONObject(response).getJSONArray("content");
//
//                            runOnUiThread(() -> {
//                                getCacheHelper().setCache("herald_terms", array.toString());
//
//                                // 下一环节
//                                loadPages();
//
//                                // 隐藏刷新控件，为了美观，先延时0.5秒
//                                getProgressDialog().hide();
//                            });
//                        }catch (JSONException e){
//                            handleException(e);
//                        }
//
//                    }
//                });
//    }
//
//    /*************************
//     * 实现::联网环节::获取课表
//     *************************/
//
//    private void loadPages() {
//
//        // 先显示刷新控件
//        getProgressDialog().show();
//
//        // 读取uuid
//        String uuid = getUuid();
//        if (uuid == null) return;
//
//        try {
//            // 读取上一环节获取的学期列表
//            JSONArray array = new JSONArray(getCacheHelper().getCache("herald_terms"));
//
//            // 多线程同时执行任务
//            ApiClient client = new ApiClient();
//
//            // 获取当前学期的课程侧栏数据
//            client.add(
//                    new ApiRequest(ApiRequest.METHOD_SIDEBAR)
//                            .post("uuid", uuid)
//                            .onResponse(response -> {
//                                JSONObject object = new JSONObject(response);
//                                JSONArray array1 = object.getJSONArray("content");
//
//                                // 保存侧栏数据
//                                editor.putString("sidebar", array1.toString());
//                                editor.apply();
//                            })
//                            .onError(this::handleException)
//            );
//
//            // 获取该学期的课表数据
//            Calendar calendar = Calendar.getInstance();
//            String date = calendar.get(Calendar.YEAR) + "-"
//                    + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
//            for (int i = 0; i < array.length(); i++) {
//
//                // 把下面这些线程要使用的变量final化
//                final String termNum = array.getString(i);
//                final boolean curTerm = i == 0;
//
//                client.add(
//                        new ApiRequest(ApiRequest.METHOD_CURRICULUM)
//                                .post("uuid", uuid)
//                                .post("term", termNum)
//                                .post("date", date)
//                                .onResponse(response -> {
//                                    JSONObject object = new JSONObject(response);
//                                    JSONObject object1 = object.getJSONObject("content");
//
//                                    if (curTerm) {
//                                        // 保存该学期的开始日期
//                                        JSONObject object2 = object.getJSONObject("startdate");
//                                        editor.putInt("startMonth", object2.getInt("month"));
//                                        editor.putInt("startDay", object2.getInt("day"));
//                                    }
//
//                                    // 以term+学期名格式命名，保存该学期的课表数据
//                                    editor.putString("term" + termNum, object1.toString());
//                                    editor.apply();
//                                })
//                                .onError(this::handleException)
//                );
//            }
//
//            // 所有请求全部结束
//            client.onFinish(() -> runOnHomeThread(() -> {
//
//                // 保存当前的系统年份和周数，供下次启动时查阅
//                editor.putString("lastStart", getWeekStamp());
//                editor.apply();
//
//                // 下一环节
//                readLocal(false);
//
//                // 隐藏刷新控件，为了美观，先延时0.5秒
//                new Handler().postDelayed(() -> srl.setRefreshing(false), 500);
//            })).run();
//
//        } catch (JSONException e) {
//            handleException(e);
//        }
//    }
//
//    /*************************
//     * 实现::联网环节::工具函数
//     *************************/
//
//    private String getWeekStamp() {
//        Calendar calendar = Calendar.getInstance();
//        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR);
//    }
//
//    private boolean isRefreshNeeded() {
//        return !(sp.getString("lastStart", "").equals(getWeekStamp()))
//                || (pager.getAdapter() == null) || (pager.getAdapter().getCount() == 0);
//    }
//
//    private String getUuid() {
//        // 读取保存的uuid，若不存在，视为首次使用
//        // 这里再写一遍是为了防止本函数在其他某些场合调用时，出现uuid不存在的情况而导致闪退
//        String uuid = sp.getString("uuid", null);
//        if (uuid == null) {
//            showSettingsDialog("登录", null, false);
//        }
//        return uuid;
//    }
//
//    /*****************************
//     * 实现::本地读取
//     *****************************/
//
//    private void readLocal(boolean savePage) {
//
//        // 保存当前页码
//        int curPage = 0;
//        if (savePage) {
//            curPage = pager.getCurrentItem();
//        }
//
//        // 用于保存各学期数据的列表
//        ArrayList<TermInfo> infos = new ArrayList<>();
//        boolean error = false;
//
//        try {
//            // 若学期列表为空，退出
//            if (sp.getString("terms", "").equals("")) return;
//
//            // 读取本地保存的学期列表
//            JSONArray array = new JSONArray(sp.getString("terms", ""));
//
//            // 若没有合法的学期数据，退出
//            if (array.length() == 0) {
//                showErrorMessage("查询到的学期列表为空，莫非被飞贼调包了？");
//                return;
//            }
//
//            // 读取各个学期
//            for (int i = 0; i < array.length(); i++) {
//                TermInfo info;
//                // 若学期名不合法，跳过
//                if ((info = TermInfo.createFromString(array.getString(i))) == null) {
//                    continue;
//                }
//                // 若学期名合法，读取本地保存的该学期数据并存入info
//                info.data = sp.getString("term" + info.toString(), null);
//                if (info.data == null) {
//                    error = true;
//                    continue;
//                }
//
//                // 若学期数据合法（如果不是当前学期，该学期课表也必须非空），将该学期info填入列表
//                boolean empty = true;
//                JSONObject obj = new JSONObject(info.data);
//                for (String weekNum : CurriculumScheduleLayout.WEEK_NUMS) {
//                    if (obj.getJSONArray(weekNum).length() != 0)
//                        empty = false;
//                }
//                if (info.data != null && (!empty || i == 0)) {
//                    infos.add(info);
//                }
//            }
//
//            // 将获取到的首个学期设为当前学期，并传递侧栏信息
//            infos.get(0).currentTerm = true;
//            infos.get(0).sidebar = new JSONArray(sp.getString("sidebar", "[]"));
//
//            // 将学期按时间顺序排序
//            Collections.sort(infos, TermInfo.comparator);
//            tagList.clear();
//            for (TermInfo info : infos) {
//                tagList.add(info.toString());
//            }
//
//            tagAdapter.notifyDataChanged();
//
//        } catch (JSONException e) {
//            handleException(e);
//        }
//
//        // 读取本地保存的当前周数
//        int thisWeek;
//        try {
//            thisWeek = Integer.valueOf(sp.getString("curWeek", "0"));
//        } catch (NumberFormatException e) {
//            thisWeek = 0;
//        }
//
//        try {
//            // 将已填好并排序的列表传给adapter做进一步处理
//            // 列表中currentTerm为true的学期为当前学期；thisWeek为当前周数
//            PagesAdapter adapter = new PagesAdapter(this, infos, thisWeek);
//
//            // 水平滚动分页控件的事件监听器
//            // noinspection deprecation
//            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                }
//
//                // 当翻到某一页时，从adapter获取并设置对应的页面标题
//                public void onPageSelected(int position) {
//                    title.setText(adapter.getTitleForPage(position));
//                }
//
//                // 在页面左右滑动过程中临时屏蔽下拉刷新控件
//                public void onPageScrollStateChanged(int state) {
//                    srl.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
//                }
//            });
//
//            // 等adapter准备完毕、界面尺寸度量就绪后，将adapter挂载到分页视图并翻到当前页
//            // 这一步必须在上一步设置事件监听器之后进行，因为runMeasurementDependentTask这个方法
//            // 在activity已经准备好的情况下会立即执行任务，而不是延后执行。如果这时没有设置好上面的监听器，
//            // 用户首次启动并登录完成后，标题文字将不会发生改变
//            final boolean isError = error;
//            pager.setAdapter(adapter);
//
//            runMeasurementDependentTask(() -> {
//                if (!savePage) {
//                    pager.setCurrentItem(adapter.getDefaultPage(), true);
//                }
//
//                if (isError) {
//                    showErrorMessage("某些学期的课表数据加载失败，请刷新");
//                } else if (adapter.getState() == PagesAdapter.State.HOLIDAY) {
//                    showSnackBar(srl, "现在是假期时间，陛下先提前熟悉一下新课表吧");
//                } else if (adapter.getState() == PagesAdapter.State.HOLIDAY_LAST_TERM) {
//                    showSnackBar(srl, "本学期已经结束，陛下先看看其他学期吧");
//                } else if (adapter.getState() == PagesAdapter.State.EMPTY_TERM) {
//                    showSnackBar(srl, "本学期没有要显示的课程，陛下先看看其他学期吧");
//                }
//            });
//
//            // 恢复到刚才保存的页面
//            if (savePage) {
//                final int newPage = Math.min(curPage, adapter.getCount() - 1);
//                runMeasurementDependentTask(() -> pager.setCurrentItem(newPage));
//            }
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//            showErrorMessage(e);
//        }
//    }
//
//    /*****************************
//     * 实现::学期选择
//     *****************************/
//
//    // 标题文本控件
//    private TextView title;
//
//    // 表格部分的容器，用于对表格整体做动效
//    private ViewGroup table;
//
//    // 表格部分的蒙版，用于在学期选择器展开时点击表格部分即折叠
//    private View mask;
//
//    // 学期选择器
//    private TagFlowLayout flowLayout;
//
//    // 学期选择器中的学期列表
//    private ArrayList<String> tagList = new ArrayList<>();
//
//    // 学期选择器的标签适配器
//    private TagAdapter<String> tagAdapter = new TagAdapter<String>(tagList) {
//        @Override
//        public View getView(FlowLayout parent, int position, String tag) {
//            TextView tv = (TextView) LayoutInflater.from(CurriculumActivity.this)
//                    .inflate(R.layout.flowlayoutitem_curriculum, parent, false);
//
//            tv.setText(tag);
//            return tv;
//        }
//    };
//
//    // 隐藏学期选择器
//    public void hideTags() {
//        int height = flowLayout.getHeight();
//        table.setY(0);
//        TranslateAnimation anim = new TranslateAnimation(0, 0, height, 0);
//        anim.setDuration(250);
//        table.startAnimation(anim);
//        title.setBackground(ContextCompat.getDrawable(CurriculumActivity.this, R.drawable.curriculum_dropdown));
//
//        mask.setVisibility(View.GONE);
//    }
//
//    // 显示学期选择器
//    public void showTags() {
//        int height = flowLayout.getHeight();
//        table.setY(height);
//        TranslateAnimation anim = new TranslateAnimation(0, 0, -height, 0);
//        anim.setDuration(250);
//        table.startAnimation(anim);
//        title.setBackground(ContextCompat.getDrawable(CurriculumActivity.this, R.drawable.curriculum_dropdown_up));
//
//        mask.setVisibility(View.VISIBLE);
//    }
//
//    /*****************************
//     * 实现::自动刷新
//     *****************************/
//
//    // 注册一个时间改变的接收器
//    @Override
//    public void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_TIME_TICK);
//        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
//        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
//        registerReceiver(timeChangeReceiver, intentFilter);
//    }
//
//    // 当时间为0点0分时，若为周日（周数改变），联网刷新；否则本地刷新
//    BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Calendar calendar = Calendar.getInstance();
//            if (calendar.get(Calendar.HOUR) == 0 && calendar.get(Calendar.MINUTE) == 0)
//                if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
//                    loadData();
//                } else {
//                    readLocal(false);
//                }
//        }
//    };
//
//    // 防火防盗防泄漏
//    @Override
//    public void onDetachedFromWindow() {
//        unregisterReceiver(timeChangeReceiver);
//        super.onDetachedFromWindow();
//    }
//
//    /*****************************
//     * 实现::设置按钮
//     *****************************/
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.settings:
//                showSettingsDialog("更改用户", null, true);
//        }
//    }
//
//    /*****************************
//     * 实现::错误处理
//     *****************************/
//
//    public void handleException(Exception e) {
//        runOnHomeThread(() -> {
//            e.printStackTrace();
//
//            // 显示对应的错误信息，并要求重新登录
//            showErrorMessage(e);
//
//            // 隐藏刷新控件，为了美观，先延时0.5秒
//            new Handler().postDelayed(() -> srl.setRefreshing(false), 500);
//        });
//    }
//
//    // 根据Exception的类型，显示一个错误信息。将根据课表显示状态自动选择SnackBar或对话框形式
//    public void showErrorMessage(Exception e) {
//        String message;
//        if (e instanceof NumberFormatException || e instanceof JSONException) {
//            message = "获取到的数据不太对劲，莫非被飞贼掉包了？";
//        } else if (e instanceof RuntimeException) {
//            message = "密码好像错了，陛下再好好想想？";
//            pager.setAdapter(null);
//            title.setText(R.string.app_name);
//            editor.remove("uuid");
//            editor.apply();
//        } else if (e instanceof ConnectException || e instanceof SocketException) {
//            message = "连不上网了，陛下不妨重新试试？";
//        } else if (e instanceof SocketTimeoutException) {
//            // 服务器端出错
//            message = "学校网络设施出现故障，暂时无法刷新";
//        } else {
//            message = "出现未知错误，陛下不妨重新试试？";
//        }
//        showErrorMessage(message);
//    }
//
//    // 显示一个错误信息。将根据课表显示状态自动选择SnackBar或对话框形式
//    public void showErrorMessage(String message) {
//        if (pager.getAdapter() == null
//                || pager.getAdapter().getCount() == 0) {
//            // 还没有加载到课表信息就出错了
//            title.setText(R.string.app_name);
//            showSettingsDialog("登录", message, false);
//        } else {
//            showSnackBar(srl, message);
//        }
//        srl.setRefreshing(false);
//    }
}
