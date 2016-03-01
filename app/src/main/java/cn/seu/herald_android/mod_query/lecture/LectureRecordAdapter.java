package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/3/1.
 */
public class LectureRecordAdapter extends RecyclerView.Adapter<LectureRecordAdapter.LectureRecord>{
    Context context;
    ArrayList<LectureRecordItem> list;
    public LectureRecordAdapter(Context context,ArrayList<LectureRecordItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public LectureRecord onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(this.context).inflate(R.layout.recyclerviewitem_lecture_record, null);
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

    public static class MyLayoutManager extends LinearLayoutManager {

        public MyLayoutManager(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec,int heightSpec) {
            View view = recycler.getViewForPosition(0);
            if(view != null){
                measureChild(view, widthSpec, heightSpec);
                int measuredWidth = View.MeasureSpec.getSize(widthSpec);
                int measuredHeight = view.getMeasuredHeight();
                setMeasuredDimension(measuredWidth, measuredHeight);
            }
        }
    }
}



