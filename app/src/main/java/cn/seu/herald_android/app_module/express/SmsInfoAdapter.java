package cn.seu.herald_android.app_module.express;

import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.seu.herald_android.R;

import static cn.seu.herald_android.helper.LogUtils.makeLogTag;
import static cn.seu.herald_android.helper.LogUtils.LOGD;

/**
 * Created by corvo on 8/2/16.
 */
public class SmsInfoAdapter extends RecyclerView.Adapter<SmsInfoAdapter.SmsInfoViewHolder> {

    private static String TAG = makeLogTag(SmsInfo.class);
    private List<SmsInfo> smsList;

    private SmsSelectDialog handler;        // dialog窗口句柄, 主动调用dismiss 触发onDismiss
    private SmsSelectDialog.DialogRefresh listener;

    /**
     *
     * @param smsList 包含短信的数组
     * @param context   dialog, 在此类中会调用dismiss
     * @param listener  作为接口(在Activity中实现一个该对象, 一直传递到此), 监听选择的短信, 并进行刷新.
     */
    public SmsInfoAdapter(List<SmsInfo> smsList,
                          SmsSelectDialog context,
                          SmsSelectDialog.DialogRefresh listener
                        ) {
        this.smsList = smsList;
        this.handler = context;
        this.listener = listener;
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

        // 格式化时间
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String date = format.format(Long.valueOf(info.getDate()));
        holder.smsDate.setText(date);

        holder.smsBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click" + info.getSmsbody());
                listener.refreshSmsText(info.getSmsbody());
                handler.dismiss();
            }
        });
    }


    class SmsInfoViewHolder extends RecyclerView.ViewHolder {
        private final String TAG = "SmsInfoViewHolder";

        public TextView smsDate;           // 短信日期
        public Button smsBody;           // 短信内容
        public RadioButton smsSelect;      // 短信选择按钮

        public SmsInfoViewHolder(View v) {
            super(v);
            Log.d(TAG, "In Create");
            smsDate = (TextView)v.findViewById(R.id.express_txt_sms_date);
            smsBody = (Button) v.findViewById(R.id.express_txt_sms_body);
        }
    }
}
