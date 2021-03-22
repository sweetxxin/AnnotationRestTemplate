package com.xxin.resttemplate.dto;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: chenyixin7
 * @create: 2020-07-30 16:03
 */
public class RequestMappingInfoDto {
    private RequestMethod methods;
    private String[] path;
    private String[] headers;
    private String[] produces;
    private String[] params;
    private String[] uri;
    private String name;
    private String exchangeKeyUrl;
    private String[] consumes;
    private Object requestBody = new HashMap<>();
    private HttpHeaders httpHeaders;
    private Map<String, Object> requestParam;
    private boolean returnHeaders;

    public RequestMappingInfoDto() {
    }

    public RequestMappingInfoDto(RequestMethod methods, String[] path, String[] headers, String[] produces, String[] params, String[] uri, String name, String[] consumes, Object requestBody, HttpHeaders httpHeaders, Map<String, Object> requestParam, boolean returnHeaders) {
        this.methods = methods;
        this.path = path;
        this.headers = headers;
        this.produces = produces;
        this.params = params;
        this.uri = uri;
        this.name = name;
        this.consumes = consumes;
        this.requestBody = requestBody;
        this.httpHeaders = httpHeaders;
        this.requestParam = requestParam;
        this.returnHeaders = returnHeaders;
    }

    public RequestMethod getMethods() {
        return methods;
    }

    public void setMethods(RequestMethod methods) {
        this.methods = methods;
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String[] getProduces() {
        return produces;
    }

    public void setProduces(String[] produces) {
        this.produces = produces;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String[] getUri() {
        return uri;
    }

    public void setUri(String[] uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public void setConsumes(String[] consumes) {
        this.consumes = consumes;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public Map<String, Object> getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(Map<String, Object> requestParam) {
        this.requestParam = requestParam;
    }

    public boolean isReturnHeaders() {
        return returnHeaders;
    }

    public void setReturnHeaders(boolean returnHeaders) {
        this.returnHeaders = returnHeaders;
    }

    public String getExchangeKeyUrl() {
        return exchangeKeyUrl;
    }

    public void setExchangeKeyUrl(String exchangeKeyUrl) {
        this.exchangeKeyUrl = exchangeKeyUrl;
    }


}
