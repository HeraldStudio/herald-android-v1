package cn.seu.herald_android.app_module.express;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;

/**
 * Created by corvo on 7/28/16.
 */
public class ExpressActivity extends BaseActivity implements AdapterView.OnItemSelectedListener{

    private Spinner mSpiner;
    ArrayList<String> data = new ArrayList<String>(Arrays.asList("梅九", "桃三四"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_express);
        mSpiner = (Spinner)findViewById(R.id.express_spinner_dest);

        ArrayAdapter<String> dataAdapter =
                new ArrayAdapter<String>(this, R.layout.mod_que_express__dest_item, data);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpiner.setAdapter(dataAdapter);
        mSpiner.setOnItemSelectedListener(this);

        Uri uri = Uri.parse("content://sms/inbox");
        SmsContent smsContent = new SmsContent(this, uri);
        List<SmsInfo> list = smsContent.getInfos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Log.d("ExpressActivity", String.valueOf(view.getId()));
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.express_spinner_dest) {
            Log.d("ExpressActivity", data.get(position));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
