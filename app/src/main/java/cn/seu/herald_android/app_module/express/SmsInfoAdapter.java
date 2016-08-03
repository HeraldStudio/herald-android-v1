package cn.seu.herald_android.app_module.express;

import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by corvo on 8/2/16.
 */
public class SmsInfoAdapter extends RecyclerView.Adapter<SmsInfoViewHolder> {

    private static String TAG = "SmsInfoAdapter";
    private List<SmsInfo> smsList;

    private SmsSelectDialog handler;        // 主动调用dismiss 触发onDismiss
    private SmsSelectDialog.DialogRefresh listener;

    /**
     *
     * @param smsList 包含短信的数组
     * @param context   dialog, 在此类中会调用dismiss
     * @param listener  作为接口(在Activity中实现一个该对象, 一直传递到次), 监听选择的短信, 并进行刷新.
     */
    public SmsInfoAdapter(List<SmsInfo> smsList,
                          SmsSelectDialog context,
                          SmsSelectDialog.DialogRefresh listener
                        ) {
        this.smsList = smsList;
        this.listener = listener;
        this.handler = context;
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

        holder.smsBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click" + info.getSmsbody());
                listener.refreshSmsText(info.getSmsbody());
                handler.dismiss();
            }
        });
    }
}
