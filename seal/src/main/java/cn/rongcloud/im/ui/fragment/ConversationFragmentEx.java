package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealCSEvaluateInfo;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.model.SealCSEvaluateItem;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.response.GetGroupDetailResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.activity.ReadReceiptDetailActivity;
import cn.rongcloud.im.ui.activity.forward.ForwardDetailActivity;
import cn.rongcloud.im.ui.activity.forward.ForwardListActivity;
import cn.rongcloud.im.ui.widget.BottomEvaluateDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongMessageItemLongClickActionManager;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imlib.CustomServiceConfig;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.cs.CustomServiceManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

import static cn.rongcloud.im.server.BaseAction.DOMAIN_APP;

/**
 * 会话 Fragment 继承自ConversationFragment
 * onResendItemClick: 重发按钮点击事件. 如果返回 false,走默认流程,如果返回 true,走自定义流程
 * onReadReceiptStateClick: 已读回执详情的点击事件.
 * 如果不需要重写 onResendItemClick 和 onReadReceiptStateClick ,可以不必定义此类,直接集成 ConversationFragment 就可以了
 */
public class ConversationFragmentEx extends ConversationFragment {
    private OnShowAnnounceListener onShowAnnounceListener;
    private BottomEvaluateDialog dialog;
    private List<SealCSEvaluateItem> mEvaluateList;
    private String mTargetId = "";
    private RongExtension rongExtension;
    private ListView listView;
    private String mUserId;
    private MessageItemLongClickAction clickAction;
    private boolean isAdmin;
    private boolean isCreator;
    private Conversation.ConversationType mConversationType;
    private Message mForwardMessage;
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RongIMClient.getInstance().setCustomServiceHumanEvaluateListener(new CustomServiceManager.OnHumanEvaluateListener() {
            @Override
            public void onHumanEvaluate(JSONObject evaluateObject) {
                JSONObject jsonObject = evaluateObject;
                SealCSEvaluateInfo sealCSEvaluateInfo = new SealCSEvaluateInfo(jsonObject);
                mEvaluateList = sealCSEvaluateInfo.getSealCSEvaluateInfoList();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        rongExtension = (RongExtension) v.findViewById(io.rong.imkit.R.id.rc_extension);
        View messageListView = findViewById(v, io.rong.imkit.R.id.rc_layout_msg_list);
        listView = findViewById(messageListView, io.rong.imkit.R.id.rc_list);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sp = getActivity().getSharedPreferences("config", 0x0000);
        getBgImage();
        if (mConversationType != Conversation.ConversationType.PRIVATE) {
            // 群聊为管理员和群主添加撤回按钮
            getGroupDetail();
        }
        addForward();
        addCollection();
    }


    /**
     * 获取群详情，如果是群主或管理员，可以撤销消息
     */
    private void getGroupDetail() {
        LoadDialog.show(getActivity());
        HttpUtil.apiS()
                .getGroupDetail(mTargetId, mUserId)
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(getActivity());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<GetGroupDetailResponse>>() {
                    @Override
                    public void Successful(NetData<GetGroupDetailResponse> response) {
                        GetGroupDetailResponse result = response.result;
                        isAdmin = null != result && "1".equals(result.isAdmin);
                        isCreator = null != result && mUserId.equals(result.creatorId);
                        if (isAdmin || isCreator) {
                            addCallBack();
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    /**
     * 添加撤销按钮
     * 与原有撤销功能独立，这里允许管理员撤销所有人发的消息
     */
    private void addCallBack() {
        // 如果原来有撤回按钮，先移除。统一添加
        List<MessageItemLongClickAction> actions = RongMessageItemLongClickActionManager.getInstance().getMessageItemLongClickActions();
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).getTitle(getActivity()).contains("撤回")) {
                RongMessageItemLongClickActionManager.getInstance().removeMessageItemLongClickAction(actions.get(i));
            }
        }

        this.clickAction = (new MessageItemLongClickAction.Builder()).title("撤回消息").actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
            public boolean onMessageItemLongClick(Context context, UIMessage message) {
                RongIM.getInstance().recallMessage(message.getMessage(), getPushContent(getActivity(), message));
                return true;
            }
        }).build();
        RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(this.clickAction, 3);
    }

    public String getPushContent(Context context, UIMessage message) {
        String userName = "";
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
        if (userInfo != null) {
            userName = userInfo.getName();
        }

        return context.getString(io.rong.imkit.R.string.rc_admin_recalled_message, new Object[]{"管理员", userName});
    }

    /**
     * 添加收藏按钮
     */
    private void addCollection() {
        this.clickAction = (new MessageItemLongClickAction.Builder()).title("收藏").actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
            @Override
            public boolean onMessageItemLongClick(Context context, UIMessage uiMessage) {
                SharedPreferences.Editor edit = sp.edit();
                String oldIds = sp.getString(mUserId, "");
                String newId = uiMessage.getUId();
                if (TextUtils.isEmpty(oldIds)) {
                    edit.putString(mUserId, newId);
                } else {
                    edit.putString(mUserId, oldIds + "," + newId);
                }
                edit.apply();
                NToast.shortToast(getActivity(), "已收藏");
                return true;
            }
        }).build();
        RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(this.clickAction, 4);
    }

    /**
     * 添加转发按钮
     */
    private void addForward() {
        this.clickAction = (new MessageItemLongClickAction.Builder()).title("转发").actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
            public boolean onMessageItemLongClick(Context context, UIMessage message) {
                Intent intent = new Intent(context, ForwardListActivity.class);
                mForwardMessage = message.mMessage;
                intent.putExtra("message", mForwardMessage);
                startActivity(intent);
                return true;
            }
        }).build();
        RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(this.clickAction, 0);
    }

    public final String LOCAL_BG_DEFAULT = DOMAIN_APP + "/avatar/chatbg/local_background_default.png";
    public final String LOCAL_BG_ONE = DOMAIN_APP + "/avatar/chatbg/local_background_one.jpg";
    public final String LOCAL_BG_TWO = DOMAIN_APP + "/avatar/chatbg/local_background_two.jpg";
    public final String LOCAL_BG_THREE = DOMAIN_APP + "/avatar/chatbg/local_background_three.jpg";
    public final String LOCAL_BG_FOUR = DOMAIN_APP + "/avatar/chatbg/local_background_four.jpg";
    public final String LOCAL_BG_FIVE = DOMAIN_APP + "/avatar/chatbg/local_background_five.jpg";
    private String[] bgs = new String[]{LOCAL_BG_DEFAULT, LOCAL_BG_ONE, LOCAL_BG_TWO, LOCAL_BG_THREE, LOCAL_BG_FOUR, LOCAL_BG_FIVE};
    private int[] resIds = new int[]{R.drawable.local_background_default,R.drawable.local_background_one,R.drawable.local_background_two,R.drawable.local_background_three,R.drawable.local_background_four,R.drawable.local_background_five};
    private void getBgImage() {
        HttpUtil.apiS()
                .getChatBackgroundImage(mUserId, mTargetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData>() {
                    @Override
                    public void Successful(NetData netData) {
                        if (!TextUtils.isEmpty(((String) netData.result))) {
                            for (int i = 0; i < bgs.length; i++) {
                                if (((String) netData.result).equals(bgs[i])) {
                                    listView.setBackgroundResource(resIds[i]);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    @Override
    protected void initFragment(Uri uri) {
        super.initFragment(uri);
        if (uri != null) {
            mTargetId = uri.getQueryParameter("targetId");
            mUserId = getArguments().getString("userId");
            mConversationType = (Conversation.ConversationType) getArguments().getSerializable("conversationType");
        }
    }

    @Override
    public void onReadReceiptStateClick(io.rong.imlib.model.Message message) {
        if (message.getConversationType() == Conversation.ConversationType.GROUP) { //目前只适配了群组会话
            Intent intent = new Intent(getActivity(), ReadReceiptDetailActivity.class);
            intent.putExtra("message", message);
            getActivity().startActivity(intent);
        }
    }

    public void onWarningDialog(String msg) {
        String typeStr = getUri().getLastPathSegment();
        if (!typeStr.equals("chatroom")) {
            super.onWarningDialog(msg);
        }
    }

    @Override
    public void onShowAnnounceView(String announceMsg, String announceUrl) {
        if (onShowAnnounceListener != null) {
            onShowAnnounceListener.onShowAnnounceView(announceMsg, announceUrl);
        }
    }

    /**
     * 显示通告栏的监听器
     */
    public interface OnShowAnnounceListener {

        /**
         * 展示通告栏的回调
         *
         * @param announceMsg 通告栏展示内容
         * @param annouceUrl  通告栏点击链接地址，若此参数为空，则表示不需要点击链接，否则点击进入链接页面
         * @return
         */
        void onShowAnnounceView(String announceMsg, String annouceUrl);
    }

    public void setOnShowAnnounceBarListener(OnShowAnnounceListener listener) {
        onShowAnnounceListener = listener;
    }

    public void showStartDialog(final String dialogId) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new BottomEvaluateDialog(getActivity(), mEvaluateList);
        dialog.show();
        dialog.setEvaluateDialogBehaviorListener(new BottomEvaluateDialog.OnEvaluateDialogBehaviorListener() {
            @Override
            public void onSubmit(int source, String tagText, CustomServiceConfig.CSEvaSolveStatus resolveStatus, String suggestion) {
                RongIMClient.getInstance().evaluateCustomService(mTargetId, source, resolveStatus, tagText,
                        suggestion, dialogId, null);
                if (dialog != null && getActivity() != null) {
                    getActivity().finish();
                }
            }

            @Override
            public void onCancel() {
                if (dialog != null && getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void onShowStarAndTabletDialog(String dialogId) {
        showStartDialog(dialogId);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().isFinishing()) {
            RongIMClient.getInstance().setCustomServiceHumanEvaluateListener(null);
        }
    }

    @Override
    public void onPluginToggleClick(View v, ViewGroup extensionBoard) {
        if (!rongExtension.isExtensionExpanded()) {
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.requestFocusFromTouch();
                    listView.setSelection(listView.getCount());
                }
            }, 100);
        }
    }

    @Override
    public void onEmoticonToggleClick(View v, ViewGroup extensionBoard) {
        if (!rongExtension.isExtensionExpanded()) {
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.requestFocusFromTouch();
                    listView.setSelection(listView.getCount());
                }
            }, 100);
        }
    }

    @Override
    public boolean showMoreClickItem() {
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 100) {
//                Intent intent = new Intent(getActivity(), ForwardDetailActivity.class);
                data.setClass(getActivity(), ForwardDetailActivity.class);
                data.putExtra("message", mForwardMessage);
//                intent.putExtra("conversationType", data.getSerializableExtra("conversationType"));
//                intent.putExtra("targetId", data.getStringExtra("targetId"));
                startActivity(data);
            }
        }
    }
}
