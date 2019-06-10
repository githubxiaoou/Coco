package cn.rongcloud.im.ui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.BlackList;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import cn.rongcloud.im.ui.activity.forward.ForwardListActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.contactcard.message.ContactMessage;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by AMing on 16/8/1.
 * Company RongCloud
 */
public class SinglePopWindow extends PopupWindow {
    private static final int ADDBLACKLIST = 167;
    private static final int REMOVEBLACKLIST = 168;
    private final BaseActivity mContext;
    private View conentView;
    private AsyncTaskManager asyncTaskManager;


    @SuppressLint("InflateParams")
    public SinglePopWindow(final Activity context, final String userId, final Friend friend, final RongIMClient.BlacklistStatus blacklistStatus) {
        mContext = ((BaseActivity) context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.popupwindow_more, null);
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationPreview);
        asyncTaskManager = AsyncTaskManager.getInstance(context);
        RelativeLayout blacklistStatusRL = (RelativeLayout) conentView.findViewById(R.id.blacklist_status);
        RelativeLayout rlDelete = (RelativeLayout) conentView.findViewById(R.id.rl_delete);
        RelativeLayout rlSend = (RelativeLayout) conentView.findViewById(R.id.rl_send);
        final TextView blacklistText = (TextView) conentView.findViewById(R.id.blacklist_text_status);

        if (blacklistStatus == RongIMClient.BlacklistStatus.IN_BLACK_LIST) {
            blacklistText.setText(R.string.remove_from_blacklist);
        } else {
            blacklistText.setText(R.string.join_the_blacklist);
        }

        rlSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCard(friend);
            }
        });

        rlDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFriend(userId, friend.getUserId());
            }
        });

        blacklistStatusRL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (blacklistStatus == RongIMClient.BlacklistStatus.IN_BLACK_LIST) {
                    RongIM.getInstance().removeFromBlacklist(friend.getUserId(), new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            asyncTaskManager.request(ADDBLACKLIST, new OnDataListener() {
                                @Override
                                public Object doInBackground(int requestCode, String parameter) throws HttpException {
                                    return new SealAction(context).removeFromBlackList(friend.getUserId());
                                }

                                @Override
                                public void onSuccess(int requestCode, Object result) {
                                    SealUserInfoManager.getInstance().deleteBlackList(friend.getUserId());
                                    NToast.shortToast(context, context.getString(R.string.remove_successful));
                                }

                                @Override
                                public void onFailure(int requestCode, int state, Object result) {

                                }
                            });
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            NToast.shortToast(context, context.getString(R.string.remove_failed));
                        }
                    });
                } else {
                    PromptPopupDialog.newInstance(context, context.getString(R.string.join_the_blacklist),
                            context.getString(R.string.des_add_friend_to_black_list)).setPromptButtonClickedListener(new PromptPopupDialog.OnPromptButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked() {
                            RongIM.getInstance().addToBlacklist(friend.getUserId(), new RongIMClient.OperationCallback() {
                                @Override
                                public void onSuccess() {

                                    asyncTaskManager.request(REMOVEBLACKLIST, new OnDataListener() {
                                        @Override
                                        public Object doInBackground(int requestCode, String parameter) throws HttpException {
                                            return new SealAction(context).addToBlackList(friend.getUserId());
                                        }

                                        @Override
                                        public void onSuccess(int requestCode, Object result) {
                                            SealUserInfoManager.getInstance().addBlackList(new BlackList(
                                                    friend.getUserId(),
                                                    null,
                                                    null
                                            ));
                                            NToast.shortToast(context, context.getString(R.string.join_success));
                                        }

                                        @Override
                                        public void onFailure(int requestCode, int state, Object result) {

                                        }
                                    });
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    NToast.shortToast(context, context.getString(R.string.join_failed));
                                }
                            });
                        }
                    }).show();
                }
                SinglePopWindow.this.dismiss();
            }

        });
    }

    /**
     * 发送名片
     *
     * @param friend
     */
    private void sendCard(Friend friend) {
        Intent intent = new Intent(mContext, ForwardListActivity.class);
        Message obtain = Message.obtain("", null, ContactMessage.obtain(friend.getUserId(), friend.getName(), friend.getPortraitUri().toString(), "", "", ""));
        ((UserDetailActivity) mContext).mForwardMessage = obtain;
        mContext.startActivityForResult(intent, 100);
    }

    /**
     * 删除好友
     *
     * @param userId
     * @param friendId
     */
    private void deleteFriend(String userId, String friendId) {
        LoadDialog.show(mContext);
        HttpUtil.apiS().deleteFriend(userId, friendId)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData>() {
                    @Override
                    public void Successful(NetData netData) {
                        mContext.startActivity(new Intent(mContext, MainActivity.class));
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                        SinglePopWindow.this.dismiss();
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "删除失败，请重试");
                    }
                });
    }

    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent, 0, 0);
        } else {
            this.dismiss();
        }
    }
}
