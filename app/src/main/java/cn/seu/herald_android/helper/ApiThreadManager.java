package cn.seu.herald_android.helper;

import java.util.Vector;

public class ApiThreadManager {

    private Vector<ApiRequest> requests = new Vector<>();

    private OnFinishListener onFinish = (success) -> {
    };

    private ApiRequest.OnFinishListener onResponse = (success, code, response) -> {
    };

    private boolean hasFailure = false;

    public ApiThreadManager add(ApiRequest request) {
        // 吃掉该线程的消息显示
        request.onFinish((success, code, response) -> {
            requests.remove(request);
            onResponse.onFinish(success, code, response);
            if (!success) {
                hasFailure = true;
            }
            if (requests.size() == 0) {
                onFinish.handle(!hasFailure);
            }
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

    public ApiThreadManager onResponse(ApiRequest.OnFinishListener listener) {
        onResponse = listener;
        return this;
    }

    public ApiThreadManager onFinish(OnFinishListener listener) {
        onFinish = listener;
        return this;
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
