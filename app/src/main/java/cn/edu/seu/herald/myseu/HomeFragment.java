package cn.edu.seu.herald.myseu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * [首页]标签
 * Created by Vhyme on 2015/10/18.
 */
public class HomeFragment extends Fragment {
    public HomeFragment(){
        super();
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_home, container, false);
        System.out.println("home fragment created!");
        return contentView;
    }
}
