package com.hjhsf.scan;

import com.hjhsf.ConsumerConfig;
import com.hjhsf.HJHSFConsumer;
import com.hjhsf.HJHSFProvider;
import com.hjhsf.proxy.HJHSFConsumerInvocationBean;
import com.hjhsf.proxy.HJHSFProviderInvocationBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by huangJin on 2023/5/16.
 */

public class MyHSFScan extends ClassPathBeanDefinitionScanner{

    private static final String SERVICE_INTERFACE = "SERVICE_INTERFACE";


    public MyHSFScan(BeanDefinitionRegistry registry) {
        super(registry, false);
        registerDefaultFilters();
    }

    @Override
    protected void registerDefaultFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(HJHSFProvider.class));
        this.addIncludeFilter(new AnnotationTypeFilter(ConsumerConfig.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        MergedAnnotation<HJHSFProvider> hjhsfProviderMergedAnnotation = beanDefinition.getMetadata().getAnnotations().get(HJHSFProvider.class);
        if (hjhsfProviderMergedAnnotation.isPresent()){
            return hjhsfProviderMergedAnnotation.getClass("serviceInterface").isInterface();
        }
        return true;
    }

    @Override
    public Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidateComponents = super.findCandidateComponents(basePackage);
        for (BeanDefinition candidateComponent : candidateComponents) {
            String beanClassName = candidateComponent.getBeanClassName();
            if (StringUtils.hasText(beanClassName)){
                try {
                    Class<?> aClass = Class.forName(beanClassName);
                    HJHSFProvider annotation = aClass.getAnnotation(HJHSFProvider.class);
                    if (annotation != null){
                        candidateComponent.setBeanClassName(annotation.serviceInterface().getName());
                        candidateComponent.setAttribute(SERVICE_INTERFACE, annotation.serviceInterface());
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return candidateComponents;
    }


    public void doScan1(String... basePackages) throws ClassNotFoundException {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
            GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();
            Class<?> beanClass = ClassUtils.forName(definition.getBeanClassName(), this.getClass().getClassLoader());
            ConsumerConfig annotationC = beanClass.getAnnotation(ConsumerConfig.class);
            if (annotationC != null){
                for (Field declaredField : beanClass.getDeclaredFields()) {
                    HJHSFConsumer annotation = declaredField.getAnnotation(HJHSFConsumer.class);
                    if (annotation != null){
                        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(HJHSFConsumerInvocationBean.class);
                        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(declaredField.getType());
                        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(annotation.serviceName());
                        this.getRegistry().registerBeanDefinition(buildDefaultBeanName(declaredField.getType().getName()), beanDefinition);
                    }
                }
            }else {
                definition.setBeanClass(HJHSFProviderInvocationBean.class);
                definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getAttribute(SERVICE_INTERFACE));
            }
        }
    }

    private static String buildDefaultBeanName(String className) {
        String shortClassName = ClassUtils.getShortName(className);
        return Introspector.decapitalize(shortClassName);
    }

}
