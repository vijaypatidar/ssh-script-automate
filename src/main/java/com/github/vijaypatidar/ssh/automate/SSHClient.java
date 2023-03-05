package com.github.vijaypatidar.ssh.automate;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class SSHClient implements Closeable {
    private final SessionProvider sessionProvider;
    private BufferedReader reader;
    private BufferedReader error;
    private ChannelShell channel;
    private Session session;
    private PrintStream printStream;

    private final ThreadGroup threadGroup = new ThreadGroup("SSHClient-" + UUID.randomUUID());


    public SSHClient(AuthenticationDetail authenticationDetail) {
        this.sessionProvider = new SessionProvider(authenticationDetail);
    }

    public void setup() throws Exception {
        session = sessionProvider.getSession();
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
                while (!Thread.interrupted() && (read = in.read(bytes)) > 0) {
                    po.write(bytes, 0, read);
                    System.out.print(new String(bytes, 0, read));
                }
                pi.close();
                po.close();
            } catch (Exception e) {

            }
        }).start();
        printStream = new PrintStream(out);
        error = new BufferedReader(new InputStreamReader(err));
        new Thread(threadGroup, () -> {
            String line;
            while (!Thread.interrupted() && channel.isConnected()) {
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
        printStream.println(command);
        printStream.flush();
        return commandId;
    }

    public void send(String command) throws Exception {
        printStream.println(command);
        printStream.flush();
    }

    public void enter() throws Exception {
        send("");
        sleep();
    }

    public List<String> expect(String expect) throws Exception {
        return expect(Pattern.compile("^" + Pattern.quote(expect) + "$"));
    }

    char[] chars = new char[1024];

    public List<String> expect(Pattern expect) throws Exception {
        List<String> lines = new LinkedList<>();
        String line = "";
        int read = 0;
        while ((read = reader.read(chars)) > 0) {
            for (int i=0;i<read;i++) {
                char ch = chars[i];
                if (ch == '\n' || ch == '\r') {
                    lines.add(line);
                    if (expect.matcher(line).find()) {
                        return lines;
                    }
                    line = "";
                } else {
                    line += ch;
                }
            }
            if (expect.matcher(line).find()) {
                return lines;
            }
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
