package cn.seu.herald_android.app_module.topic;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.framework.network.OnResponseListener;

/**
 * Created by corvo on 11/28/16.
 */

public class CommentsMakeActivity extends BaseActivity{
    private String mTid;
    private String mCardNum;
    private String mContent;
    private String mIsAno;

    private EditText editComment;
    private CheckBox isAno;
    private ImageButton commit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_topic_comment_activity);
        Intent intent = getIntent();
        mTid = intent.getStringExtra("tid");
        mCardNum = intent.getStringExtra("cardnum");

        editComment = (EditText) findViewById(R.id.edit_topic_comment);
        isAno = (CheckBox) findViewById(R.id.ckbok_topic_commnt_ano);
        commit = (ImageButton) findViewById(R.id.ibtn_topic_comment_commit);

        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContent = editComment.getText().toString();
                mIsAno = isAno.isChecked()? "1": "0";
                makeComment();
                showProgressDialog();
            }
        });
    }

    private void makeComment() {
        new ApiSimpleRequest(Method.POST)
                .url("http://223.3.81.245:7000/herald/api/v1/topic")
                .post("askcode", "103")
                .post("cardnum", mCardNum)
                .post("tid", mTid)
                .post("quo", "1")
                .post("ano", mIsAno)
                .post("content", mContent)
                .onResponse(new OnResponseListener() {
                    @Override
                    public void onResponse(boolean success, int code, String response) {
                        hideProgressDialog();
                        if (success) {
                            try {
                                JSONObject jRes = new JSONObject(response);
                                if (jRes.getInt("code") == 200) {
                                    showSnackBar(jRes.getString("content"));
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            showSnackBar("评论失败, 请重试");
                        }
                    }
                }).run();
    }
}
