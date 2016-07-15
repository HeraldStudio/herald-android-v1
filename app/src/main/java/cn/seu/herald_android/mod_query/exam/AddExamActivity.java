package cn.seu.herald_android.mod_query.exam;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.CacheHelper;

public class AddExamActivity extends BaseActivity {

    @BindView(R.id.btn_select_date)
    Button btn_select_date;
    @BindView(R.id.btn_select_time)
    Button btn_select_time;
    @BindView(R.id.edit_duratime)
    EditText et_duratime;
    @BindView(R.id.edit_location)
    EditText et_location;
    @BindView(R.id.edit_examname)
    EditText et_examname;
    @BindView(R.id.edit_teacher)
    EditText et_teacher;

    //标识是否日期跟时间已选择
    boolean[] selecteds = new boolean[]{false,false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_exam__add_exam);
        ButterKnife.bind(this);
        setTitle("自定义考试");
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
            //判断选择时间是周几
            String weekday = CalendarUtils.weekdayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            String date = String.format("%04d-%02d-%02d %s", year, monthOfYear + 1, dayOfMonth, weekday);
            btn_select_date.setText(date);
            selecteds[0] = true;
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @OnClick(R.id.btn_select_time)
    void selectTimeOnClick() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            //设定选择时间后的操作
            String time = String.format("%02d:%02d", hourOfDay, minute);
            btn_select_time.setText(time);
            selecteds[1] = true;
        }, 0, 0, true);
        timePickerDialog.show();
    }

    void saveDefinedExam(){
        if (!selecteds[0]){
            showSnackBar("请选择考试开始日期");
            return;
        }
        if (!selecteds[1]){
            showSnackBar("请选择考试开始时间");
            return;
        }
        JSONObject newexam = new JSONObject();

        String hour = et_duratime.getText().toString();
        if (hour.equals(""))hour = "?";

        String course = et_examname.getText().toString();
        if (course.equals(""))course = "暂无考试名";

        String date = btn_select_date.getText().toString();
        String time = btn_select_time.getText().toString();
        String dateandtime = String.format("%s %s(%s)",date.split(" ")[0],time,date.split(" ")[1]);//转化为2016-06-13 09:00(星期一)的形式

        String location = et_location.getText().toString();
        String teacher = et_teacher.getText().toString();
        if (location.equals("")) location = "暂无地点";
        try {
            newexam.put("hour", hour);
            newexam.put("course", course);
            newexam.put("time", dateandtime);
            newexam.put("location", location);
            newexam.put("teacher", teacher);
            newexam.put("type", "自定义考试");
            //获取其他的考试列表
            JSONArray array = getDefinedExamsJSONArray();
            array.put(newexam);
            //保存新的自定义考试
            saveDefinedExamsFromJSONArray(array);
            showSnackBar("保存成功");
            Handler handler = new Handler();
            //延时0.5秒显示保存成功信息后返回
            handler.postDelayed(this::finish, 500);
        } catch (JSONException e) {
            e.printStackTrace();
            showSnackBar("考试信息不规范，请重新输入");
        }
    }

    JSONArray getDefinedExamsJSONArray() {
        String cache = CacheHelper.get("herald_exam_definedexam");
        try {
            return new JSONArray(cache);
        } catch (JSONException e) {
            e.printStackTrace();
            CacheHelper.set("herald_exam_definedexam", new JSONArray().toString());
        }
        return new JSONArray();
    }

    void saveDefinedExamsFromJSONArray(JSONArray array){
        CacheHelper.set("herald_exam_definedexam",array.toString());
    }
}
