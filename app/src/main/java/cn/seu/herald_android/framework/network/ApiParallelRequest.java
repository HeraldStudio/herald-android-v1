package cn.seu.herald_android.framework.network;

import java.util.LinkedList;

/**
 * ApiParallelRequest | 同时请求
 * <p>
 * 利用 request1.parallel(request2) 运算可得到一个 ApiParallelRequest
 * 所有子请求同时开始执行，直到最后结束的请求结束。
 * 仅当所有子请求都执行成功，才视为 ApiParallelRequest 执行成功。
 **/
public class ApiParallelRequest extends ApiRequest {

    private static final int SIDE_LEFT = 0, SIDE_RIGHT = 1;

    private final ApiRequest[] requests;

    private boolean[] finished = {false, false};

    private int code = 0;

    public ApiParallelRequest(ApiRequest left, ApiRequest right) {
        requests = new ApiRequest[]{left, right};

        requests[SIDE_LEFT].onFinish((success, code) ->
                invokeCallback(code, SIDE_LEFT));

        requests[SIDE_RIGHT].onFinish((success, code) ->
                invokeCallback(code, SIDE_RIGHT));
    }

    private synchronized void invokeCallback(int code, int finishSide) {
        finished[finishSide] = true;

        // 首先更新复合请求的 code
        this.code = NetworkUtil.mergeStatusCodes(this.code, code);

        for (boolean finish : finished) {
            if (!finish) return;
        }

        for (OnFinishListener listener : onFinishListeners) {
            listener.onFinish(this.code < 300, this.code);
        }
    }

    public ApiRequest onResponse(OnResponseListener listener) {
        for (ApiRequest request : requests) {
            request.onResponse(listener);
        }
        return this;
    }

    private LinkedList<OnFinishListener> onFinishListeners = new LinkedList<>();

    public ApiRequest onFinish(OnFinishListener listener) {
        onFinishListeners.add(listener);
        return this;
    }

    public void runWithoutFatalListener() {
        for (ApiRequest request : requests) {
            request.runWithoutFatalListener();
        }
    }

    public void run() {
        NetworkUtil.addFatalErrorListenerInOnFinishList(onFinishListeners);
        runWithoutFatalListener();
    }
}
