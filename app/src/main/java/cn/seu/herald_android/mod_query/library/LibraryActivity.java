package cn.seu.herald_android.mod_query.library;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

public class LibraryActivity extends BaseAppCompatActivity {

    private ListView listView_hotbook;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        init();
    }

    private void init() {
        //设置toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorLibraryprimary));
        enableSwipeBack();



        //加载最热门图书，初始化列表控件
        listView_hotbook = (ListView)findViewById(R.id.list_library_hotbook);

        //获取最热图书信息
        refreshRemoteHotBook();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_library, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_library_search){
            startActivity(new Intent(this,LibrarySearchActivity.class));
        }else if(id ==  R.id.action_library_mybook){
            //显示已借书的对话框
            refreshRemoteBorrowRocord();
        }
        return super.onOptionsItemSelected(item);
    }


    private void refreshRemoteHotBook() {
        //加载校内最热门图书列表
        showProgressDialog();
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_LIBRARY_HOTBOOK))
                .addParams("uuid", getApiHelper().getUUID())
                .build()
                .readTimeOut(10000).connTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        hideProgressDialog();
                        getApiHelper().dealApiException(e);
                    }
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        try{
                            JSONObject json_res = new JSONObject((response));
                            if(json_res.getInt("code") == 200){
                                JSONArray jsonArray = json_res.getJSONArray("content");
                                loadHotBookList(HotBook.transformJSONArrayToArrayList(jsonArray));
                                showMsg("刷新成功");
                            } else {
                                showMsg("服务器遇到了一些问题，不妨稍后再试试");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showMsg("数据解析失败，请重试");
                        }
                    }
                });
    }

    private void loadHotBookList(ArrayList<HotBook> list){
        listView_hotbook.setAdapter(new HotBookAdapter(this, R.layout.listviewitem_library_hotbook, list));
        //设置高度自适应
        HotBookAdapter.setHeightWithContent(listView_hotbook);
    }

    private void refreshRemoteBorrowRocord() {
        //获取最新的已借书记录
        showProgressDialog();
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_LIBRARY_MYBOOK))
                .addParams("uuid",getApiHelper().getUUID())
                .build()
                .readTimeOut(10000).connTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        hideProgressDialog();
                        getApiHelper().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        try{
                            JSONObject json_res = new JSONObject((response));
                            if(json_res.getInt("code") == 200){
                                //状态码为200说明获取成功开始加载列表
                                JSONArray jsonArray = json_res.getJSONArray("content");
                                if(jsonArray.length()==0){
                                    //如果列表为空则说明没有借过书
                                    showMsg("目前尚无在借图书");
                                } else {
                                    //反之打开借书记录对话框
                                    displayBorrowRecordDialog(MyBorrowBook.transfromJSONArrayToArrayList(jsonArray));
                                }

                            }else if(json_res.getInt("code") == 401){
                                //如果为401说明未绑定图书馆账号或者已经失效
                                displayLibraryAuthDialog();
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            showMsg("数据解析失败，不妨刷新试试~");
                        }
                    }
                });
    }

    private void displayBorrowRecordDialog(ArrayList<MyBorrowBook> list) {
        //显示已借书记录的对话框,加载list里的项
        //加载借阅记录对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog_borrowed_book_records = builder.create();
        //show函数需要在getWindow前调用
        dialog_borrowed_book_records.show();
        //对话框窗口设置布局文件
        Window window = dialog_borrowed_book_records.getWindow();
        window.setContentView(R.layout.content_dialog_borrowbook_record);

        //获取对话窗口中的ListView
        ListView list_record = (ListView) window.findViewById(R.id.list_borrowbook_record);
        //设置适配器
        list_record.setAdapter(new MyBorrowBookAdapter(this,R.layout.listviewitem_library_borrowbook,list));

    }

    private void displayLibraryAuthDialog() {
        //显示图书馆账号需要绑定的对话框
        final EditText et_pwd = new EditText(this);
        et_pwd.setHint("图书馆密码(默认为一卡通)");
        et_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //设置对话框布局
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(et_pwd);
        new AlertDialog.Builder(this).setTitle("登陆图书馆").setMessage("你没有绑定图书馆账号或绑定失效，" +
                "请输入图书馆密码").setView(linearLayout)
                .setPositiveButton("确定", (arg0, arg1) -> {
                    String pwd = et_pwd.getText().toString();
                    //发送更新请求
                    updateAuthInfo(pwd);
                })
                .setNegativeButton("取消", null).show();
    }


    /**
     * @param password  图书馆密码
     */
    private void updateAuthInfo(String password) {
        //用于更新图书馆的账号和密码
        showProgressDialog();
        OkHttpUtils.post()
                .url(ApiHelper.auth_update_url)
                .addParams("cardnum",getApiHelper().getUserName())
                .addParams("password",getApiHelper().getPassword())
                .addParams("lib_username", getApiHelper().getUserName())
                .addParams("lib_password",password)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        hideProgressDialog();
                        getApiHelper().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();
                        if(response.equals("OK")){
                            //返回OK说明认证成功
                            refreshRemoteBorrowRocord();
                        }else{
                            showMsg("信息绑定失败，请重新再试。如多次失败请尝试注销登录,或者联系管理员");
                        }
                    }
                });
    }
}