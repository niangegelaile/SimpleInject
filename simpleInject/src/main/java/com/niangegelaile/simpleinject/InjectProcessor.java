package com.niangegelaile.simpleinject;

import com.google.auto.service.AutoService;
import com.niangegelaile.injectannotation.AutoInject;
import com.niangegelaile.injectannotation.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class InjectProcessor extends AbstractProcessor {

    private Messager messager;//用来打印日志消息
    private Elements elementUtils;//元素
    private Map<String,ProxyInfo> mProxyMap=new HashMap<>();//宿主的信息注解信息
    private Map<String,String> componentMethedsMap=new HashMap<>();//容器的方法，key是返回类型,value

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager=processingEnvironment.getMessager();
        elementUtils=processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE,"process...");
        mProxyMap.clear();
        //获取被@Bind标注的元素
        Set<? extends Element> elementsBind= roundEnvironment.getElementsAnnotatedWith(AutoInject.class);
        Set<? extends Element> elementsComponent=roundEnvironment.getElementsAnnotatedWith(Component.class);
        TypeElement componentElement=null;
        if(elementsComponent.size()>0){
            Iterator<Element> iterator= (Iterator<Element>) elementsComponent.iterator();
             componentElement= (TypeElement) iterator.next();
            List<? extends Element> chirldElements= componentElement.getEnclosedElements();//获取子元素
            for(Element element:chirldElements){
                if(element instanceof ExecutableElement){
                    ExecutableElement executableElement= (ExecutableElement) element;
                    componentMethedsMap.put(executableElement.getReturnType().toString(),executableElement.getSimpleName().toString());
                }
            }
        }
        for (Element element:elementsBind){
            //判断被注解的变量是否合法
            checkAnnotationValid(element,AutoInject.class);
            //变量 被绑定的变量
            VariableElement variableElement= (VariableElement) element;
            //变量所在的类，这个类也是一个元素
            TypeElement classElement= (TypeElement) variableElement.getEnclosingElement();
            //类的全名
            String fgClassName=classElement.getQualifiedName().toString();

            ProxyInfo proxyInfo=mProxyMap.get(fgClassName);//用这个类的全名作为HashMap的Key
            if(proxyInfo==null){
                proxyInfo=new ProxyInfo(elementUtils,classElement,componentElement,componentMethedsMap);
                mProxyMap.put(fgClassName,proxyInfo);
            }
            AutoInject bindAnnotation =variableElement.getAnnotation(AutoInject.class);

            proxyInfo.injectVariables.add(variableElement);
        }
        createSourceFileByProxyMap(mProxyMap);
        return true;
    }


    /**
     * 根据ProxyMap创建源文件
     * @param mProxyMap
     */
    private void createSourceFileByProxyMap(Map<String,ProxyInfo> mProxyMap){
        for(String key:mProxyMap.keySet()){
            ProxyInfo proxyInfo=mProxyMap.get(key);
            try {
                JavaFileObject jfo=processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(),
                        proxyInfo.getTypeElement());
                Writer writer=jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }
        }


    }







    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes=new HashSet<>();
        supportTypes.add(AutoInject.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 判断注解的地方是否合法
     * @param annotatedElement
     * @param clazz
     * @return
     */
    private boolean checkAnnotationValid(Element annotatedElement , Class clazz){
        if(annotatedElement.getKind()!= ElementKind.FIELD){//判断是否是一个元素
            error(annotatedElement,"%s must be declared on field.",clazz.getSimpleName());
            return false;
        }
        if(ClassValidator.isPrivate(annotatedElement)){//判断该元素不能是private
            error(annotatedElement,"%s() must can not be private.",annotatedElement.getSimpleName());
            return false;
        }
        return true;
    }

    private void error(Element element,String message ,Object... args){
        if(args.length>0){
            message=String.format(message,args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,message,element);
    }


}
