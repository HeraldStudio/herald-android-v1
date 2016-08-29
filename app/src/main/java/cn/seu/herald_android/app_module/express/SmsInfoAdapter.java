package cn.seu.herald_android.app_module.express;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.StaticViewHolder;

public class SmsInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
        return Math.max(smsList.size(), 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (smsList.size() == 0) {
            return new StaticViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.custom__view_empty_tip, parent, false));
        }

        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.mod_que_express__sms_select_item, parent, false);

        return new SmsInfoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder _holder, int position) {
        // 列表为空，显示一个无内容的提示
        if (smsList.size() == 0) {
            return;
        }

        // 列表非空，则必为SmsInfoViewHolder，直接强制转换
        SmsInfoViewHolder holder = (SmsInfoViewHolder) _holder;
        SmsInfo info = smsList.get(position);
        holder.smsBody.setText(info.getSmsbody());

        // 格式化时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d H:mm");
        String date = format.format(Long.valueOf(info.getDate()));
        holder.smsDate.setText(date);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.refreshSmsText(info.getSmsbody());
                handler.dismiss();
            }
        });
    }


    class SmsInfoViewHolder extends RecyclerView.ViewHolder {
        private final String TAG = "SmsInfoViewHolder";

        @BindView(R.id.express_txt_sms_date)
        public TextView smsDate;           // 短信日期

        @BindView(R.id.express_txt_sms_body)
        public TextView smsBody;           // 短信内容

        public SmsInfoViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
