package cn.seu.herald_android.app_framework;

/**
 * 模拟其它语言中的 Property.
 *
 *  Property 本身不存储数据, 却包含了一个 get 函数和一个 set 函数(可选), 对外显示成变量的形态.
 * 定义一个 Property 变量之后, "取它的值", 即可自动调用预设的 get 函数; 给它"赋值", 即可自动调用预设的 set 函数.
 *
 *  当然 Java 不能重载运算符, 无法完全实现这种自动调用函数的功能, 我们还是要通过调用函数的方式来调用它的 get/set
 * 函数. 这样对于调用来说并不算很方便, 但定义的时候比较省事了. 所以这个类也许纯粹是为了简化 get/set 函数的成对定义
 * 而产生的吧.
 *
 * 定义方法:
 *
 *  private String mStr = "Hello,";
 *
 *  $<String> str = new $<>(() -> {
 *      return mStr;
 *  }, (newValue) -> {
 *      mStr = newValue;
 *  });
 *
 *  str.$set(str.$get() + " World!");
 *
 **/
public class $<T> {

    public interface $Getter<T> {
        T get();
    }

    public interface $Setter<T> {
        void set(T value);
    }

    private $Getter<T> getter;

    private $Setter<T> setter;

    public $($Getter<T> getter, $Setter<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public $($Getter<T> getter) {
        this.getter = getter;
    }

    public final T $get() {
        return getter.get();
    }

    public final void $set(T value) {
        if (setter != null) {
            setter.set(value);
        } else {
            throw new Error("Invoking a property setter which has not been initialized always fails");
        }
    }
}
