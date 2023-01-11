package io.github.leeg4ng.flygroovy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Nook Li
 * @date 11/11/2022
 */
@Data
//@Component
@ConfigurationProperties("flygroovy")
public class FlygroovyProperties {

    private Boolean enabled = true;

    private String path = "/groovy/exec";

}
