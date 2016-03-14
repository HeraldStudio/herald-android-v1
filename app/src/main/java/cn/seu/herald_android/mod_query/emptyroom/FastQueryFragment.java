package cn.seu.herald_android.mod_query.emptyroom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_query.lecture.LectureActivity;


public class FastQueryFragment extends Fragment {
    //查询今天的按钮
    Button btnQueryForToday;

    public FastQueryFragment() {
        // Required empty public constructor
    }

    //校区选择
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fast_query, container, false);


        //查询按钮设置
        btnQueryForToday = (Button) view.findViewById(R.id.btn_query_for_today);
        btnQueryForToday.setOnClickListener(v -> startActivity(new Intent(getContext(), LectureActivity.class)));
        return view;
    }

}
