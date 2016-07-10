package cn.seu.herald_android.mod_query.exam;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.CacheHelper;

public class AddExamActivity extends BaseActivity {

    //选择日期
    Button btn_select_date;
    //选择时间
    Button btn_select_time;
    //持续时间
    EditText et_duratime;
    //考试地点
    EditText et_location;
    //考试名字
    EditText et_examname;
    //老师姓名
    EditText et_teacher;
    //标识是否日期跟时间已选择
    boolean[] selecteds = new boolean[]{false,false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_exam__add_exam);
        init();
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

    void init(){
        //toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
                finish();
            });
        }

        setTitle("自定义考试");
        //沉浸式
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorExamprimary));
        enableSwipeBack();

        //控件初始化
        btn_select_date = (Button)findViewById(R.id.btn_select_date);
        btn_select_time = (Button)findViewById(R.id.btn_select_time);
        et_duratime = (EditText)findViewById(R.id.edit_duratime);
        et_location =(EditText)findViewById(R.id.edit_location);
        et_examname = (EditText)findViewById(R.id.edit_examname);
        et_teacher = (EditText)findViewById(R.id.edit_teacher);

        //绑定点击事件
        btn_select_date.setOnClickListener(v -> {
            Calendar today =  Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year,monthOfYear,dayOfMonth);
                //判断选择时间是周几
                String weekday = CalendarUtils.weekdayNames[calendar.get(Calendar.DAY_OF_WEEK)-1];
                String date = String.format("%04d-%02d-%02d %s",year,monthOfYear+1,dayOfMonth,weekday);
                btn_select_date.setText(date);
                selecteds[0] = true;
            }, today.get(Calendar.YEAR),today.get(Calendar.MONTH),today.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btn_select_time.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                //设定选择时间后的操作
                String time = String.format("%02d:%02d",hourOfDay,minute);
                btn_select_time.setText(time);
                selecteds[1] = true;
            },0,0,true);
            timePickerDialog.show();
        });
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
        if (location.equals(""))location = "暂无地点";
        try{
            newexam.put("hour",hour);
            newexam.put("course",course);
            newexam.put("time",dateandtime);
            newexam.put("location",location);
            newexam.put("teacher",teacher);
            newexam.put("type","自定义考试");
            //获取其他的考试列表
            JSONArray array = getDefinedExamsJSONArray();
            array.put(newexam);
            //保存新的自定义考试
            saveDefinedExamsFromJSONArray(array);
            showSnackBar("保存成功");
            Handler handler = new Handler();
            //延时0.5秒显示保存成功信息后返回
            handler.postDelayed(this::finish, 500);
        }catch (JSONException e){
            e.printStackTrace();
            showSnackBar("考试信息不规范，请重新输入");
        }
    }

    JSONArray getDefinedExamsJSONArray(){
        String cache = CacheHelper.get("herald_exam_definedexam");
        try{
            return new JSONArray(cache);
        }catch (JSONException e){
            e.printStackTrace();
            CacheHelper.set("herald_exam_definedexam",new JSONArray().toString());
        }
        return new JSONArray();
    }

    void saveDefinedExamsFromJSONArray(JSONArray array){
        CacheHelper.set("herald_exam_definedexam",array.toString());
    }

}
