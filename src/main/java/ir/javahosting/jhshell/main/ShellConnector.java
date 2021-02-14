package ir.javahosting.jhshell.main;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;


public class ShellConnector {
    public Session jSchSession;
    public Channel jSchchannel;
    public PrintStream printStream;
    public String ip;
    public ShellConnector(String ip, Integer port, String privateKey) {
        this.ip = ip;
        try {
            JSch jsch = new JSch();
            System.out.println("connecting to jsch");
            jsch.setKnownHosts("/home/mahdi/.ssh/known_hosts");
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            jsch.addIdentity(privateKey);
            String user = "mahdi";
            jSchSession = jsch.getSession(user, ip, port);
            jSchSession.setConfig(config);
            //String passwd = "41740125";
            //jSchSession.setPassword(passwd);

            // making a connection with timeout.
            jSchSession.connect(1000);
            jSchchannel = jSchSession.openChannel("shell");
            OutputStream outputStream = jSchchannel.getOutputStream();
            printStream = new PrintStream(outputStream, true);
            jSchchannel.connect();

            System.out.println("connection2 established");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public String toString() {
        return "ShellConnector{" +
                "jSchSession=" + jSchSession +
                ", jSchchannel=" + jSchchannel +
                ", printStream=" + printStream +
                ", ip='" + ip + '\'' +
                '}';
    }
}
