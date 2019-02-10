package tcpChannel.server;

import tcpChannel.server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TCPServer
 * @Description: TCP服务端
 * @Author lsh
 * @Date 2019/1/27 13:37
 * @Version
 */
public class TCPServer {
    private final int port;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();

    public TCPServer(int port) {
        this.port = port;
    }
    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
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
        clientHandlerList.clear();
    }

    public void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    /**
     * 监听客户端连接
     */
    private class ClientListener implements Runnable {

        private ServerSocket server;
        private boolean done = true;
        private ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " P:" + server.getLocalPort());
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪～");
            //等待客户端连接
            do {
                //获取客户端
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }
                //构建异步线程客户端
                ClientHandler clientHandler = null;
                try {
                    clientHandler = new ClientHandler(client, new ClientHandler.CloseNotify() {
                        @Override
                        public void onSelfClosed(ClientHandler handler) {
                            clientHandlerList.remove(handler);
                        }
                    });
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            } while (!done);
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
}
