package cn.seu.herald_android.mod_query.library;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import okhttp3.Call;

/**
 * Created by corvo on 3/13/16.
 */
public class LibrarySearchActivity extends BaseAppCompatActivity
    implements SearchView.OnQueryTextListener{
    // 用于显示搜索结果的列表
    private RecyclerView recyclerView_search_result;             // 可以复用的recyclerview
    // 搜索框
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_search);
        init();
    }

    private void init() {
        //设置toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.libary_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //设置展示搜索结果的列表
        recyclerView_search_result = (RecyclerView)findViewById(R.id.recyclerview_library_searchres);
        recyclerView_search_result.setHasFixedSize(true);
        recyclerView_search_result.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_borrow_books, menu);

        //添加搜索框和相关设置
        MenuItem searchItem = menu.findItem(R.id.action_library_search);
        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                //设置提示信息
                searchView.setQueryHint("图书馆藏书查询");
                //设置默认展开
                searchView.setIconified(false);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(LibrarySearchActivity.this);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }


    //搜索按钮点击时运行的函数
    @Override
    public boolean onQueryTextSubmit(String query) {
        //发送搜索请求
        getProgressDialog().setCancelable(false);
        getProgressDialog().show();
        OkHttpUtils.post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_LIBRARY_SEARCH))
                .addParams("uuid", getApiHelper().getUUID())
                .addParams("book", query)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        showMsg("请求超时");
                        getProgressDialog().dismiss();
                        //关闭搜索栏
                        searchView.setIconified(true);
                    }

                    @Override
                    public void onResponse(String response) {
                        getProgressDialog().dismiss();
                        //关闭搜索栏
                        searchView.setIconified(true);
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                if (json_res.getString("content").equals("[]")) {
                                    showMsg("当前书目不存在, 换个关键字试试");
                                    return;
                                }
                                ArrayList<Book> searchResultlist =
                                        Book.transformJSONArrayToArrayList(json_res.getJSONArray("content"));
                                loadSearchResult(searchResultlist);
                            } else if (json_res.getInt("code") == 500) {
                                showMsg("获取的姿势不对, 换个姿势再来一次吧");
                            }
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                            showMsg("数据解析错误。");
                        }
                    }
                });
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    //根据搜索结果加载展示结果的列表
    public void loadSearchResult(ArrayList<Book> list){
        BookAdapter bookAdapter = new BookAdapter(list);
        recyclerView_search_result.setAdapter(bookAdapter);
    }


}

