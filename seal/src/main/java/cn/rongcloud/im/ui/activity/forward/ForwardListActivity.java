package cn.rongcloud.im.ui.activity.forward;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.activity.SelectFriendsActivity;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.tools.CharacterParser;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

/**
 * 转发功能
 * 选择一个聊天页面
 */
public class ForwardListActivity extends BaseActivity implements View.OnClickListener {
    private List<Conversation> mSourceDataList = new ArrayList<>();
    private ListView mLvChat;
    private ForwardListAdapter mAdapter;
    private EditText mEtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward_list);
        setTitle("选择一个聊天");
        initView();
        initData();
    }

    private void initData() {
        getConversationList();
    }

    private void getConversationList() {
        LoadDialog.show(mContext);
        RongIMClient.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                LoadDialog.dismiss(mContext);
                mSourceDataList.clear();
                mSourceDataList.addAll(conversations);
                mAdapter.setList(mSourceDataList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                LoadDialog.dismiss(mContext);
            }
        });
    }

    private void initView() {
        mEtSearch = ((EditText) findViewById(R.id.et_search));
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                List<Conversation> filterDataList = new ArrayList<>();

                if (TextUtils.isEmpty(s.toString())) {
                    filterDataList = mSourceDataList;
                } else {
                    filterDataList.clear();
                    for (Conversation conversation : mSourceDataList) {
                        Conversation.ConversationType conversationType = conversation.getConversationType();
                        String targetId = conversation.getTargetId();
                        String name = null;
                        if (conversationType == Conversation.ConversationType.PRIVATE) {
                            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
                            if (userInfo != null) {
                                name = userInfo.getName();
                            }
                        } else if (conversationType == Conversation.ConversationType.GROUP) {
                            Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(targetId);
                            if (groupInfo != null) {
                                name = groupInfo.getName();
                            }
                        }

                        if (name != null) {
                            if (name.contains(s) || CharacterParser.getInstance().getSelling(name).startsWith(s.toString())) {
                                filterDataList.add(conversation);
                            }
                        }
                    }
                }
                mAdapter.setList(filterDataList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        findViewById(R.id.ll_create_chat).setOnClickListener(this);
        mLvChat = ((ListView) findViewById(R.id.lv_chat));
        mAdapter = new ForwardListAdapter();
        mLvChat.setAdapter(mAdapter);
        mLvChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                intent.putExtra("conversationType", mSourceDataList.get(position).getConversationType());
                intent.putExtra("targetId", mSourceDataList.get(position).getTargetId());
                intent.setClass(ForwardListActivity.this, ForwardDetailActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_create_chat:
                Intent intent = new Intent(new Intent(mContext, SelectFriendsActivity.class));
                intent.putExtra("isForward", true);
                startActivityForResult(intent, 100);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            switch (requestCode) {
                default:
                    break;
                case 100:
                    Intent intent = getIntent();
                    intent.putExtra("conversationType", data.getSerializableExtra("conversationType"));
                    intent.putExtra("targetId", data.getStringExtra("targetId"));
                    intent.setClass(ForwardListActivity.this, ForwardDetailActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

    private class ForwardListAdapter extends BaseAdapter {
        private List<Conversation> mList = new ArrayList<>();

        @Override
        public int getCount() {
            if (mList != null) {
                return mList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mList == null)
                return null;

            if (position >= mList.size())
                return null;

            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ChattingRecordsViewHolder viewHolder;
            Conversation conversation = (Conversation) getItem(position);
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

            String targetId = conversation.getTargetId();
            Conversation.ConversationType conversationType = conversation.getConversationType();
            if (conversationType == Conversation.ConversationType.PRIVATE) {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
                if (userInfo != null) {
                    ImageLoader.getInstance().displayImage(userInfo.getPortraitUri().toString(), viewHolder.portraitImageView, App.getOptions());
                    viewHolder.nameTextView.setText(userInfo.getName());
                }
            } else {
                Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(targetId);
                if (groupInfo != null) {
                    ImageLoader.getInstance().displayImage(groupInfo.getPortraitUri().toString(), viewHolder.portraitImageView, App.getOptions());
                    viewHolder.nameTextView.setText(groupInfo.getName());
                }
            }
//            viewHolder.chatRecordsDetailTextView.setText(CharacterParser.getInstance().getColoredChattingRecord("", conversation.getLatestMessage()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            String date = simpleDateFormat.format(new Date(conversation.getSentTime()));
            String formatDate = date.replace("-", "/");
            viewHolder.chatRecordsDateTextView.setText(formatDate);
            return convertView;
        }

        public void setList(List<Conversation> list) {
            mList = list;
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
