package com.github.leeg4ng.flygroovy.auth;

import org.springframework.http.HttpHeaders;

/**
 * @author Nook Li
 * @date 11/14/2022
 */
public class NoAuthPolicy implements AuthPolicy {

    @Override
    public boolean auth(HttpHeaders httpHeaders) {
        return true;
    }
}
