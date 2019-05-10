package cn.rongcloud.im.net;

import cn.rongcloud.im.model.NetData;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by will
 * on 2018/3/19.
 * retrofit 统一回调  rxJava
 * 调用记得切回主线程 否则Toast会报错
 */

public abstract class NetObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        if (t instanceof NetData) {
            NetData bean = (NetData) t;
            switch (bean.code) {
                case 200:
                    Successful(t);
                    break;
//                case 900:// 版本更新
//                    String msg = ((NetData) t).msg;
//                    if (msg != null) {
//                        UpdateVersionBean versionBean = new Gson().fromJson(msg, UpdateVersionBean.class);
//                        UpdateDialog.showDialog(((AppCompatActivity) MyActivityManager.getInstance().getCurrentActivity()).getSupportFragmentManager(), versionBean);
//                    }
//                    break;
//                case 401://认证过期
//                    TToast.show(((NetData) t).msg);
//                    DataUtil.cleanUserInfo();
//                    LoginActivity.cleanStart(App.getContext());
//                    PushAgent.getInstance(MyApp.getContext()).disable(new IUmengCallback() {
//                        @Override
//                        public void onSuccess() {
//                        }
//
//                        @Override
//                        public void onFailure(String s, String s1) {
//                        }
//                    });
//                    break;
                default:
                    onError(new NetServerException(bean.msg != null ? bean.msg : "", bean.code));
                    break;
            }
        }
    }

    @Override
    public void onError(Throwable e) {
//        if (e instanceof UnknownHostException || e instanceof ConnectException) {
//            TToast.show("没有网络");
//        } else if (e instanceof SocketTimeoutException) {
//            TToast.show("请求超时");
//        } else if (e instanceof NetServerException) {
//            TToast.show(e.getMessage());
//        } else {
//            TToast.show("网络错误");
////            Logger.e(e.toString());
//        }
        Failure(e);
    }

    @Override
    public void onComplete() {
    }

    public abstract void Successful(T t);

    public abstract void Failure(Throwable t);
}
