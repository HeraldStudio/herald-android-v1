package cn.seu.herald_android.framework.network;

import java.util.LinkedList;

/**
 * ApiEmptyRequest | 空请求
 * 请求运算中的单位元，任何请求与空请求做运算都得到其本身。
 **/
public class ApiEmptyRequest extends ApiRequest {

    private LinkedList<OnResponseListener> onResponseListeners = new LinkedList<>();

    public ApiRequest onResponse(OnResponseListener listener) {
        onResponseListeners.add(listener);
        return this;
    }

    public ApiRequest onFinish(OnFinishListener listener) {
        return onResponse((success, code, response) -> listener.onFinish(success, code));
    }

    public void runWithoutFatalListener() {
        for (OnResponseListener listener : onResponseListeners) {
            listener.onResponse(true, 200, "Warning: This is an empty request.");
        }
    }

    @Override
    public void run() {
        runWithoutFatalListener();
    }
}
