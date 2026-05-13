package com.careround.shared.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.LinkedHashSet;

@Configuration
public class FlywayJpaDependencyConfig {

    @Bean
    static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlyway() {
        return beanFactory -> {
            if (!beanFactory.containsBeanDefinition("entityManagerFactory")
                    || !beanFactory.containsBeanDefinition("flywayInitializer")) {
                return;
            }

            BeanDefinition entityManagerFactory = beanFactory.getBeanDefinition("entityManagerFactory");
            LinkedHashSet<String> dependencies = new LinkedHashSet<>();
            String[] existing = entityManagerFactory.getDependsOn();
            if (existing != null) {
                dependencies.addAll(Arrays.asList(existing));
            }
            dependencies.add("flywayInitializer");
            entityManagerFactory.setDependsOn(dependencies.toArray(String[]::new));
        };
    }
}
