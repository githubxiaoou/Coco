//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.rongcloud.im.message.manager;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import cn.rongcloud.im.message.GifMessage;
import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongIM.OnSendMessageListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.RongIMClient.SendImageMessageCallback;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message.SentStatus;
import io.rong.message.ImageMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SendGifManager {
    private static final String TAG = "SendGifManager";
    private ExecutorService executorService;
    private SendGifManager.UploadController uploadController;

    public static SendGifManager getInstance() {
        return SendGifManager.SingletonHolder.sInstance;
    }

    private SendGifManager() {
        this.executorService = this.getExecutorService();
        this.uploadController = new SendGifManager.UploadController();
    }

    public void sendImages(ConversationType conversationType, String targetId, List<Uri> imageList, boolean isFull) {
        RLog.d(TAG, "sendImages " + imageList.size());
        Iterator var5 = imageList.iterator();

        while(var5.hasNext()) {
            Uri image = (Uri)var5.next();
            if (!TextUtils.isEmpty(image.getPath())) {
                File file = new File(image.getPath());
                if (file.exists()) {
                    GifMessage content = GifMessage.obtain(image, image, isFull);
                    OnSendMessageListener listener = RongContext.getInstance().getOnSendMessageListener();
                    if (listener != null) {
                        Message message = listener.onSend(Message.obtain(targetId, conversationType, content));
                        if (message != null) {
                            RongIMClient.getInstance().insertMessage(conversationType, targetId, (String)null, message.getContent(), new ResultCallback<Message>() {
                                public void onSuccess(Message message) {
                                    message.setSentStatus(SentStatus.SENDING);
                                    RongIMClient.getInstance().setMessageSentStatus(message.getMessageId(), SentStatus.SENDING, (ResultCallback)null);
                                    RongContext.getInstance().getEventBus().post(message);
                                    SendGifManager.this.uploadController.execute(message);
                                }

                                public void onError(ErrorCode e) {
                                }
                            });
                        }
                    } else {
                        RongIMClient.getInstance().insertMessage(conversationType, targetId, (String)null, content, new ResultCallback<Message>() {
                            public void onSuccess(Message message) {
                                message.setSentStatus(SentStatus.SENDING);
                                RongIMClient.getInstance().setMessageSentStatus(message.getMessageId(), SentStatus.SENDING, (ResultCallback)null);
                                RongContext.getInstance().getEventBus().post(message);
                                SendGifManager.this.uploadController.execute(message);
                            }

                            public void onError(ErrorCode e) {
                            }
                        });
                    }
                }
            }
        }

    }

    public void cancelSendingImages(ConversationType conversationType, String targetId) {
        RLog.d(TAG, "cancelSendingImages");
        if (conversationType != null && targetId != null && this.uploadController != null) {
            this.uploadController.cancel(conversationType, targetId);
        }

    }

    public void cancelSendingImage(ConversationType conversationType, String targetId, int messageId) {
        RLog.d(TAG, "cancelSendingImages");
        if (conversationType != null && targetId != null && this.uploadController != null && messageId > 0) {
            this.uploadController.cancel(conversationType, targetId, messageId);
        }

    }

    public void reset() {
        this.uploadController.reset();
    }

    private ExecutorService getExecutorService() {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(1, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue(), this.threadFactory("Rong SendMediaManager", false));
        }

        return this.executorService;
    }

    private ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            public Thread newThread(@Nullable Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

    private class UploadController implements Runnable {
        final List<Message> pendingMessages = new ArrayList();
        Message executingMessage;

        public UploadController() {
        }

        public void execute(Message message) {
            synchronized(this.pendingMessages) {
                this.pendingMessages.add(message);
                if (this.executingMessage == null) {
                    this.executingMessage = (Message)this.pendingMessages.remove(0);
                    SendGifManager.this.executorService.submit(this);
                }

            }
        }

        public void reset() {
            RLog.w(TAG, "Rest Sending Images.");
            synchronized(this.pendingMessages) {
                Iterator var2 = this.pendingMessages.iterator();

                while(true) {
                    if (!var2.hasNext()) {
                        this.pendingMessages.clear();
                        break;
                    }

                    Message message = (Message)var2.next();
                    message.setSentStatus(SentStatus.FAILED);
                    RongContext.getInstance().getEventBus().post(message);
                }
            }

            if (this.executingMessage != null) {
                this.executingMessage.setSentStatus(SentStatus.FAILED);
                RongContext.getInstance().getEventBus().post(this.executingMessage);
                this.executingMessage = null;
            }

        }

        public void cancel(ConversationType conversationType, String targetId) {
            synchronized(this.pendingMessages) {
                int count = this.pendingMessages.size();

                for(int i = 0; i < count; ++i) {
                    Message msg = (Message)this.pendingMessages.get(i);
                    if (msg.getConversationType().equals(conversationType) && msg.getTargetId().equals(targetId)) {
                        this.pendingMessages.remove(msg);
                    }
                }

                if (this.pendingMessages.size() == 0) {
                    this.executingMessage = null;
                }

            }
        }

        public void cancel(ConversationType conversationType, String targetId, int messageId) {
            synchronized(this.pendingMessages) {
                int count = this.pendingMessages.size();

                for(int i = 0; i < count; ++i) {
                    Message msg = (Message)this.pendingMessages.get(i);
                    if (msg.getConversationType().equals(conversationType) && msg.getTargetId().equals(targetId) && msg.getMessageId() == messageId) {
                        this.pendingMessages.remove(msg);
                        break;
                    }
                }

                if (this.pendingMessages.size() == 0) {
                    this.executingMessage = null;
                }

            }
        }

        private void polling() {
            synchronized(this.pendingMessages) {
                RLog.d(TAG, "polling " + this.pendingMessages.size());
                if (this.pendingMessages.size() > 0) {
                    this.executingMessage = (Message)this.pendingMessages.remove(0);
                    SendGifManager.this.executorService.submit(this);
                } else {
                    this.pendingMessages.clear();
                    this.executingMessage = null;
                }

            }
        }

        public void run() {
            RongIM.getInstance().sendImageMessage(this.executingMessage, (String)null, (String)null, false, new SendImageMessageCallback() {
                public void onAttached(Message message) {
                }

                public void onError(Message message, ErrorCode code) {
                    UploadController.this.polling();
                }

                public void onSuccess(Message message) {
                    UploadController.this.polling();
                }

                public void onProgress(Message message, int progress) {
                }
            });
        }
    }

    static class SingletonHolder {
        static SendGifManager sInstance = new SendGifManager();

        SingletonHolder() {
        }
    }
}
