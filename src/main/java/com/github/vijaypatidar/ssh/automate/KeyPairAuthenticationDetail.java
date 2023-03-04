package com.github.vijaypatidar.ssh.automate;

import lombok.Getter;

@Getter
public class KeyPairAuthenticationDetail extends AuthenticationDetail {

    private final String keyPairPath;

    public KeyPairAuthenticationDetail(String hostName, String username, int port, String keyPairPath) {
        super(hostName, username, port);
        this.keyPairPath = keyPairPath;
    }

}
