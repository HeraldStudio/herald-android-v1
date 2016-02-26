package cn.seu.herald_android.mod_query.grade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;

public class GradeActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);
        setStatusBarColor(this,getResources().getColor(R.color.colorPrimaryGreen));
    }
}
