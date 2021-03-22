package com.xxin.resttemplate.proxy;


import com.xxin.resttemplate.interceptor.RestTemplateInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


/**
 * @description:
 * @author: chenyixin7
 * @create: 2020-07-30 14:18
 */
public class AnnotationRestTemplateFactory<T> implements FactoryBean<T> {
    private Class<T> interfaceType;
    private Environment environment;
    private RestTemplateInterceptor interceptor;

    public AnnotationRestTemplateFactory(Class<T> interfaceType, @Autowired(required = false) RestTemplateInterceptor interceptor, Environment environment) {
        this.interfaceType = interfaceType;
        this.interceptor = interceptor;
        this.environment = environment;
    }

    @Override
    public T getObject() throws Exception {
        //这里主要是创建接口对应的实例，便于注入到spring容器中
        InvocationHandler handler = new AnnotationRestTemplateProxy<>(interfaceType,  interceptor, environment);
        return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(),
                new Class[]{interfaceType}, handler);
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;

    }
}