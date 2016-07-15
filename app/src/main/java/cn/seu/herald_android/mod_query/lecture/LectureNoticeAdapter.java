package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

public class LectureNoticeAdapter extends RecyclerView.Adapter<LectureNoticeAdapter.LectureRecord> {
    private Context context;
    private ArrayList<LectureNoticeModel> list;

    public LectureNoticeAdapter(Context context, ArrayList<LectureNoticeModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public LectureRecord onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.mod_que_lecture__item, null);
        return new LectureRecord(view);
    }

    @Override
    public void onBindViewHolder(LectureRecord holder, int position) {
        LectureNoticeModel lectureNoticeModel = list.get(position);
        holder.tv_location.setText(lectureNoticeModel.getLocation());
        holder.tv_date.setText(lectureNoticeModel.getDate());
        holder.tv_speaker.setText(lectureNoticeModel.getSpeaker());
        holder.tv_topic.setText(lectureNoticeModel.getTopic());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class LectureRecord extends RecyclerView.ViewHolder {
        TextView tv_date;
        TextView tv_location;
        TextView tv_speaker;
        TextView tv_topic;

        public LectureRecord(View itemView) {
            super(itemView);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_location = (TextView) itemView.findViewById(R.id.tv_location);
            tv_speaker = (TextView) itemView.findViewById(R.id.tv_speaker);
            tv_topic = (TextView) itemView.findViewById(R.id.tv_topic);
        }
    }

}



