package org.example;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AuthenticationDetail {

    private final String hostName;
    private final String username;
    private final int port;

}
