package cn.seu.herald_android.app_framework;

/**
 * 模拟 Swift 语言的 get-set 变量.
 *
 * 定义方法:
 * public class A {
 *     public $$<Boolean> varName = new $$<Boolean>(){
 *         public Boolean get() {
 *             return true;
 *         }
 *         public void set(Boolean value){
 *             // do something
 *         }
 *     };
 * }
 *
 * 调用方法:
 * A a = new A();
 * boolean b = a.varName.get();
 * a.varName.set(false);
 **/
public abstract class $$<T> {
    public abstract T get();
    public abstract void set(T value);
}
