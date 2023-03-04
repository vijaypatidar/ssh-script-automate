package com.github.vijaypatidar.ssh.automate;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SessionProvider {
    private final AuthenticationDetail authenticationDetail;

    public SessionProvider(AuthenticationDetail authenticationDetail) {
        this.authenticationDetail = authenticationDetail;
    }

    public Session getSession() throws Exception {
        JSch jSch = new JSch();
        if (authenticationDetail instanceof KeyPairAuthenticationDetail) {
            KeyPairAuthenticationDetail keyPairAuthenticationDetail = (KeyPairAuthenticationDetail) authenticationDetail;
            jSch.addIdentity(keyPairAuthenticationDetail.getKeyPairPath());
        }
        Session session = jSch.getSession(authenticationDetail.getUsername(),
                authenticationDetail.getHostName(),
                authenticationDetail.getPort()
        );

        if (authenticationDetail instanceof PasswordAuthenticationDetail) {
            PasswordAuthenticationDetail passwordAuthenticationDetail = (PasswordAuthenticationDetail) authenticationDetail;
            session.setPassword(passwordAuthenticationDetail.getPassword());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

}
