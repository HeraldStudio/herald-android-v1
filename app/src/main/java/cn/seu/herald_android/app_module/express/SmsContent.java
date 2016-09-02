package cn.seu.herald_android.app_module.express;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by corvo on 7/30/16.
 */
public class SmsContent {

    private Context mContext;
    private Uri mUri;

    List<SmsInfo> infos;

    public SmsContent(Context context, Uri uri) {
        infos = new ArrayList<SmsInfo>();
        this.mContext = context;
        this.mUri = uri;
    }

    // 获取
    public List<SmsInfo> getInfos() {
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};

            Cursor cursor = null;
            try {

                /**
                 * 获取时间, 过滤短信, 默认只读入24小时以内的短信,
                 * 如若24小时内未取快递, 那么短信一定已经失效, 快递已被移动
                 */
                Date dt = new Date();
                long cur = dt.getTime();                            // 当前时
                long last = cur - TimeUnit.HOURS.toMillis(24);      // 减去24小时

                //TODO 修改短信日期
                String filter = "date > " + String.valueOf(last);   // 查询中的where语句, date > 1469839964323
                //String filter = null;   // 查询中的where语句, date > 1469839964323

                cursor = mContext.getContentResolver().query(mUri, projection, filter, null, "date desc");

                if (cursor == null) {
                    //Log.d(TAG, "cursor is null. uri:" + mUri);
                }

                int nameColumn = cursor.getColumnIndex("person");
                int phoneNumberColumn = cursor.getColumnIndex("address");
                int smsbodyColumn = cursor.getColumnIndex("body");
                int dateColumn = cursor.getColumnIndex("date");
                int typeColumn = cursor.getColumnIndex("type");

                /**
                 *  读取信息存入数组
                 */
                while (cursor.moveToNext()) {
                    SmsInfo smsInfo = new SmsInfo();
                    smsInfo.setName(cursor.getString(nameColumn));
                    smsInfo.setDate(cursor.getString(dateColumn));
                    smsInfo.setPhoneNumber(cursor.getString(phoneNumberColumn));
                    smsInfo.setType(cursor.getString(typeColumn));
                    smsInfo.setSmsbody(cursor.getString(smsbodyColumn));

                    //Log.d(TAG, cursor.getString(phoneNumberColumn));
                    //Log.d(TAG, cursor.getString(smsbodyColumn));
                    //Log.d(TAG, cursor.getString(dateColumn));

                    infos.add(smsInfo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        return infos;
    }
}
