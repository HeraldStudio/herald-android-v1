package cn.seu.herald_android.framework.network;

import java.util.LinkedList;

/**
 * ApiParallelRequest | 同时请求
 * <p>
 * 利用 request1.parallel(request2) 运算可得到一个 ApiParallelRequest
 * 所有子请求同时开始执行，直到最后结束的请求结束。
 * 仅当所有子请求都执行成功，才视为 ApiParallelRequest 执行成功。
 **/
public class ApiParallelRequest implements ApiRequest {

    private ApiRequest leftRequest;

    private boolean leftFinished = false;

    private ApiRequest rightRequest;

    private boolean rightFinished = false;

    private int code = 0;

    public ApiParallelRequest(ApiRequest left, ApiRequest right) {
        leftRequest = left;
        rightRequest = right;

        leftRequest.onFinish((success, code) -> {
            synchronized (this) {
                leftFinished = true;

                // 首先更新复合请求的 code
                this.code = NetworkUtil.mergeStatusCodes(this.code, code);

                if (rightFinished) {
                    for (OnFinishListener listener : onFinishListeners) {
                        listener.parseFinish(this.code < 300, this.code);
                    }
                }
            }
        });

        rightRequest.onFinish((success, code) -> {
            synchronized (this) {
                rightFinished = true;

                // 首先更新复合请求的 code
                this.code = NetworkUtil.mergeStatusCodes(this.code, code);

                if (leftFinished) {
                    for (OnFinishListener listener : onFinishListeners) {
                        listener.parseFinish(this.code < 300, this.code);
                    }
                }
            }
        });
    }

    public ApiRequest onResponse(OnResponseListener listener) {
        leftRequest.onResponse(listener);
        rightRequest.onResponse(listener);
        return this;
    }

    private LinkedList<OnFinishListener> onFinishListeners = new LinkedList<>();

    public ApiRequest onFinish(OnFinishListener listener) {
        onFinishListeners.add(listener);
        return this;
    }

    public ApiRequest chain(ApiRequest nextRequest) {
        return new ApiChainRequest(this, nextRequest);
    }

    public ApiRequest parallel(ApiRequest anotherRequest) {
        return new ApiParallelRequest(this, anotherRequest);
    }

    public void runWithoutFatalListener() {
        leftRequest.runWithoutFatalListener();
        rightRequest.runWithoutFatalListener();
    }

    public void run() {
        NetworkUtil.addFatalErrorListenerInOnFinishList(onFinishListeners);
        runWithoutFatalListener();
    }
}
