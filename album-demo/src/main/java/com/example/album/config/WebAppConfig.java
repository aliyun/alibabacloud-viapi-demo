package com.example.album.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {
    @Value("${img.storage.path}")
    public String imgPath;

    @Bean
    public WebAppConfig newWebAppConfig() {
        return new WebAppConfig();
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        newWebAppConfig();
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/img/**").addResourceLocations("file:" + imgPath);

    }
}
