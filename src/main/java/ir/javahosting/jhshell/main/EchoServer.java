package ir.javahosting.jhshell.main;

import ir.javahosting.jhshell.io.WebSocketServerOutputStream;
import ir.javahosting.jhshell.net.WebSocket;
import ir.javahosting.jhshell.net.WebSocketServerSocket;

import java.io.*;
import java.net.ServerSocket;


public class EchoServer {
    public static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Start");
        EchoServer echoServer = new EchoServer();
        try {
            echoServer.doIt();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }
    }

    public void doIt() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        WebSocketServerSocket webSocketServerSocket
                = new WebSocketServerSocket(serverSocket);
        while (finished == false) {
            WebSocket socket = webSocketServerSocket.accept();
//            socket.setJhssh(new test("localhost",22));
            new WebSocketThread(socket).start();
        }
    }

    public void finish() {
        finished = true;
    }

    private boolean finished = false;
}

class WebSocketThread extends Thread {
    public WebSocketThread(WebSocket socket) {
        this.webSocket = socket;
    }
    FileWriter fileWriter;
    @Override
    public void run() {
        try {
            WebSocketServerOutputStream webSocketOutputStream = webSocket.getOutputStream();
            webSocketOutputStream.writeString("\n");
            fileWriter = new FileWriter(webSocket.shellConnector.ip+".log", true);

            System.out.println(fileWriter);
            InputStream inputStream = webSocket.getInputStream();
            int data = inputStream.read();
            String command = "";
            while (finished == false && data != -1) {
                webSocketOutputStream.writeString((char) data + "");
                System.out.println(data);
                System.out.println((char) data + "");
                command += (char) data + "";
                if (data == 13) {
                    command = command.replace("{", "");
                    command = command.replace("}", "");
                    command = command.trim();
                    System.out.println(command);
                    fileWriter.write(command+"\n");
                    fileWriter.flush();
                    String response = call(command);
                    command = "";
//                    response = response.replaceAll("\n", "")
//                            .replaceAll("\t", "")
//                            .replaceAll(" ", "");
                    //fileWriter.append(response);

                    webSocketOutputStream.writeString(response);
                }

                data = inputStream.read();
            }

//
        } catch (IOException e) {
            finished = true;
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

        try {
            webSocket.close();
        } catch (IOException e) {
            finished = true;
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }
    }

    public String call(String command) {
        try {
            webSocket.shellConnector.printStream.println(command);
            InputStream inputStream = webSocket.shellConnector.jSchchannel.getInputStream();
            String response = "";
            byte[] tmp = new byte[1024];
            boolean k = false;
            while (true) {
                while (inputStream.available() > 0) {
                    int i = inputStream.read(tmp, 0, 1024);
                    if (i < 0) break;
                    response += new String(tmp, 0, i);
                    k = true;
                }
                if (webSocket.shellConnector.jSchchannel.isClosed()) {
                    if (inputStream.available() > 0) continue;
                    System.out.println("exit-status: " + webSocket.shellConnector.jSchchannel.getExitStatus());
                    break;
                }
                try {
                    if (k) break;
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }

            }
            System.out.println(response);
            return response;

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void finish() {
        finished = true;
    }

    private boolean finished = false;

    private final WebSocket webSocket;
}
