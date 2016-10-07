package cn.seu.herald_android.framework.network;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ApiSimpleRequest | 简单请求
 * 网络请求的一个基本单元，包含一次请求和一次回调。
 **/
public class ApiSimpleRequest extends ApiRequest {

    private Method method;

    /**
     * 构造部分
     **/
    public ApiSimpleRequest(Method method) {
        this.method = method;
    }

    private String url;

    public ApiSimpleRequest url(String url) {
        this.url = url;
        return this;
    }

    public ApiSimpleRequest api(String name) {
        return url(ApiHelper.getApiUrl(name));
    }

    /**
     * 联网设置部分
     * builder  参数表
     **/
    private FormBody.Builder body = new FormBody.Builder();

    public ApiSimpleRequest addUuid() {
        body.add("uuid", ApiHelper.getCurrentUser().uuid);
        return this;
    }

    public ApiSimpleRequest post(String... map) {
        for (int i = 0; i < map.length / 2; i++) {
            String key = map[2 * i];
            String value = map[2 * i + 1];
            this.body.add(key, value);
        }
        return this;
    }

    /**
     * 一级回调设置部分
     * 一级回调只是跟OkHttpUtils框架之间的交互，并在此交互过程中为二级回调提供接口
     * 从此类外面看，不存在一级回调，只有二级回调和三级回调
     * <p>
     * callback     默认的Callback（自动调用二级回调，若出错还会执行错误处理）
     **/
    private Callback callback = new Callback() {

        @Override
        public void onResponse(Call call, Response response) {
            try {
                // 解析 HTTP Response Status Code
                int httpCode = response.code();

                // 取返回的字符串值
                String responseString = response.body().string();

                // 将 HTTP Response Status Code 与 JSON code 合并, 取其中较严重的一个。
                // 若返回字符串无法解析成 JSON, 按照我们的 JSON 解析库的实现, 后一个参数将为0, 仍取前一个参数作为 code
                final int code = NetworkUtil.mergeStatusCodes(httpCode, new JObj(responseString).$i("code"));

                // 按照错误码判断是否成功
                boolean success = code < 400;
                if (!success) {
                    Log.w("Herald_Android", code + " Response : " + responseString);
                }

                // 触发回调
                uiThreadHandler.post(() -> {
                    for (OnResponseListener listener : onResponseListeners) {
                        listener.onResponse(success, code, responseString);
                    }
                });
            } catch (IOException e) {
                onFailure(call, e);
            }
        }

        @Override
        public void onFailure(Call call, IOException e) {
            uiThreadHandler.post(() -> {
                for (OnResponseListener listener : onResponseListeners) {
                    listener.onResponse(false, 500, "I/O Error");
                    Log.w("Herald_Android", 500 + " I/O Error : " + e);
                }
            });
        }
    };

    private Handler uiThreadHandler = new Handler();

    /**
     * 二级回调设置部分
     * 二级回调是对返回状态和返回数据处理方式的定义，相当于重写Callback，
     * 但这里允许多个二级回调策略进行叠加，因此比Callback更灵活
     * <p>
     * onFinishListeners    二级回调接口，内含一个默认的回调操作，该操作仅在设置了三级回调策略时有效
     **/

    private LinkedList<OnResponseListener> onResponseListeners = new LinkedList<>();

    public ApiRequest onResponse(OnResponseListener listener) {
        onResponseListeners.add(listener);
        return this;
    }

    public ApiRequest onFinish(OnFinishListener listener) {
        return onResponse((success, code, response) -> listener.onFinish(success, code));
    }

    /**
     * 三级回调设置部分
     * 三级回调是对一些比较典型的回调策略的包装，此处暂时只实现了将数据存入缓存这一种三级回调策略
     * <p>
     * JSONParser   将原始数据转换为要存入缓存的目标数据的中转过程
     * toCache      将目标数据存入缓存的回调策略
     * toCache()    用于设置三级回调策略的函数
     **/

    public interface JSONParser {
        Object parse(JObj src);
    }

    public ApiSimpleRequest toCache(String key) {
        return toCache(key, src -> src);
    }

    public ApiSimpleRequest toCache(String key, JSONParser parser) {
        onResponse((success, code, response) -> {
            if (success) {
                CacheHelper.set(key, parser.parse(new JObj(response)).toString());
            }
        });
        return this;
    }

    public ApiSimpleRequest toServiceCache(String key) {
        return toServiceCache(key, o -> o);
    }

    public ApiSimpleRequest toServiceCache(String key, JSONParser parser) {
        onResponse((success, code, response) -> {
            if (success) {
                String cache = parser.parse(new JObj(response)).toString();
                ServiceHelper.set(key, cache);
            }
        });
        return this;
    }

    /**
     * 执行部分
     **/
    public void runWithoutFatalListener() {
        switch (method) {
            case GET:
                getClientInstance().newCall(
                        new Request.Builder().url(url).build()
                ).enqueue(callback);
                break;
            case POST:
                getClientInstance().newCall(
                        new Request.Builder().url(url).post(body.build()).build()
                ).enqueue(callback);
                break;
        }
    }

    public void run() {
        NetworkUtil.addFatalErrorListenerInOnResponseList(onResponseListeners);
        runWithoutFatalListener();
    }

    private static OkHttpClient clientInstance = null;

    public static OkHttpClient getClientInstance() {
        if (clientInstance == null) {
            clientInstance = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS).build();
        }
        return clientInstance;
    }
}
