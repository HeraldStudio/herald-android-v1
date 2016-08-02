package cn.seu.herald_android.app_module.express;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;

/**
 * Created by corvo on 7/28/16.
 */
public class ExpressActivity extends BaseActivity
        implements AdapterView.OnItemSelectedListener,
        View.OnClickListener {
    private static String TAG = "ExpressActivity";

    private Spinner mSpiner;
    private Button mSmsSelect;      // 短信选择
    private Button mSubmit;         // 提交请求

    private TextView mSmsShow;      // 短信框

    ArrayList<String> data = new ArrayList<String>(Arrays.asList("梅九", "桃三四"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express);
        mSpiner = (Spinner)findViewById(R.id.express_spinner_dest);

        mSmsSelect = (Button) findViewById(R.id.express_button_sms_select);
        mSubmit = (Button) findViewById(R.id.express_button_confirm);
        mSmsShow = (TextView) findViewById(R.id.express_txt_sms);

        mSmsSelect.setOnClickListener(this);
        mSubmit.setOnClickListener(this);

        ArrayAdapter<String> dataAdapter =
                new ArrayAdapter<String>(this, R.layout.mod_que_express__dest_item, data);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpiner.setAdapter(dataAdapter);
        mSpiner.setOnItemSelectedListener(this);

        Uri uri = Uri.parse("content://sms/inbox");
        SmsContent smsContent = new SmsContent(this, uri);
        List<SmsInfo> list = smsContent.getInfos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Log.d("ExpressActivity", String.valueOf(view.getId()));
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.express_spinner_dest) {
            Log.d("ExpressActivity", data.get(position));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        Log.d("ExpressActivity", String.valueOf(v.getId()));
        switch (v.getId()) {
            case R.id.express_button_confirm:
                Log.d(TAG, "Click Submit");
                Log.d(TAG, mSmsShow.getText().toString());
                break;
            case R.id.express_button_sms_select:
                Log.d(TAG, "Click Sms Select");
                onSmsSelect();
                break;
        }
    }


    /**
     * 调用此接口改变短信文本内容, 定义在SmsSelectDialog中
     */
    private SmsSelectDialog.DialogRefresh smsRefresh = new SmsSelectDialog.DialogRefresh() {
        @Override
        public void refreshSmsText(String text) {
            mSmsShow.setText(text);
        }
    };

    // 选择短信内容
    private void onSmsSelect() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("listener", smsRefresh);

        SmsSelectDialog dialog = SmsSelectDialog.newInstance(bundle, this);
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        dialog.show(ft, null);

    }

}
