package cn.seu.herald_android.framework.network;

import java.util.LinkedList;

import cn.seu.herald_android.helper.ApiHelper;

public class NetworkUtil {

    // 获取指定 Status Code 对应的错误级别，返回值越大，错误越严重
    public static int getErrorLevelForStatusCode(int code) {
        final int SUCCESS = 0, WARNING = 1, ERROR = 2, FATAL_ERROR = 3;

        if (code <= 200) { // HTTP 正常通信代码
            return SUCCESS;
        } else if (code < 300) { // 稍有异常但无关痛痒的代码
            return WARNING;
        } else if (code < 400 || code >= 500) { // HTTP 出错的代码
            return ERROR;
        } else { // 400 ~ 499 表示认证失败，必须注销的代码
            return FATAL_ERROR;
        }
    }

    // 合并两个错误码，即去掉其中错误轻的，保留错误严重的代码
    public static int mergeStatusCodes(int leftCode, int rightCode) {
        boolean leftSlighter = getErrorLevelForStatusCode(leftCode)
                < getErrorLevelForStatusCode(rightCode);
        return leftSlighter ? rightCode : leftCode;
    }

    // 用来在将要运行的请求中优先加入 4xx 致命错误（400 ~ 499）的监听器
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

    // 用来在将要运行的请求中优先加入 4xx 致命错误（400 ~ 499）的监听器
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
