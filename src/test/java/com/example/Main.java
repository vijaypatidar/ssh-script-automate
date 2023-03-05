package com.example;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.github.vijaypatidar.ssh.automate.AuthenticationDetail;
import com.github.vijaypatidar.ssh.automate.KeyPairAuthenticationDetail;
import com.github.vijaypatidar.ssh.automate.SSHClient;
import com.github.vijaypatidar.ssh.automate.helpers.DiskHelper;

public class Main {

    public static void main(String[] args) throws Exception {
        AuthenticationDetail detail = new KeyPairAuthenticationDetail(
                "3.111.147.48",
                "ubuntu",
                22,
                "aws-ssh.pem"
        );
        SSHClient client = new SSHClient(detail);
        client.setup();
        String requestId = UUID.randomUUID().toString();
        client.runAndBlock("curl -fsSL https://deb.nodesource.com/setup_19.x | sudo -E bash -");
        client.runAndBlock("sudo apt-get install -y nodejs");
        client.runAndBlock("mkdir demo && cd demo");
        String init = client.run("npm init");
        client.expect(Pattern.compile("package name:.*"));
        client.send("vijay");
        client.expect(Pattern.compile("version:.*"));
        client.send("1.3.5");
        client.expect(Pattern.compile("description:.*"));
        client.send("1.3.5");
        client.expect(Pattern.compile("entry point:.*"));
        client.send("server.js");
        client.expect(Pattern.compile("test command:.*"));
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.enter();
        client.expect(init);
        client.close();
    }


}
