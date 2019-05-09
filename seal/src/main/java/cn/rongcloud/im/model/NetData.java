package cn.rongcloud.im.model;

import java.io.Serializable;

/**
 * Created by will
 * on 2018/10/8.
 */
public class NetData<T> implements Serializable {
    public String msg;
    public int error;
    public T data;
}
