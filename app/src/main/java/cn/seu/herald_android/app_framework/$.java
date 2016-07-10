package cn.seu.herald_android.app_framework;

/**
 * 模拟 Swift 语言的 get-only 变量.
 *
 *  这里的 get-only 变量并非变量, 它本身不存储数据, 却包含了一个 get 函数, 对外显示成只读变量的形态.
 * 定义一个 get-only 变量之后, 取它的值, 即可自动调用预设的 get 函数.
 *
 *  当然 Java 不能重载运算符, 无法完全实现这种自动调用函数的功能, 我们还是要通过调用函数的方式来调用它的 get 函数.
 * 这个类本身并没有什么大用途, 它是为了配合 $$ 类 (模拟 set-get 变量) 的存在而存在的. 只是 $ 类偶尔可以用来代替
 * get 函数.
 *
 * 定义方法:
 *
 *  $<Boolean> myVar = new $<>(() -> {
 *      return 1 + 1 == 2;
 *  });
 *
 *  myVar.$get(); // The result is true
 *
 **/
public class $<T> {

    public interface $Getter<T> {
        T get();
    }

    private $Getter<T> getter;

    public $($Getter<T> getter) {
        this.getter = getter;
    }

    public final T $get() {
        return getter.get();
    }
}
