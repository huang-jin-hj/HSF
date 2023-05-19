package com.hjhsf.scan;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Created by huangJin on 2023/5/17.
 */

public class HJHSFScannerRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        MyHSFScan myHSFScan = new MyHSFScan(registry);
        //这里先写死扫描路径, 后面可以动态
        try {
            myHSFScan.doScan1(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
