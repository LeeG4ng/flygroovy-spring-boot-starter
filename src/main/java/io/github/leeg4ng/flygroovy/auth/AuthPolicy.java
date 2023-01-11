package io.github.leeg4ng.flygroovy.auth;

import org.springframework.http.HttpHeaders;

/**
 * @author Nook Li
 * @date 11/14/2022
 */
public interface AuthPolicy {

    boolean auth(HttpHeaders httpHeaders);
}
