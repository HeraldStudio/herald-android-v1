package cn.seu.herald_android.mod_query.pedetail;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiThreadManager;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.mod_cards.PedetailCard;

public class PedetailActivity extends BaseActivity {

    public static final int[] FORECAST_TIME_PERIOD = {
            6 * 60 + 20, 7 * 60 + 20
    };
    // 左右滑动分页的日历容器
    @BindView(R.id.calendarPager)
    ViewPager pager;
    // 跑操次数数字
    @BindView(R.id.tv_count)
    TextView count;
    @BindView(R.id.tv_remain)
    TextView remain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_pedetail);
        ButterKnife.bind(this);

        // 首先加载一次缓存数据（如未登录则弹出登陆窗口）
        readLocal();

        // 检查是否需要联网刷新，如果需要则刷新，不需要则取消
        if (isRefreshNeeded()) refreshCache();
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiThreadManager()
                .addAll(PedetailCard.getRefresher())
                .onFinish((success) -> {
                    hideProgressDialog();
                    if (success) {
                        readLocal();
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

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

    private void readLocal() {
        try {
            // 读取本地保存的跑操数据
            JSONArray array = new JSONArray(CacheHelper.get("herald_pedetail"));
            String countStr = CacheHelper.get("herald_pe_count");
            String remainStr = CacheHelper.get("herald_pe_remain");

            // 用户有数据
            // 有效跑操计数器，用于显示每一个跑操是第几次
            int exerciseCount = 0;

            // 创建一个包含所有有效跑操记录的列表（单重列表结构）
            List<PedetailRecordModel> infoList = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                // 将跑操数据倾倒到列表
                PedetailRecordModel info = new PedetailRecordModel(obj, exerciseCount + 1);
                if (info.getValid()) {
                    infoList.add(info);
                    exerciseCount++;
                }
            }
            count.setText(countStr);
            remain.setText(remainStr);

            // 用年月时间戳（年*12+自然月-1）比较器进行排序以防万一
            Collections.sort(infoList, PedetailRecordModel.yearMonthComparator);

            // 当前所在月的年月戳
            Calendar cal = Calendar.getInstance();
            int curMonth = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH);

            // 起始月为最早记录所在月
            int startMonth = infoList.size() > 0 ? infoList.get(0).getYearMonth() : curMonth;

            // 终止月为最晚记录所在月
            int endMonth = infoList.size() > 0 ?
                    Math.max(infoList.get(infoList.size() - 1).getYearMonth(), curMonth) : curMonth;

            // 创建一个键值对结构，键为年月戳，值为该月的跑操记录列表
            Map<Integer, List<PedetailRecordModel>> pages = new HashMap<>();
            for (int i = startMonth; i <= endMonth; i++) {
                pages.put(i, new ArrayList<>());
            }

            // 将单重列表的每个元素倾倒到双重列表中对应的位置
            for (PedetailRecordModel info : infoList) {
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

            if (infoList.size() == 0) {
                showSnackBar("本学期暂时没有跑操记录");
            }
        } catch (Exception e) {
            showSnackBar("解析失败，请刷新");
            e.printStackTrace();
        }
    }
}
