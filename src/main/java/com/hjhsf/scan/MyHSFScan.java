package com.hjhsf.scan;

import com.hjhsf.ConsumerConfig;
import com.hjhsf.HJHSFConsumer;
import com.hjhsf.HJHSFProvider;
import com.hjhsf.proxy.HJHSFConsumerInvocationBean;
import com.hjhsf.proxy.HJHSFProviderInvocationBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * Created by huangJin on 2023/5/16.
 */

public class MyHSFScan extends ClassPathBeanDefinitionScanner{


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
        return beanDefinition.getMetadata().isInterface();
    }

    public void doScan1(String... basePackages) throws ClassNotFoundException {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
            GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();
            Class<?> beanClass = ClassUtils.forName(definition.getBeanClassName(), this.getClass().getClassLoader());
            HJHSFProvider annotationP = beanClass.getAnnotation(HJHSFProvider.class);
            if (annotationP != null){
                definition.setBeanClass(HJHSFProviderInvocationBean.class);
                definition.getConstructorArgumentValues().addGenericArgumentValue(annotationP.serviceInterface());
            }

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
            }
        }
    }

    private static String buildDefaultBeanName(String className) {
        String shortClassName = ClassUtils.getShortName(className);
        return Introspector.decapitalize(shortClassName);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(AutoConfigurationPackages.class.getName());

//        MyHSFScan testScan = new MyHSFScan(new DefaultListableBeanFactory());
//        Set<BeanDefinitionHolder> beanDefinitionHolders = testScan.doScan("");
//        System.out.println(beanDefinitionHolders);

//        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
//         MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(pathMatchingResourcePatternResolver);
//
//        Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:com/yes/mapper/**/*.class");
//
//        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resources[0]);
//
//        ScannedGenericBeanDefinition scannedGenericBeanDefinition = new ScannedGenericBeanDefinition(metadataReader);
//
//        AnnotationMetadata metadata = scannedGenericBeanDefinition.getMetadata();
    }
}
