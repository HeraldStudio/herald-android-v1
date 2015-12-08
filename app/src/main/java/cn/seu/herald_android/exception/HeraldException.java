package cn.seu.herald_android.exception;

/**
 * Created by heyon on 2015/12/8.
 */
public class HeraldException extends Exception{
    private String msg;
    private int code;

    public HeraldException(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public HeraldException() {

    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
