package cn.seu.herald_android.app_module.topic;


import android.animation.Animator;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.framework.network.OnResponseListener;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;

/**
 * Created by corvo on 7/21/16.
 */
public class TopicActivity extends BaseActivity implements CommentsFragment.onParentListener{

    AnimatedFloatingActionButton fab;
    TextView mTopicHeader;          // 话题标题
    TextView mTopicHeaderContent;   // 话题详情
    TextView mTopicCommentN;        // 话题评论数
    TextView mTopicStart;           // 话题开始时间

    Topic mTopic;                   // 该Activity对应的当前话题

    TabLayout tabLayout;
    ViewPager viewPager;
    Toolbar mToolbar;
    String TAG = "TopicActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_topic);


        fab = (AnimatedFloatingActionButton) findViewById(R.id.fab_add_comment);
        tabLayout = (TabLayout) findViewById(R.id.tab_topic);
        viewPager = (ViewPager) findViewById(R.id.viewpage_topic);
        mTopicHeader = (TextView) findViewById(R.id.text_topic_theme);
        mTopicHeaderContent = (TextView) findViewById(R.id.text_topic_detail);
        mTopicCommentN = (TextView) findViewById(R.id.text_topic_comment_cnt);


        fab.postDelayed(new Runnable() {
            @Override
            public void run() {
                revealEffectFab();
            }
        }, 300);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeComments();
            }
        });


        Log.d(TAG, "create finish");
        loadHeaderCache();
    }

    /**
     * 向当前话题做出评论, 在此之前, mTopic已被构建完成
     */
    public void makeComments() {
        Intent intent = new Intent(TopicActivity.this, CommentsMakeActivity.class);
        intent.putExtra("tid", mTopic.getmId());
        intent.putExtra("cardnum", ApiHelper.getCurrentUser().userName);
        startActivityForResult(intent, 1);      // 开始评论Activity, 评论成功与否将会返回到主Activity中
    }

    /**
     * 刷新评论数目
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isCommentOk = data.getExtras().getBoolean("IsCommentOk");
        Log.d(TAG, "getResult " + String.valueOf(isCommentOk));
        if (isCommentOk) {
            mTopic.addCommentN();
            setupText();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 加载话题头信息, 如果当前没有, 直接做出请求
     */
    public void loadHeaderCache() {
        Log.d(TAG, "loadHeaderCache");
        String jsonRes = CacheHelper.get("topic_header");
        if (jsonRes.isEmpty()){     // 缓存中尚无数据, 直接进行刷新
            onRefresh();
            return;
        }

        try {
            JSONObject jRes = new JSONObject(jsonRes);
            if (jRes.getInt("code") != 200) {
                showSnackBar("获取数据失败");
            } else {
                JSONArray jTopics = jRes.getJSONArray("content");
                JSONObject curTopic = jTopics.getJSONObject(0);

                setupViewPager(viewPager, curTopic.getString("id"));
                mTopic = new Topic(curTopic.getString("id"),
                       curTopic.getString("commentN"),
                       curTopic.getString("name"),
                       curTopic.getString("startT"),
                       curTopic.getString("content"));
                setupText();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 此函数仅在mTopic != null 时使用
     */
    public void setupText() {
        if (mTopic == null) {
            return;
        }
        Log.d(TAG, "set " + mTopic.getmCommentN());
        mTopicHeader.setText(mTopic.getmName());
        mTopicHeaderContent.setText(mTopic.getContent());
        mTopicCommentN.setText("已有" + mTopic.getmCommentN() + "人评论");
    }

    /**
     * 进行刷新操作, 此操作刷新整个页面, 包括两个tabLayout
     */
    public void onRefresh() {
        Log.d(TAG, "Start Refresh");
        new ApiSimpleRequest(Method.POST)
                .url(TopicUtils.TOPIC_URL)
                .post("askcode", "110")         // 请求所有话题
                .post("cardnum", ApiHelper.getCurrentUser().userName)
                .onResponse(new OnResponseListener() {
                    @Override
                    public void onResponse(boolean success, int code, String response) {
                        if (!success) {
                            Log.d(TAG, "ERROR");
                            hideProgressDialog();
                        } else {
                            hideProgressDialog();
                            CacheHelper.set("topic_header", response);
                            loadHeaderCache();
                        }
                    }
                }).run();
    }

    /**
     * 加载下方两个评论Fragment
     * @param viewPager
     * @param tId
     */
    public void setupViewPager(ViewPager viewPager, String tId) {
        Log.d(TAG, "setupViewPage");
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        CommentsFragment hotCommentsFragment = new CommentsFragment();      // 点赞最多的评论
        Bundle bundleHot = new Bundle();
        bundleHot.putString("type", "hot");
        bundleHot.putString("tid", tId);
        hotCommentsFragment.setArguments(bundleHot);
        adapter.addFragment(hotCommentsFragment, "最热");

        CommentsFragment newCommentsFragment = new CommentsFragment();      // 最新的评论
        Bundle bundleNew = new Bundle();
        bundleNew.putString("type", "new");
        bundleNew.putString("tid", tId);
        newCommentsFragment.setArguments(bundleNew);
        adapter.addFragment(newCommentsFragment, "最近");

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            Log.d("TopicActivity", "Refresh");
            showProgressDialog();
            onRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // fab 初始效果
    void revealEffectFab() {
        if (Build.VERSION.SDK_INT > 20){
            int cx = fab.getMeasuredWidth() / 2;
            int cy = fab.getMeasuredHeight() / 2;
            int finalRadius = Math.max(fab.getWidth(), fab.getHeight());

            Animator a = ViewAnimationUtils.createCircularReveal(fab, cx, cy, 0, finalRadius);
            a.setDuration(400);
            fab.setVisibility(View.VISIBLE);
            a.start();
        }
    }

    // 绑定fab对象与recyclerview, 实现下滑时fab消失
    @Override
    public void deliveRecycler(RecyclerView recyclerView) {
        fab.attchToRecyclerView(recyclerView);
    }
}
