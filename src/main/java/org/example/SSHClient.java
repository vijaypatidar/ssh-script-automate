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

public class SSHClient implements Closeable{
    private final AuthenticationDetail authenticationDetail;
    private BufferedReader reader;
    private BufferedReader error;

    private ChannelShell channel;
    private Session session;
    private BufferedWriter writer;

    private final ThreadGroup threadGroup = new ThreadGroup("SSHClient-" + UUID.randomUUID());


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

        PipedInputStream pi = new PipedInputStream();
        PipedOutputStream po = new PipedOutputStream(pi);
        reader = new BufferedReader(new InputStreamReader(pi));
        new Thread(threadGroup, () -> {
            int read = 0;
            byte[] bytes = new byte[1024];
            try {
                while (!Thread.interrupted()&&(read = in.read(bytes)) > 0) {
                    po.write(bytes, 0, read);
                    System.out.print(new String(bytes, 0, read));
                }
                pi.close();
                po.close();
            } catch (Exception e) {

            }
        }).start();
        writer = new BufferedWriter(new OutputStreamWriter(out));
        error = new BufferedReader(new InputStreamReader(err));
        new Thread(threadGroup, () -> {
            String line;
            while (!Thread.interrupted()&&channel.isConnected()) {
                try {
                    if ((line = error.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.err.println(line);
            }
        }).start();
    }

    public void runAndBlock(String command) throws Exception {
        expect(run(command));
    }

    public String run(String command) throws Exception {
        String commandId = "STEP_" + UUID.randomUUID().toString().replace("-", "") + "_Complete";
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
        return expect(Pattern.compile("^" + Pattern.quote(expect) + "$"));
    }

    public List<String> expect(Pattern expect) throws Exception {
        List<String> lines = new LinkedList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (expect.matcher(line).find()) {
                return lines;
            }
            lines.add(line);
        }
        return lines;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        threadGroup.interrupt();
    }

    private void sleep() throws Exception {
        Thread.sleep(1500);
    }
}
