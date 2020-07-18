package com.vahidmostofi.micromuck.configuration;

import com.vahidmostofi.micromuck.jaeger.Jaeger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private Jaeger jaeger;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JaegerInterceptor(jaeger));
    }
}
