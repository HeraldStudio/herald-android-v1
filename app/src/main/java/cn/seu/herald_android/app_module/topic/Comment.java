package cn.seu.herald_android.app_module.topic;

/**
 * 实体类, 保存每个评论对象
 * Created by corvo on 11/27/16.
 */

public class Comment {

    /**
     * 是否匿名回复
     */
    private boolean mIsAnonymous;

    /**
     * 回复内容
     */
    private String mContent;

    /**
     * 回复时间
     */
    private String mTime;

    /**
     * 点赞数
     */
    private int mLike;

    /**
     * 评论用户
     */
    private String mName;

    /**
     * 是否向该评论点过赞
     */
    private boolean mIsLike;

    public Comment(boolean mIsAnonymous, String mContent, String mTime, int mLike, String mName, boolean mIsLike) {
        this.mIsAnonymous = mIsAnonymous;
        this.mContent = mContent;
        this.mTime = mTime;
        this.mLike = mLike;
        this.mName = mName;
        this.mIsLike = mIsLike;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public boolean ismIsAnonymous() {
        return mIsAnonymous;
    }

    public void setmIsAnonymous(boolean mIsAnonymous) {
        this.mIsAnonymous = mIsAnonymous;
    }

    public int getmLike() {
        return mLike;
    }

    public void setmLike(int mLike) {
        this.mLike = mLike;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }
}
