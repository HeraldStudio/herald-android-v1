package cn.seu.herald_android.mod_query.schoolbus;


import android.os.Bundle;
import android.view.View;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;


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

/**
 * 2016/2/22 By heyongdong
 * 校车时刻表查询
 */
public class SchoolBusActivity extends BaseAppCompatActivity {
    ImageButton btn_explore_toschoollist;
    ImageButton btn_explore_tosubwaylist;
    LinearLayout explore_toschool;
    LinearLayout explore_tosubway;
    ListView listview_toschool_weekday;
    ListView listView_tosubway_weekday;
    ListView listview_toschool_weekend;
    ListView listView_tosubway_weekend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_bus);
        //沉浸式布局
        setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
        //初始化函数
        init();
        //加载校车数据
        refreshCache();
    }

    public void init() {
        //初始化控件
        listview_toschool_weekday = (ListView) findViewById(R.id.list_schoolbus_toschool_weekday);
        listView_tosubway_weekday = (ListView) findViewById(R.id.list_schoolbus_tosubway_weekday);
        listview_toschool_weekend = (ListView) findViewById(R.id.list_schoolbus_toschool_weekend);
        listView_tosubway_weekend = (ListView) findViewById(R.id.list_schoolbus_tosubway_weekend);
        //设置点开时展示具体数据区域，再点击关闭
        explore_toschool = (LinearLayout)findViewById(R.id.explorespace_toschool);
        explore_tosubway = (LinearLayout)findViewById(R.id.explorespace_tosubway);
        btn_explore_toschoollist = (ImageButton) findViewById(R.id.btn_explore_toschool);
        btn_explore_tosubwaylist = (ImageButton) findViewById(R.id.btn_explore_tosubway);
        btn_explore_toschoollist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(explore_toschool.getVisibility()==View.GONE){
                    explore_toschool.setVisibility(View.VISIBLE);
                    btn_explore_toschoollist.setImageResource(R.drawable.ic_keyboard_arrow_up_24dp);
                }else{
                    explore_toschool.setVisibility(View.GONE);
                    btn_explore_toschoollist.setImageResource(R.drawable.ic_keyboard_arrow_down_24dp);
                }
            }
        });
        btn_explore_tosubwaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(explore_tosubway.getVisibility()==View.GONE){
                    explore_tosubway.setVisibility(View.VISIBLE);
                    btn_explore_tosubwaylist.setImageResource(R.drawable.ic_keyboard_arrow_up_24dp);
                }else{
                    explore_tosubway.setVisibility(View.GONE);
                    btn_explore_tosubwaylist.setImageResource(R.drawable.ic_keyboard_arrow_down_24dp);
                }
            }
        });
        //设置点击函数
    }

    public void refreshCache(){
        //通过联网刷新数据
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_SCHOOLBUS))
                .addParams("uuid",getApiHepler().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        getApiHepler().dealApiException(e);
                        try{
                            loadListWithCace();
                        }catch (JSONException e1){
                            e1.printStackTrace();
                            showMsg("数据解析错误。");
                        }
                    }

                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject json_res = new JSONObject(response);
                            if(json_res.getInt("code")==200){
                                getCacheHelper().setCache("herald_schoolbus_cache",json_res.getString("content"));
                                loadListWithCace();
                            }
                        }catch (JSONException e2){
                            e2.printStackTrace();
                            showMsg("数据解析错误。");
                        }

                    }
                });
    }

    public void loadListWithCace()throws JSONException{
        JSONObject cache_json = new JSONObject(getCacheHelper().getCache("herald_schoolbus_cache"));
        JSONArray weekend_tosubway = cache_json.getJSONObject("weekend").getJSONArray("前往地铁站");
        JSONArray weekend_toschool = cache_json.getJSONObject("weekend").getJSONArray("返回九龙湖");
        JSONArray weekday_tosubway = cache_json.getJSONObject("weekday").getJSONArray("前往地铁站");
        JSONArray weekday_toschool = cache_json.getJSONObject("weekday").getJSONArray("返回九龙湖");
        //生成周末前往地铁站列表
        ArrayList<SchoolBusItem> list_weekend_tosubway = SchoolBusItem.transfromtransfromJSONtoArrayList(weekend_toschool);
        //生成周末去学校列表
        ArrayList<SchoolBusItem> list_weekend_toschool = SchoolBusItem.transfromtransfromJSONtoArrayList(weekend_tosubway);
        //平时前往地铁站的列表
        ArrayList<SchoolBusItem> list_weekday_tosubway = SchoolBusItem.transfromtransfromJSONtoArrayList(weekday_tosubway);
        //平时前往学校的列表
        ArrayList<SchoolBusItem> list_weekday_toschool = SchoolBusItem.transfromtransfromJSONtoArrayList(weekday_toschool);
        //加载listview
        listview_toschool_weekday.setAdapter(new SchoolBusListAdapter(getBaseContext(),R.layout.listviewitem_schoolbus,list_weekday_toschool));
        listView_tosubway_weekday.setAdapter(new SchoolBusListAdapter(getBaseContext(),R.layout.listviewitem_schoolbus,list_weekday_tosubway));
        listview_toschool_weekend.setAdapter(new SchoolBusListAdapter(getBaseContext(),R.layout.listviewitem_schoolbus,list_weekend_toschool));
        listView_tosubway_weekend.setAdapter(new SchoolBusListAdapter(getBaseContext(), R.layout.listviewitem_schoolbus, list_weekend_tosubway));
        //根据listview内容设置它的大小
        SchoolBusListAdapter.setListViewHeightBasedOnChildren(listview_toschool_weekday);
        SchoolBusListAdapter.setListViewHeightBasedOnChildren(listView_tosubway_weekday);
        SchoolBusListAdapter.setListViewHeightBasedOnChildren(listview_toschool_weekend);
        SchoolBusListAdapter.setListViewHeightBasedOnChildren(listView_tosubway_weekend);
    }
}





