package cn.seu.herald_android.framework.network;

import java.util.LinkedList;

/**
 * ApiChainRequest | 短路顺次请求
 * <p>
 * 利用 request1.chain(request2) 运算可得到一个 ApiChainRequest
 * 当前一个子请求执行完毕后，判断其是否执行成功，若执行成功则启动后一个子请求，直到所有子请求结束。
 * 仅当所有子请求都执行成功，才视为 ApiChainRequest 执行成功。
 * 此请求是短路的，即左边的请求如果失败，将不会继续向右执行。
 */
public class ApiChainRequest extends ApiRequest {

    private ApiRequest leftRequest;

    private ApiRequest rightRequest;

    private int code = 0;

    public ApiChainRequest(ApiRequest left, ApiRequest right) {
        leftRequest = left;
        rightRequest = right;

        leftRequest.onFinish((success, code) -> {

            // 首先更新复合请求的 code
            this.code = NetworkUtil.mergeStatusCodes(this.code, code);

            // 若前一个请求成功，运行下一个请求
            if (success) {
                rightRequest.runWithoutFatalListener();
            } else {
                // 否则直接报告请求结束
                for (OnFinishListener listener : onFinishListeners) {
                    listener.onFinish(this.code < 300, this.code);
                }
            }
        });

        rightRequest.onFinish((success, code) -> {

            // 首先更新复合请求的 code
            this.code = NetworkUtil.mergeStatusCodes(this.code, code);

            // 报告请求结束
            for (OnFinishListener listener : onFinishListeners) {
                listener.onFinish(this.code < 300, this.code);
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

    public void runWithoutFatalListener() {
        leftRequest.runWithoutFatalListener();
    }

    public void run() {
        NetworkUtil.addFatalErrorListenerInOnFinishList(onFinishListeners);
        runWithoutFatalListener();
    }
}
