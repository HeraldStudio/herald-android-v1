package cn.seu.herald_android.mod_afterschool;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.FadeOutHeaderContainer;


/**
 * Created by heyon on 2016/5/9.
 */
public class AfterSchoolActivityAdapter extends RecyclerView.Adapter<AfterSchoolActivityAdapter.AfterSchoolActivityViewHolder> {
    private Context context;
    private ArrayList<AfterSchoolActivityItem> list;

    public AfterSchoolActivityAdapter(android.content.Context context, ArrayList<AfterSchoolActivityItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public AfterSchoolActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.recyclerviewitem_afterschoolactivity, null);
        return new AfterSchoolActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AfterSchoolActivityViewHolder holder, int position) {
        AfterSchoolActivityItem afterSchoolActivityItem = list.get(position);
        float resolution = 5 / 2f;

        //设置缩略图
        try{
            Picasso.with(context).load(afterSchoolActivityItem.getPicUrl()).into(holder.imgv_activity);
        }catch (Exception e){
            //若url出错则捕获异常
            Picasso.with(context).load(R.drawable.default_herald).into(holder.imgv_activity);
            e.printStackTrace();
        }

        //设置活动标题
        holder.tv_title.setText(afterSchoolActivityItem.title);
        //设置活动简介
        holder.tv_introduction.setText(afterSchoolActivityItem.introduciton);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class AfterSchoolActivityViewHolder extends RecyclerView.ViewHolder{
        //活动缩略图
        ImageView  imgv_activity;
        //活动标题
        TextView tv_title;
        TextView tv_introduction;
        public AfterSchoolActivityViewHolder(View itemView) {
            super(itemView);
            imgv_activity = (ImageView)itemView.findViewById(R.id.imgv_activity);
            tv_title = (TextView)itemView.findViewById(R.id.tv_title);
            tv_introduction = (TextView)itemView.findViewById(R.id.tv_introduction);
            float resolution = 5f/2f;
            int weight = itemView.getResources().getDisplayMetrics().widthPixels;
            int height = (int) ( weight / resolution);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(weight,height);
            imgv_activity.setLayoutParams(params);

        }
    }


}
