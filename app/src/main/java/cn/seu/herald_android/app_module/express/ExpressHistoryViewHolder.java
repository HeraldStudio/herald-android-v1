package cn.seu.herald_android.app_module.express;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import cn.seu.herald_android.R;

/**
 * Created by corvo on 8/6/16.
 */
public class ExpressHistoryViewHolder extends RecyclerView.ViewHolder {

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
