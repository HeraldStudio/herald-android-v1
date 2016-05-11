package cn.seu.herald_android.custom.refreshrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.zip.Inflater;

/**
 * Created by heyon on 2016/5/10.
 */
public class RefreshRecyclerView extends RecyclerView {


    onFooterListener onFooterListener = null;
    int lastLoadIndex = 0;
    RecyclerView.OnScrollListener scrollListener = new OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(onFooterListener!=null){
                LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager){
                    //获得最后可见的一项的位置
                    int index = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    if(recyclerView.getAdapter().getItemViewType(index) == RefreshRecyclerAdapter.FOOTER && index != lastLoadIndex){
                        //如果是Footer则运行响应函数,同一位置只会运行一次
                        lastLoadIndex = index;
                        onFooterListener.footerListener(index);
                    }
                }
            }
        }
    };

    public RefreshRecyclerView(Context context) {
        super(context);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setOnFooterListener(onFooterListener onFooterListener){
        this.onFooterListener = onFooterListener;
        this.addOnScrollListener(scrollListener);
    }





    public static abstract class RefreshRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        final static int HEADER = 0;
        final static int ITEM = 1;
        final static int FOOTER = 2;
        FooterViewHolder footerViewHolder = null;
        HeaderViewHolder headerViewHolder = null;
        public Context context;
        public Context getContext(){
            return context;
        }

        public RefreshRecyclerAdapter(android.content.Context context) {
            this.context = context;
            headerViewHolder = onCreateHeaderViewHolder();
            footerViewHolder = onCreateFooterViewHolder();

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HEADER){
                return headerViewHolder;
            }else if(viewType == FOOTER){
                return footerViewHolder;
            }else{
                return onCreateItemViewHolder(parent,viewType);
            }
        }

        public abstract RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);
        public abstract HeaderViewHolder onCreateHeaderViewHolder();
        public abstract FooterViewHolder onCreateFooterViewHolder();

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)){
                case HEADER:
                    onBindHeaderViewHolder(holder,position);
                    break;
                case FOOTER:
                    onBindFooterViewHolder(holder,position);
                    break;
                default:
                    if( headerViewHolder!= null)position--;
                    onBindItemViewHolder(holder,position);
            }
        }

        public abstract void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position);
        public abstract void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position);
        public abstract void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position);

        @Override
        public int getItemCount() {
            int count = getItemTotalCount();
            if(headerViewHolder != null)count++;
            if(footerViewHolder != null)count++;
            return count;
        }

        public abstract int getItemTotalCount();

        @Override
        public int getItemViewType(int position) {
            if(position == 0 && headerViewHolder!= null)
                return HEADER;
            if(position == getItemCount() -1  && footerViewHolder != null){
                return FOOTER;
            }
            return ITEM;
        }

        public static class FooterViewHolder extends RecyclerView.ViewHolder{
            View itemView;
            public FooterViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
            }

            public View getItemView() {
                return itemView;
            }
        }

        public static class HeaderViewHolder extends RecyclerView.ViewHolder{
            View itemView;
            public HeaderViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
            }

            public View getItemView() {
                return itemView;
            }
        }
    }
}
