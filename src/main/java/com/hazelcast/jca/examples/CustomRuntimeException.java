package com.hazelcast.jca.examples;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CustomRuntimeException extends RuntimeException {
    public CustomRuntimeException(String s) {
        super(s);
    }
}
