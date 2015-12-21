package cn.seu.herald_android.exception;

/**
 * Created by heyon on 2015/12/21.
 */
public class ModuleLoadException extends HeraldException{
    //模块未安装
    public static final int MODE_NOT_INSTALLED =0;
    //模块不是最新版本
    public static final int MODE_ISNOT_NEW=1;
    @Override
    public String getMsg() {
        return super.getMsg();
    }

    @Override
    public int getCode() {
        return super.getCode();
    }

    @Override
    public void setCode(int code) {
        super.setCode(code);
    }

    @Override
    public void setMsg(String msg) {
        super.setMsg(msg);
    }

    public ModuleLoadException() {
        super();
    }

    public ModuleLoadException(String msg, int code) {
        super(msg, code);
    }
}
