package cn.seu.herald_android.framework.network;
/**
 * ApiRequest | 通用联网请求接口
 * <p>
 * 设计要求：
 * 以 SimpleApiRequest(简单请求) 为单元，通过 chain(顺次执行) 和 parallel(同时执行) 两种
 * 运算，可以得到满足不同需求的复合请求，而复合请求又可以作为新的单元，形成更大的复合请求。
 **/

/** 空请求、简单请求、顺次复合请求、同时复合请求都要实现该接口，以保证这种递归式的多态性 */
public abstract class ApiRequest {

    public abstract ApiRequest onResponse(OnResponseListener listener);

    public abstract ApiRequest onFinish(OnFinishListener listener);

    public ApiRequest chain(ApiRequest nextRequest) {
        return new ApiChainRequest(this, nextRequest);
    }

    public ApiRequest parallel(ApiRequest anotherRequest) {
        return new ApiParallelRequest(this, anotherRequest);
    }

    /**
     * 不添加 4xx 错误监听器，直接运行.
     * 该函数用于外层复合请求调用内层请求时使用，防止 4xx 错误监听器重复添加。
     * 在需要忽略 4xx 错误的情况下，此函数也可以从外部调用。
     */
    public abstract void runWithoutFatalListener();

    /** 添加 4xx 错误监听器并运行 */
    public abstract void run();
}
