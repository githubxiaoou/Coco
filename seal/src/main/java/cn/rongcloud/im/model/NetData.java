package cn.rongcloud.im.model;

import java.io.Serializable;

/**
 * Created by will
 * on 2018/10/8.
 */
public class NetData<T> implements Serializable {
    public String msg;
    public int code;
    public String url;
    public T result;
    public String isAllForbiddenWords;// 是否开启了全员禁言，1开启，其他未开启。
}
