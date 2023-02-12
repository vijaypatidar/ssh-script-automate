package org.example;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.example.helpers.DiskHelper;

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
        client.runAndBlock("wget --no-check-certificate -c --header \"Cookie: oraclelicense=accept-securebackup-cookie\" https://download.oracle.com/java/17/archive/jdk-17.0.1_linux-x64_bin.rpm");
        String df = client.run(DiskHelper.getDiskSpaceCommand());
        List<String> expect = client.expect(df);
        Map<String, Double> availableDiskSpace = new DiskHelper().getAvailableDiskSpace(expect);

        client.close();
    }


}
