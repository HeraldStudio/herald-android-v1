package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/3/1.
 */
public class LectureNoticeAdapter extends RecyclerView.Adapter<LectureNoticeAdapter.LectureRecord>{
    Context context;
    ArrayList<LectureRecordItem> list;
    public LectureNoticeAdapter(Context context, ArrayList<LectureRecordItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public LectureRecord onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(this.context).inflate(R.layout.recyclerviewitem_lecture_notice, null);
        return new LectureRecord(view);
    }

    @Override
    public void onBindViewHolder(LectureRecord holder, int position) {
        LectureRecordItem lectureRecordItem = list.get(position);
        holder.tv_time.setText(lectureRecordItem.time);
        holder.tv_place.setText(lectureRecordItem.place);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class LectureRecord extends RecyclerView.ViewHolder{
        TextView tv_time;
        TextView tv_place;
        public LectureRecord(View itemView) {
            super(itemView);
            tv_time = (TextView)itemView.findViewById(R.id.tv_time);
            tv_place = (TextView)itemView.findViewById(R.id.tv_place);
        }
    }

}



