package cn.seu.herald_android.mod_query.exam;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ViewHolder> {

    private List<ExamModel> examList;

    public interface onItemClickListener {
        void onItemClick(ExamModel item, int position);
    }

    private onItemClickListener mListener = null;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        TextView tv_time;
        @BindView(R.id.title)
        TextView tv_course;
        @BindView(R.id.tv_location)
        TextView tv_location;
        @BindView(R.id.subtitle)
        TextView tv_teacher;
        @BindView(R.id.tv_hour)
        TextView tv_hour;
        @BindView(R.id.tv_numtitle)
        TextView tv_numtitle;
        @BindView(R.id.num)
        TextView tv_num;
        public View rootView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, rootView = v);
        }
    }

    public void setOnItemClickListener(onItemClickListener mlistner) {
        this.mListener = mlistner;
    }

    public ExamAdapter(List<ExamModel> exams) {
        this.examList = exams;
    }

    @Override
    public ExamAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_que_exam__item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExamModel examModel = examList.get(position);
        holder.tv_time.setText("时间：" + examModel.time);

        String undefinedText = "未知";
        if (examModel.hour.equals("续一秒") || examModel.hour.equals("+1s") || examModel.teacher.equals("长者")) {
            undefinedText = "无可奉告";
        }
        holder.tv_course.setText(examModel.course.equals("") ? "未命名" : examModel.course);
        holder.tv_hour.setText("时长：" + (examModel.hour.equals("") ? undefinedText : examModel.hour + "分钟"));
        holder.tv_location.setText("地点：" + (examModel.location.equals("") ? undefinedText : examModel.location));
        holder.tv_teacher.setText("教师：" + (examModel.teacher.equals("") ? undefinedText : examModel.teacher));
        holder.rootView.setOnClickListener(v -> mListener.onItemClick(examModel, position)
        );
        try {
            int remainingDays = examModel.getRemainingDays();
            holder.tv_num.setText(String.valueOf(remainingDays));
            holder.tv_num.setVisibility(View.VISIBLE);
            if (remainingDays < 0) {
                //如果考试已经结束就标识已结束
                holder.tv_numtitle.setText("已结束");
                holder.tv_num.setVisibility(View.INVISIBLE);
            } else if (remainingDays == 0) {
                holder.tv_numtitle.setText("今天考试");
                holder.tv_num.setVisibility(View.INVISIBLE);
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

    public void setList(List<ExamModel> exams) {
        this.examList = exams;
    }
}
