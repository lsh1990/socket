package tcpChannel.server.handle;

import tcpChannel.server.clink.net.qiujuer.clink.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.Executors.*;

/**
 * @ClassName ClientHandler
 * @Description: 处理连接客户端
 * @Author lsh
 * @Date 2019/1/27 17:50
 * @Version
 */
public class ClientHandler {
    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.closeNotify = closeNotify;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
    }

    /**
     * 统一退出方法
     */
    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端已退出：" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint() {
        new Thread(readHandler).start();
    }
    private void exitBySelf() {
        exit();
        closeNotify.onSelfClosed(this);
    }
    public interface CloseNotify {
        void onSelfClosed(ClientHandler handler);
    }


    /**
     * 读线程
     */
    private class ClientReadHandler implements Runnable{
        private boolean done = false;
        private final InputStream inputStream;

        public ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
        try {
            // 得到输入流，用于接收数据
            BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
            do {
                // 客户端拿到一条数据
                String str = socketInput.readLine();
                if (str == null) {
                    System.out.println("客户端已无法读取数据！");
                    // 退出当前客户端
                    ClientHandler.this.exitBySelf();
                    break;
                }
                // 打印到屏幕
                System.out.println(str);
            } while (!done);
        } catch (Exception e) {
            if (!done) {
                System.out.println("连接异常断开");
                ClientHandler.this.exitBySelf();
            }
        } finally {
            // 连接关闭
            CloseUtils.close(inputStream);
        }

        }

        /**
         * 退出操作
         */
        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }

    /**
     * 写操作，利用线程池实现
     * 如果设计为线程的话，启动、阻塞、等待，实现等待比较复杂
     * 利用单例线程池解决
     */
    class ClientWriteHandler {
        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        public ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = newSingleThreadExecutor();
        }

        /**
         * 写操作，退出操作
         */
        void exit() {
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }
        /**
         * 发送消息
         */
        void send(String str) {
            executorService.execute(new WriteRunnable(str));
        }


        private class WriteRunnable implements Runnable {
            private final String msg;
            public WriteRunnable(String str) {
                this.msg = str;
            }
            @Override
            public void run() {
                if (ClientWriteHandler.this.done) {
                    return;
                }
                try {
                    ClientWriteHandler.this.printStream.println(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
