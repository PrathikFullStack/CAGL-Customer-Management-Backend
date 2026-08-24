package com.iexceed.appzillonbanking.cagl.cob.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.context = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext not yet initialized");
        }
        return context.getBean(type);
    }
}