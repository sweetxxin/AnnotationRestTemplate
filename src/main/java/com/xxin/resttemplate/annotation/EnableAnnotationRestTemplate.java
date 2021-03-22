package com.xxin.resttemplate.annotation;

import java.lang.annotation.*;

/**
 * 标注该参数为 动态请求头 入参类型必须为 HttpHeaders
 *
 * @author tangyilve
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableAnnotationRestTemplate {
    String basePackages() default "";
}
