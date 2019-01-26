package udpAndTcp2.server;

import udpAndTcp2.constants.UDPConstants;
import udpAndTcp2.utils.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @ClassName ServerProvider
 * @Description: 服务提供方
 * @Author lsh
 * @Date 2019/1/20 20:59
 * @Version
 */
public class ServerProvider {

    private static Provider PROVIDER_INSTANCE;

    static void start(int port) {
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        new Thread(provider).start();
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }


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

        public Provider(String sn, int port) {
            this.sn = sn.getBytes();
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
                    byte[] clientData = recivePack.getData();
                    //校验数据长度(CMD两个字节，ip地址int类型4个字节)
                    boolean isValid = clientLength >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("ServerProvider receive form ip:" + clientIp
                            + "\tport:" + clientPort + "\tdataValid:" + isValid);

                    if (!isValid) {
                        //无效继续
                        continue;
                    }
                    //解析命令与回送端口
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));
                    //判断合法性
                    if (cmd == 1 && responsePort >0) {
                        //构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short)2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        //获取数据长度
                        int len = byteBuffer.position();
                        //回送数据
                        DatagramPacket responsePacket = new DatagramPacket(buffer, len, recivePack.getAddress(), responsePort);
                        ds.send(responsePacket);
                        System.out.println("ServerProvider response to:" + clientIp + "\tport:" + responsePort + "\tdataLen:" + len);
                    } else {
                        System.out.println("ServerProvider receive cmd nonsupport; cmd:" + cmd + "\tport:" + port);
                    }
                }

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }

        /**
         * 关闭
         */
        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        /**
         * 退出
         */
        void exit() {
            done = true;
            close();
        }
    }


}
