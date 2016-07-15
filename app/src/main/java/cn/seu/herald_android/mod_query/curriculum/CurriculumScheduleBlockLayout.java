package cn.seu.herald_android.mod_query.curriculum;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;


@SuppressLint("ViewConstructor")
public class CurriculumScheduleBlockLayout extends FrameLayout implements View.OnClickListener {

    private static int color[] = {
            R.drawable.curriculum_block_bg_today1,
            R.drawable.curriculum_block_bg_today2,
            R.drawable.curriculum_block_bg_today3,
            R.drawable.curriculum_block_bg_today4,
            R.drawable.curriculum_block_bg_today5,
            R.drawable.curriculum_block_bg_today6,
            R.drawable.curriculum_block_bg_today7,
            R.drawable.curriculum_block_bg_today8
    };
    private ClassModel classModel;
    private int layout;
    private Pair<String, String> teacherAndGPA;

    @BindView(R.id.className)
    TextView className;
    @BindView(R.id.classPlace)
    TextView classPlace;

    public CurriculumScheduleBlockLayout(Context c, ClassModel info,
                                         Pair<String, String> teacherAndGPA,
                                         boolean isToday) {
        super(c);
        classModel = info;
        this.teacherAndGPA = teacherAndGPA;
        layout = isToday ? R.layout.mod_que_curriculum__cell_today : R.layout.mod_que_curriculum__cell_normal;
    }

    // 要显示在屏幕上时再进行添加view的操作，显著提高应用启动速度
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // 实例化视图
        View v = LayoutInflater.from(getContext()).inflate(layout, null);

        // 根据课程名的String长度和byte[]长度确定背景色
        // 这样可以让不同课程的颜色尽量不同，而同一课程的颜色一致
        v.setBackground(ContextCompat.getDrawable(getContext(),
                color[(classModel.getClassName().getBytes().length * 2 + classModel.getClassName().length()) % 8]));
        addView(v);

        ButterKnife.bind(this);

        className.setText(classModel.getClassName());
        classPlace.setText(classModel.getPlace());

        setOnClickListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeAllViews();
        super.onDetachedFromWindow();
    }

    @Override
    public void onClick(View v) {
        // 课程信息
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("课程信息")
                .setMessage(
                        "课程名称：" + classModel.getClassName() + "\n" +
                                "上课地点：" + classModel.getPlace().replace("(单)", "").replace("(双)", "") + "\n" +
                                "上课周次：" + classModel.getStartWeek() + "~" + classModel.getEndWeek()
                                + (classModel.getPlace().startsWith("(单)") ? "周单" : "")
                                + (classModel.getPlace().startsWith("(双)") ? "周双" : "") + "周"
                                + classModel.weekNum + "\n" +
                                "上课时间：" + classModel.getStartTime() + "~" + classModel.getEndTime() + "节 (" + classModel.getTimePeriod() + ")\n" +
                                (teacherAndGPA == null ? "获取教师及学分信息失败，请刷新" :
                                        ("授课教师：" + teacherAndGPA.first + "\n" +
                                                "课程学分：" + teacherAndGPA.second)))
                .create();
        dialog.show();
    }
}
