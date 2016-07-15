package cn.seu.herald_android.mod_query.curriculum;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;

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

        //异步加载背景图
        runMeasurementDependentTask(() -> Picasso.with(this)
                .load(R.drawable.curriculum_bg)
                .resize(bg.getWidth(), bg.getHeight())
                .centerCrop().into(bg));

        readLocal();
    }

    private void refreshCache() {
        showProgressDialog();
        new ApiRequest().api("sidebar").addUUID()
                .toCache("herald_sidebar", o -> o.getJSONArray("content"))
                .onFinish((success, code, response) -> {
                    if (success) {
                        refreshCacheStep2();
                    } else {
                        hideProgressDialog();
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    private void refreshCacheStep2() {
        new ApiRequest().api("curriculum").addUUID()
                .toCache("herald_curriculum", o -> o.getJSONObject("content"))
                .onFinish((success, code, response) -> {
                    hideProgressDialog();
                    if (success) {
                        readLocal();
                    } else {
                        showSnackBar("刷新失败，请重试");
                    }
                }).run();
    }

    private void readLocal() {
        String data = CacheHelper.get("herald_curriculum");
        String sidebar = CacheHelper.get("herald_sidebar");
        if (data.equals("")) {
            refreshCache();
            return;
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            refreshCache();
        }
        return super.onOptionsItemSelected(item);
    }
}
