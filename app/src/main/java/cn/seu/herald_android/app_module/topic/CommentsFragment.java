package cn.seu.herald_android.app_module.topic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.framework.network.OnResponseListener;
import cn.seu.herald_android.helper.ApiHelper;

/**
 * Created by corvo on 11/27/16.
 */

public class CommentsFragment extends Fragment {


    private final int TOPIC_HOT = 1;
    private final int TOPIC_NEW = 2;
    private int mCommentsType;    // 选择返回的评论类型类型

    private String mTid;        // 保存话题编号信息
    private String TAG = "CommentsFragment";
    String mRes;
    private List<Comment> mCommentList;
    private RecyclerView mRecycleView;

    public interface onParentListener{
        void deliveRecycler(RecyclerView recyclerView);
    }

    private onParentListener listener;

    public CommentsFragment() {

    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Attch context");
        super.onAttach(context);
        listener = (onParentListener) context;
        Bundle bundle = getArguments();     //从activity传过来的Bundle
        String type = bundle.getString("type");
        if (type.equals("hot")) {
            mCommentsType = TOPIC_HOT;
        } else if(type.equals("new")) {
            mCommentsType = TOPIC_NEW;
        } else {
            mCommentsType = 0;
        }
        mTid = bundle.getString("tid");

    }

    /**
     * 刷新评论区
     */
    public void onRefreshComments() {
        Log.d(TAG, "OnRefreshComments");
        if (mCommentsType == TOPIC_HOT)
            loadCommentHot();
        if (mCommentsType == TOPIC_NEW)
            loadCommentNew();
    }

    public void loadCommentHot() {
        new ApiSimpleRequest(Method.POST)
                .url("http://223.3.81.245:7000/herald/api/v1/topic")
                .post("askcode", "107")
                .post("tid", mTid)
                .post("cardnum", ApiHelper.getCurrentUser().userName)
                .onResponse(new OnResponseListener() {
                    @Override
                    public void onResponse(boolean success, int code, String response) {
                        if (success) {
                            mRes = response;
                            setUpRecyclerView();
                        }
                    }
                }).run();
    }

    /**
     *  通过返回字符串填充List
     */
    public void setUpRecyclerView() {
        try {
            JSONObject jRes = new JSONObject(mRes);
            JSONArray jComments = jRes.getJSONArray("content");
            mCommentList = new ArrayList<Comment>();
            for (int i = 0; i < jComments.length(); i++) {
                JSONObject jComment = jComments.getJSONObject(i);
                boolean isAno = jComment.getString("cardnum").equals("匿名小公举")? true:false;
                // TODO 添加用户自己是否点赞
                Comment comment = new Comment(isAno,
                        jComment.getString("content"),
                        jComment.getString("time"),
                        jComment.getInt("likeN"),
                        jComment.getString("cardnum"),
                        false);
                mCommentList.add(comment);
            }
            mRecycleView.setLayoutManager(new LinearLayoutManager(mRecycleView.getContext()));
            mRecycleView.setAdapter(new CommentRecyclerViewAdapter(getActivity(),
                    mCommentList));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂未启用
     */
    public void loadCommentNew() {

        /*
        new ApiSimpleRequest(Method.POST)
                .url("http://223.3.81.245:7000/herald/api/v1/topic")
                .post("askcode", "107")
                .post("tid", mTid)
                .post("cardnum", ApiHelper.getCurrentUser().userName)
                .onResponse(new OnResponseListener() {
                    @Override
                    public void onResponse(boolean success, int code, String response) {
                        Log.d("CommentFragment", response);
                        if (success) {
                            mRes = response;
                        }
                    }
                }).run();
            */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecycleView = (RecyclerView) inflater.inflate(
                R.layout.mod_que_topic_fragment_newest, container, false
        );
        listener.deliveRecycler(mRecycleView);

        onRefreshComments();
        return mRecycleView;
    }

    public List<Comment> createData() {
        List<Comment> commentList = new ArrayList<Comment>();

        Comment comment1 = new Comment(true, "跟自己聊天, 问一些奇怪的问题", "2016-11-27 12:18:11"
                                , 2, "匿名小公举", false);
        Comment comment2 = new Comment(true, "和宿舍楼下的猫对骂", "2016-11-27 14:56:10",
                                    2, "匿名小公举", true);
        Comment comment3 = new Comment(false, "我就是来逛逛看有没有单身妹子需要男朋友的", "2016-11-27 15:02:05",
                                    2, "213141748", false);

        Comment comment4 = new Comment(false, "调bug, 还用问吗", "2016-11-27 15:02:05",
                2, "213141748", false);

        Comment comment5 = new Comment(false, "一个人的时候, 阅读是首选. 但是孤独的时候, 更喜欢发呆", "2016-11-27 15:02:05",
                2, "213141748", false);

        commentList.add(comment1);
        commentList.add(comment2);
        commentList.add(comment3);
        commentList.add(comment4);
        commentList.add(comment5);
        return commentList;
    }
}
