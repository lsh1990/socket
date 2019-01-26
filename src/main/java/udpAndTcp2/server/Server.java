package udpAndTcp2.server;


import udpAndTcp2.constants.TCPConstants;

import java.io.IOException;

public class Server {
    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }
        ServerProvider.start(TCPConstants.PORT_SERVER);
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServerProvider.stop();
        tcpServer.stop();
    }
}
