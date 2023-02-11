package org.example;

import java.util.UUID;

public class RequestInfo {
    private String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}
