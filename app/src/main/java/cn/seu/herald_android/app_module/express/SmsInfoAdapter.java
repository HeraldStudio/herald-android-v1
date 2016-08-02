package cn.seu.herald_android.app_module.express;

import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by corvo on 8/2/16.
 */
public class SmsInfoAdapter extends RecyclerView.Adapter<SmsInfoViewHolder> {

    private static String TAG = "SmsInfoAdapter";
    private List<SmsInfo> smsList;
    public SmsInfoAdapter(List<SmsInfo> smsList) {
        this.smsList = smsList;
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    @Override
    public SmsInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "OnCreateViewHolder");
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.mod_que_express__sms_select_item, parent, false);

        return new SmsInfoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SmsInfoViewHolder holder, int position) {
        SmsInfo info = smsList.get(position);
        holder.smsBody.setText(info.getSmsbody());
        holder.smsDate.setText(info.getDate());
    }
}
