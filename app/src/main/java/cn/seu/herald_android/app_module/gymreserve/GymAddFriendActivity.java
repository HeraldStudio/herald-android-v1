package cn.seu.herald_android.app_module.gymreserve;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

public class GymAddFriendActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    @BindView(R.id.searchResult)
    ListView listView;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_gymreserve__search_friend);
        ButterKnife.bind(this);
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
                searchView.setQueryHint("一卡通或姓名查找");
                // 设置默认展开
                searchView.setIconifiedByDefault(false);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(this);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    // 搜索按钮点击时运行的函数
    @Override
    public boolean onQueryTextSubmit(String query) {
        // 发送搜索请求
        showProgressDialog();
        new ApiSimpleRequest(Method.POST).api("yuyue")
                .addUuid()
                .post("method", "getFriendList")
                .post("cardNo", query)
                .onResponse((success, code, response) -> {
                    if (success) {
                        JArr array = new JObj(response).$a("content");
                        ArrayList<FriendModel> list = new ArrayList<>();
                        for (int i = 0; i < array.size(); i++) {
                            list.add(new FriendModel(array.$o(i)));
                        }
                        // 加载搜索结果
                        loadSearchResult(list);
                    } else {
                        showSnackBar("查询失败，请重试");
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
    private void loadSearchResult(ArrayList<FriendModel> list) {
        listView.setAdapter(new FriendModel.FriendAdapter(getBaseContext(), R.layout.mod_que_gymreserve__search_friend__item, list));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            FriendModel friendModel = (FriendModel) parent.getItemAtPosition(position);
            if (!getFriendArrayList().contains(friendModel))
                addFriend(friendModel);
            finish();
        });
        hideProgressDialog();
    }

    public ArrayList<FriendModel> getFriendArrayList() {
        JArr array = getFriendJArr();
        ArrayList<FriendModel> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(new FriendModel(array.$o(i)));
        }
        return list;
    }

    public JArr getFriendJArr() {
        String cache = Cache.gymReserveFriend.getValue();
        if (!cache.equals("")) {
            return new JArr(cache);
        }
        return new JArr();
    }

    public void addFriend(FriendModel friendModel) {
        JArr arr = getFriendJArr();
        arr.put(friendModel.getJObj());
        Cache.gymReserveFriend.setValue(arr.toString());
    }
}
