package com.xxin.resttemplate.config;


import com.xxin.resttemplate.annotation.AnnotationRestTemplate;
import com.xxin.resttemplate.annotation.EnableAnnotationRestTemplate;
import com.xxin.resttemplate.proxy.AnnotationRestTemplateFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: chenyixin7
 * @create: 2020-07-30 13:48
 */

public class AnnotationRestTemplateDefinitionRegistry implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, EnvironmentAware, Ordered, ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Environment environment;
    private ResourceLoader resourceLoader;
    private ApplicationContext applicationContext;
    private static String propertyName = "annotaion.resttemplate.scan.packages";
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Set<String> basePackages = new HashSet<>();
        String property = getPackagesPath();
        if (StringUtils.isNotBlank(property)) {
            logger.info("用户自定义扫描：{}", property);
            String[] packages = property.split(",");
            for (String aPackage : packages) {
                basePackages.add(aPackage);
            }
        } else {
            Map<String, Object> annotatedBeans = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
            String name = annotatedBeans.values().toArray()[0].getClass().getName();
            String basePackage = name.substring(0,name.lastIndexOf("."));
            logger.info("默认扫描：{}", basePackage);
            basePackages.add(basePackage);
        }

        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(AnnotationRestTemplate.class));
        LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
        }

        for (BeanDefinition candidateComponent : candidateComponents) {
            if (candidateComponent instanceof AnnotatedBeanDefinition) {
                AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                Class clazz = ClassUtils.resolveClassName(annotationMetadata.getClassName(), null);

                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                definition.getConstructorArgumentValues().addGenericArgumentValue(clazz);
                definition.setBeanClass(AnnotationRestTemplateFactory.class);
                definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_CONSTRUCTOR);
                beanDefinitionRegistry.registerBeanDefinition(clazz.getSimpleName(), definition);
            }
        }
    }

    private String getPackagesPath(){
        String property = environment.getProperty(propertyName);
        if (StringUtils.isBlank(property)){
            Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(EnableAnnotationRestTemplate.class);
            if (!beansWithAnnotation.isEmpty()){
                Object o = beansWithAnnotation.values().toArray()[0];
                EnableAnnotationRestTemplate annotation = o.getClass().getAnnotation(EnableAnnotationRestTemplate.class);
                if (StringUtils.isNotBlank(annotation.basePackages())){
                    property =  annotation.basePackages();
                }
            }
        }
        return property;
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
       this.resourceLoader = resourceLoader;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
