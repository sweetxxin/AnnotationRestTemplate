package com.xxin.resttemplate.annotation;


import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface AnnotationRestTemplate {
    String ip()default "";
    String port() default "";
    String protocol() default "http";
    String description() default "";
    String context() default "";
}
