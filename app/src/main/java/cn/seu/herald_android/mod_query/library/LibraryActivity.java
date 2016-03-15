package cn.seu.herald_android.mod_query.library;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

public class LibraryActivity extends BaseAppCompatActivity {


    ListView listView_hotbook;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        init();
    }

    public void init(){
        //设置toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

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


    public void refreshRemoteHotBook(){
        //加载校内最热门图书列表
        getProgressDialog().show();
        getProgressDialog().setCancelable(false);
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_LIBRARY_HOTBOOK))
                .addParams("uuid",getApiHelper().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getProgressDialog().dismiss();
                        getApiHelper().dealApiException(e);
                    }
                    @Override
                    public void onResponse(String response) {
                        getProgressDialog().dismiss();
                        try{
                            JSONObject json_res = new JSONObject((response));
                            if(json_res.getInt("code") == 200){
                                JSONArray jsonArray = json_res.getJSONArray("content");
                                loadHotBookList(HotBook.transformJSONArrayToArrayList(jsonArray));

                            }else{
                                showMsg("获取最热门图书失败，也许是服务器先生罢工了，不妨稍后重新试试？");
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            showMsg("数据解析失败，不妨刷新试试~");
                        }
                    }
                });
    }

    private void loadHotBookList(ArrayList<HotBook> list){
        listView_hotbook.setAdapter(new HotBookAdapter(this,R.layout.listviewitem_library_hotbook,list));
        //设置高度自适应
        HotBookAdapter.setHeightWithContent(listView_hotbook);
    }

    public void refreshRemoteBorrowRocord(){
        //获取最新的已借书记录
        getProgressDialog().show();
        getProgressDialog().setCancelable(false);
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_LIBRARY_MYBOOK))
                .addParams("uuid",getApiHelper().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getProgressDialog().dismiss();
                        getApiHelper().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        getProgressDialog().dismiss();
                        try{
                            JSONObject json_res = new JSONObject((response));
                            if(json_res.getInt("code") == 200){
                                //状态码为200说明获取成功开始加载列表
                                JSONArray jsonArray = json_res.getJSONArray("content");
                                if(jsonArray.length()==0){
                                    //如果列表为空则说明没有借过书
                                    showMsg("目前尚未在借图书");
                                }else {
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

    public void displayBorrowRecordDialog(ArrayList<MyBorrowBook> list){
        //显示已借书记录的对话框,加载list里的项
        //加载借阅记录对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog_borrowbook_records = builder.create();
        //show函数需要在getWindow前调用
        dialog_borrowbook_records.show();
        //对话框窗口设置布局文件
        Window window = dialog_borrowbook_records.getWindow();
        window.setContentView(R.layout.content_dialog_borrowbook_record);

        //获取对话窗口中的listview
        ListView list_record = (ListView) window.findViewById(R.id.list_borrowbook_record);
        //设置适配器
        list_record.setAdapter(new MyBorrowBookAdapter(this,R.layout.listviewitem_library_borrowbook,list));

    }

    public void displayLibraryAuthDialog(){
        //显示图书馆账号需要绑定的对话框
        final EditText et_user = new EditText(this);
        final EditText et_pwd = new EditText(this);
        et_user.setHint("图书馆账号(一卡通)");
        et_pwd.setHint("图书馆密码(默认为一卡通)");
        et_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //设置对话框布局
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(et_user);
        linearLayout.addView(et_pwd);
        new AlertDialog.Builder(this).setTitle("图书馆账号绑定").setMessage("图书馆账号信息有误，或者已经失效，请重新绑定").setView(linearLayout)
                .setPositiveButton("确定", (arg0, arg1) -> {
                    String user = et_user.getText().toString();
                    String psd = et_user.getText().toString();
                    //发送更新请求
                    updateAuthInfo(user,psd);
                }).setNegativeButton("取消", null).show();
    }


    /**
     *
     * @param user      图书馆账号
     * @param password  图书馆密码
     */
    public void updateAuthInfo(String user,String password){
        //用于更新图书馆的账号和密码
        getProgressDialog().show();
        OkHttpUtils.post()
                .url(ApiHelper.auth_update_url)
                .addParams("cardnum",getApiHelper().getUserName())
                .addParams("password",getApiHelper().getPassword())
                .addParams("lib_username",user)
                .addParams("lib_password",password)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getProgressDialog().dismiss();
                        getApiHelper().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        getProgressDialog().dismiss();
                        if(response.equals("OK")){
                            //返回OK说明认证成功
                            showMsg("图书馆账号信息已经更新啦，赶紧再试试吧");
                        }else{
                            showMsg("信息绑定失败，请重新再试。如多次失败请尝试注销登录,或者联系管理员");
                        }
                    }
                });
    }
}
