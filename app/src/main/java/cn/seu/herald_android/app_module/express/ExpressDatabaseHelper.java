package cn.seu.herald_android.app_module.express;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static cn.seu.herald_android.helper.LogUtils.LOGGING_ENABLED;
import static cn.seu.herald_android.helper.LogUtils.makeLogTag;

/**
 * Created by corvo on 8/5/16.
 */
public class ExpressDatabaseHelper extends SQLiteOpenHelper {
    private static String TAG = makeLogTag(ExpressDatabaseHelper.class);

    public static final String CREATE_EXPRESS = "CREATE TABLE express("
            + "id integer primary key autoincrement, "
            + "username text, "
            + "userphone text, "
            + "sms text, "
            + "dest text, "
            + "arrival text, "
            + "locate text, "
            + "weight text, "
            + "submit_time datetime, "
            + "is_fetched text, "
            + "is_received text)";

    private Context mContext;
    public ExpressDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                                  int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EXPRESS);
        Log.d(TAG, "Create Database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
