package udpAndTcp2.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName TCPServer
 * @Description: TCP服务端
 * @Author lsh
 * @Date 2019/1/26 13:24
 * @Version
 */
public class TCPServer {
    private final int port;
    private ClientListener mListener;

    public TCPServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            ClientListener listener = null;
            listener = new ClientListener(port);
            mListener = listener;
            new Thread(listener).start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null) {
            mListener.exit();
        }
    }

    private static class ClientListener implements Runnable {
        private ServerSocket server;
        private boolean done = false;

        public ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " P:" + server.getLocalPort());
        }

        @Override
        public void run() {

            System.out.println("服务器准备就绪～");
            do {
            //得到客户端
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }
                //构建异步线程
                ClientHandler clientHandler = new ClientHandler(client);
                //启动线程
                new Thread(clientHandler).start();
            }while (!done);
            System.out.println("服务器已关闭！");
        }
        void exit() {
            done = true;
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理客户端线程
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private boolean flag = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("新客户端连接：" + socket.getInetAddress() +
                    " P:" + socket.getPort());
            try {
                //获取打印流，用于数据输出，服务器回送数据使用
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                //获取输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                do {
                    //客户端拿到一条数据
                    String str = socketInput.readLine();
                    if ("bye".equalsIgnoreCase(str)) {
                        flag = false;
                        // 回送
                        socketOutput.println("bye");
                    } else {
                        // 打印到屏幕。并回送数据长度
                        System.out.println(str);
                        socketOutput.println("回送：" + str.length());
                    }
                } while (flag);
                socketInput.close();
                socketOutput.close();
            } catch (IOException e) {
                System.out.println("连接异常断开");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("客户端已退出：" + socket.getInetAddress() +
                    " P:" + socket.getPort());
        }
    }
}
