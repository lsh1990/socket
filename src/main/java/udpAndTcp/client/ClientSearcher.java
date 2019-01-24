package udpAndTcp.client;

import udpAndTcp.client.bean.ServerInfo;
import udpAndTcp.constants.UDPConstants;
import udpAndTcp.utils.ByteUtils;

import java.io.IOException;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ClientSearcher
 * @Description: 客户端搜索功能
 * 业务介绍：客户端通过指定端口发送广播信息，并开启监听服务，广播信息中携带ip地址信息。在指定时间内若没有服务端响应则关闭服务。
 * 1.监听端口的线程
 * 2.发送广播信息
 * 3.搜索服务
 * @Author lsh
 * @Date 2019/1/22 20:25
 * @Version
 */
public class ClientSearcher {

    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher Started.");
        //接收回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiveLatch);
            //广播数据
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        // 完成
        System.out.println("UDPSearcher Finished.");
        if (listener == null) {
            return null;
        }
        List<ServerInfo> devices = listener.getServerAndClose();
        if (devices.size() > 0) {
            return devices.get(0);
        }
        return null;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started.");
        //作为搜索方，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();
        //构建一份请求数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        // 头部
        byteBuffer.put(UDPConstants.HEADER);
        // CMD命名
        byteBuffer.putShort((short) 1);
        //回送端口信息
        byteBuffer.putInt(LISTEN_PORT);
        // 直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(),
                byteBuffer.position() + 1);
        //广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        // 设置端口
        requestPacket.setPort(UDPConstants.PORT_SERVER);
        // 发送
        ds.send(requestPacket);
        ds.close();

        // 完成
        System.out.println("UDPSearcher sendBroadcast finished.");
    }
    /**
     * 启动监听服务
     * @param receiveLatch
     * @return
     */
    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen.");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiveLatch);
        new Thread(listener).start();
        //等待监听服务启动
        startDownLatch.await();
        return listener;
    }

    /**
     * 监听端口线程
     */
    private static class Listener implements Runnable {
        private final int listenPort;
        /**
         * 线程启动栅栏
         */
        private final CountDownLatch startDownLatch;
        /**
         * 接收数据栅栏
         */
        private final CountDownLatch receiveDownLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveDownLatch) {
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            //通知已启动
            startDownLatch.countDown();

            try {
                //监听端口
                ds = new DatagramSocket(listenPort);
                //构建接收实体
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);
                while (!done) {
                    //接收
                    ds.receive(receivePack);
                    //打印接收的信息
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLen >= minLen
                            && ByteUtils.startsWith(data, UDPConstants.HEADER);

                    System.out.println("UDPSearcher receive form ip:" + ip
                            + "\tport:" + port + "\tdataValid:" + isValid);

                    if (!isValid) {
                        // 无效继续
                        continue;
                    }
                    //包装数据
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 || serverPort <= 0) {
                        System.out.println("UDPSearcher receive cmd:" + cmd + "\tserverPort:" + serverPort);
                        continue;
                    }
                    String sn = new String(buffer, minLen, dataLen - minLen);
                    ServerInfo info = new ServerInfo(sn,serverPort, ip);
                    serverInfoList.add(info);
                    //成功接收到一份数据
                    receiveDownLatch.countDown();
                }

            } catch (Exception ignored) {
            } finally {
                close();
            }
            System.out.println("UDPSearcher listener finished.");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }


        public List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }
    }

}
