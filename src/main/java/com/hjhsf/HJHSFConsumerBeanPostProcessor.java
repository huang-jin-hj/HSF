package com.hjhsf;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.lang.reflect.Field;

/**
 * Created by huangJin on 2023/5/19.
 */
public class HJHSFConsumerBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    BeanFactory beanFactory;
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        if (bean.getClass().getAnnotation(ConsumerConfig.class) == null){
            Class<?> aClass = bean.getClass();
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                HJHSFConsumer annotation = declaredField.getAnnotation(HJHSFConsumer.class);
                Object target = this.beanFactory.getBean(annotation.serviceName());
                try {
                    declaredField.set(bean, target);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
