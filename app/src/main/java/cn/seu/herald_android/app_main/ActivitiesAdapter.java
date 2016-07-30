package cn.seu.herald_android.app_main;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_secondary.WebModuleActivity;
import cn.seu.herald_android.custom.refreshrecyclerview.RefreshRecyclerView;

public class ActivitiesAdapter extends RefreshRecyclerView.RefreshRecyclerAdapter {
    ArrayList<ActivitiesItem> list;
    boolean loadFinished = false;

    public ActivitiesAdapter(android.content.Context context, ArrayList<ActivitiesItem> list) {
        super(context);
        this.list = list;
    }

    public void setLoadFinished(boolean loadFinished){
        this.loadFinished = loadFinished;
    }

    public void addItem(ActivitiesItem content) {
        list.add(content);
    }

    public void removeAll(){list.clear();}

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.app_main__fragment_activities__item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder() {
        return null;
    }

    @Override
    public FooterViewHolder onCreateFooterViewHolder() {
        View view = LayoutInflater.from(this.context).inflate(R.layout.app_main__fragment_activities__item_footer, null, false);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        view.setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new FooterViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        final ActivitiesItem item = list.get(position);
        // 设置缩略图
        try {
            if (item.picUrl.equals(""))
                Picasso.with(context).load(R.drawable.default_herald).into(viewHolder.image);
            else
                Picasso.with(context).load(item.getPicUrl()).into(viewHolder.image);
        } catch (Exception e) {
            // 若url出错则捕获异常
            Picasso.with(context).load(R.drawable.default_herald).into(viewHolder.image);
            e.printStackTrace();
        }


        // 链接不为空则显示“详情按钮”
        if (!item.detailUrl.equals("")) {
            viewHolder.detail.setVisibility(View.VISIBLE);
        } else {
            viewHolder.detail.setVisibility(View.GONE);
        }


        // 点击相应函数
        viewHolder.cardView.setOnClickListener(o -> {
            // 链接不为空则打开
            if (!item.detailUrl.equals(""))
                WebModuleActivity.startWebModuleActivity(item.title, item.getDetailUrl(), R.style.ActivitiesTheme);
        });
        // 判断活动是否开始
        viewHolder.tag.setText(item.getTag());
        viewHolder.tag.setTextColor(ContextCompat.getColor(getContext(), item.getTagColorId()));

        // 设置活动时间和地点
        viewHolder.timeAndPlace.setText(
                String.format("活动时间: %s\n活动地点: %s", item.activityTime, item.location)
        );
        // 设置活动主办方
        viewHolder.assoc.setText(item.assoc);
        // 设置活动标题
        viewHolder.title.setText(item.title);
        // 设置活动简介
        viewHolder.desc.setText(item.desc);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }


    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position) {
        FooterViewHolder viewHolder = (FooterViewHolder) holder;
        TextView tv_tip = (TextView) viewHolder.getItemView().findViewById(R.id.tv_tip);
        if (loadFinished)
            tv_tip.setText("没有更多数据");
        else
            tv_tip.setText("上拉加载");
    }

    @Override
    public int getItemTotalCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cardView)
        View cardView;
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.timeAndPlace)
        TextView timeAndPlace;
        @BindView(R.id.desc)
        TextView desc;
        @BindView(R.id.tag)
        TextView tag;
        @BindView(R.id.assoc)
        TextView assoc;
        @BindView(R.id.detail)
        TextView detail;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            float resolution = 5f/2f;
            image.post(() -> {
                int weight = image.getWidth();
                int height = (int) (weight / resolution);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(weight,height);
                image.setLayoutParams(params);
            });
            image.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
