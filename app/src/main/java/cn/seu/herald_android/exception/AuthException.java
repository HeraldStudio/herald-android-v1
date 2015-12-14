package cn.seu.herald_android.exception;

/**
 * Created by heyon on 2015/12/8.
 */
public class AuthException extends HeraldException{
    public static final int NETWORK_ERROR = -2;
    //网络错误
    public static final int UNKONW_ERROR =-1;
    //未知错误
    public static final int ERROR_PWD =0;
    //密码错误
    public static final int HAVE_NOT_LOGIN=1;
    //未登录
    public static final int ERROR_UUID = 2;
    //错误的uuid

    private int code;
    private String msg;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }

    public AuthException(String msg, int code) {
        super(msg, code);
        this.code = code;
        this.msg = msg;
    }
}
