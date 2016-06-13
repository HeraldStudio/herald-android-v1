package cn.seu.herald_android.mod_query.cardextra;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private Context context;
    private ArrayList<CardItem> list;

    public CardAdapter(Context context, ArrayList<CardItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.recyclerviewitem_card, null);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        CardItem cardItem = list.get(position);
//        判断日期和分割线是否显示，用来给每天的消费记录分组
        if (position == 0 || !list.get(position).getDate().equals(list.get(position - 1).getDate())) {
            holder.tv_date.setVisibility(View.VISIBLE);
        } else {
            holder.tv_date.setVisibility(View.GONE);
        }
        //判断消费是否为负，如果不为负，则为正，需要设置成其他颜色
        if (!cardItem.getPrice().startsWith("-") && !cardItem.getPrice().equals("")) {
            //如果不为负，则设置绿色和加号
            holder.tv_price.setText("+" + cardItem.getPrice());
            holder.tv_price.setTextColor(ContextCompat.getColor(context, R.color.colorCardprimary));
        } else {
            //如果为负则设置成红色
            holder.tv_price.setText(cardItem.getPrice());
            holder.tv_price.setTextColor(ContextCompat.getColor(context, R.color.relaxRed));
        }

        holder.tv_date.setText(cardItem.getDisplayDate());
        holder.tv_time.setText(cardItem.getTime());

        //没有标题的条目，把说明显示在标题上
        if (cardItem.getSystem().equals("")) {
            holder.tv_system.setText(cardItem.getType());
            holder.tv_type.setText("");
            holder.tv_type.setVisibility(View.GONE);
        } else {
            holder.tv_type.setText(cardItem.getType());
            holder.tv_system.setText(cardItem.getSystem());
            holder.tv_type.setVisibility(View.VISIBLE);
        }
        holder.tv_left.setText(cardItem.getLeft());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        //消费时间
        TextView tv_date;
        //消费时间
        TextView tv_time;
        //消费数目
        TextView tv_price;
        //消费种类
        TextView tv_type;
        //扣费系统（消费地点
        TextView tv_system;
        //消费后余额
        TextView tv_left;

        public CardViewHolder(View itemView) {
            super(itemView);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_price = (TextView) itemView.findViewById(R.id.tv_price);
            tv_type = (TextView) itemView.findViewById(R.id.tv_type);
            tv_system = (TextView) itemView.findViewById(R.id.tv_system);
            tv_left = (TextView) itemView.findViewById(R.id.tv_left);
            tv_time = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
