package com.hjhsf;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.lang.reflect.Field;

/**
 * Created by huangJin on 2023/5/19.
 */
@Deprecated
public class HJHSFConsumerBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    BeanFactory beanFactory;

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            HJHSFConsumer annotation = declaredField.getAnnotation(HJHSFConsumer.class);
            if (annotation != null) {
                Object target = this.beanFactory.getBean(buildDefaultBeanName(declaredField.getType().getName()));
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, target);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return pvs;
    }

    private static String buildDefaultBeanName(String className) {
        String shortClassName = ClassUtils.getShortName(className);
        return Introspector.decapitalize(shortClassName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
