package com.example.picsingularcore.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig: WebMvcConfigurer{
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/images/**").addResourceLocations("file:/picSingular/image/")
        registry.addResourceHandler("/banner/**").addResourceLocations("file:/picSingular/banner/")
    }
}