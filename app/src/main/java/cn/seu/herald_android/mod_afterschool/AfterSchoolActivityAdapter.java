package cn.seu.herald_android.mod_afterschool;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.refreshrecyclerview.RefreshRecyclerView;
import cn.seu.herald_android.mod_webmodule.WebShowActivity;


/**
 * Created by heyon on 2016/5/9.
 */
public class AfterSchoolActivityAdapter extends RefreshRecyclerView.RefreshRecyclerAdapter {
    ArrayList<AfterSchoolActivityItem> list;
    boolean loadFinished = false;
    public AfterSchoolActivityAdapter(android.content.Context context, ArrayList<AfterSchoolActivityItem> list) {
        super(context);
        this.list = list;
    }

    public void setLoadFinished(boolean loadFinished){
        this.loadFinished = loadFinished;
    }

    public void addItem(AfterSchoolActivityItem content) {
        list.add(content);
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.recyclerviewitem_afterschoolactivity, parent,false);
        return new AfterSchoolActivityViewHolder(view);
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder() {
        return null;
    }

    @Override
    public FooterViewHolder onCreateFooterViewHolder() {
        View view = LayoutInflater.from(this.context).inflate(R.layout.refresh_recyclerview_footer,null,false);
        WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        view.setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new FooterViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        AfterSchoolActivityViewHolder viewHolder = (AfterSchoolActivityViewHolder) holder;
        final AfterSchoolActivityItem item = list.get(position);
        //设置缩略图
        try{
            Picasso.with(context).load(item.getPicUrl()).into(viewHolder.imgv_activity);
        }catch (Exception e){
            //若url出错则捕获异常
            Picasso.with(context).load(R.drawable.default_herald).into(viewHolder.imgv_activity);
            e.printStackTrace();
        }
        //点击相应函数
        viewHolder.rootView.setOnClickListener(o->{
                WebShowActivity.startWebShowActivity(getContext(),item.title,item.getDetailUri(),R.style.AfterSchoolActivityTheme);
        });
        //判断活动是否开始
        long now_time = CalendarUtils.toSharpDay(Calendar.getInstance()).getTimeInMillis();
        long start_time = item.getStartCalendar().getTimeInMillis();
        long end_time = item.getEndCalendar().getTimeInMillis();
        if(now_time < start_time){
            viewHolder.tv_tag.setText("即将开始");
            viewHolder.tv_tag.setTextColor( ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        } else if (now_time > end_time) {
            viewHolder.tv_tag.setText("已结束");
            viewHolder.tv_tag.setTextColor( ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        }else {
            viewHolder.tv_tag.setText("进行中");
            viewHolder.tv_tag.setTextColor( ContextCompat.getColor(getContext(), R.color.relaxGreen));
        }
        //设置活动时间
        viewHolder.tv_time.setText(item.activity_time);
        //设置活动标题
        viewHolder.tv_title.setText(item.title);
        //设置活动简介
        viewHolder.tv_introduction.setText(item.introduciton);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }


    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position) {
        FooterViewHolder viewHolder = (FooterViewHolder) holder;
        TextView tv_tip = (TextView) viewHolder.getItemView().findViewById(R.id.tv_tip);
        if(loadFinished)
            tv_tip.setText("已无更多内容");
        else
            tv_tip.setText("Loading");
    }

    @Override
    public int getItemTotalCount() {
        return list.size();
    }

    public static class AfterSchoolActivityViewHolder extends RecyclerView.ViewHolder{
        //根视图
        View rootView;
        //活动缩略图
        ImageView  imgv_activity;
        //活动标题
        TextView tv_title;
        //活动时间
        TextView tv_time;
        //活动介绍
        TextView tv_introduction;
        //标识活动是否开始
        TextView tv_tag;
        public AfterSchoolActivityViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            imgv_activity = (ImageView)itemView.findViewById(R.id.imgv_activity);
            tv_title = (TextView)itemView.findViewById(R.id.tv_title);
            tv_introduction = (TextView)itemView.findViewById(R.id.tv_introduction);
            tv_time = (TextView)itemView.findViewById(R.id.tv_time);
            tv_tag = (TextView)itemView.findViewById(R.id.tv_tag);
            float resolution = 5f/2f;
            int weight = itemView.getResources().getDisplayMetrics().widthPixels;
            int height = (int) ( weight / resolution);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(weight,height);
            imgv_activity.setLayoutParams(params);
        }

    }




}
