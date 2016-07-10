package cn.seu.herald_android.helper;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class ApiRequest {

    public static final int CONN_TIMEOUT = 10000, READ_TIMEOUT = 10000;

    public enum Method {
        POST, GET
    }

    private String url;

    private Method method;

    private boolean noCheck200 = false;

    public ApiRequest() {
        //默认为post请求
        method = Method.POST;
    }

    public ApiRequest url(String url) {
        this.url = url;
        return this;
    }

    public ApiRequest api(String name) {
        return url(ApiHelper.getApiUrl(name));
    }

    public ApiRequest noCheck200() {
        noCheck200 = true;
        return this;
    }

    /**
     * 联网设置部分
     * builder  参数表
     **/
    private Map<String, String> map = new HashMap<>();

    public ApiRequest addUUID() {
        map.put("uuid", ApiHelper.getUUID());
        return this;
    }

    public ApiRequest post(String... map) {
        for (int i = 0; i < map.length / 2; i++) {
            String key = map[2 * i];
            String value = map[2 * i + 1];
            this.map.put(key, value);
        }
        return this;
    }

    public ApiRequest get(){
        method = Method.GET;
        return this;
    }

    /**
     * 一级回调设置部分
     * 一级回调只是跟OkHttpUtils框架之间的交互，并在此交互过程中为二级回调提供接口
     * 从此类外面看，不存在一级回调，只有二级回调和三级回调
     *
     * callback     默认的Callback（自动调用二级回调，若出错还会执行错误处理）
     **/
    private StringCallback callback = new StringCallback() {

        @Override
        public void onResponse(String response) {
            try {
                JSONObject json_res = new JSONObject(response);
                int code = json_res.getInt("code");
                if (noCheck200) {
                    for (OnFinishListener onFinishListener : onFinishListeners) {
                        onFinishListener.onFinish(true, code, response);
                    }
                } else {
                    if (code == 400) {
                        ApiHelper.doLogout("用户身份已过期,请重新登录");
                    }
                    for (OnFinishListener onFinishListener : onFinishListeners) {
                        onFinishListener.onFinish(code == 200, code, response);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                for (OnFinishListener onFinishListener : onFinishListeners) {
                    onFinishListener.onFinish(false, -1, response);
                }
            }
        }

        @Override
        public void onError(Call call, Exception e) {
            e.printStackTrace();
            for (OnFinishListener onFinishListener : onFinishListeners) {
                onFinishListener.onFinish(false, 0, e.toString());
            }
        }
    };

    /**
     * 二级回调设置部分
     * 二级回调是对返回状态和返回数据处理方式的定义，相当于重写Callback，
     * 但这里允许多个二级回调策略进行叠加，因此比Callback更灵活
     * <p>
     * onFinishListeners    二级回调接口，内含一个默认的回调操作，该操作仅在设置了三级回调策略时有效
     **/

    public interface OnFinishListener {
        void onFinish(boolean success, int code, String response);
    }

    private ArrayList<OnFinishListener> onFinishListeners = new ArrayList<>();

    // 外部可调用
    public ApiRequest onFinish(OnFinishListener listener) {
        this.onFinishListeners.add(listener);
        return this;
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
        Object parse(JSONObject src) throws JSONException;
    }

    public ApiRequest toCache(String key) {
        return toCache(key, src -> src, null);
    }

    public ApiRequest toCache(String key, JSONParser parser) {
        return toCache(key, parser, null);
    }

    public ApiRequest toCache(String key, AppModule notifyModuleIfChanged) {
        return toCache(key, o -> o, notifyModuleIfChanged);
    }

    // 若对应的缓存发生了改变, 向对应的模块缓存中保存"已改动"的标记
    // 目前暂时只有CacheHelper有更新检测机制，如果另外两个也需要该机制，请修改对应的Helper的setCache函数
    public ApiRequest toCache(String key, JSONParser parser, AppModule notifyModuleIfChanged) {
        onFinish((success, code, response) -> {
            if (success) {
                try {
                    String cache = parser.parse(new JSONObject(response)).toString();
                    if (CacheHelper.set(key, cache) && notifyModuleIfChanged != null) {
                        notifyModuleIfChanged.hasUpdates.set(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    for (OnFinishListener onFinishListener : onFinishListeners) {
                        onFinishListener.onFinish(false, 0, "");
                    }
                }
            }
        });
        return this;
    }

    public ApiRequest toServiceCache(String key) {
        return toServiceCache(key, o -> o);
    }

    public ApiRequest toServiceCache(String key, JSONParser parser) {
        onFinish((success, code, response) -> {
            if (success) {
                try {
                    String cache = parser.parse(new JSONObject(response)).toString();
                    ServiceHelper.set(key, cache);
                } catch (JSONException e) {
                    e.printStackTrace();
                    for (OnFinishListener onFinishListener : onFinishListeners) {
                        onFinishListener.onFinish(false, 0, "");
                    }
                }
            }
        });
        return this;
    }

    public ApiRequest toAuthCache(String key) {
        return toAuthCache(key, o -> o);
    }

    public ApiRequest toAuthCache(String key, JSONParser parser) {
        onFinish((success, code, response) -> {
            if (success) {
                try {
                    String cache = parser.parse(new JSONObject(response)).toString();
                    ApiHelper.setAuthCache(key, cache);
                } catch (JSONException e) {
                    e.printStackTrace();
                    for (OnFinishListener onFinishListener : onFinishListeners) {
                        onFinishListener.onFinish(false, 0, "");
                    }
                }
            }
        });
        return this;
    }



    /**
     * 执行部分
     **/
    public void run() {
        switch (method){
            case GET:
                OkHttpUtils.get().url(url).params(map).build()
                        .connTimeOut(CONN_TIMEOUT).readTimeOut(READ_TIMEOUT)
                        .execute(callback);
                break;
            case POST:
                OkHttpUtils.post().url(url).params(map).build()
                        .connTimeOut(CONN_TIMEOUT).readTimeOut(READ_TIMEOUT)
                        .execute(callback);
                break;
        }
    }

}
