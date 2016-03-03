package cn.seu.herald_android.mod_query.grade;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.colorizers.TableDataRowColorizer;

/**
 * Created by heyon on 2016/2/28.
 */
public class GradeItemDataAdapter extends TableDataAdapter<GradeItem> {
    public GradeItemDataAdapter(Context context, List<GradeItem> data) {
        super(context, data);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        //返回一行中各列的view
        GradeItem gradeItem = getRowData(rowIndex);
        View renderedView = null;
        switch (columnIndex) {
            case 0:
                //课程名称
                renderedView = renderSimpleTextView(gradeItem.getName());
                break;
            case 1:
                //课程学期
                renderedView = renderSimpleTextView(gradeItem.getSemester().semester);
                break;
            case 2:
                //课程成绩
                renderedView = renderSimpleTextView(gradeItem.getScore());
                break;
            case 3:
                //课程类型
                renderedView = renderSimpleTextView(gradeItem.getType());
                break;
            case 4:
                //课程学分
                renderedView = renderSimpleTextView(gradeItem.getCredit() + "");
                break;
        }
        return renderedView;
    }



    public View renderSimpleTextView(String text){
        //为子项加载布局
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.tableviewitem_grade, null);
//        TextView tv_cell =(TextView)view.findViewById(R.id.tv_gradetableitem);
//        tv_cell.setText(text);
        TextView tv_cell = new TextView(getContext());
        tv_cell.setText(text);
        tv_cell.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        tv_cell.setPadding(10, 10, 10, 10);
        tv_cell.setGravity(Gravity.CENTER);
        return tv_cell;
    }

    public static class GradeRowColorizer implements TableDataRowColorizer<GradeItem> {
        Context context;
        public GradeRowColorizer(Context context){
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        //用于设置行颜色的类
        @Override
        public int getRowColor(int rowIndex, GradeItem rowData) {
            if(rowIndex%2==0){
                //如果是偶数列则设置颜色为淡绿色
                return getContext().getResources().getColor(R.color.white);
            }else{
                return getContext().getResources().getColor(R.color.colorPrimaryGreenDark);
            }
        }
    }


}
