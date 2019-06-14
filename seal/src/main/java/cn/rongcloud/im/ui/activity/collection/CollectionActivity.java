package cn.rongcloud.im.ui.activity.collection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.push.RongPushClient;

/**
 * 收藏消息列表页面
 * 没分页
 */
public class CollectionActivity extends BaseActivity {
    private boolean mCompleteFlag;
    private List<Message> mSourceDataList = new ArrayList<>();
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
                if (object instanceof Message) {
                    Message message = (Message)object;
                    String title;
                    if (message.getConversationType() == Conversation.ConversationType.GROUP) {
                        Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(message.getTargetId());
                        title = groupInfo.getName();
                    } else {
                        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
                        title = userInfo.getName();
                    }
                    RongIM.getInstance().startConversation(mContext, message.getConversationType(), message.getTargetId(), title, mSourceDataList.get(position).getSentTime());
                }
            }
        });
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
                            mSourceDataList.add(message);
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
            Message message = (Message) getItem(position);
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