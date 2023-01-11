package io.github.leeg4ng.flygroovy;

import io.github.leeg4ng.flygroovy.auth.AuthPolicy;
import io.github.leeg4ng.flygroovy.auth.NoAuthPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

/**
 * @author Nook Li
 * @date 11/11/2022
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FlygroovyProperties.class)
public class FlygroovyAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression("#{'${flygroovy.enabled}'=='true'}")
    @Lazy(value = false)
    GroovyController groovyController(FlygroovyProperties flygroovyProperties) {
        GroovyController groovyController = new GroovyController();

        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());
        RequestMappingInfo requestMappingInfo = RequestMappingInfo
                .paths(flygroovyProperties.getPath())
                .methods(RequestMethod.POST)
                .options(options)
                .build();
        try {
            handlerMapping.registerMapping(requestMappingInfo, groovyController, GroovyController.class.getDeclaredMethod("exec", HttpHeaders.class, String.class, String.class, List.class));
        } catch (NoSuchMethodException e) {
            log.error("config groovy controller error, ", e);
            System.exit(-1);
        }

        return groovyController;
    }

    @Bean
    @ConditionalOnMissingBean
    @Lazy(value = false)
    AuthPolicy authPolicy(FlygroovyProperties flygroovyProperties) {
        return new NoAuthPolicy();
    }
}
