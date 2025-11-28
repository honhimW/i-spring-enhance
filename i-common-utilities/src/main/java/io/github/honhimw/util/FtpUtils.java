package io.github.honhimw.util;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author hon_him
 * @since 2022-03-07
 */

@Slf4j
public class FtpUtils {

    private static final Charset GBK = Charset.forName("GBK");

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements Serializable {
        private String hostName;

        private int port;

        private String username;

        private String password;

        private int defaultTimeout;

        private int connectTimeout;

        private int dataTimeout;

        private Builder() {
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder defaultTimeout(int defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder dataTimeout(int dataTimeout) {
            this.dataTimeout = dataTimeout;
            return this;
        }

        public Client build() {
            Client client = new Client();
            client.setHostName(hostName);
            client.setPort(port);
            client.setUsername(username);
            client.setPassword(password);
            client.setDefaultTimeout(defaultTimeout);
            client.setConnectTimeout(connectTimeout);
            client.setDataTimeout(dataTimeout);

            return client;
        }

    }

    @Setter
    public static class Client implements Closeable {
        private String hostName;

        private int port;

        private String username;

        private String password;

        private int defaultTimeout;

        private int connectTimeout;

        private int dataTimeout;

        private final FTPClient ftpClient = new FTPClient();

        private Client() {
        }


        public synchronized void connect() throws IOException {
            try {
                ftpClient.connect(hostName, port);
                Charset charset = GBK;
                if (ftpClient.login(username, password)) {
                    if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                        "OPTS UTF8", "ON"))) {
                        charset = UTF_8;
                    }
                    ftpClient.setCharset(charset);
                    ftpClient.setControlEncoding(charset.name());
                } else {
                    throw new IllegalArgumentException(String.format("FTP failed: username: [%s], password: [%s]", username, password));
                }
                ftpClient.isConnected();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }

        public boolean isActive() {
            try {
                int code = ftpClient.pwd();
                return code >= 200 && code < 300;
            } catch (IOException e) {
                return false;
            }
            // return FTPReply.isPositiveCompletion(ftpClient.getReplyCode());
        }

        private void checkConnection() throws IOException {
            if (!isActive()) {
                connect();
            }
        }

        public void read(String ftpFile, OutputStream ops) throws IOException {
            checkConnection();
            ftpFile = rencode2ISO(ftpFile);
            try (ops) {
                ftpClient.retrieveFile(ftpFile, ops);
            }
        }

        public void write(String ftpFile, InputStream ips) throws IOException {
            checkConnection();
            ftpFile = rencode2ISO(ftpFile);
            try (ips) {
                ftpClient.storeFile(ftpFile, ips);
            }
        }

        public FTPFile[] ls() throws IOException {
            checkConnection();
            return ftpClient.listFiles();
        }

        public FTPFile[] ls(String path) throws IOException {
            checkConnection();
            path = rencode2ISO(path);
            return ftpClient.listFiles(path);
        }

        public String pwd() throws IOException {
            checkConnection();
            return ftpClient.printWorkingDirectory();
        }

        public void cd(String path) throws IOException {
            checkConnection();
            path = pathAnalyze(path);
            path = rencode2ISO(path);
            String pwd = pwd();

            if (!ftpClient.changeWorkingDirectory(path)) {
                ftpClient.changeWorkingDirectory(pwd);
            }
        }

        public boolean exists(String path) throws IOException {
            checkConnection();
            path = pathAnalyze(path);
            path = rencode2ISO(path);
            return ls(path).length > 0;
        }

        public long size(String path) throws IOException {
            FTPFile[] ffs = ls(path);
            long size = 0L;
            for (FTPFile ff : ffs) {
                size += ff.getSize();
            }
            return size;
        }

        public void close() throws IOException {
            ftpClient.disconnect();
        }

        public String pathAnalyze(String path) throws IOException {
            String pwd = pwd();
            switch (path) {
                case ".":
                    return pwd;
                case "..":
                    if (Strings.CS.equals("/", pwd))
                        return "/";
                    ftpClient.changeToParentDirectory();
                    String parentPath = ftpClient.printWorkingDirectory();
                    ftpClient.changeWorkingDirectory(pwd);
                    return parentPath;
                default:
                    if (Strings.CS.startsWith(path, "/")) {
                        return path;
                    } else {
                        return pwd + "/" + path;
                    }
            }
        }
    }

    public static String rencode2ISO(String src) {
        return rencode(src, UTF_8, ISO_8859_1);
    }

    public static String rencode(String src, Charset srcCharset, Charset targetCharset) {
        if (StringUtils.isBlank(src)) {
            return src;
        }
        if (targetCharset.newEncoder().canEncode(src)) {
            return src;
        }
        return new String(src.getBytes(srcCharset), targetCharset);
    }

    public static class Terminal {

        public static void start(String host, String user, String password) throws Exception {
            String[] ipPort = host.split(":");
            String ip = ipPort[0];
            String port = "21";
            if (ipPort.length > 1)
                port = ipPort[1];

            Builder builder = builder();
            Client client = builder
                .hostName(ip)
                .port(Integer.parseInt(port))
                .username(user)
                .password(password)
                .build();
            String pwd = client.pwd();
            FTPFile[] ftpFiles = client.ls("/app/logs/ftp");
            System.out.println(pwd);
            while (true) {
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                if (StringUtils.isBlank(input)) {
                    continue;
                }
                String[] inputs = input.split(" ", 0);
                input = inputs[0];
                String as = "";
                if (inputs.length > 1) {
                    as = inputs[1];
                }
                String result = "no return";
                switch (input) {
                    case "ls":
                    case "ll":
                    case "dir":
                        FTPFile[] ffs = client.ls(as);
                        for (FTPFile ff : ffs) {
                            System.err.println(ff.toFormattedString());
                        }
                        break;
                    case "cd":
                        client.cd(as);
                        System.err.println(client.pwd());
                        break;
                    case "pwd":
                        System.err.println(client.pwd());
                        break;
                    case "get":
                    case "cat":
                    case "download":
                    case "read":
                        ByteArrayOutputStream baops = new ByteArrayOutputStream();
                        client.read(as, baops);
                        System.err.println(new String(baops.toByteArray(), UTF_8));
                        break;
                    case "exists":
                        System.err.println(client.exists(as));
                        break;
                    case "size":
                        System.err.println(client.size(as));
                        break;
                    case "exit":
                    case "quit":
                    case "stop":
                    case "disconnect":
                        client.close();
                        return;
                    default:
                        System.err.println(result);
                        break;
                }
            }
        }
    }

}
