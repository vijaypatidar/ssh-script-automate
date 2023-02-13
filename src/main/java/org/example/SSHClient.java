package org.example;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;

public class SSHClient {
    private final AuthenticationDetail authenticationDetail;
    private BufferedReader reader;
    private BufferedReader error;

    private ChannelShell channel;
    private Session session;
    private BufferedWriter writer;

    public SSHClient(AuthenticationDetail authenticationDetail) {
        this.authenticationDetail = authenticationDetail;
    }

    public void setup() throws Exception {
        JSch jSch = new JSch();
        if (authenticationDetail instanceof KeyPairAuthenticationDetail) {
            KeyPairAuthenticationDetail keyPairAuthenticationDetail = (KeyPairAuthenticationDetail) authenticationDetail;
            jSch.addIdentity(keyPairAuthenticationDetail.getKeyPairPath());
        }
        session = jSch.getSession(authenticationDetail.getUsername(),
                authenticationDetail.getHostName(),
                authenticationDetail.getPort()
        );

        if (authenticationDetail instanceof PasswordAuthenticationDetail) {
            PasswordAuthenticationDetail passwordAuthenticationDetail = (PasswordAuthenticationDetail) authenticationDetail;
            session.setPassword(passwordAuthenticationDetail.getPassword());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        channel = (ChannelShell) session.openChannel("shell");
        channel.connect();

        InputStream in = channel.getInputStream();
        InputStream err = channel.getExtInputStream();
        OutputStream out = channel.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(in));
        writer = new BufferedWriter(new OutputStreamWriter(out));
        error = new BufferedReader(new InputStreamReader(err));
        new Thread(() -> {
            String line;
            while (channel.isConnected()) {
                try {
                    if ((line = error.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.err.println(line);
            }
        }).start();
        new Thread(() -> {

            Scanner scanner = new Scanner(System.in);
            while (channel.isConnected()) {
                try {
                    run(scanner.nextLine());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void runAndBlock(String command) throws Exception {
        expect(run(command));
    }

    public String run(String command) throws Exception {
        String commandId = "STEP_" + UUID.randomUUID().toString().replace("-","")+"_Complete";
        command += " && echo " + commandId;
        writer.write(command + "\n");
        writer.flush();
        return commandId;
    }

    public void send(String command) throws Exception {
        writer.write(command + "\n");
        writer.flush();
    }

    public void enter() throws Exception {
        send("");
        sleep();
    }

    public List<String> expect(String expect) throws Exception {
        List<String> lines = new LinkedList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            log(line);
            if (expect.equals(line)) {
                return lines;
            }
            lines.add(line);
        }
        return lines;
    }

    public List<String> expect(Pattern expect) throws Exception {
        List<String> lines = new LinkedList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            log(line);
            if (expect.matcher(line).find()) {
                return lines;
            }
            lines.add(line);
        }
        return lines;
    }

    public void close() {

        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    private void log(String log) {
        System.out.println(log);
    }

    private void sleep() throws Exception {
        Thread.sleep(1500);
    }
}
