package cn.seu.herald_android.app_module.express;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.seu.herald_android.R;

/**
 * Created by corvo on 8/24/16.
 */
public class ExpressWelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.mod_que_express__welcome_fragment, container, false);
    }
}
