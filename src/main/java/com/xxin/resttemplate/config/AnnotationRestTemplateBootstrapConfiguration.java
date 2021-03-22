package com.xxin.resttemplate.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @description:
 * @author: chenyixin7
 * @create: 2020-07-30 11:48
 */

@ConditionalOnClass({AnnotationRestTemplateDefinitionRegistry.class, RestTemplate.class})
@Configuration
public class AnnotationRestTemplateBootstrapConfiguration {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public AnnotationRestTemplateDefinitionRegistry annotationRestTemplateDefinitionRegistry() {
        return new AnnotationRestTemplateDefinitionRegistry();
    }
}
