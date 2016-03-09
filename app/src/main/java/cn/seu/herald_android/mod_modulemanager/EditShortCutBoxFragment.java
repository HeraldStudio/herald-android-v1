package cn.seu.herald_android.mod_modulemanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

/**
 * 这个碎片的内容是：展示所给的模块列表，以便用户设置这些模块的快捷方式是否可用
 * Created by heyon on 2016/3/8.
 */
public class EditShortCutBoxFragment extends Fragment {

    View layout_view;
    //将显示的模块的列表
    ArrayList<SeuModule> tagList;
    public static EditShortCutBoxFragment newInstance( ArrayList<SeuModule> list ){
        EditShortCutBoxFragment editShortCutBoxFragment = new EditShortCutBoxFragment();
        editShortCutBoxFragment.tagList = list;
        return editShortCutBoxFragment;
    }
    public EditShortCutBoxFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout_view = inflater.inflate(R.layout.fragment_edit_shortcutbox,container,false);
        ListView listView = (ListView)layout_view.findViewById(R.id.list_edit_shortcut);
        //获得适配器
        ShortCutBoxEditAdapter shortCutBoxEditAdapter = new ShortCutBoxEditAdapter(getContext(),R.layout.listview_edit_shortcut,tagList);
        //设置适配器
        listView.setAdapter(shortCutBoxEditAdapter);
        return layout_view;
    }
}


