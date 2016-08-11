package cn.seu.herald_android.framework;

import cn.seu.herald_android.framework.network.ApiEmptyRequest;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.framework.network.OnFinishListener;
import cn.seu.herald_android.helper.CacheHelper;

public class AppCache {

    /**
     * 缓存键名
     */
    public final String key;

    /**
     * 设置缓存值
     */
    public void setValue(String newValue) {
        CacheHelper.set(key, newValue);
    }

    /**
     * 获取缓存值
     */
    public String getValue() {
        return CacheHelper.get(key);
    }

    /**
     * 由于同一个 ApiRequest 实例不能多次使用，所以需要存储一个能动态创建新 ApiRequest 实例的闭包
     */
    public interface RequestFactory {
        ApiRequest createRequest();
    }

    private final RequestFactory refresherCreator;

    /**
     * 每次取 refresher 变量时，按照当前 cache 的要求动态创建一个 ApiRequest 并返回
     */
    public ApiRequest getRefresher() {
        return refresherCreator.createRequest();
    }

    /**
     * 缓存是否为空
     */
    public boolean isEmpty() {
        return getValue().equals("");
    }

    /**
     * 构造函数
     */
    public AppCache(String key, RequestFactory refresher) {
        this.key = key;
        this.refresherCreator = refresher;
    }

    public AppCache(String key) {
        this.key = key;
        this.refresherCreator = ApiEmptyRequest::new;
    }

    /**
     * 刷新函数，可以直接无参数调用或者带一个 OnFinishListener 的 Lanbda
     */
    public void refresh(OnFinishListener onFinishListener) {
        if (onFinishListener != null) {
            getRefresher().onFinish(onFinishListener).run();
        } else {
            getRefresher().run();
        }
    }

    public void refresh() {
        refresh(null);
    }

    /**
     * 刷新函数，当缓存为空时刷新
     */
    public void refreshIfEmpty(OnFinishListener onFinishListener) {
        if (isEmpty()) {
            refresh(onFinishListener);
        }
    }

    public void refreshIfEmpty() {
        refreshIfEmpty(null);
    }

    /**
     * 设为空
     */
    public void clear() {
        setValue("");
    }
}
