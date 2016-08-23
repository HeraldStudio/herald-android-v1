package cn.seu.herald_android.app_module.gymreserve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.EmptyTipArrayAdapter;
import cn.seu.herald_android.framework.json.JObj;

public class FriendModel implements Serializable {
    String nameDepartment;
    String userId;
    String userInfo;

    public FriendModel(String nameDepartment, String userId, String userInfo) {
        this.nameDepartment = nameDepartment;
        this.userId = userId;
        this.userInfo = userInfo;
    }

    public FriendModel(JObj obj) {
        this(obj.$s("nameDepartment"),
                obj.$s("userId"),
                obj.$s("userInfo"));
    }

    public JObj getJObj() {
        JObj obj = new JObj();
        obj.put("nameDepartment",nameDepartment);
        obj.put("userId",userId);
        obj.put("userInfo",userInfo);
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FriendModel) {
            FriendModel f = (FriendModel) o;
            return f.nameDepartment.equals(nameDepartment) && f.userId.equals(userId) && f.userInfo.equals(userInfo);
        }
        return false;
    }

    // 搜索结果列表展示用Adapter
    public static class FriendAdapter extends EmptyTipArrayAdapter<FriendModel> {
        int resource;

        public FriendAdapter(Context context, int resource, List<FriendModel> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView) {
            FriendModel friendModel = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_gymreserve__search_friend__item,null);
            TextView tv_name = (TextView)convertView.findViewById(R.id.tv_friendname);
            TextView tv_department = (TextView)convertView.findViewById(R.id.tv_frienddepartment);
            try {
                tv_name.setText(friendModel.nameDepartment.split("\\(")[0]);
                tv_department.setText(friendModel.nameDepartment.split("\\(")[1].split("\\)")[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                tv_name.setText(friendModel.nameDepartment);
            }

            return convertView;
        }
    }




}


