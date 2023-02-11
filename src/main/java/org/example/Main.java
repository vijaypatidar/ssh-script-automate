package org.example;


public class Main {
    public static void main(String[] args) throws Exception {
        RequestInfo requestInfo = new RequestInfo();
        HostInfo hostInfo = new HostInfo(
                "ubuntu", "vijay987", "3.111.219.95", 22
        );

        Ssh ssh = new Ssh(hostInfo);
        ssh.setup();
//        ssh.runAndBlock("ls -a");
//        ssh.runAndBlock("sudo apt-get update");
//        ssh.runAndBlock("sudo apt install npm");
        ssh.runAndBlock(String.format("mkdir %s && cd %s",requestInfo.getId(),requestInfo.getId()));
        String npmInit = ssh.run("npm init");
        ssh.expect(npmInit);
        ssh.close();
    }
}
