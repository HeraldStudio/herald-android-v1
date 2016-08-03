package cn.seu.herald_android.app_module.express;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.seu.herald_android.R;

/**
 * Created by corvo on 8/2/16.
 */
public class SmsInfoViewHolder extends RecyclerView.ViewHolder {
    private static String TAG = "SmsInfoViewHolder";

    public TextView smsDate;           // 短信日期
    public Button smsBody;           // 短信内容
    public RadioButton smsSelect;      // 短信选择按钮

    public SmsInfoViewHolder(View v) {
        super(v);
        Log.d(TAG, "In Create");
        smsDate = (TextView)v.findViewById(R.id.express_txt_sms_date);
        smsBody = (Button) v.findViewById(R.id.express_txt_sms_body);
//        smsSelect = (RadioButton)v.findViewById(R.id.express_button_radio_select);
    }
}
