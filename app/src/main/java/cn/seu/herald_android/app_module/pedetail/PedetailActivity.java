package cn.seu.herald_android.app_module.pedetail;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.factory.PedetailCard;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class PedetailActivity extends BaseActivity implements CompactCalendarView.CompactCalendarViewListener {

    // 跑操次数数字
    @BindView(R.id.tv_count)
    TextView countLabel;
    @BindView(R.id.tv_remain)
    TextView remainLabel;
    @BindView(R.id.pedetail_week)
    TextView weekLabel;
    @BindView(R.id.compactcalendar_view)
    CompactCalendarView calendarView;

    @BindColor(R.color.colorAccent)
    public int primaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_pedetail);
        ButterKnife.bind(this);
        calendarView.setListener(this);
        calendarView.setShouldShowMondayAsFirstDay(false);
        calendarView.setDayColumnNames(new String[]{"日", "一", "二", "三", "四", "五", "六"});
        calendarView.setCurrentSelectedDayIndicatorStyle(CompactCalendarView.NO_FILL_LARGE_INDICATOR);
        calendarView.setCurrentDayIndicatorStyle(CompactCalendarView.NO_FILL_LARGE_INDICATOR);

        // 首先加载一次缓存数据（如未登录则弹出登陆窗口）
        loadCache();

        // 联网刷新
        refreshCache();
    }

    private void refreshCache() {
        showProgressDialog();
        PedetailCard.getRefresher().onFinish((success, code) -> {
            hideProgressDialog();
            if (!success) {
                showSnackBar("刷新失败，请重试");
            }
            loadCache();
        }).run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<PedetailRecordModel> history = new ArrayList<>();

    private void loadCache() {
        try {
            String cache = Cache.peDetail.getValue();
            String count = Cache.peCount.getValue();
            String remain = Cache.peRemain.getValue();
            countLabel.setText(count);
            remainLabel.setText(remain);

            JArr jsonArray = new JArr(cache);
            calendarView.removeAllEvents();
            history.clear();

            for (int i = 0; i < jsonArray.size(); i++) {
                JObj k = jsonArray.$o(i);
                PedetailRecordModel model = new PedetailRecordModel(k);
                calendarView.addEvent(new Event(
                        primaryColor,
                        model.getDateTime().getTimeInMillis()), false);
                history.add(model);
            }
            calendarView.invalidate();
            updateCurrentDayColor();
            updateMonthDisplay();

            if (history.size() == 0) {
                showSnackBar("本学期暂时没有跑操记录");
            }
        } catch (Exception e) {
            showSnackBar("解析失败，请刷新");
            e.printStackTrace();
        }
    }

    @Override
    public void onDayClick(Date dateClicked) {
        updateSelectColor(dateClicked);
        updateCurrentDayColor();
        for (int i = 0; i < history.size(); i++) {
            PedetailRecordModel model = history.get(i);
            if (dateClicked.equals(CalendarUtils.toSharpDay(model.getDateTime()).getTime())) {
                showSnackBar("(第 " + (i + 1) + " 次跑操) " + model.toString());
                return;
            }
        }
        showSnackBar("该日无跑操记录");
    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {
        updateSelectColor(firstDayOfNewMonth);
        updateCurrentDayColor();
        updateMonthDisplay();
    }

    private void updateMonthDisplay() {
        Date date = calendarView.getFirstDayOfCurrentMonth();
        SimpleDateFormat format = new SimpleDateFormat("yyyy年M月");
        weekLabel.setText(format.format(date));
    }

    private void updateSelectColor(Date date) {
        for (int i = 0; i < history.size(); i++) {
            PedetailRecordModel model = history.get(i);
            if (date.equals(CalendarUtils.toSharpDay(model.getDateTime()).getTime())) {
                calendarView.setCurrentSelectedDayBackgroundColor(primaryColor);
                return;
            }
        }
        calendarView.setCurrentSelectedDayBackgroundColor(Color.argb(255, 240, 240, 240));
    }

    private void updateCurrentDayColor() {
        for (int i = 0; i < history.size(); i++) {
            PedetailRecordModel model = history.get(i);
            if (CalendarUtils.toSharpDay(Calendar.getInstance()).getTime()
                    .equals(CalendarUtils.toSharpDay(model.getDateTime()).getTime())) {
                calendarView.setCurrentDayBackgroundColor(primaryColor);
                return;
            }
        }
        calendarView.setCurrentDayBackgroundColor(Color.argb(255, 240, 240, 240));
    }
}
