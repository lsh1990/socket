package udpAndTcp.server;

import udpAndTcp.constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @ClassName ServerProvider
 * @Description: 服务提供方
 * @Author lsh
 * @Date 2019/1/20 20:59
 * @Version
 */
public class ServerProvider {


    /**
     * 服务提供方线程类
     */
    private static class Provider implements Runnable {
        /**
         * 客户端标识
         */
        private final byte[] sn;
        /**
         * 端口
         */
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        /**
         * 存储消息的buffer
         */
        private final byte[] buffer = new byte[128];

        public Provider(byte[] sn, int port) {
            this.sn = sn;
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider Started.");

            try {
                //监听端口
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                //接收消息的packet
                DatagramPacket recivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    //接收
                    ds.receive(recivePack);
                    //打印接收到的信息与发送者信息
                    //客户端信息
                    String clientIp = recivePack.getAddress().getHostAddress();
                    int clientPort = recivePack.getPort();
                    int clientLength = recivePack.getLength();
                    byte[] clientDate = recivePack.getData();
                    //校验数据长度(CMD两个字节，ip地址int类型4个字节)
                    boolean isValid = clientLength >= (UDPConstants.HEADER.length + 2 + 4);

                }

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
