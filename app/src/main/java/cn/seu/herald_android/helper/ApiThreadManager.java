package cn.seu.herald_android.helper;

import android.content.Context;
import android.os.Handler;

import java.util.Vector;

import cn.seu.herald_android.custom.ContextUtils;

public class ApiThreadManager {
    private Vector<ApiRequest> requests = new Vector<>();

    private OnFinishListener onFinish = (success) -> {
    };

    private ApiRequest.OnResponseListener onResponse = (success, code, response) -> {
    };

    private Vector<Exception> exceptionPool = new Vector<>();

    public ApiThreadManager add(ApiRequest request) {
        // 吃掉该线程的消息显示
        request.exceptionPool(exceptionPool);
        request.onFinish((success, code, response) -> {
            requests.remove(request);
            onResponse.onFinish(success, code, response);
            if (requests.size() == 0)
                onFinish.handle(exceptionPool.size() == 0);
        });
        requests.add(request);
        return this;
    }

    public ApiThreadManager addAll(ApiRequest[] requests) {
        for (ApiRequest request : requests) {
            add(request);
        }
        return this;
    }

    public ApiThreadManager onResponse(ApiRequest.OnResponseListener listener) {
        onResponse = listener;
        return this;
    }

    public ApiThreadManager onFinish(OnFinishListener listener) {
        onFinish = listener;
        return this;
    }

    public void flushExceptions(Context context, String message) {
        if (exceptionPool.size() != 0) {
            new Handler().postDelayed(() -> ContextUtils.showMessage(context, message), 500);
        }
    }

    public void run() {
        for (ApiRequest request : requests) {
            request.run();
        }
    }

    public interface OnFinishListener {
        void handle(boolean success);
    }
}
