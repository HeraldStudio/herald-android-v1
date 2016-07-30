package cn.seu.herald_android.framework.network;

/**
 * ApiEmptyRequest | 空请求
 * 请求运算中的单位元，任何请求与空请求做运算都得到其本身。
 **/
public class ApiEmptyRequest implements ApiRequest {
    public ApiRequest onResponse(OnResponseListener listener) {
        return this;
    }

    public ApiRequest onFinish(OnFinishListener listener) {
        return this;
    }

    public ApiRequest chain(ApiRequest nextRequest) {
        return nextRequest;
    }

    public ApiRequest parallel(ApiRequest anotherRequest) {
        return anotherRequest;
    }

    public void runWithoutFatalListener() {
        // do nothing
    }

    @Override
    public void run() {
        // do nothing
    }
}
