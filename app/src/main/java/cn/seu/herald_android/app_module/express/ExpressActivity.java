package cn.seu.herald_android.app_module.express;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import okhttp3.Call;

/**
 * Created by corvo on 7/28/16.
 */
public class ExpressActivity extends BaseActivity
        implements View.OnClickListener {
    private static String TAG = "ExpressActivity";

    private Spinner mDestSpinner;       // 取回地点
    private Spinner mArrivalSpinner;    // 取回时间
    private Spinner mWeightSpinner;     // 快件重量
    private Spinner mLocateSpinner;     // 快件所在地

    private Button mSmsSelect;          // 短信选择
    private Button mSubmit;             // 提交请求

    private TextView mSmsShow;          // 短信框

    private EditText mUsername;         // 姓名编辑框
    private EditText mPhone;            // 手机号码编辑框

    String[] mDestArray;// = getResources().getStringArray(R.array.mod_express_dest);
    String[] mLocateArray;// = getResources().getStringArray(R.array.mod_express_locate);
    String[] mArrivalArray;// = getResources().getStringArray(R.array.mod_express_arrival);
    String[] mWeightArray;// = getResources().getStringArray(R.array.mod_express_weight);

    /**
     * 调用此接口改变短信文本内容, 定义在SmsSelectDialog中
     */
    private SmsSelectDialog.DialogRefresh mSmsRefresh = new SmsSelectDialog.DialogRefresh() {
        @Override
        public void refreshSmsText(String text) {
            mSmsShow.setText(text);
        }
    };

    private ExpressDatabaseContent mDBContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express);
        mDestSpinner = (Spinner)findViewById(R.id.express_spinner_dest);
        mArrivalSpinner = (Spinner) findViewById(R.id.express_spinner_arrival);
        mLocateSpinner = (Spinner) findViewById(R.id.express_spinner_locate);
        mWeightSpinner = (Spinner) findViewById(R.id.express_spinner_weight);
        mUsername = (EditText) findViewById(R.id.express_edit_username);
        mPhone = (EditText) findViewById(R.id.express_edit_phone);


        mSmsSelect = (Button) findViewById(R.id.express_button_sms_select);
        mSubmit = (Button) findViewById(R.id.express_button_submit);
        mSmsShow = (TextView) findViewById(R.id.express_txt_sms);

        mSmsSelect.setOnClickListener(this);
        mSubmit.setOnClickListener(this);

        mDBContent = new ExpressDatabaseContent(this);
    }

    @Override
    protected void onResume() {
        initSpinner();
        super.onResume();
    }

    /**
     * 初始化四个选择框
     */
    private void initSpinner() {
        mDestArray = getResources().getStringArray(R.array.mod_express_dest);
        mLocateArray = getResources().getStringArray(R.array.mod_express_locate);
        mArrivalArray = getResources().getStringArray(R.array.mod_express_arrival);
        mWeightArray = getResources().getStringArray(R.array.mod_express_weight);

        Log.d(TAG, "initSpinner");
        mDestSpinner.setAdapter(new ArrayAdapter<String>(
                this, R.layout.mod_que_express__spin_item, mDestArray
        ));

        mArrivalSpinner.setAdapter(new ArrayAdapter<String>(
                this, R.layout.mod_que_express__spin_item, mArrivalArray
        ));

        mLocateSpinner.setAdapter(new ArrayAdapter<String>(
                this, R.layout.mod_que_express__spin_item, mLocateArray
        ));

        mWeightSpinner.setAdapter(new ArrayAdapter<String>(
                this, R.layout.mod_que_express__spin_item, mWeightArray
        ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_express, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.express_button_history) {
            onShowHisory();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Log.d("ExpressActivity", String.valueOf(v.getId()));
        switch (v.getId()) {
            case R.id.express_button_submit:
                Log.d(TAG, "Click Submit");
                onSubmit();
                break;
            case R.id.express_button_sms_select:
                Log.d(TAG, "Click Sms Select");
                onSmsSelect();
                break;
        }
    }



    /**
     * 选择短信内容
     */
    private void onSmsSelect() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("listener", mSmsRefresh);

        SmsSelectDialog dialog = SmsSelectDialog.newInstance(bundle, this);
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        dialog.show(ft, null);
    }

    /**
     *  向服务端提交请求并插入数据库
     */
    private void onSubmit() {
        Log.d(TAG, mSmsShow.getText().toString());
        ExpressInfo info = new ExpressInfo();
        info.setUsername(mUsername.getText().toString());
        info.setUserphone(mPhone.getText().toString());
        info.setSmsInfo(mSmsShow.getText().toString());
        info.setDest(mDestArray[mDestSpinner.getSelectedItemPosition()]);
        info.setArrival(mArrivalArray[mArrivalSpinner.getSelectedItemPosition()]);
        info.setLocate(mLocateArray[mLocateSpinner.getSelectedItemPosition()]);
        info.setWeight(mWeightArray[mWeightSpinner.getSelectedItemPosition()]);
        //info.setSubmitTime(new Date().getTime());
        info.setFetched(false);

        makeSubmit(info);
    }

    private void onShowHisory() {
        Log.d(TAG, "show history");
        Intent intent = new Intent(this, ExpressHistoryActivity.class);
        startActivity(intent);
    }

    /**
     * 提交请求后将数据保存在数据库中
     */
    private void dbInsert(ExpressInfo info) {
        Log.d(TAG, "Database Insert");
        mDBContent.dbInsert(info);
    }

    /**
     * 向服务器端提交请求
     * example
     *  {
     *      "content": {
     *          "sub_time": "1470620033"
     *      },
     *      "code": 200
     *  }
     */
    private void makeSubmit(ExpressInfo info) {
        String submit = "http://192.168.1.105:8080/kuaidi/submit";
        // post之后会返回时间戳, 之后再保存数据库
        OkHttpUtils.post()
                .url(submit)
                .addParams("user_name", info.getUsername())
                .addParams("user_phone", info.getUserphone())
                .addParams("cardnumber", "一卡通号码")
                .addParams("sms_txt", info.getSmsInfo())
                .addParams("dest", info.getDest())
                .addParams("arrival", info.getArrival())
                .addParams("locate", info.getLocate())
                .addParams("weight", info.getWeight())
                .build()
                .connTimeOut(20000)
                .readTimeOut(20000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        Log.d(TAG, String.valueOf(new Date().getTime()));
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.getInt("code") == 200) {
                                JSONObject content = res.getJSONObject("content");
                                info.setSubmitTime(1000 * content.getLong("sub_time")); // python 与 java 时间戳转换 * 1000
                                dbInsert(info);     // 存入数据库
                                showSnackBar("提交成功");
                            } else {
                                showSnackBar(res.getString("content"));
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
