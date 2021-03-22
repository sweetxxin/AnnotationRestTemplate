package com.xxin.resttemplate.proxy;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xxin.resttemplate.annotation.AnnotationRestTemplate;
import com.xxin.resttemplate.annotation.Headers;
import com.xxin.resttemplate.annotation.ReturnHeaders;
import com.xxin.resttemplate.dto.RequestMappingInfoDto;
import com.xxin.resttemplate.interceptor.RestTemplateInterceptor;
import com.xxin.resttemplate.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: chenyixin7
 * @create: 2020-07-30 14:25
 */

public class AnnotationRestTemplateProxy<T> implements InvocationHandler {
    final String propertyStart = "${";
    final String propertyEnd = "}";
    private Class<T> interfaceType;
    private RestTemplate originRestTemplate = new RestTemplate();
    private Environment environment;
    private RestTemplateInterceptor interceptor;
    private String ip;
    private String port;
    private String protocol;
    private String context;
    private Logger logger = LoggerFactory.getLogger(getClass());


    public AnnotationRestTemplateProxy(Class<T> interfaceType,  RestTemplateInterceptor interceptor, Environment environment) {
        this.interfaceType = interfaceType;
        this.interceptor = interceptor;
        this.environment = environment;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        if (deduceServiceAnnotation()) {
            RequestMappingInfoDto requestMappingInfoDto = deduceMethodAnnotation(method);
            deduceParamsAnnotation(method, args, requestMappingInfoDto);
            //检查是否返回 返回头信息
            if (method.isAnnotationPresent(ReturnHeaders.class)) {
                requestMappingInfoDto.setReturnHeaders(true);
            } else {
                requestMappingInfoDto.setReturnHeaders(false);
            }
            return doInvokeService(requestMappingInfoDto, method.getGenericReturnType());
        }
        throw new IllegalArgumentException("can`t deduce service");
    }

    /**
     * 解析参数级别的注解
     *
     * @param method
     * @param args
     * @param requestMappingInfoDto
     * @return
     */
    private RequestMappingInfoDto deduceParamsAnnotation(Method method, Object[] args, RequestMappingInfoDto requestMappingInfoDto) {
        //解析请求信息
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Map<String, Object> requestParam = new HashMap<>();
        int i = 0;
        //解析参数
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof RequestParam) {
                    requestParam.put(((RequestParam) annotation).value(), args[i]);
                    break;
                } else if (annotation instanceof PathVariable) {
                    String[] uri = requestMappingInfoDto.getUri();
                    for (int i1 = 0; i1 < uri.length; i1++) {
                        uri[i1] = uri[i1].replace("{" + ((PathVariable) annotation).value() + "}", args[i].toString());
                    }
                    requestMappingInfoDto.setUri(uri);
                    break;
                } else if (annotation instanceof RequestBody) {
                    requestMappingInfoDto.setRequestBody(args[i]);
                    break;
                } else if (annotation instanceof Headers) {
                    requestMappingInfoDto.setHttpHeaders((HttpHeaders) args[i]);
                    break;
                }
            }
            i++;
        }
        requestMappingInfoDto.setRequestParam(requestParam);
        return requestMappingInfoDto;
    }

    /**
     * 解析服务器路由
     */
    private boolean deduceServiceAnnotation() {
        AnnotationRestTemplate annotation = interfaceType.getAnnotation(AnnotationRestTemplate.class);
        Assert.notNull(annotation, "RestTemplate annotation is null : " + interfaceType);
        if (isGetByProperty(annotation.ip())) {
            ip = environment.getProperty(annotation.ip().replace(propertyStart, "").replace(propertyEnd, ""));
        } else {
            ip = annotation.ip();
        }

        if (isGetByProperty(annotation.port())) {
            port = environment.getProperty(annotation.port().replace(propertyStart, "").replace(propertyEnd, ""));
        } else {
            port = annotation.port();
        }

        if (isGetByProperty(annotation.protocol())) {
            protocol = environment.getProperty(annotation.protocol().replace(propertyStart, "").replace(propertyEnd, ""));
        } else {
            protocol = annotation.protocol();
        }

        if (isGetByProperty(annotation.context())) {
            context = environment.getProperty(annotation.context().replace(propertyStart, "").replace(propertyEnd, ""));
        } else {
            context = annotation.context();
        }
        return true;
    }

    /**
     * 通过配置文件获取
     *
     * @param str
     * @return
     */
    private boolean isGetByProperty(String str) {
        return StringUtils.isNotBlank(str) && str.startsWith(propertyStart) && str.endsWith(propertyEnd);
    }

    /**
     * 设置请求头信息
     */
    private MultiValueMap<String, String> setHeaders(RequestMappingInfoDto requestMappingInfoDto) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String[] requestHeader = requestMappingInfoDto.getHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (requestHeader.length == 0 && requestMappingInfoDto.getHttpHeaders() == null) {
            return headers;
        }
        for (String s : requestHeader) {
            String[] split = s.split("=");
            headers.add(split[0], split[1]);
        }
        if (Objects.nonNull(requestMappingInfoDto.getHttpHeaders())) {
            requestMappingInfoDto.getHttpHeaders().forEach(headers::put);
        }
        return headers;
    }

    private String getUrl(String uri, Map<String, Object> requestParam) {
        StringBuilder url;
        String contextPath = "";
        if (StringUtils.isNotBlank(context)) {
            contextPath = "/" + context;
        }
            url = new StringBuilder(protocol).append("://").append(ip);
            if (StringUtils.isNotBlank(port)) {
                url.append(":").append(port);
            }
            url.append(contextPath).append(uri);
        if (requestParam != null && !requestParam.isEmpty()) {
            url.append("?");
            for (String key : requestParam.keySet()) {
                url.append(key).append("=").append(requestParam.get(key)).append("&");
            }
            url.deleteCharAt(url.length() - 1);
        }
        return url.toString();
    }

    /**
     * 解析方法的注解
     *
     * @param method
     */
    private RequestMappingInfoDto deduceMethodAnnotation(Method method) {
        RequestMappingInfoDto requestMappingInfoDto = new RequestMappingInfoDto();
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            String[] value = analysisUri(annotation.value());
            requestMappingInfoDto.setMethods(RequestMethod.GET);
            requestMappingInfoDto.setConsumes(annotation.consumes());
            requestMappingInfoDto.setHeaders(annotation.headers());
            requestMappingInfoDto.setName(annotation.name());
            requestMappingInfoDto.setParams(annotation.params());
            requestMappingInfoDto.setProduces(annotation.produces());
            requestMappingInfoDto.setUri(value);
            requestMappingInfoDto.setPath(annotation.path());

        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            String[] value = analysisUri(annotation.value());
            requestMappingInfoDto.setMethods(RequestMethod.POST);
            requestMappingInfoDto.setConsumes(annotation.consumes());
            requestMappingInfoDto.setHeaders(annotation.headers());
            requestMappingInfoDto.setName(annotation.name());
            requestMappingInfoDto.setParams(annotation.params());
            requestMappingInfoDto.setProduces(annotation.produces());
            requestMappingInfoDto.setUri(value);
            requestMappingInfoDto.setPath(annotation.path());
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            String[] value = analysisUri(annotation.value());
            requestMappingInfoDto.setMethods(annotation.method()[0]);
            requestMappingInfoDto.setConsumes(annotation.consumes());
            requestMappingInfoDto.setHeaders(annotation.headers());
            requestMappingInfoDto.setName(annotation.name());
            requestMappingInfoDto.setParams(annotation.params());
            requestMappingInfoDto.setProduces(annotation.produces());
            requestMappingInfoDto.setUri(value);
            requestMappingInfoDto.setPath(annotation.path());
        } else {
            throw new IllegalArgumentException("not find methodAnnotation : RequestMapping or PostMapping or GetMapping ");
        }
        return requestMappingInfoDto;
    }

    /**
     * 解析 uri 是否是配置
     */
    private String[] analysisUri(String[] value) {
        for (int i = 0; i < value.length; i++) {
            if (isGetByProperty(value[i])) {
                value[i] = environment.getProperty(value[i].replace(propertyStart, "").replace(propertyEnd, ""));
            }
        }
        return value;
    }

    private Object doInvokeService(RequestMappingInfoDto requestMappingInfoDto, Type responseType) throws Exception {
        RequestMethod method = requestMappingInfoDto.getMethods();
        if (RequestMethod.GET.equals(method)) {
            return doGet(requestMappingInfoDto, responseType);
        } else if (RequestMethod.POST.equals(method)) {
            return doPost(requestMappingInfoDto, responseType);
        }
        throw new IllegalArgumentException("not find RequestMethod : " + JSON.toJSONString(method));
    }

    private Object doPost(RequestMappingInfoDto requestMappingInfoDto, Type responseType) throws Exception {
        MultiValueMap<String, String> headers = setHeaders(requestMappingInfoDto);
        String url;
        HttpEntity<Object> entity;
        if (headers.get("Content-Type").contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            url = getUrl(requestMappingInfoDto.getUri()[0], null);
            entity = new HttpEntity(requestMappingInfoDto.getRequestParam(), headers);
        } else {
            url = getUrl(requestMappingInfoDto.getUri()[0], requestMappingInfoDto.getRequestParam());
            entity = new HttpEntity(requestMappingInfoDto.getRequestBody() == null ? new HashMap<>() : requestMappingInfoDto.getRequestBody(), headers);
        }
        logger.info("-------------------------------------------------------------------------------------------");
        logger.info("请求url ====== {}", url);
        logger.info("请求头====== {}", JSON.toJSONString(headers));
        logger.info("请求的参数body==== {}", JSON.toJSONString(requestMappingInfoDto.getRequestBody()));
        logger.info("请求的参数==== {}", JSON.toJSONString(requestMappingInfoDto.getRequestParam()));
            ResponseEntity<Object> resp = doOriginRestTemplatePost(url, entity);
            return dealResponse(resp, responseType, requestMappingInfoDto.isReturnHeaders());
    }

    private ResponseEntity<Object> doOriginRestTemplatePost(String url, HttpEntity<Object> entity) {
        if (interceptor != null) {
            logger.trace("自定义拦截器");
            List<ClientHttpRequestInterceptor> interceptors = originRestTemplate.getInterceptors();
            if (!interceptors.contains(interceptor)) {
                interceptors.add(interceptor);
                originRestTemplate.setInterceptors(interceptors);
            }
        }
        return originRestTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
    }

    private Object doGet(RequestMappingInfoDto requestMappingInfoDto, Type responseType) {
        MultiValueMap<String, String> headers = setHeaders(requestMappingInfoDto);
        String url = getUrl(requestMappingInfoDto.getUri()[0], requestMappingInfoDto.getRequestParam());
        HttpEntity<Object> entity = new HttpEntity(null, headers);
        ResponseEntity<Object> resp = originRestTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
        return dealResponse(resp, responseType, requestMappingInfoDto.isReturnHeaders());
    }

    private Object dealResponse(ResponseEntity<Object> resp, Type responseType, boolean isReturnHeaders) {
        logger.info("响应类型：{}", responseType.getTypeName());
        if (isReturnHeaders) {
            logger.info("只返回頭部信息 {}", resp.getHeaders());
            logger.info("-------------------------------------------------------------------------------------------");
            return JsonUtil.toPojo(JsonUtil.toJson(resp.getHeaders()), new TypeReference<Object>() {
                @Override
                public Type getType() {
                    return responseType;
                }
            });
        }
        if (resp.getStatusCode() == HttpStatus.OK && null != resp.getBody()) {
            String body = JSON.toJSONString(resp.getBody());
            logger.info("响应信息 {}", body);
            logger.info("-------------------------------------------------------------------------------------------");
            return JsonUtil.toPojo(body, new TypeReference<Object>() {
                @Override
                public Type getType() {
                    return responseType;
                }
            });
        }
        logger.info("响应错误 {}", JsonUtil.toJson(resp));
        logger.info("-------------------------------------------------------------------------------------------");
        return null;
    }

}