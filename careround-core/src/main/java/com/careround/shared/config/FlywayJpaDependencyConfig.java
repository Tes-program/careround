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
            if (!beanFactory.containsBeanDefinition("entityManagerFactory")) {
                return;
            }

            // In Spring Boot 4.x the separate "flywayInitializer" bean was removed;
            // migrations are triggered by the "flyway" bean itself. Depend on whichever
            // bean name is present so this works across Spring Boot 3.x and 4.x.
            String flywayBeanName = beanFactory.containsBeanDefinition("flywayInitializer")
                    ? "flywayInitializer"
                    : beanFactory.containsBeanDefinition("flyway")
                            ? "flyway"
                            : null;

            if (flywayBeanName == null) {
                return;
            }

            BeanDefinition entityManagerFactory = beanFactory.getBeanDefinition("entityManagerFactory");
            LinkedHashSet<String> dependencies = new LinkedHashSet<>();
            String[] existing = entityManagerFactory.getDependsOn();
            if (existing != null) {
                dependencies.addAll(Arrays.asList(existing));
            }
            dependencies.add(flywayBeanName);
            entityManagerFactory.setDependsOn(dependencies.toArray(String[]::new));
        };
    }
}
