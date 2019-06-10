package cn.rongcloud.im.ui.activity.records;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SealSearchConversationResult;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongCommonDefine;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * 按群成员查找的历史消息列表页面
 */
public class MemberActivity extends BaseActivity {
    private SealSearchConversationResult mResult;
    private String mMemberId;
    private UserInfo mMemberInfo;
    private boolean mCompleteFlag;
    private List<Message> mSourceDataList = new ArrayList<>();
    private ListView mLvMember;
    private ChattingRecordsAdapter mAdapter;
    private long mOldestSendTime;
    private boolean mayHaveMoreMsg;// 可以加载更多

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        initView();
        initData();
    }

    private void initData() {
        mMemberInfo = RongUserInfoManager.getInstance().getUserInfo(mMemberId);
        if (mMemberInfo == null) {
            return;
        }
        mCompleteFlag = true;
        getHistoryMessages(0);
    }

    private void getHistoryMessages(long timestamp) {
        RongIMClient.getInstance().getHistoryMessages(mResult.getConversation().getConversationType(),
                mResult.getConversation().getTargetId(), Arrays.asList("RC:TxtMsg", "RC:ImgTextMsg", "RC:FileMsg"),
                timestamp, 30, RongCommonDefine.GetMessageDirection.FRONT, new RongIMClient.ResultCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        mCompleteFlag = true;
                        if (null != messages) {
                            // 找到是这个群成员发的消息
                            List<Message> list = new ArrayList<>();
                            for (Message message : messages) {
                                if (message.getSenderUserId().equals(mMemberId)) {
                                    list.add(message);
                                }
                            }

                            Log.e("swo", list.size() + "");
                            mSourceDataList.addAll(list);
                            mAdapter.notifyDataSetChanged();

                            if (messages.size() > 0) {
                                mOldestSendTime = messages.get(messages.size() - 1).getSentTime();
                                if (list.size() == 0) {
                                    // 有消息但是没有这个人发的，再取一组
                                    getHistoryMessages(mOldestSendTime);
                                } else {
                                    // 有消息，且有这个人的消息，让用户选择可以加载更多
                                    mayHaveMoreMsg = true;
                                }
                            } else {
                                mayHaveMoreMsg = false;
                            }

                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        Log.e("swo", errorCode.toString());
                    }
                });
    }

    private void initView() {
        setTitle("按成员查找");
        mResult = getIntent().getParcelableExtra("searchConversationResult");
        mMemberId = getIntent().getStringExtra("memberId");
        mLvMember = ((ListView) findViewById(R.id.lv_member));
        mAdapter = new ChattingRecordsAdapter();
        mLvMember.setAdapter(mAdapter);
        mLvMember.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (mCompleteFlag && mayHaveMoreMsg) {
                        mCompleteFlag = false;
                        getHistoryMessages(mOldestSendTime);
                    }
                }
            }
        });

        mLvMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof Message) {
                    Message message = (Message)object;
                    Conversation conversation = mResult.getConversation();
                    RongIM.getInstance().startConversation(mContext, conversation.getConversationType(), conversation.getTargetId(), mResult.getTitle(), mSourceDataList.get(position).getSentTime());
                }
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
                viewHolder = new MemberActivity.ChattingRecordsViewHolder();
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

            ImageLoader.getInstance().displayImage(mMemberInfo.getPortraitUri().toString(), viewHolder.portraitImageView, App.getOptions());
            viewHolder.nameTextView.setText(mMemberInfo.getName());
            viewHolder.chatRecordsDetailTextView.setText(CharacterParser.getInstance().getColoredChattingRecord("", message.getContent()));
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
