package cn.seu.herald_android.app_framework;

/**
 * 模拟 Swift 语言的 get-only 变量.
 *
 * 定义方法:
 * public class A {
 *     public $<Boolean> varName = new $<Boolean>(){
 *         public Boolean get() {
 *             return true;
 *         }
 *     };
 * }
 *
 * 调用方法:
 * A a = new A();
 * boolean b = a.varName.get();
 **/
public abstract class $<T> {
    public abstract T get();
}
