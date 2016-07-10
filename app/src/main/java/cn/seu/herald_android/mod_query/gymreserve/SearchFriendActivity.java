package cn.seu.herald_android.mod_query.gymreserve;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;

public class SearchFriendActivity extends BaseActivity implements SearchView.OnQueryTextListener{

    ListView listView_searchfriendrelust;
    SearchView searchView;
    public static int RESULT_CODE_CANCEL = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        init();
    }

    private void init() {
        //设置toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.libary_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                //如果是返回则设置result为CANCEL
                onBackPressed();
                setResult(RESULT_CODE_CANCEL);
                finish();
            });
        }

        setStatusBarColor(ContextCompat.getColor(this, R.color.colorGymReserveprimary));
        enableSwipeBack();

        //设置展示搜索结果的列表
        listView_searchfriendrelust = (ListView) findViewById(R.id.listview_searchfriendsresult);

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
                searchView.setQueryHint("一卡通或姓名查找");
                //设置默认展开
                searchView.setIconifiedByDefault(false);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(this);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }


    //搜索按钮点击时运行的函数
    @Override
    public boolean onQueryTextSubmit(String query) {
        //发送搜索请求
        showProgressDialog();
        new ApiRequest().api("yuyue")
                .addUUID()
                .post("method","getFriendList")
                .post("cardNo",query)
                .onFinish((success, code, response) -> {
                    if (success){
                        try{
                            JSONArray array = new JSONObject(response).getJSONArray("content");
                            ArrayList<Friend> list = new ArrayList<>();
                            for (int i= 0;i<array.length();i++){
                                list.add(new Friend(array.getJSONObject(i)));
                            }
                            //加载搜索结果
                            loadSearchResult(list);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }else {
                        showSnackBar("查询失败，请重试");
                    }
                }).run();

        //保留输入框内容
        searchView.setQuery(query, false);
        //取消焦点，收起软键盘
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    //根据搜索结果加载展示结果的列表
    private void loadSearchResult(ArrayList<Friend> list) {
        listView_searchfriendrelust.setAdapter(new Friend.FriendAdapter(getBaseContext(),R.layout.listviewitem_gym_searchfriend,list));
        listView_searchfriendrelust.setOnItemClickListener((parent, view, position, id) -> {
            Friend friend  =(Friend) parent.getItemAtPosition(position);
            if (!getFriendArrayList().contains(friend))
                addFriend(friend);
            finish();
        });
        hideProgressDialog();
    }

    public  ArrayList<Friend> getFriendArrayList(){
        try{
            JSONArray array = getFriendJSONArray();
            ArrayList<Friend> list = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                list.add(new Friend(array.getJSONObject(i)));
            }
            return list;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public  JSONArray getFriendJSONArray(){
        String cache = CacheHelper.get("herald_gymreserve_recentfriendlist");
        try{
            if(!cache.equals("")){
                return new JSONArray(cache);
            }
        }catch (JSONException e){
            e.printStackTrace();
            CacheHelper.set("herald_gymreserve_recentfriendlist","");
        }
        return new JSONArray();
    }

    public void addFriend(Friend friend){
        try{
            CacheHelper.set("herald_gymreserve_recentfriendlist", getFriendJSONArray().put(friend.getJSONObject()).toString());
        }catch (JSONException e){
            e.printStackTrace();
        }
    }


}
