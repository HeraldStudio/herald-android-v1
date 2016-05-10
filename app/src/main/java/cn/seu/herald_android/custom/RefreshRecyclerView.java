package cn.seu.herald_android.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.os.TraceCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.mod_afterschool.AfterSchoolActivityItem;

/**
 * Created by heyon on 2016/5/10.
 */
public class RefreshRecyclerView extends RecyclerView{

    public RefreshRecyclerView(Context context) {
        super(context);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder{
        View footerView;
        public FooterViewHolder(View itemView) {
            super(itemView);
            footerView = itemView;
        }

    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{
        View headerView;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerView = itemView;
        }
    }

    public static abstract class Adapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH>{
        final static int HEADER = 0;
        final static int ITEM = 1;
        final static int FOOTER = 2;
        FooterViewHolder footerViewHolder = null;
        HeaderViewHolder headerViewHolder = null;
        Context context;
        //表项的list
        ArrayList<Object> list;
        public Context getContext(){
            return context;
        }

        public void setFooterView(View view){
            footerViewHolder = new FooterViewHolder(view);
        }

        public void setHeadView(View view){
            headerViewHolder = new HeaderViewHolder(view);
        }

        public  Adapter(android.content.Context context, ArrayList<Object> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HEADER){
                return onCreateHeaderViewHolder(parent,viewType);
            }else if(viewType == FOOTER){
                return onCreateFooterViewHolder(parent,viewType);
            }else
                return onCreateItemViewHolder(parent,viewType);
        }

        public abstract VH onCreateHeaderViewHolder(ViewGroup parent, int viewType);


        public abstract VH onCreateItemViewHolder(ViewGroup parent, int viewType);


        public abstract VH onCreateFooterViewHolder(ViewGroup parent, int viewType);

        @Override
        public void onBindViewHolder(VH holder, int position) {
            switch (getItemViewType(position)){
                case HEADER:
                    onBindHeaderViewHolder(headerViewHolder);
                    break;
                case FOOTER:
                    onBindFooterViewHolder(footerViewHolder);
                    break;
                default:
                    onBindItemViewHolder(holder,position);
            }
        }

        public abstract void onBindItemViewHolder(VH holder, int position);
        public abstract void onBindHeaderViewHolder(HeaderViewHolder holder);
        public abstract void onBindFooterViewHolder(FooterViewHolder holder);

        @Override
        public int getItemCount() {
            if(list == null)
                return 0;
            int count = list.size();
            if(headerViewHolder != null)count++;
            if(footerViewHolder != null)count++;
            return count;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0 && headerViewHolder != null)
                return HEADER;
            if(position == getItemCount() && footerViewHolder != null){
                return FOOTER;
            }
            return ITEM;
        }

    }
}
