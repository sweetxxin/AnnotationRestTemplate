package com.xxin.resttemplate.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description: DemoProperties
 * @author: chenyixin7
 * @create: 2020-07-23 11:27
 */
@Data
@ConfigurationProperties(prefix = "annotaion.resttemplate")
public class ScanProperties {
    private String scanPackages;
}
