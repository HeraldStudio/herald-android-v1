package cn.seu.herald_android.app_framework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.LinkedList;

public class UITableView extends FrameLayout {

    private ListView mListView;

    private boolean mIsGrouped;

    private LinkedList<View> reusableCellQueue = new LinkedList<>();

    /**
     * 暂时只有 DataSource, 里面自带了 Delegate 中的点击事件
     */
    public UITableViewDataSource dataSource;

    /**
     * 从布局文件中实例化的构造函数
     */
    public UITableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsGrouped = attrs.getAttributeBooleanValue("app", "grouped", false);
        mListView = new ListView(context);
        mListView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mListView.setAdapter(mAdapter);
        addView(mListView);
        reloadData();
    }

    public void reloadData() {
        mAdapter.notifyDataSetChanged();
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return numberOfRawItemsInListView();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    };

    /**
     * 计算转换成 Android ListView 的总条目数
     */
    public int numberOfRawItemsInListView() {
        int numberOfRawItems = 0;
        for (int i = 0; i < numberOfSectionsInTableView(); i++) {
            if (mIsGrouped) {
                /** 若分区, 要算上 Header, Header 下面的分割线, 以及 Footer */
                numberOfRawItems += 3;
            }
            /** 每一个 cell 及其下面的分割线 */
            numberOfRawItems += numberOfRowsInSection(i) * 2;
        }
        return numberOfRawItems;
    }

    /**
     * 调用代理类, 获取某分区中行数
     */
    public int numberOfRowsInSection(int section) {
        if (dataSource != null) {
            return dataSource.numberOfRowsInSection(section);
        }
        return 0;
    }

    /**
     * 调用代理类, 实例化对应的 Cell
     */
    public UITableViewCell cellForRowAtIndexPath(NSIndexPath indexPath) {
        if (dataSource != null) {
            return dataSource.cellForRowAtIndexPath(indexPath);
        }
        return null;
    }

    /**
     * 调用代理类, 获取分区数. 若不分区或代理类为空, 分区数为1
     */
    public int numberOfSectionsInTableView() {
        if (!mIsGrouped) {
            return 1;
        }
        if (dataSource != null) {
            return dataSource.numberOfSectionsInTableView();
        }
        return 1;
    }

    /**
     * 调用代理类, 获取分区头部标题
     */
    public String titleForHeaderInSection(int section) {
        if (dataSource != null) {
            return dataSource.titleForHeaderInSection(section);
        }
        return null;
    }

    /**
     * 调用代理类, 获取分区尾部标题
     */
    public String titleForFooterInSection(int section) {
        if (dataSource != null) {
            return dataSource.titleForFooterInSection(section);
        }
        return null;
    }

    /**
     * 调用代理类, 处理点击事件
     */
    public void onClickRowAtIndexPath(NSIndexPath indexPath) {
        if (dataSource != null) {
            dataSource.didSelectRowAtIndexPath(indexPath);
        }
    }
}
