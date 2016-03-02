package cn.seu.herald_android.mod_query.experiment;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import cn.seu.herald_android.R;

public class ExperimentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("TabbedCoordinatorLayout");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        //collapsingToolbar.setTitle("Title");
        //collapsingToolbar.setCollapsedTitleGravity(Gravity.CENTER);
        collapsingToolbar.setTitleEnabled(false);
        collapsingToolbar.setExpandedTitleGravity(Gravity.BOTTOM);

        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
