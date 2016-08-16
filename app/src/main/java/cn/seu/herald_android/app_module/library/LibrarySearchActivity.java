package cn.seu.herald_android.app_module.library;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

public class LibrarySearchActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    // 用于显示搜索结果的列表
    @BindView(R.id.recyclerview_library_searchres)
    RecyclerView recyclerView_search_result;
    // 搜索框
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_library__search);
        ButterKnife.bind(this);

        recyclerView_search_result.setHasFixedSize(true);
        recyclerView_search_result.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_borrow_books, menu);

        // 添加搜索框和相关设置
        MenuItem searchItem = menu.findItem(R.id.action_library_search);
        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                // 设置提示信息
                searchView.setQueryHint("图书馆藏书查询");
                // 设置默认展开
                searchView.setIconifiedByDefault(false);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(LibrarySearchActivity.this);
            }
        }

        // 若有启动参数，按参数执行查询
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("q")) {
            String q = intent.getStringExtra("q");
            searchView.setQuery(q, true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    // 搜索按钮点击时运行的函数
    @Override
    public boolean onQueryTextSubmit(String query) {
        // 发送搜索请求
        showProgressDialog();
        new ApiSimpleRequest(Method.POST).api("search").addUuid()
                .post("book", query).onResponse((success, code, response) -> {
            hideProgressDialog();
            if (success) {
                JObj json_res = new JObj(response);
                if (json_res.$s("content").equals("[]")) {
                    showSnackBar("当前书目不存在, 换个关键字试试");
                    return;
                }
                ArrayList<SearchBookModel> searchResultList =
                        SearchBookModel.transformJArrToArrayList(json_res.$a("content"));
                loadSearchResult(searchResultList);
            } else {
                showSnackBar("刷新失败，请重试");
            }
        }).run();

        // 保留输入框内容
        searchView.setQuery(query, false);
        // 取消焦点，收起软键盘
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    // 根据搜索结果加载展示结果的列表
    private void loadSearchResult(ArrayList<SearchBookModel> list) {
        SearchBookAdapter bookAdapter = new SearchBookAdapter(list);
        recyclerView_search_result.setAdapter(bookAdapter);
    }
}

