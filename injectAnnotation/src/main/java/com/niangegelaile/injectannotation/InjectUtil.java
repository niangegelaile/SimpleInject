package com.niangegelaile.injectannotation;

public class InjectUtil {
    private static final String SUFFIX = "$$"+Constants.PROXY_NAME;
    public static void inject(Object component,Object target){
        InjectProxy injectProxy=findProxy(target);
        injectProxy.inject(target,component);
    }


    private static InjectProxy findProxy(Object target){
        try
        {
            Class clazz = target.getClass();
            Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
            return (InjectProxy) injectorClazz.newInstance();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.", target.getClass().getSimpleName() + SUFFIX));



    }


}
