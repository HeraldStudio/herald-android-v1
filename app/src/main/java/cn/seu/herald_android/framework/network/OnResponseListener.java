package cn.seu.herald_android.framework.network;

/**
 * Lambda 类型，表示表示当前请求中各个简单请求结束的事件。
 * 若当前请求就是简单请求，则只触发一次，相当于请求结束的事件；
 * 若当前请求是复合请求，其中包含的每个简单请求结束时都会触发一次
 */
public interface OnResponseListener {
    void processResponse(boolean success, int code, String response);
}
