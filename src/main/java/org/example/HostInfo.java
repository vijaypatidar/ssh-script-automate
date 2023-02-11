package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HostInfo {
    String username;
    String password;
    String host;
    int port;
}
