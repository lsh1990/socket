package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName tcp.Server
 * @Description: TODO
 * @Author lsh
 * @Date 2019/1/9 21:46
 * @Version
 */
public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(2000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("服务器准备就绪~");
        System.out.println("服务器信息：" + serverSocket.getInetAddress() + "P:" + serverSocket.getLocalPort());

        //等待客户端连接
        for(;;) {
            //获取客户端
            Socket accept = null;
            try {
                accept = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ClietnHandler clietnHandler = new ClietnHandler(accept);
            clietnHandler.start();
        }

    }

    private static class ClietnHandler extends Thread {
        private Socket socket;
        private boolean flag = true;
        ClietnHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("新客户端连接：" + socket.getInetAddress() + ",P" + socket.getPort());
            try {
                //获取打印流，用于数据输出；服务器回送数据使用
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                //获取输入流，用于接收数据
                BufferedReader socktInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                do {
                    //客户端拿到一条数据
                    String str = socktInput.readLine();
                    if ("bye".equalsIgnoreCase(str)) {
                        flag = false;
                        //回送
                        socketOutput.println("bye");
                    } else {
                        //打印到屏幕，并回送数据
                        System.out.println(str);
                        socketOutput.println("回送:" + str.length());
                    }
                } while (flag);

                socktInput.close();
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
            System.out.println("客户端已退出:" + socket.getInetAddress() + ",P" + socket.getPort());
        }
    }
}
