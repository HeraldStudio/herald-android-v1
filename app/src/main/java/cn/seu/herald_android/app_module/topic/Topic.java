package cn.seu.herald_android.app_module.topic;

/**
 * 实体类, 保存话题对象
 * Created by corvo on 11/27/16.
 */

public class Topic {

    /**
     * 话题 id
     */
    private String mId;

    /**
     * 话题名
     */
    private String mName;

    /**
     * 评论数
     */
    private String mCommentN;

    /**
     * 话题开始时间
     */
    private String mStartT;

    /**
     * 话题详情
     */
    private String content;

    public Topic(String mId, String mCommentN, String mName, String mStartT, String content) {
        this.mId = mId;
        this.mCommentN = mCommentN;
        this.mName = mName;
        this.mStartT = mStartT;
        this.content = content;
    }

    public Topic() {}

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmCommentN() {
        return mCommentN;
    }

    public void setmCommentN(String mCommentN) {
        this.mCommentN = mCommentN;
    }

    public void addCommentN() {
        int commentN = Integer.parseInt(mCommentN);
        commentN ++;
        mCommentN = String.valueOf(commentN);
    }

    public String getmStartT() {
        return mStartT;
    }

    public void setmStartT(String mStartT) {
        this.mStartT = mStartT;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
