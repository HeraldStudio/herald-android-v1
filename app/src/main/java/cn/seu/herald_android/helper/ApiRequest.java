package cn.seu.herald_android.helper;

import android.content.Context;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import okhttp3.Call;

public class ApiRequest {

    public static final int CONN_TIMEOUT = 10000, READ_TIMEOUT = 10000;

    /**
     * 构造部分
     * context  当前上下文
     * exceptionPool   是否吞掉错误消息
     * url      请求的目标url
     **/
    private Context context;

    private Vector<Exception> exceptionPool = null;

    private String url;

    // 外部可调用
    public ApiRequest(Context context) {
        this.context = context;
    }

    // 外部可调用
    public ApiRequest exceptionPool(Vector<Exception> pool) {
        exceptionPool = pool;
        return this;
    }

    // 外部可调用
    public ApiRequest url(String url) {
        this.url = url;
        return this;
    }

    // 外部可调用
    public ApiRequest api(int api) {
        return url(ApiHelper.getApiUrl(api));
    }

    /**
     * 联网设置部分
     * builder  参数表
     **/
    private Map<String, String> map = new HashMap<>();

    // 外部可调用
    public ApiRequest uuid() {
        map.put("uuid", new ApiHelper(context).getUUID());
        return this;
    }

    // 外部可调用
    public ApiRequest post(String... map) {
        for (int i = 0; i < map.length / 2; i++) {
            String key = map[2 * i];
            String value = map[2 * i + 1];
            this.map.put(key, value);
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
    private StringCallback callback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {
            if (exceptionPool != null) {
                exceptionPool.add(e);
            } else {
                new ApiHelper(context).dealApiException(e);
            }
            for (OnFinishListener onFinishListener : onFinishListeners) {
                onFinishListener.onFinish(false, 0, e.toString());
            }
        }

        @Override
        public void onResponse(String response) {
            try {
                JSONObject json_res = new JSONObject(response);
                for (OnFinishListener onFinishListener : onFinishListeners) {
                    onFinishListener.onFinish(json_res.getInt("code") == 200, json_res.getInt("code"), response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (exceptionPool != null) {
                    exceptionPool.add(e);
                } else {
                    new ApiHelper(context).dealApiException(e);
                }
                for (OnFinishListener onFinishListener : onFinishListeners) {
                    onFinishListener.onFinish(false, -1, e.toString());
                }
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

    // 外部可调用
    public ApiRequest toCache(String key, JSONParser parser) {
        onFinish((success, code, response) -> {
            if (success) {
                try {
                    String cache = parser.parse(new JSONObject(response)).toString();
                    new CacheHelper(context).setCache(key, cache);
                } catch (JSONException e) {
                    for (OnFinishListener onFinishListener : onFinishListeners) {
                        onFinishListener.onFinish(false, -1, e.toString());
                    }
                }
            }
        });
        return this;
    }

    // 外部可调用
    public ApiRequest toServiceCache(String key, JSONParser parser) {
        onFinish((success, code, response) -> {
            if (success) {
                try {
                    String cache = parser.parse(new JSONObject(response)).toString();
                    new ServiceHelper(context).setServiceCache(key, cache);
                } catch (JSONException e) {
                    for (OnFinishListener onFinishListener : onFinishListeners) {
                        onFinishListener.onFinish(false, -1, e.toString());
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
        OkHttpUtils.post().url(url).params(map).build()
                .connTimeOut(CONN_TIMEOUT).readTimeOut(READ_TIMEOUT)
                .execute(callback);
    }
}
