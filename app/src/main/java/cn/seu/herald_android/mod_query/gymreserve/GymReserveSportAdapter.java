package cn.seu.herald_android.mod_query.gymreserve;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class GymReserveSportAdapter extends RecyclerView.Adapter<GymReserveSportAdapter.GymReserveHolder> {
    Context context;
    ArrayList<GymSportModel> list;
    OnItemClickListener mListener = null;

    public GymReserveSportAdapter(Context context, ArrayList<GymSportModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public GymReserveHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.mod_que_gymreserve__cell, null);
        return new GymReserveHolder(view);
    }

    @Override
    public void onBindViewHolder(GymReserveHolder holder, int position) {
        GymSportModel item = list.get(position);
        //根据运动名设置图标
        Picasso.with(this.context).load(item.ic_maps.get(item.name)).into(holder.img_sport);
        holder.tv_name.setText(item.name);
        holder.rootView.setOnClickListener(v -> {
            if(mListener!=null)mListener.onItemClick(holder.rootView,item);
        });
    }

    public interface OnItemClickListener {
        void onItemClick(View view, GymSportModel item);
    }

    public void setItemClickListener(OnItemClickListener mListener){
        this.mListener = mListener;
    }

    public static class GymReserveHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.tv_itemname)
        TextView tv_name;
        @BindView(R.id.img_sportitem)
        ImageView img_sport;
        View rootView;
        public GymReserveHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, rootView = itemView);
        }
    }
}
