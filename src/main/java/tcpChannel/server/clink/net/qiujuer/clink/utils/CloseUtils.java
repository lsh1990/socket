package tcpChannel.server.clink.net.qiujuer.clink.utils;

import java.io.Closeable;
import java.io.IOException;
 /**
* @Description: 关闭流
* @Param
* @Return
* @author lsh
* @date 2019/1/27 19:38
*/
public class CloseUtils {
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
