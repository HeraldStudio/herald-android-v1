package cn.seu.herald_android.app_module.library;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.User;

/**
 * 图书主页面Acvitity
 */
public class LibraryActivity extends BaseActivity {

    public static ApiSimpleRequest remoteRefreshNotifyDotState() {
        return new ApiSimpleRequest(Method.POST).api("library").addUuid()
                .toCache("herald_library_borrowbook",
                        /** notifyModuleIfChanged: */Module.library);
    }

    // 热门书籍展示列表
    @BindView(R.id.list_library_hotbook)
    ListView listView_hotbook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_library);
        ButterKnife.bind(this);

        // 取消滑动条
        if (listView_hotbook != null) {
            listView_hotbook.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        }

        // 获取最热图书信息
        refreshHotBook();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_library, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_library_search) {
            AppContext.startActivitySafely(LibrarySearchActivity.class);
        } else if (id == R.id.action_library_mybook) {
            // 显示已借书的对话框
            refreshBorrowRocord();
        }
        return super.onOptionsItemSelected(item);
    }


    private void refreshHotBook() {
        // 加载校内最热门图书列表
        showProgressDialog();
        new ApiSimpleRequest(Method.POST).api("library_hot").addUuid()
                .onResponse((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            JSONArray jsonArray = json_res.getJSONArray("content");
                            loadHotBookList(HotBookModel.transformJSONArrayToArrayList(jsonArray));
                            // showSnackBar("刷新成功");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackBar("解析失败，请刷新");
                        }
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    private void loadHotBookList(ArrayList<HotBookModel> list) {
        listView_hotbook.setAdapter(new HotBookAdapter(this, R.layout.mod_que_library__item, list));
    }

    public void refreshBorrowRocord() {
        // 获取最新的已借书记录
        showProgressDialog();
        new ApiSimpleRequest(Method.POST).api("library").addUuid().toCache("herald_library_borrowbook")
                .onResponse((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 401) {
                                displayLibraryAuthDialog();
                                return;
                            }

                            JSONArray jsonArray = json_res.getJSONArray("content");
                            if (jsonArray.length() == 0) {
                                // 如果列表为空则说明没有借过书
                                showSnackBar("目前尚无在借图书");
                            } else {
                                // 反之打开借书记录对话框
                                displayBorrowRecordDialog(BorrowBookModel.transformJSONArrayToArrayList(jsonArray));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackBar("解析失败，请刷新");
                        }
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    private void displayBorrowRecordDialog(ArrayList<BorrowBookModel> list) {
        // 显示已借书记录的对话框,加载list里的项
        // 加载借阅记录对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog_borrowed_book_records = builder.create();
        // show函数需要在getWindow前调用
        dialog_borrowed_book_records.show();
        // 对话框窗口设置布局文件
        Window window = dialog_borrowed_book_records.getWindow();
        window.setContentView(R.layout.mod_que_library__dialog_borrow_record);

        // 获取对话窗口中的ListView
        ListView list_record = (ListView) window.findViewById(R.id.list_borrowbook_record);
        // 设置适配器
        list_record.setAdapter(new BorrowBookAdapter(this, R.layout.mod_que_library__dialog_borrow_record__item, list));
    }

    private void displayLibraryAuthDialog() {
        // 显示图书馆账号需要绑定的对话框
        final EditText et_pwd = new EditText(this);
        et_pwd.setHint("图书馆密码（默认为一卡通号）");
        et_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        // 设置对话框布局
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(et_pwd);
        new AlertDialog.Builder(this).setTitle("绑定图书馆账号").setMessage("你还没有绑定图书馆账号或账号不正确，" +
                "请重新绑定：").setView(linearLayout)
                .setPositiveButton("确定", (arg0, arg1) -> {
                    String pwd = et_pwd.getText().toString();
                    // 发送更新请求
                    updateAuthInfo(pwd);
                })
                .setNegativeButton("取消", null).show();
    }

    /**
     * @param password 图书馆密码
     */
    private void updateAuthInfo(String password) {
        // 用于更新图书馆的账号和密码
        showProgressDialog();

        User currentUser = ApiHelper.getCurrentUser();

        new ApiSimpleRequest(Method.POST).url(ApiHelper.auth_update_url)
                .post("cardnum", currentUser.userName, "password", currentUser.password)
                .post("lib_username", currentUser.userName, "lib_password", password)
                .onResponse((success, code, response) -> {
                    hideProgressDialog();
                    if (response.equals("OK")) {
                        // 返回OK说明认证成功
                        refreshBorrowRocord();
                    } else {
                        showSnackBar("绑定失败，请重试");
                    }
                }).run();
    }
}
