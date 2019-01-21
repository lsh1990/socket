package udp2;

/**
 * @ClassName MessageCreator
 * @Description: 消息创建者
 * @Author lsh
 * @Date 2019/1/13 18:35
 * @Version
 */
public class MessageCreator {
    private static final String SN_HEADER = "收到暗号，我是（SN）";
    private static final String PORT_HEADER = "这是暗号，请回电端口（Port）";

    /**
     * 构建端口
     * @param port
     * @return
     */
    public static String buildWhithPort(int port) {
        return PORT_HEADER + port;
    }

    /**
     * 解析端口
     * @param data
     * @return
     */
    public static int parsePort(String data) {
        if (data.startsWith(PORT_HEADER)) {
            return Integer.parseInt(data.substring(PORT_HEADER.length()));
        }
        return -1;
    }

    /**
     * 构建口令
     * @param sn
     * @return
     */
    public static String buildWithSn(String sn) {
        return SN_HEADER + sn;
    }

    /**
     * 解析口令
     * @param data
     * @return
     */
    public static String parseSn(String data) {
        if (data.startsWith(SN_HEADER)) {
            return data.substring(SN_HEADER.length());
        }
        return null;
    }
}
