package cn.seu.herald_android.mod_query.curriculum;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 水平滑动分页的适配器，修改时很容易出错，请慎重操作
 */
public class PagesAdapter extends PagerAdapter {

    private List<View> viewList;

    private List<String> titleList;

    private List<String> pageTermList;

    private int defaultPage = 0;

    public enum State {
        NORMAL, // 正常有课状态
        EMPTY_TERM, // 整个学期都没课的奇怪状态（比如新生军训短学期）
        HOLIDAY, // 放假没课状态
        HOLIDAY_LAST_TERM // 放假没课，而且以后所有学期都没课的状态
    }

    private State state;

    // 构造函数
    public PagesAdapter(Context context, ArrayList<TermInfo> data, int thisWeek) {
        try {
            // 初始化视图链表
            viewList = new ArrayList<>();
            titleList = new ArrayList<>();
            pageTermList = new ArrayList<>();

            for (TermInfo termInfo : data) {
                int maxWeek = 0;

                // 读取json内容
                JSONObject content = new JSONObject(termInfo.data);

                // 计算总周数
                for (String weekNum : CurriculumScheduleLayout.WEEK_NUMS) {
                    JSONArray arr = content.getJSONArray(weekNum);
                    for (int i = 0; i < arr.length(); i++) {
                        ClassInfo info = new ClassInfo(arr.getJSONArray(i));
                        if (info.getEndWeek() > maxWeek) maxWeek = info.getEndWeek();
                    }
                }
                termInfo.weekCount = maxWeek;

                Map<String, Pair<String, String>> sidebarInfo = new HashMap<>();

                // 计算一下当前学期的状态问题
                if(termInfo.currentTerm) {
                    /* 考虑四种状态：
                    *  (1)本周有课或以后某周有课
                    *  (2)本学期已结束（获取当前周数失败）
                    *  (3)本学期未结束（能获取当前周数），但本学期课程全部结束；
                    *  (4)整个学期都没课
                    * */

                    // 下面将第二种情况与第三种情况合并，因为它们现象不同但原因类似，可以共用同一个提示信息
                    if (thisWeek > maxWeek) thisWeek = 0;

                    // 把这两种现象统称为holiday
                    state = thisWeek == 0 ? State.HOLIDAY : State.NORMAL;

                    // 将课程的授课教师和学分信息放入键值对
                    JSONArray sidebar = termInfo.sidebar;
                    for(int i = 0; i < sidebar.length(); i++){
                        JSONObject obj = sidebar.getJSONObject(i);
                        sidebarInfo.put(obj.getString("course"),
                                new Pair<>(obj.getString("lecturer"), obj.getString("credit")));
                    }
                }

                // 实例化各页
                if (maxWeek == 0){
                    CurriculumScheduleLayout schedule =
                            new CurriculumScheduleLayout(context,
                                    content, sidebarInfo, 1,
                                    false, termInfo.currentTerm);
                    viewList.add(schedule);
                    titleList.add(termInfo.toString() + "丨本学期无课程");
                    pageTermList.add(termInfo.toString());
                } else for (int i = 1; i <= maxWeek; i++) {
                    CurriculumScheduleLayout schedule =
                            new CurriculumScheduleLayout(context,
                                    content, sidebarInfo, i,
                                    termInfo.currentTerm && i == thisWeek, termInfo.currentTerm);
                    viewList.add(schedule);
                    titleList.add(termInfo.toString() + "丨第" + i + "周");
                    pageTermList.add(termInfo.toString());
                }
            }

            // 计算当前周所在的页数
            for (TermInfo termInfo : data) {
                if (termInfo.currentTerm) {
                    // 若为holiday（参照上面注释中的两种情况），并且猜测本学期已经结束而不是还未开始，
                    // 则返回下一学期的第一周（可能溢出）；否则返回当前周
                    defaultPage += state == State.HOLIDAY && isTermFinished(termInfo.toString())
                            ? Math.max(1, termInfo.weekCount) : Math.max(thisWeek - 1, 0);
                    break;
                }
                defaultPage += Math.max(1, termInfo.weekCount);
            }

            // 为了防止上一步产生溢出，这里做溢出保护
            if (defaultPage >= viewList.size()) {
                defaultPage = viewList.size() - 1;
                // 这时就是后面所有学期都没课的状态了
                state = State.HOLIDAY_LAST_TERM;
            }

        } catch (JSONException e) {
            Toast.makeText(context, "数据错误，请尝试刷新", Toast.LENGTH_SHORT).show();
        }
    }

    public int getDefaultPage() {
        return defaultPage;
    }

    public State getState() {
        return state;
    }

    public int getPageForTermName(String name){
        return pageTermList.indexOf(name);
    }

    public String getTitleForPage(int page) {
        return titleList.get(page);
    }

    public static boolean isTermFinished(String term){
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        if(term.endsWith("3")) return month >= 6;
        if(term.endsWith("2")) return month <= 3;
        if(term.endsWith("1")) return month >= 9;
        return false;
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = viewList.get(position);
        container.addView(v);

        return v;
    }
}
