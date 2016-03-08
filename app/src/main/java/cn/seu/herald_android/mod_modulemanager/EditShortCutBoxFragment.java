package cn.seu.herald_android.mod_modulemanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by heyon on 2016/3/8.
 */
public class EditShortCutBoxFragment extends Fragment {

    public static EditShortCutBoxFragment newInstance(){
        return new EditShortCutBoxFragment();
    }
    private EditShortCutBoxFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}


