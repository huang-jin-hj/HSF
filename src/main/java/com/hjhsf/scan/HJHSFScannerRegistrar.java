package com.hjhsf.scan;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Created by huangJin on 2023/5/17.
 */

public class HJHSFScannerRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        MyHSFScan myHSFScan = new MyHSFScan(registry);
        //这里先写死扫描路径, 后面可以动态
        try {
            myHSFScan.doScan1(deducePackage(registry));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String deducePackage(BeanDefinitionRegistry registry){
        Field basePackages = null;
        try {
            BeanDefinition beanDefinition = registry.getBeanDefinition(AutoConfigurationPackages.class.getName());
            Class<? extends BeanDefinition> aClass = beanDefinition.getClass();
            basePackages = aClass.getDeclaredField("basePackages");
            basePackages.setAccessible(true);
            return ((Set<String>) basePackages.get(beanDefinition)).iterator().next();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }finally {
            basePackages.setAccessible(false);
        }
    }
}
