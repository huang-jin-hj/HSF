package com.hjhsf.proxy;

import com.hjhsf.HJHSFCommunication;
import com.hjhsf.HJHSFConfigServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by huangJin on 2023/5/16.
 */
public class HJHSFProviderInvocationBean implements InvocationHandler, FactoryBean, ApplicationContextAware, InitializingBean {
    private Class<?> hsfInterface;

    private ApplicationContext applicationContext;

    @Autowired
    private HJHSFCommunication hjhsfCommunication;

    @Autowired
    private HJHSFConfigServer hjhsfConfigServer;

    private Object targetBean;

    public HJHSFProviderInvocationBean(Class hsfInterface) {
        this.hsfInterface = hsfInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (this.targetBean == null){
            synchronized (this){
                if (this.targetBean == null){
                    Map<String, ?> beansOfType = this.applicationContext.getBeansOfType(this.hsfInterface);
                    for (Map.Entry<String, ?> stringEntry : beansOfType.entrySet()) {
                        if (!stringEntry.getKey().equals(buildDefaultBeanName(this.hsfInterface.getName()))){
                            this.targetBean = stringEntry.getValue();
                        }
                    }

                }
            }
        }
        return method.invoke(targetBean, args);
    }

    private static String buildDefaultBeanName(String className) {
        String shortClassName = ClassUtils.getShortName(className);
        return Introspector.decapitalize(shortClassName);
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(hsfInterface.getClassLoader(), new Class[]{hsfInterface}, this);
    }

    @Override
    public Class<?> getObjectType() {
        return this.hsfInterface;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        hjhsfCommunication.createService(hjhsfConfigServer.getServiceName());
    }
}
