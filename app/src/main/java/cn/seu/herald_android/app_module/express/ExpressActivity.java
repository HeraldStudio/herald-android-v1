package cn.seu.herald_android.app_module.express;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_secondary.WebModuleActivity;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

public class ExpressActivity extends BaseActivity {
    final private int REQUEST_CODE_ASK_READ_SMS = 123;

    @BindView(R.id.express_spinner_dest)
    Spinner mDestSpinner;       // 取回地点

    @BindView(R.id.express_spinner_arrival)
    Spinner mArrivalSpinner;    // 取回时间

    @BindView(R.id.express_spinner_weight)
    Spinner mWeightSpinner;     // 快件重量

    @BindView(R.id.express_spinner_locate)
    Spinner mLocateSpinner;     // 快件所在地

    @BindView(R.id.express_txt_sms)
    TextView mSmsShow;          // 短信框

    @BindView(R.id.express_edit_username)
    EditText mUsername;         // 姓名编辑框

    @BindView(R.id.express_edit_phone)
    EditText mPhone;            // 手机号码编辑框

    @BindView(R.id.express_button_submit)
    Button mSubmit;         // 同意条款

    @BindArray(R.array.mod_express_dest)
    String[] mDestArray;// = getResources().getStringArray(R.array.mod_express_dest);

    @BindArray(R.array.mod_express_locate)
    String[] mLocateArray;// = getResources().getStringArray(R.array.mod_express_locate);

    @BindArray(R.array.mod_express_arrival)
    String[] mArrivalArray;// = getResources().getStringArray(R.array.mod_express_arrival);

    @BindArray(R.array.mod_express_weight)
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
        ButterKnife.bind(this);

        mUsername.setText(Cache.expressUserName.getValue());

        mPhone.setText(Cache.expressUserPhone.getValue());

        mDBContent = new ExpressDatabaseContent(this);
    }

    @Override
    protected void onResume() {
        initSpinner();
        super.onResume();
    }

    @OnCheckedChanged(R.id.checkbox)
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mSubmit.setEnabled(isChecked);
    }

    /**
     * 初始化四个选择框
     */
    private void initSpinner() {
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

    /**
     * 选择短信内容
     */
    @OnClick(R.id.express_button_sms_select)
    void onSmsSelect() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("listener", mSmsRefresh);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_SMS},
                    REQUEST_CODE_ASK_READ_SMS);
        } else {
            SmsSelectDialog dialog = SmsSelectDialog.newInstance(bundle, this);
            FragmentTransaction ft = this.getFragmentManager().beginTransaction();
            dialog.show(ft, null);
        }
    }

    @OnClick(R.id.express_txt_terms)
    void onClickTerms() {
        WebModuleActivity.startWebModuleActivity("快递代取",
                "http://app.heraldstudio.com/kuaidi_terms.htm", R.style.ExpressActivityTheme);
    }

    /**
     * 向服务端提交请求并插入数据库
     */
    @OnClick(R.id.express_button_submit)
    void onSubmit() {
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
        AppContext.startActivitySafely(ExpressHistoryActivity.class);
    }

    /**
     * 提交请求后将数据保存在数据库中
     */
    private void dbInsert(ExpressInfo info) {
        mDBContent.dbInsert(info);
    }

    /**
     * 向服务器端提交请求
     * example
     * {
     * "content": {
     * "sub_time": "1470620033"
     * },
     * "code": 200
     * }
     */
    private void makeSubmit(ExpressInfo info) {
        String submit = "http://app.heraldstudio.com/kuaidi/submit";
        // post之后会返回时间戳, 之后再保存数据库

        if (info.getSmsInfo().isEmpty()) {
            showSnackBar("请选择短信内容");
            return;
        }

        if (info.getUsername().isEmpty() || info.getUserphone().isEmpty()) {
            showSnackBar("请将信息填写完整");
            return;
        }

        Cache.expressUserName.setValue(info.getUsername());

        Cache.expressUserPhone.setValue(info.getUserphone());

        showProgressDialog();
        new ApiSimpleRequest(Method.POST)
                .url(submit)
                .addUuid()
                .post("user_name", info.getUsername())
                .post("user_phone", info.getUserphone())
                .post("sms_txt", info.getSmsInfo())
                .post("dest", info.getDest())
                .post("arrival", info.getArrival())
                .post("locate", info.getLocate())
                .post("weight", info.getWeight())
                .onResponse(((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        JObj res = new JObj(response);
                        if (res.$i("code") == 200) {
                            JObj content = res.$o("content");
                            info.setSubmitTime(1000 * content.$l("sub_time")); // python 与 java 时间戳转换 * 1000
                            dbInsert(info);     // 存入数据库
                            new AlertDialog.Builder(this)
                                    .setMessage("尊敬的客户您好，已收到您的订单。请您于"
                                            + info.getArrival()
                                            + "内到"
                                            + info.getDest()
                                            + "取回您的快件，现场付款，支持支付宝和微信转账。"
                                            + "如有疑问，请按照“价格、条款和条件”中的联系方式进行咨询。")
                                    .setNegativeButton("确定", null).show();
                        } else {
                            String error = res.$s("content");
                            if (error.isEmpty()) {
                                error = "提交失败，未知错误";
                            }
                            showSnackBar(error);
                        }
                    } else {
                        showSnackBar("提交失败, 请重试");
                    }
                })).run();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_READ_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onSmsSelect();
            }
        } else {
            showSnackBar("获取短信权限失败");
        }
    }
}
