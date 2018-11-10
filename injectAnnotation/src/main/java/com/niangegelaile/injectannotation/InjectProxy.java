package com.niangegelaile.injectannotation;

/**
 * 生成的代码要实现这个接口
 */
public interface InjectProxy<T,C> {
    //从容器里取出实例赋值给目标成员变量
    void inject(T target,C component);
}
