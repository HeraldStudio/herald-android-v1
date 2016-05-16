package cn.seu.herald_android.mod_query.gymreserve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/5/14.
 */
public class Friend implements Serializable{
    String nameDepartment;
    String userId;
    String userInfo;

    public Friend(String nameDepartment, String userId, String userInfo) {
        this.nameDepartment = nameDepartment;
        this.userId = userId;
        this.userInfo = userInfo;
    }

    public Friend(JSONObject obj) throws JSONException {
        this(obj.getString("nameDepartment"),
                obj.getString("userId"),
                obj.getString("userInfo"));
    }

    public JSONObject getJSONObject() throws JSONException{
        JSONObject obj = new JSONObject();
        obj.put("nameDepartment",nameDepartment);
        obj.put("userId",userId);
        obj.put("userInfo",userInfo);
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Friend){
            Friend f = (Friend)o;
            return f.nameDepartment.equals(nameDepartment) && f.userId.equals(userId) && f.userInfo.equals(userInfo);
        }
        return false;
    }

    //搜索结果列表展示用Adapter
    public static class FriendAdapter extends ArrayAdapter<Friend> {
        int resource;
        public FriendAdapter(Context context, int resource, List<Friend> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Friend friend = getItem(position);
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitem_gym_searchfriend,null);
            TextView tv_name = (TextView)convertView.findViewById(R.id.tv_friendname);
            TextView tv_department = (TextView)convertView.findViewById(R.id.tv_frienddepartment);
            try{
                tv_name.setText(friend.nameDepartment.split("\\(")[0]);
                tv_department.setText(friend.nameDepartment.split("\\(")[1].split("\\)")[0]);
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
                tv_name.setText(friend.nameDepartment);
            }

            return convertView;
        }
    }




}


