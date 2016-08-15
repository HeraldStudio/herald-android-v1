package cn.seu.herald_android.framework.network;

import java.util.LinkedList;

import cn.seu.herald_android.helper.ApiHelper;

public class NetworkUtil {

    // 合并两个错误码，即去掉其中错误轻的，保留错误严重的代码
    // 具体逻辑是以401为最严重，其它错误数字越大越严重
    public static int mergeStatusCodes(int leftCode, int rightCode) {
        if (leftCode == 401 || rightCode == 401) {
            return 401;
        }
        return Math.max(leftCode, rightCode);
    }

    // 用来在将要运行的请求中优先加入 401 错误的监听器
    // 所有复合请求的 run() 函数必须首先调用本函数
    // 当请求出现致命错误时，提示身份过期并退出登录
    public static void addFatalErrorListenerInOnFinishList(LinkedList<OnFinishListener> list) {
        OnFinishListener listener = (success, code) -> {
            if (code == 401) {
                ApiHelper.notifyUserIdentityExpired();
            }
        };
        list.add(0, listener);
    }

    // 用来在将要运行的请求中优先加入 401 错误的监听器
    // 所有简单请求的 run() 函数必须首先调用本函数
    // 当请求出现致命错误时，提示身份过期并退出登录
    public static void addFatalErrorListenerInOnResponseList(LinkedList<OnResponseListener> list) {
        OnResponseListener listener = (success, code, response) -> {
            if (code == 401) {
                ApiHelper.notifyUserIdentityExpired();
            }
        };
        list.add(0, listener);
    }
}
