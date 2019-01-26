package udpAndTcp2.client;


import udpAndTcp2.client.bean.ServerInfo;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);
        if (info != null) {
            try {
                TCPClietn.linkWith(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
