package cn.seu.herald_android.app_module.exam;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class AddExamActivity extends BaseActivity {

    @BindView(R.id.btn_select_date)
    Button btn_select_date;
    @BindView(R.id.btn_select_time)
    Button btn_select_time;
    @BindView(R.id.edit_duration)
    EditText et_duration;
    @BindView(R.id.edit_location)
    EditText et_location;
    @BindView(R.id.edit_examname)
    EditText et_exam_name;
    @BindView(R.id.edit_teacher)
    EditText et_teacher;

    // 标识是否日期跟时间已选择
    boolean[] selected = new boolean[]{false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_exam__add_exam);
        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            saveDefinedExam();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_select_date)
    void selectDateOnClick() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            // 判断选择时间是周几
            String weekday = CalendarUtils.weekdayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            String date = String.format("%04d-%02d-%02d %s", year, monthOfYear + 1, dayOfMonth, weekday);
            btn_select_date.setText(date);
            selected[0] = true;
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @OnClick(R.id.btn_select_time)
    void selectTimeOnClick() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            // 设定选择时间后的操作
            String time = String.format("%02d:%02d", hourOfDay, minute);
            btn_select_time.setText(time);
            selected[1] = true;
        }, 0, 0, true);
        timePickerDialog.show();
    }

    void saveDefinedExam() {
        if (!selected[0]) {
            showSnackBar("请选择考试开始日期");
            return;
        }
        if (!selected[1]) {
            showSnackBar("请选择考试开始时间");
            return;
        }
        JObj newExam = new JObj();

        String hour = et_duration.getText().toString();
        String course = et_exam_name.getText().toString();
        String date = btn_select_date.getText().toString();
        String time = btn_select_time.getText().toString();
        String dateAndTime = String.format("%s %s", date.split(" ")[0], time);//转化为2016-06-13 09:00的形式

        String location = et_location.getText().toString();
        String teacher = et_teacher.getText().toString();

        newExam.put("hour", hour);
        newExam.put("course", course);
        newExam.put("time", dateAndTime);
        newExam.put("location", location);
        newExam.put("teacher", teacher);
        newExam.put("type", "自定义考试");
        // 获取其他的考试列表
        JArr array = getCustomExamJArr();
        array.put(newExam);
        // 保存新的自定义考试
        saveDefinedExamsFromJArr(array);
        showSnackBar("保存成功");
        Handler handler = new Handler();
        // 延时0.5秒显示保存成功信息后返回
        handler.postDelayed(this::finish, 500);
    }

    public static JArr getCustomExamJArr() {
        String cache = Cache.examCustom.getValue();
        return new JArr(cache);
    }

    void saveDefinedExamsFromJArr(JArr array) {
        Cache.examCustom.setValue(array.toString());
    }
}
