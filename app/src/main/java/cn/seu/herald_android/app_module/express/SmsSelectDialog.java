package cn.seu.herald_android.app_module.express;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by corvo on 8/2/16.
 */
public class SmsSelectDialog extends DialogFragment{

    private static String TAG = "SmsSelectDialog";

    private RecyclerView smsRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<SmsInfo> smsInfoList;
    private static Context context;


    private DialogRefresh listener;   // 为了改变原Activity中的短信文本

    public static SmsSelectDialog newInstance(Bundle bundle, Context context) {
        SmsSelectDialog dialog = new SmsSelectDialog();
        dialog.setArguments(bundle);
        SmsSelectDialog.context = context;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.mod_que_express__sms_select, null);

        listener = (DialogRefresh) getArguments().getSerializable("listener");
        smsRecyclerView = (RecyclerView)view.findViewById(R.id.express_view_sms);

        smsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        smsInfoList = new ArrayList<>();

        Uri uri = Uri.parse("content://sms/inbox");
        SmsContent smsContent = new SmsContent(context, uri);
        smsInfoList = smsContent.getInfos();


        Log.d(TAG, "setAdapter");
        adapter = new SmsInfoAdapter(smsInfoList, this, listener);
        smsRecyclerView.setAdapter(adapter);
        smsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        builder.setView(view);
        return builder.create();
    }

    /**
     * 主Activity将会实现一个该接口的对象,
     * 通过此对象来实现对原Activity中数据的操作
     */
    interface DialogRefresh extends Serializable{

        /**
         * 此函数为调用Dialog时的返回内容
         * @param text
         */
        void refreshSmsText(String text);
    }
}
