package cn.rongcloud.im.ui.activity.forward;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.util.List;

import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import io.rong.contactcard.R;
import io.rong.contactcard.message.ContactMessage;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;
import io.rong.message.SightMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * 转发用
 */

public class ForwardDetailActivity extends RongBaseNoActionbarActivity {

    private AsyncImageView mTargetPortrait;
    private TextView mTargetName;
    private TextView mContactName;
    private EditText mEtMessage;
    private TextView mSend;
    private TextView mCancel;
    private ImageView mArrow;
    private ViewAnimator mViewAnimator;
    private GridView mGridView;

    private Conversation.ConversationType mConversationType;
    private String mTargetId;
    private Message mMessage;
    private Group group;
    private List<UserInfo> mGroupMember;
    private boolean mGroupMemberShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rc_ac_contact_detail);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView() {
        mTargetPortrait = (AsyncImageView) findViewById(R.id.target_portrait);
        mTargetName = (TextView) findViewById(R.id.target_name);
        mArrow = (ImageView) findViewById(R.id.target_group_arrow);
        mContactName = (TextView) findViewById(R.id.contact_name);
        mEtMessage = (EditText) findViewById(R.id.message);
        mSend = (TextView) findViewById(R.id.send);
        mCancel = (TextView) findViewById(R.id.cancel);
        mViewAnimator = (ViewAnimator) findViewById(R.id.va_detail);
        mGridView = (GridView) findViewById(R.id.gridview);

        mCancel.requestFocus();
        this.setFinishOnTouchOutside(false);
    }

    private void initData() {
        mTargetId = getIntent().getStringExtra("targetId");
        mConversationType = (Conversation.ConversationType) getIntent().getSerializableExtra("conversationType");
        mMessage = ((Message) getIntent().getParcelableExtra("message"));

        switch (mConversationType) {
            case PRIVATE:
                UserInfo mine = RongUserInfoManager.getInstance().getUserInfo(mTargetId);
                onEventMainThread(mine);
                break;
            case ENCRYPTED:
                String userId = null;
                String str[] = mTargetId.split(";;;");
                if (str.length >= 2) {
                    userId = str[1];
                }
                mine = RongUserInfoManager.getInstance().getUserInfo(userId);
                onEventMainThread(mine);
                break;
            case GROUP:
                group = RongUserInfoManager.getInstance().getGroupInfo(mTargetId);
                onEventMainThread(group);

                RongIM.IGroupMembersProvider groupMembersProvider = RongMentionManager.getInstance().getGroupMembersProvider();
                if (groupMembersProvider != null) {
                    groupMembersProvider.getGroupMembers(mTargetId, new RongIM.IGroupMemberCallback() {
                        @Override
                        public void onGetGroupMembersResult(final List<UserInfo> members) {
                            mGroupMember = members;
                            if (mGroupMember != null) {
                                if (group != null) {
                                    mTargetName.setText(String.format(getResources().getString(R.string.rc_contact_group_member_count),
                                            group.getName(), mGroupMember.size()));
                                }
                                mGridView.setAdapter(new GridAdapter(ForwardDetailActivity.this, mGroupMember));
                            }
                        }
                    });
                    mArrow.setVisibility(View.VISIBLE);
                }

                mArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mGroupMemberShown) {
                            hideInputKeyBoard();
                            mViewAnimator.setDisplayedChild(1);
                            if (mGroupMember != null && mGroupMember.size() > 4)
                                mGridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RongUtils.dip2px(160)));
                            else
                                mGridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RongUtils.dip2px(90)));

                            ObjectAnimator animator = ObjectAnimator.ofFloat(mArrow, "rotation", 0f, 180f);
                            animator.setDuration(500);
                            animator.start();
                            mGroupMemberShown = true;
                        } else {
                            mViewAnimator.setDisplayedChild(0);
                            mGridView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
                            ObjectAnimator animator = ObjectAnimator.ofFloat(mArrow, "rotation", 180f, 0f);
                            animator.setDuration(500);
                            animator.start();
                            mGroupMemberShown = false;
                        }
                    }
                });
                break;
            default:
                break;
        }

        if (mMessage != null) {
            String objectName = mMessage.getObjectName();
            if (objectName.contains("Card")) {
                mContactName.setText(getString(R.string.rc_plugins_contact) + ": " + ((ContactMessage) mMessage.getContent()).getName());
            } else if (objectName.contains("Img")) {
                mContactName.setText("[" + "图片" + "]");
            } else if (objectName.contains("Vc")) {
                mContactName.setText("[" + "语音" + "]");
            } else if (objectName.contains("Sight")) {
                mContactName.setText("[" + "小视频" + "]");
            } else {
                mContactName.setText(CharacterParser.getInstance().getColoredChattingRecord("", mMessage.getContent()));
            }
        }

        mEtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    int start = mEtMessage.getSelectionStart();
                    int end = mEtMessage.getSelectionEnd();
                    mEtMessage.removeTextChangedListener(this);
                    mEtMessage.setText(AndroidEmoji.ensure(s.toString()));
                    mEtMessage.addTextChangedListener(this);
                    mEtMessage.setSelection(start, end);
                }
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfo sendUserInfo = RongUserInfoManager.getInstance().
                        getUserInfo(RongIMClient.getInstance().getCurrentUserId());
                String sendUserName = sendUserInfo == null ? "" : sendUserInfo.getName();
                String objectName = mMessage.getObjectName();
                if (objectName.contains("Card")) {
                    ContactMessage contactMessage = (ContactMessage) mMessage.getContent();
                    ContactMessage.obtain(contactMessage.getId(), contactMessage.getName(), contactMessage.getImgUrl(),
                            RongIMClient.getInstance().getCurrentUserId(), sendUserName, "");
                    String pushContent = String.format(RongContext.getInstance().getResources().getString(R.string.rc_recommend_clause_to_me), sendUserName, contactMessage.getName());
                    RongIMClient.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, contactMessage), pushContent, null,
                            new IRongCallback.ISendMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {

                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                                }
                            });
                } else if (objectName.contains("Img")) {
                    ImageMessage imageMessage = ImageMessage.obtain();
                    String pushContent = "[图片]";
                    RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, imageMessage),
                            pushContent, null, new IRongCallback.ISendMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {

                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                                }
                            });
                } else if (objectName.contains("Vc")) {
                    VoiceMessage voiceMessage = (VoiceMessage) mMessage.getContent();
                    String pushContent = "[语音]";
                    RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, voiceMessage),
                            pushContent, null, new IRongCallback.ISendMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {

                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                                }
                            });
                } else if (objectName.contains("Sight")) {
                    SightMessage sightMessage = (SightMessage) mMessage.getContent();
                    String pushContent = "[小视频]";
                    RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, sightMessage),
                            pushContent, null, new IRongCallback.ISendMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {

                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                                }
                            });
                } else {
                    TextMessage mText = TextMessage.obtain(((TextMessage) mMessage.getContent()).getContent());
                    RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, mText),
                            null, null, new IRongCallback.ISendMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {
                                    Log.e("swo onSuccess", message.toString());
                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                                    Log.e("swo onError", message.toString());
                                }
                            });
                }


                String message = mEtMessage.getText().toString().trim();
                if (!("".equals(message))) {
                    TextMessage mTextMessage = TextMessage.obtain(message);
                    RongIM.getInstance().sendMessage(Message.obtain(mTargetId, mConversationType, mTextMessage), null, null,
                            new IRongCallback.ISendMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {

                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                                }
                            });
                } else {
                    hideInputKeyBoard();
                }
                finish();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInputKeyBoard();
                finish();
            }
        });
    }

    public void onEventMainThread(UserInfo mine) {
        if (mine != null) {
            if (mine.getPortraitUri() != null)
                mTargetPortrait.setAvatar(mine.getPortraitUri());
            if (mine.getName() != null)
                mTargetName.setText(mine.getName());
        }
    }

    public void onEventMainThread(Group group) {
        if (group != null) {
            this.group = group;
            if (group.getPortraitUri() != null)
                mTargetPortrait.setAvatar(group.getPortraitUri());
            if (group.getName() != null) {
                if (mGroupMember != null && mGroupMember.size() > 0) {
                    mTargetName.setText(String.format(getResources().getString(R.string.rc_contact_group_member_count),
                            group.getName(), mGroupMember.size()));
                } else {
                    mTargetName.setText(group.getName());
                }
            }
        }
    }

    private static class GridAdapter extends BaseAdapter {

        private List<UserInfo> list;
        Context context;

        GridAdapter(Context context, List<UserInfo> list) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.rc_gridview_item_contact_group_members, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.portrait = (AsyncImageView) convertView.findViewById(R.id.iv_avatar);
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_username);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            UserInfo member = list.get(position);
            if (member != null) {
                viewHolder.portrait.setAvatar(member.getPortraitUri());
                viewHolder.name.setText(member.getName());
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static class ViewHolder {
        AsyncImageView portrait;
        TextView name;
    }

    private void hideInputKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEtMessage.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {

    }
}
