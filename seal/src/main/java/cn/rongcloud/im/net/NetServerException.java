package cn.rongcloud.im.net;

import java.io.IOException;

/**
 * Created by will
 * on 2018/6/15.
 */
public class NetServerException extends IOException{
    private static final long serialVersionUID = -1024L;
    private int errCode;
    public NetServerException(String message) {
        super(message);
    }

    public NetServerException(String message, int errCode) {
        super(message);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }
}
