package cn.seu.herald_android.mod_query.exam;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ViewHolder> {

    private List<ExamItem> examList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_time;
        public TextView tv_course;
        public TextView tv_location;
        public TextView tv_teacher;
        public TextView tv_hour;
        public TextView tv_numtitle;
        public TextView tv_num;

        public ViewHolder(View v) {
            super(v);
            tv_time = (TextView) v.findViewById(R.id.tv_time);
            tv_course = (TextView) v.findViewById(R.id.tv_course);
            tv_location = (TextView) v.findViewById(R.id.tv_location);
            tv_teacher = (TextView) v.findViewById(R.id.tv_teacher);
            tv_hour = (TextView) v.findViewById(R.id.tv_hour);
            tv_numtitle = (TextView) v.findViewById(R.id.tv_numtitle);
            tv_num = (TextView) v.findViewById(R.id.tv_num);
        }
    }

    public ExamAdapter(List<ExamItem> exams) {
        this.examList = exams;
    }

    @Override
    public ExamAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listviewitem_exam, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExamItem examItem = examList.get(position);
        holder.tv_time.setText("时间：" + examItem.time);
        holder.tv_course.setText(examItem.course);
        holder.tv_location.setText("地点：" + examItem.location);
        holder.tv_teacher.setText("教师：" + examItem.teacher);
        holder.tv_hour.setText("时长：" + examItem.hour + "分钟");

        try {
            int remainingDays = examItem.getRemainingDays();
            holder.tv_num.setText(String.valueOf(remainingDays));

            if (remainingDays < 0) {
                holder.tv_numtitle.setText("已结束");
            } else if (remainingDays == 0) {
                holder.tv_numtitle.setText("今天考试");
            } else {
                holder.tv_numtitle.setText("剩余天数");
            }
        } catch (Exception e) {
            holder.tv_numtitle.setText("");
            holder.tv_num.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    public void setList(List<ExamItem> exams) {
        this.examList = exams;
    }
}
