package com.questbuddy.config;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Exposes Spring's ApplicationContext statically so container-managed endpoints (@ServerEndpoint) can resolve Spring beans (services, mappers, etc.).
 */
@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


    public static <T> T getBean(Class<T> type) {
        return context.getBean(type);
    }
}