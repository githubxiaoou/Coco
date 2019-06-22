package cn.rongcloud.im.ui.activity.collection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utils.RongOperationPermissionUtils;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.SightMessage;
import io.rong.message.TextMessage;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * 收藏消息列表页面
 * 没分页
 */
public class CollectionActivity extends BaseActivity {
    private boolean mCompleteFlag;
    private List<UIMessage> mSourceDataList = new ArrayList<>();
    private ListView mLvCollection;
    private ChattingRecordsAdapter mAdapter;
    private boolean mayHaveMoreMsg;// 可以加载更多
    private SharedPreferences sp;
    private String mUserId;
    private List<String> mMessageIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        initView();
        initData();
    }

    private void initData() {
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        String msgIds = sp.getString(mUserId, "");
        if (!TextUtils.isEmpty(msgIds)) {
            mMessageIds = Arrays.asList(msgIds.split(","));
            getHistoryMessages();
        }
    }

    private void initView() {
        setTitle("收藏");
        mLvCollection = ((ListView) findViewById(R.id.lv_collection));
        mAdapter = new ChattingRecordsAdapter();
        mLvCollection.setAdapter(mAdapter);
//        mLvCollection.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
//                    if (mCompleteFlag && mayHaveMoreMsg) {
//                        mCompleteFlag = false;
//                        getHistoryMessages();
//                    }
//                }
//            }
//        });

        mLvCollection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof UIMessage) {
                    UIMessage message = (UIMessage) object;
//                    String title;
//                    if (message.getConversationType() == Conversation.ConversationType.GROUP) {
//                        Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(message.getTargetId());
//                        title = groupInfo.getName();
//                    } else {
//                        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
//                        title = userInfo.getName();
//                    }
//                    RongIM.getInstance().startConversation(mContext, message.getConversationType(), message.getTargetId(), title, mSourceDataList.get(position).getSentTime());
                    String objectName = message.getObjectName();
                    if (objectName.contains("TxtMsg")) {
                        Intent intent = new Intent();
                        intent.setClass(mContext, CollectionDetailActivity.class);
                        intent.putExtra("senderUserId", message.getSenderUserId());
                        intent.putExtra("content", message.getContent());
                        intent.putExtra("sentTime", message.getSentTime());
                        startActivity(intent);
                        return;
                    }

                    // 消息点击事件
                    Object provider;
                    if (getNeedEvaluate(message)) {
                        provider = RongContext.getInstance().getEvaluateProvider();
                    } else {
                        provider = RongContext.getInstance().getMessageTemplate(message.getContent().getClass());
                    }

                    if (provider != null) {
                        // TODO: 2019/6/18 这里存在转化错误
                        try {
                            ((MessageProvider) provider).onItemClick(view, position, message.getContent(), message);
                        } catch (Exception e) {
                            if (message.getContent() instanceof SightMessage) {
                                SightMessage content = (SightMessage) message.getContent();
                                if (content != null) {
                                    if (!RongOperationPermissionUtils.isMediaOperationPermit(view.getContext())) {
                                        return;
                                    }

                                    String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
                                    if (!PermissionCheckUtil.checkPermissions(view.getContext(), permissions)) {
                                        Activity activity = (Activity) view.getContext();
                                        PermissionCheckUtil.requestPermissions(activity, permissions, 100);
                                        return;
                                    }

                                    Uri.Builder builder = new Uri.Builder();
                                    builder.scheme("rong").authority(view.getContext().getPackageName()).appendPath("sight").appendPath("player");
                                    String intentUrl = builder.build().toString();
                                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(intentUrl));
                                    intent.setPackage(view.getContext().getPackageName());
                                    intent.putExtra("SightMessage", content);
                                    intent.putExtra("Message", message.getMessage());
                                    intent.putExtra("Progress", message.getProgress());
                                    if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
                                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                        view.getContext().startActivity(intent);
                                    } else {
                                        Toast.makeText(view.getContext(), "Sight Module does not exist.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    boolean evaForRobot = false;
    boolean robotMode = true;

    protected boolean getNeedEvaluate(UIMessage data) {
        String extra = "";
        String robotEva = "";
        String sid = "";
        if (data != null && data.getConversationType() != null && data.getConversationType().equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
            if (data.getContent() instanceof TextMessage) {
                extra = ((TextMessage) data.getContent()).getExtra();
                if (TextUtils.isEmpty(extra)) {
                    return false;
                }

                try {
                    JSONObject jsonObj = new JSONObject(extra);
                    robotEva = jsonObj.optString("robotEva");
                    sid = jsonObj.optString("sid");
                } catch (JSONException var6) {
                }
            }

            if (data.getMessageDirection() == Message.MessageDirection.RECEIVE && data.getContent() instanceof TextMessage && this.evaForRobot && this.robotMode && !TextUtils.isEmpty(robotEva) && !TextUtils.isEmpty(sid) && !data.getIsHistoryMessage()) {
                return true;
            }
        }

        return false;
    }

    private int mFuck;

    private void getHistoryMessages() {
        LoadDialog.show(mContext);
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) {
                for (String id : mMessageIds) {
                    RongIMClient.getInstance().getMessageByUid(id, new RongIMClient.ResultCallback<Message>() {
                        @Override
                        public void onSuccess(Message message) {
                            mFuck++;
                            mSourceDataList.add(UIMessage.obtain(message));
                            if (mFuck == mMessageIds.size()) {
                                emitter.onComplete();
                            }
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            emitter.onError(new Exception());
                        }
                    });
                }
            }
        })
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        NToast.shortToast(mContext, "获取失败，请稍后重试");
                        LoadDialog.dismiss(mContext);
                    }

                    @Override
                    public void onComplete() {
                        LoadDialog.dismiss(mContext);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }


    private class ChattingRecordsAdapter extends BaseAdapter {

        public ChattingRecordsAdapter() {

        }

        @Override
        public int getCount() {
            if (mSourceDataList != null) {
                return mSourceDataList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mSourceDataList == null)
                return null;

            if (position >= mSourceDataList.size())
                return null;

            return mSourceDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ChattingRecordsViewHolder viewHolder;
            UIMessage message = (UIMessage) getItem(position);
            if (convertView == null) {
                viewHolder = new ChattingRecordsViewHolder();
                convertView = View.inflate(getBaseContext(), R.layout.item_filter_chatting_records_list, null);
                viewHolder.portraitImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.item_iv_record_image);
                viewHolder.chatDetailLinearLayout = (LinearLayout) convertView.findViewById(R.id.item_ll_chatting_records_detail);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_tv_chat_name);
                viewHolder.chatRecordsDetailTextView = (TextView) convertView.findViewById(R.id.item_tv_chatting_records_detail);
                viewHolder.chatRecordsDateTextView = (TextView) convertView.findViewById(R.id.item_tv_chatting_records_date);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChattingRecordsViewHolder) convertView.getTag();
            }

            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            if (userInfo != null) {
                ImageLoader.getInstance().displayImage(userInfo.getPortraitUri().toString(), viewHolder.portraitImageView, App.getOptions());
                viewHolder.nameTextView.setText(userInfo.getName());
            }
            String objectName = message.getObjectName();
            if (objectName.contains("Card")) {
                viewHolder.chatRecordsDetailTextView.setText("[" + "名片" + "]");
            } else if (objectName.contains("Img")) {
                viewHolder.chatRecordsDetailTextView.setText("[" + "图片" + "]");
            } else if (objectName.contains("Vc")) {
                viewHolder.chatRecordsDetailTextView.setText("[" + "语音" + "]");
            } else if (objectName.contains("Sight")) {
                viewHolder.chatRecordsDetailTextView.setText("[" + "小视频" + "]");
            } else {
                viewHolder.chatRecordsDetailTextView.setText(CharacterParser.getInstance().getColoredChattingRecord("", message.getContent()));
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            String date = simpleDateFormat.format(new Date(message.getSentTime()));
            String formatDate = date.replace("-", "/");
            viewHolder.chatRecordsDateTextView.setText(formatDate);

            return convertView;
        }
    }

    class ChattingRecordsViewHolder {
        SelectableRoundedImageView portraitImageView;
        LinearLayout chatDetailLinearLayout;
        TextView nameTextView;
        TextView chatRecordsDetailTextView;
        TextView chatRecordsDateTextView;
    }
}
