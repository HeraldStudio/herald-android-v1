package cn.seu.herald_android.app_module.express;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.seu.herald_android.R;

import static cn.seu.herald_android.helper.LogUtils.makeLogTag;

/**
 * Created by corvo on 8/6/16.
 */
public class ExpressHistoryAdapter extends RecyclerView.Adapter<ExpressHistoryAdapter.ExpressHistoryViewHolder>{
    private static String TAG = makeLogTag(ExpressHistoryAdapter.class);

    private List<ExpressInfo> expressInfoList;
    private ExpressHistoryActivity.OnDelete onDelete;
    private Context context;

    public ExpressHistoryAdapter(List<ExpressInfo> infoList) {
        this.expressInfoList = infoList;
    }

    public ExpressHistoryAdapter(List<ExpressInfo> infoList, ExpressHistoryActivity.OnDelete onDelete, Context context) {
        this.expressInfoList = infoList;
        this.onDelete = onDelete;
        this.context = context;

    }
    @Override
    public int getItemCount() {
        return expressInfoList.size();
    }

    @Override
    public ExpressHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.mod_que_express__history_item, parent, false);

        return new ExpressHistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ExpressHistoryViewHolder holder, int position) {
        ExpressInfo info = expressInfoList.get(position);

        holder.arrivalRecord.setText(info.getArrival());
        holder.locateRecord.setText(info.getLocate());
        holder.smsRecord.setText(info.getSmsInfo());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.submitRecord.setText(format.format(info.getSubmitTime()));
        holder.isFetchedRecord.setText(info.isReceived()? "已接单":"未接单");

        if (info.isReceived()) {
            holder.containerRecord.setClickable(false);
            holder.containerRecord.setOnClickListener(null);
        } else {    // 若快递被接单
            holder.containerRecord.setClickable(true);
            holder.containerRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteItem(position);
                }
            });
        }
    }

    public void deleteItem(int position) {
        new AlertDialog.Builder(context).setTitle("删除订单")
                .setMessage("删除记录将取消该订单，是否确认删除？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除订单", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExpressInfo info = expressInfoList.get(position);
                        onDelete.deleteItem(info);
                    }
                }).show();
    }


    class ExpressHistoryViewHolder extends RecyclerView.ViewHolder {

        public TextView submitRecord;
        public TextView isFetchedRecord;
        public TextView smsRecord;
        public TextView locateRecord;
        public TextView arrivalRecord;

        public CardView containerRecord;

        public ExpressHistoryViewHolder(View v) {
            super(v);
            submitRecord = (TextView)v.findViewById(R.id.express_txt_submit_time_record);
            isFetchedRecord = (TextView)v.findViewById(R.id.express_txt_is_fetched_record);
            smsRecord = (TextView) v.findViewById(R.id.express_txt_sms_record);
            locateRecord = (TextView) v.findViewById(R.id.express_txt_locate_record);
            arrivalRecord = (TextView) v.findViewById(R.id.express_txt_arrival_record);

            containerRecord = (CardView) v.findViewById(R.id.express_card_history_record);
        }
    }
}
