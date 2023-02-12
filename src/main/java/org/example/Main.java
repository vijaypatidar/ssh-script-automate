package org.example;


import java.util.UUID;

public class Main {
    public static void main(String[] args) throws Exception {
        AuthenticationDetail detail = new KeyPairAuthenticationDetail(
                "3.111.219.95",
                "ubuntu",
                22,
                "aws-ssh.pem"
        );
        SSHClient client = new SSHClient(detail);
        client.setup();
        String requestId = UUID.randomUUID().toString();
        client.runAndBlock(String.format("mkdir %s && cd %s", requestId, requestId));
        client.runAndBlock(String.format("mkdir test && cd test"));
        client.send("npm init");
        Thread.sleep(3000);
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.send("Opensource");
        client.enter();
        client.enter();
        client.expect("sasdsd");
        client.close();
    }


}
