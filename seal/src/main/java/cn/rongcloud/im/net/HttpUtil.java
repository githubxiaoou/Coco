package cn.rongcloud.im.net;

public class HttpUtil {
    public static ServiceS apiS() {
        return NetManager.getInstance().create(ServiceS.class);
    }
}
