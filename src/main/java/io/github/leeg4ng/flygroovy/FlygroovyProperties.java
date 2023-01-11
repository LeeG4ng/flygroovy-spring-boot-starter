package io.github.leeg4ng.flygroovy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Nook Li
 * @date 11/11/2022
 */
@Data
@Component
@ConfigurationProperties("flygroovy")
public class FlygroovyProperties {

    private Boolean enabled = true;

    private String path = "/groovy/exec";

//    @Autowired
//    private GroovyController groovyController;
//
//    @PostConstruct
//    void init() {
//        RequestMappingHandlerMapping handlerMapping = ApplicationContextProvider.getBean(RequestMappingHandlerMapping.class);
//        RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
//        options.setPatternParser(new PathPatternParser());
//        RequestMappingInfo requestMappingInfo = RequestMappingInfo
//                .paths(path)
//                .methods(RequestMethod.POST)
//                .options(options)
//                .build();
//        try {
//
//            handlerMapping.registerMapping(requestMappingInfo, groovyController, GroovyController.class.getDeclaredMethod("exec", String.class, String.class, List.class));
//        } catch (NoSuchMethodException e) {
//            System.exit(-1);
//        }
//    }
}
