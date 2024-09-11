package org.example.expert.domain.user.config;

import org.example.expert.domain.user.aop.AdminAccessAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.context.request.RequestContextListener;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {

    @Bean
    public AdminAccessAspect adminAccessAspect() {
        return new AdminAccessAspect();
    }
}