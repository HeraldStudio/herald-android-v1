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
import java.util.Collections;

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

    public void removeAll(){list.clear();}

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
            if (item.pic_url.equals(""))
                Picasso.with(context).load(R.drawable.default_herald).into(viewHolder.imgv_activity);
            else
                Picasso.with(context).load(item.getPicUrl()).into(viewHolder.imgv_activity);
        }catch (Exception e){
            //若url出错则捕获异常
            Picasso.with(context).load(R.drawable.default_herald).into(viewHolder.imgv_activity);
            e.printStackTrace();
        }


        //链接不为空则显示“详情按钮”
        if(!item.detail_url.equals("")){
            viewHolder.tv_details.setVisibility(View.VISIBLE);
        }else {
            viewHolder.tv_details.setVisibility(View.GONE);
        }


        //点击相应函数
        viewHolder.cardview.setOnClickListener(o->{
            //链接不为空则打开
            if(!item.detail_url.equals(""))
                WebShowActivity.startWebShowActivity(getContext(),item.title,item.getDetailUri(),R.style.AfterSchoolActivityTheme);
        });
        //判断活动是否开始
        viewHolder.tv_tag.setText(item.getTag());
        viewHolder.tv_tag.setTextColor( ContextCompat.getColor(getContext(), item.getTagColorId()));

        //设置活动时间和地点
        viewHolder.tv_time_and_location.setText(
                String.format("活动时间: %s\n活动地点: %s",item.activity_time,item.location)
        );
        //设置活动主办方
        viewHolder.tv_association.setText(item.assiciation);
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
        //活动缩略图
        ImageView  imgv_activity;
        //活动标题
        TextView tv_title;
        //活动时间和地点
        TextView tv_time_and_location;
        //活动介绍
        TextView tv_introduction;
        //标识活动是否开始
        TextView tv_tag;
        //活动主办方
        TextView tv_association;
        //可点击的卡片区域
        View cardview;
        //详情
        TextView tv_details;
        public AfterSchoolActivityViewHolder(View itemView) {
            super(itemView);
            cardview = itemView.findViewById(R.id.layout_card);
            imgv_activity = (ImageView)itemView.findViewById(R.id.imgv_activity);
            tv_title = (TextView)itemView.findViewById(R.id.tv_title);
            tv_time_and_location = (TextView)itemView.findViewById(R.id.tv_time_and_location);
            tv_introduction = (TextView)itemView.findViewById(R.id.tv_introduction);
            tv_tag = (TextView)itemView.findViewById(R.id.tv_tag);
            tv_association = (TextView)itemView.findViewById(R.id.tv_association);
            tv_details = (TextView)itemView.findViewById(R.id.tv_details);
            float resolution = 5f/2f;
            imgv_activity.post(()->{
                int weight = imgv_activity.getWidth();
                int height = (int) ( weight / resolution);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(weight,height);
                imgv_activity.setLayoutParams(params);
            });
            imgv_activity.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }




}
