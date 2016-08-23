package cn.seu.herald_android.framework.network;

/**
 * Lambda 类型，表示整个请求结束的事件。
 * 由于请求结束的事件可能是简单请求结束，也可能是复合请求结束，
 * 而复合请求作为一个整体，本身没有 code 和 response 值，
 * 这里定义：** 一个复合请求中所有简单请求返回的 code 的最大值，作为这个复合请求的 code。**
 * 所以这个 listener 有两个参数。
 * 如要监听简单请求结束的事件，可使用OnResponseListener
 */
public interface OnFinishListener {
    void onFinish(boolean allSuccess, int mostCriticalCode);
}
