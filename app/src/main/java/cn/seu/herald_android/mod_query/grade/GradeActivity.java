package cn.seu.herald_android.mod_query.grade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableHeaderAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class GradeActivity extends BaseAppCompatActivity {

    private static final String[][] DATA_TO_SHOW = { { "This", "is", "a", "test" },
            { "and", "a", "second", "test" } };

    SortableTableView<String[]> tableview_grade;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);
        setStatusBarColor(this,getResources().getColor(R.color.colorPrimaryGreen));
        init();
    }

    private void init(){
//        tableview_grade = (SortableTableView<String[]>)findViewById(R.id.tableview_grade);
//        tableview_grade.setDataAdapter(new SimpleTableDataAdapter(this, DATA_TO_SHOW));
    }
}
