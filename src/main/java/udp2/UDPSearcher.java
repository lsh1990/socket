package udp2;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName UDPSearcher
 * @Description: TODO
 * @Author lsh
 * @Date 2019/1/13 21:21
 * @Version
 */
public class UDPSearcher {
    private static final int LISTEN_PROT = 30000;

    public static void main(String[] args) {
        System.out.println("UDPSearcher Started.");

        try {
            Listener listener = listener();
            sendBroadCast();
            //读取任意键盘信息可以退出
            System.in.read();
            List<Device> devices = listener.getDevicesAndClose();
            for (Device device : devices) {
                System.out.println("Device:" + device.toString());
            }
            //完成
            System.out.println("UDPSearcher Finished.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送广播信息
     */
    private static void sendBroadCast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started.");
        //作为搜索方，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();
        // 构建一份请求数据
        String requestData = MessageCreator.buildWhithPort(LISTEN_PROT);
        byte[] requestDataBytes = requestData.getBytes();
        // 直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes,
                requestDataBytes.length);
        // 20000端口, 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(20000);

        // 发送
        ds.send(requestPacket);
        ds.close();

        // 完成
        System.out.println("UDPSearcher sendBroadcast finished.");

    }

    private static Listener listener() throws InterruptedException {
        System.out.println("UDPSearcher start listen.");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PROT, countDownLatch);
        new Thread(listener).start();

        countDownLatch.await();
        return listener;
    }


    private static class Listener implements Runnable {
        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            //通知已启动
            countDownLatch.countDown();
            //监听回送端口
            try {
                ds = new DatagramSocket(listenPort);
                while (!done) {
                    // 构建接收实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, dataLen);
                    System.out.println("UDPSearcher receive form ip:" + ip
                            + "\tport:" + port + "\tdata:" + data);

                    String sn = MessageCreator.parseSn(data);
                    if (sn != null) {
                        Device device = new Device(port, ip, sn);
                        devices.add(device);
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            System.out.println("UDPSearcher listener finished.");
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
         * 关闭并获取设备信息
         * @return
         */
        List<Device> getDevicesAndClose() {
            done = true;
            close();
            return devices;
        }

    }



    /**
     * 设备参数
     */
    private static class Device {
        final int port;
        final String ip;
        final String sn;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }
}
