package udp2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * @ClassName UDPProvider
 * @Description: UDP提供者，用于提供服务
 * @Author lsh
 * @Date 2019/1/13 18:48
 * @Version
 */
public class UDPProvider {
    public static void main(String[] args) throws IOException {
        //生成唯一标识
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        new Thread(provider).start();
        //读取任意键盘信息后可以退出
        System.in.read();
        provider.exit();
    }

    private static class Provider implements Runnable {
        /**
         * 私有口令
         */
        private final String sn;
        /**
         * 停止标识
         */
        private boolean done = false;

        private DatagramSocket ds;

        Provider(String sn) {
            this.sn = sn;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider Started.");
            try {
                //监听20000端口
                ds = new DatagramSocket(20000);


                while (!done) {
                    //构建接收实体
                    byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);
                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, dataLen);
                    System.out.println("UDPProvider receive form ip:" + ip
                            + "\tport:" + port + "\tdata:" + data);

                    //解析端口信息
                    int responsePort = MessageCreator.parsePort(data);
                    if (responsePort != -1) {
                        //构建一份回送数据
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                responseDataBytes.length,
                                receivePack.getAddress(),
                                responsePort);

                        ds.send(responsePacket);
                    }
                }

            } catch (Exception ignored) {
            }  finally {
                close();
            }
            //完成
            System.out.println("UDPProvider Finished.");
        }

        /**
         * 关闭socket
         */
        private void close() {
            if (ds != null) {
                ds.close();
            }
        }

        /**
         * 退出操作
         */
        private void exit() {
            done = true;
            close();
        }
    }
}
