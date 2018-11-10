package com.niangegelaile.simpleinject;

import com.niangegelaile.injectannotation.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class ProxyInfo
{
    private String packageName;     //宿主类的包名
    private String proxyClassName;  //生成代理类名
    private TypeElement typeElement;//有注解元素的类（宿主类）
    private TypeElement componentElement;//容器类
    private Map<String,String> componentMethedsMap;//容器的方法，key是返回类型,value
    public List<VariableElement> injectVariables = new ArrayList<>();

    public static final String PROXY = Constants.PROXY_NAME;

    public ProxyInfo(Elements elementUtils, TypeElement classElement,TypeElement componentElement,Map<String,String> componentMethedsMap)
    {
        this.typeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();//
        //classname
        String className = ClassValidator.getClassName(classElement, packageName);
        this.packageName = packageName;
        this.proxyClassName = className + "$$" + PROXY;
        this.componentElement=componentElement;
        this.componentMethedsMap=componentMethedsMap;
    }


    public String generateJavaCode()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");
        //引包
        builder.append("import com.niangegelaile.injectannotation.InjectProxy;\n");
        builder.append('\n');

        builder.append("public class ").append(proxyClassName).append(" implements " + ProxyInfo.PROXY + "<" + typeElement.getQualifiedName() +","+componentElement.getQualifiedName()+ ">");
        builder.append(" {\n");

        generateMethods(builder);
        builder.append('\n');

        builder.append("}\n");
        return builder.toString();

    }


    private void generateMethods(StringBuilder builder)
    {

        builder.append("@Override\n ");
        builder.append("public void inject(" + typeElement.getQualifiedName() + " host,"+componentElement.getQualifiedName()+" component" +" ) {\n");


        for (VariableElement element : injectVariables)
        {
            String name = element.getSimpleName().toString();//宿主成员变量
            String type = element.asType().toString();//宿主成员变量的类型
            builder.append("host." + name).append(" = ");
            builder.append("component."+componentMethedsMap.get(type)+"();\n");
        }
        builder.append("  }\n");


    }

    /**
     * 代理类的文件名
     * @return
     */
    public String getProxyClassFullName()
    {
        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement()
    {
        return typeElement;
    }


}