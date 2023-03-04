package com.github.vijaypatidar.ssh.automate;

import lombok.Getter;

@Getter
public class PasswordAuthenticationDetail extends AuthenticationDetail {

    private final String password;

    public PasswordAuthenticationDetail(String hostName, String username, int port, String password) {
        super(hostName, username, port);
        this.password = password;
    }

}
