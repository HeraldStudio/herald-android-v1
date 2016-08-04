package cn.seu.herald_android.framework.network;
/**
 * ApiRequest | 通用联网请求接口
 * <p>
 * 设计要求：
 * 以 SimpleApiRequest(简单请求) 为单元，通过 chain(顺次执行) 和 parallel(同时执行) 两种
 * 运算，可以得到满足不同需求的复合请求，而复合请求又可以作为新的单元，形成更大的复合请求。
 **/

/** 空请求、简单请求、顺次复合请求、同时复合请求都要实现该接口，以保证这种递归式的多态性 */
public interface ApiRequest {

    ApiRequest onResponse(OnResponseListener listener);

    ApiRequest onFinish(OnFinishListener listener);

    ApiRequest chain(ApiRequest nextRequest);

    ApiRequest parallel(ApiRequest anotherRequest);

    /**
     * 不添加 4xx 错误监听器，直接运行.
     * 该函数用于外层复合请求调用内层请求时使用，防止 4xx 错误监听器重复添加。
     * 在需要忽略 4xx 错误的情况下，此函数也可以从外部调用。
     */
    void runWithoutFatalListener();

    /** 添加 4xx 错误监听器并运行 */
    void run();
}
