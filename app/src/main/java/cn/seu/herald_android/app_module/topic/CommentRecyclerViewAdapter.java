package cn.seu.herald_android.app_module.topic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.framework.network.OnResponseListener;
import cn.seu.herald_android.helper.ApiHelper;

/**
 * Created by corvo on 11/27/16.
 */

public class CommentRecyclerViewAdapter
        extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder>{
    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private List<Comment> mCommentList;

    public static final int VIEW_TYPE_DEFAULT = 1;
    public static final int VIEW_TYPE_LOADER = 2;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public String mBoundString;

        public final View mView;
        public final TextView mTextCommentUser;     // 评论用户
        public final TextView mTextCommentTime;     // 发布时间
        public final TextView mTextCommentLikeN;    // 点赞数
        public final ImageButton mImageLike;        // 自己是否点赞
        public final TextView mTextCommentContent;  // 评论主体
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTextCommentUser = (TextView) view.findViewById(R.id.text_topic_comment_user);
            mTextCommentTime = (TextView) view.findViewById(R.id.text_topic_comment_time);
            mTextCommentLikeN = (TextView) view.findViewById(R.id.text_topic_comment_like_cnt);
            mTextCommentContent = (TextView)  view.findViewById(R.id.text_topic_comment_content);
            mImageLike = (ImageButton) view.findViewById(R.id.ibtn_topic_comment_like);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextCommentUser.getText();
        }
    }

    public CommentRecyclerViewAdapter(Context context, List<Comment> comments) {
        mCommentList = comments;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mod_que_topic_comment_item, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //holder.mBoundString = mValues.get(position);
        //holder.mTextView.setText(mValues.get(position));
        Comment comment = mCommentList.get(position);
        holder.mTextCommentUser.setText(comment.getmName());
        holder.mTextCommentTime.setText(comment.getmTime());
        holder.mTextCommentLikeN.setText(String.valueOf(comment.getmLike()) + " likes");
        holder.mTextCommentContent.setText(comment.getmContent());


        holder.mView.setOnClickListener(view -> {
            Context context = view.getContext();
            Log.d("HotestFragmetn", "OnChange " + String.valueOf(position));
        });

        holder.mImageLike.setOnClickListener(view -> {
            // TODO 添加处理点赞的函数
            Log.d("CommentRecycler", "OnClicked");
            new ApiSimpleRequest(Method.POST)
                    .url(TopicUtils.TOPIC_URL)
                    .post("askcode", "105")     // 为评论点赞
                    .post("cid", comment.getmCid())
                    .post("cardnum", ApiHelper.getCurrentUser().userName)
                    .onResponse(new OnResponseListener() {
                        @Override
                        public void onResponse(boolean success, int code, String response) {
                            Log.d("CommentRecycler", response);
                            if (code == 200) {
                                try {
                                    JSONObject jRes = new JSONObject(response);
                                    if (jRes.getInt("code") == 200) {
                                        holder.mImageLike.setImageResource(R.drawable.ic_heart_red);    // 点赞成功则修改图片状态
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).run();
        });

    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

}
