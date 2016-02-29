package cn.seu.herald_android;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by heyon on 2016/2/28.
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public ActivityTest(Class<MainActivity> activityClass) {
        super(MainActivity.class);
    }

    public void TestActivity(){
        getActivity().startActivity(new Intent(getActivity(),MainActivity.class));
    }
}
