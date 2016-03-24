package cn.seu.herald_android.helper;

import java.util.Vector;

public class ApiThreadManager {
    private Vector<ApiRequest> requests;

    private Runnable onFinish = () -> {
    };

    public ApiThreadManager add(ApiRequest request) {
        request.onFinish((success, response) -> {
            requests.remove(request);
            if (requests.size() == 0)
                onFinish.run();
        });
        requests.add(request);
        return this;
    }

    public ApiThreadManager onFinish(Runnable runnable) {
        onFinish = runnable;
        return this;
    }

    public void run() {
        for (ApiRequest request : requests) {
            request.run();
        }
    }
}
