package org.example;

import java.util.UUID;
import lombok.Data;

@Data
public class RequestInfo {
    private String id = UUID.randomUUID().toString();
}
