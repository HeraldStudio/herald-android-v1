package cn.seu.herald_android.app_module.curriculum;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.BaseActivity;
import cn.seu.herald_android.framework.json.JArr;

public class CurriculumActivity extends BaseActivity {

    @BindView(R.id.pager)
    ViewPager pager;

    @BindView(R.id.curriculum_bg)
    ImageView bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_curriculum);
        ButterKnife.bind(this);

        // 异步加载背景图
        runMeasurementDependentTask(() -> Picasso.with(this)
                .load(R.drawable.curriculum_bg)
                .resize(bg.getWidth(), bg.getHeight())
                .centerCrop().into(bg));

        readLocal();
    }

    private void refreshCache() {
        showProgressDialog();

        (
                Cache.curriculumSidebar.getRefresher()
        ).parallel(
                Cache.curriculum.getRefresher()
        ).onFinish((allSuccess, mostCriticalCode) -> {
            hideProgressDialog();
            readLocal();
            if (!allSuccess) {
                showSnackBar("刷新失败，请重试");
            }
        }).run();
    }

    private void readLocal() {
        String data = Cache.curriculum.getValue();
        String sidebar = Cache.curriculumSidebar.getValue();
        if (data.equals("")) {
            refreshCache();
            return;
        }

        reloadFloatClassCount();

        PagesAdapter adapter = new PagesAdapter(this, data, sidebar);
        pager.setAdapter(adapter);
        pager.setCurrentItem(adapter.getCurrentPage());
        setTitle("第" + (adapter.getCurrentPage() + 1) + "周");

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int p1, float p2, int p3) {
            }

            @Override
            public void onPageSelected(int position) {
                setTitle("第" + (position + 1) + "周");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void reloadFloatClassCount() {
        ActionMenuItemView item = (ActionMenuItemView) findViewById(R.id.action_float_class);
        if (item != null) {
            item.setTitle("浮动课程(" + getFloatClassCount() + ")");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_curriculum, menu);
        reloadFloatClassCount();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            refreshCache();
        }
        if (id == R.id.action_float_class) {
            displayFloatClassDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getFloatClassCount() {
        // 设置列表
        JArr array = new JArr(Cache.curriculumSidebar.getValue());
        ArrayList<SidebarClassModel> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            SidebarClassModel model = new SidebarClassModel(array.$o(i));
            if (!model.isAdded()) {
                list.add(model);
            }
        }
        return String.valueOf(list.size());
    }

    private void displayFloatClassDialog() {
        // 加载浮动课程对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog_lecture_records = builder.create();
        // show函数需要在getWindow前调用
        dialog_lecture_records.show();
        // 对话框窗口设置布局文件
        Window window = dialog_lecture_records.getWindow();
        window.setContentView(R.layout.mod_que_curriculum__dialog_float_class);

        // 获取对话窗口中的listview
        ListView list_record = (ListView) window.findViewById(R.id.list_float_class);

        // 设置列表
        JArr array = new JArr(Cache.curriculumSidebar.getValue());
        ArrayList<SidebarClassModel> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            SidebarClassModel model = new SidebarClassModel(array.$o(i));
            if (!model.isAdded()) {
                list.add(model);
            }
        }

        list_record.setAdapter(new CurriculumFloatClassAdapter(
                getBaseContext(),
                R.layout.mod_que_curriculum__dialog_float_class__item,
                list));
    }
}
