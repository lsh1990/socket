package tcp;

import java.io.*;
import java.net.*;

/**
 * @ClassName tcp.Client
 * @Description: TODO
 * @Author lsh
 * @Date 2019/1/9 21:12
 * @Version
 */
public class Client {
    public static void main(String[] args) throws SocketException {
        Socket socket = new Socket();
        //读取流超时时间
        socket.setSoTimeout(300000000);
        //连接本地，端口2000.超时时间设置3000ms
        try {
            socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 2000), 300000000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("已发起服务连接，并进入后续流程~");
        System.out.println("客户端信息:" + socket.getLocalAddress() + ",P:" + socket.getLocalPort());
        System.out.println("服务器信息:" + socket.getInetAddress() + ",P" + socket.getPort());

        //发送数据信息
        try {
            todo(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void todo(Socket socket) throws IOException {
        //构建键盘输入流
        InputStream in = System.in;
        //转换成bufread
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        //获取socket输出流
        OutputStream outputStream = socket.getOutputStream();
        //获取打印输出流
        PrintStream printStream = new PrintStream(outputStream);
        
        //获取socket输入流
        InputStream inputStream = socket.getInputStream();
        BufferedReader socketBr = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;
        do {
            //键盘读取一行
            String str = input.readLine();
            //发送到服务器
            printStream.println(str);

            //从服务器读取一行
            String echo = socketBr.readLine();
            if ("bye".equals(echo)) {
                flag = false;
            } else {
                System.out.println(echo);
            }
        } while (false);
        //释放资源
        printStream.close();
        socketBr.close();

    }
}
