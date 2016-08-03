package cn.seu.herald_android.app_module.gymreserve;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.CacheHelper;

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
                        try {
                            JSONArray array = new JSONObject(response).getJSONArray("content");
                            ArrayList<FriendModel> list = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                list.add(new FriendModel(array.getJSONObject(i)));
                            }
                            // 加载搜索结果
                            loadSearchResult(list);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        try {
            JSONArray array = getFriendJSONArray();
            ArrayList<FriendModel> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                list.add(new FriendModel(array.getJSONObject(i)));
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public  JSONArray getFriendJSONArray(){
        String cache = CacheHelper.get("herald_gymreserve_friend_list");
        try {
            if (!cache.equals("")) {
                return new JSONArray(cache);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            CacheHelper.set("herald_gymreserve_friend_list", "");
        }
        return new JSONArray();
    }

    public void addFriend(FriendModel friendModel) {
        try {
            CacheHelper.set("herald_gymreserve_friend_list", getFriendJSONArray().put(friendModel.getJSONObject()).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
