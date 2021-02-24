//package com.app.studentromania.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//
//@Configuration
////@EnableWebMvc
//public class WebApplicationConfig extends WebMvcConfigurerAdapter {
//
//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry registry) {
//		registry.addResourceHandler("/").addResourceLocations("/index");
//	}
//
//	public static final String INDEX_VIEW_NAME = "forward:index";
//
//	public void addViewControllers(final ViewControllerRegistry registry) {
//		registry.addViewController("/").setViewName(INDEX_VIEW_NAME);
//	}
////
//////	@Bean
//////	public ViewResolver viewResolver() {
//////		UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
//////		viewResolver.setViewClass(InternalResourceView.class);
//////		return viewResolver;
//////	}
////
//}