package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.pinyin.PinyinComparator;
import cn.rongcloud.im.server.pinyin.SideBar;
import cn.rongcloud.im.server.response.AddGroupMemberResponse;
import cn.rongcloud.im.server.response.DeleteGroupMemberResponse;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/1/21.
 * Company RongCloud
 */
public class SelectFriendsActivity extends BaseActivity implements View.OnClickListener {

    private static final int ADD_GROUP_MEMBER = 21;
    private static final int DELETE_GROUP_MEMBER = 23;
    public static final String DISCUSSION_UPDATE = "DISCUSSION_UPDATE";
    /**
     * 好友列表的 ListView
     */
    private ListView mListView;
    /**
     * 发起讨论组的 adapter
     */
    private StartDiscussionAdapter adapter;
    /**
     * 中部展示的字母提示
     */
    public TextView dialog;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser mCharacterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private TextView mNoFriends;
    private List<Friend> data_list = new ArrayList<>();
    private List<Friend> sourceDataList = new ArrayList<>();
    private LinearLayout mSelectedFriendsLinearLayout;
    private boolean isCrateGroup;
    private boolean isConversationActivityStartDiscussion;
    private boolean isConversationActivityStartPrivate;
    private List<GroupMember> addGroupMemberList;// 除群内人员的其他朋友
    private List<GroupMember> deleteGroupMemberList;// 群内非群主的成员
    private List<GroupMember> setManagerList;// 群内可设置为管理员的成员(非群主，非管理员)
    private List<GroupMember> setJinyanList;// 群内可设置禁言的成员(非群主，非已禁言)
    private String groupId;
    private String userId;// 添加或删除群成员的时候，会有userId
    private String conversationStartId;
    private String conversationStartType = "null";
    private ArrayList<String> discListMember;
    private ArrayList<UserInfo> addDisList, deleDisList;
    private boolean isStartPrivateChat;
    private List<Friend> mSelectedFriend;
    private boolean isAddGroupMember;
    private boolean openAuth;
    private boolean isDeleteGroupMember;
    private String creatorId;
    private boolean isSetManager;
    private ArrayList<String> managerIdList = new ArrayList<>();
    private boolean isSetJinyan;
    private ArrayList<String> jinyanIdList = new ArrayList<>();
    private boolean isForward;// 转发

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_disc);
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(getString(R.string.confirm));
        mHeadRightText.setOnClickListener(this);
        mSelectedFriend = new ArrayList<>();
        mSelectedFriendsLinearLayout = (LinearLayout) findViewById(R.id.ll_selected_friends);
        isCrateGroup = getIntent().getBooleanExtra("createGroup", false);
        isConversationActivityStartDiscussion = getIntent().getBooleanExtra("CONVERSATION_DISCUSSION", false);
        isConversationActivityStartPrivate = getIntent().getBooleanExtra("CONVERSATION_PRIVATE", false);
        groupId = getIntent().getStringExtra("GroupId");
        userId = getIntent().getStringExtra("userId");
        isAddGroupMember = getIntent().getBooleanExtra("isAddGroupMember", false);
        openAuth = getIntent().getBooleanExtra("openAuth", false);
        isDeleteGroupMember = getIntent().getBooleanExtra("isDeleteGroupMember", false);
        creatorId = getIntent().getStringExtra("creatorId");
        isSetManager = getIntent().getBooleanExtra("isSetManager", false);
        isSetJinyan = getIntent().getBooleanExtra("isSetJinyan", false);
        isForward = getIntent().getBooleanExtra("isForward", false);
        if (isAddGroupMember || isDeleteGroupMember || isSetManager || isSetJinyan) {
            if (isSetManager) {
                managerIdList = getIntent().getStringArrayListExtra("managerIdList");
            }

            if (isSetJinyan) {
                jinyanIdList = getIntent().getStringArrayListExtra("jinyanIdList");
            }
            initGroupMemberList();
        }
        addDisList = (ArrayList<UserInfo>) getIntent().getSerializableExtra("AddDiscuMember");
        deleDisList = (ArrayList<UserInfo>) getIntent().getSerializableExtra("DeleteDiscuMember");

        setTitle();
        initView();

        /**
         * 根据进行的操作初始化数据,添加删除群成员和获取好友信息是异步操作,所以做了很多额外的处理
         * 数据添加后还需要过滤已经是群成员,讨论组成员的用户
         * 最后设置adapter显示
         * 后两个操作全都根据异步操作推后
         */
        initData();
    }

    private void initGroupMemberList() {
        SealUserInfoManager.getInstance().getGroupMembers(groupId, new SealUserInfoManager.ResultCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                if (isAddGroupMember) {
                    addGroupMemberList = groupMembers;
                    fillSourceDataListWithFriendsInfo();
                } else if (isDeleteGroupMember) {
                    deleteGroupMemberList = groupMembers;
                    fillSourceDataListForDeleteGroupMember();
                } else if (isSetManager) {
                    setManagerList = groupMembers;
                    fillSourceDataListForSetManager();
                } else if (isSetJinyan) {
                    setJinyanList = groupMembers;
                    fillSourceDataListForSetJinyan();
                }
            }

            @Override
            public void onError(String errString) {

            }
        });
    }


    private void setTitle() {
        if (isConversationActivityStartPrivate) {
            conversationStartType = "PRIVATE";
            conversationStartId = getIntent().getStringExtra("DEMO_FRIEND_TARGETID");
            setTitle(getString(R.string.select_discussion_group_member));
        } else if (isConversationActivityStartDiscussion) {
            conversationStartType = "DISCUSSION";
            conversationStartId = getIntent().getStringExtra("DEMO_FRIEND_TARGETID");
            discListMember = getIntent().getStringArrayListExtra("DISCUSSIONMEMBER");
            setTitle(getString(R.string.select_discussion_group_member));
        } else if (isDeleteGroupMember) {
            setTitle(getString(R.string.remove_group_member));
        } else if (isAddGroupMember) {
            setTitle(getString(R.string.add_group_member));
        } else if (isCrateGroup) {
            setTitle(getString(R.string.select_group_member));
        } else if (addDisList != null) {
            setTitle(getString(R.string.add_discussion_group_member));
        } else if (deleDisList != null) {
            setTitle(getString(R.string.remove_discussion_group_member));
        } else if (isSetManager) {
            setTitle("设置管理员");
        } else if (isSetJinyan) {
            setTitle("添加禁言");
        } else if (isForward) {
            // TODO: 2019/6/11 转发也暂时只能转发给一个联系人
            setTitle("选择联系人");
            if (!getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
                isStartPrivateChat = true;
            }
        } else {
            setTitle(getString(R.string.select_contact));
            if (!getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
                isStartPrivateChat = true;
            }
        }
    }

    private void initView() {
        //实例化汉字转拼音类
        mCharacterParser = CharacterParser.getInstance();
        pinyinComparator = PinyinComparator.getInstance();
        mListView = (ListView) findViewById(R.id.dis_friendlistview);
        mNoFriends = (TextView) findViewById(R.id.dis_show_no_friend);
        SideBar mSidBar = (SideBar) findViewById(R.id.dis_sidrbar);
        dialog = (TextView) findViewById(R.id.dis_dialog);
        mSidBar.setTextView(dialog);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        adapter = new StartDiscussionAdapter(mContext, sourceDataList);
        mListView.setAdapter(adapter);
        initOptionPicker();
    }

    private void initData() {
        if (deleDisList != null && deleDisList.size() > 0) {
            for (int i = 0; i < deleDisList.size(); i++) {
                if (deleDisList.get(i).getUserId().contains(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""))) {
                    continue;
                }
                data_list.add(new Friend(deleDisList.get(i).getUserId(),
                        deleDisList.get(i).getName(),
                        deleDisList.get(i).getPortraitUri(),
                        null //TODO displayName 需要处理 暂为 null
                ));
            }
            /**
             * 以下3步是标准流程
             * 1.填充数据sourceDataList
             * 2.过滤数据,邀请新成员时需要过滤掉已经是成员的用户,但做删除操作时不需要这一步
             * 3.设置adapter显示
             */
            fillSourceDataList();
            filterSourceDataList();
            updateAdapter();
        } else if (!isDeleteGroupMember && !isAddGroupMember && !isSetManager && !isSetJinyan) {
            fillSourceDataListWithFriendsInfo();
        }
    }

    private void fillSourceDataList() {
        if (data_list != null && data_list.size() > 0) {
            sourceDataList = filledData(data_list); //过滤数据为有字母的字段  现在有字母 别的数据没有
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        //还原除了带字母字段的其他数据
        for (int i = 0; i < data_list.size(); i++) {
            sourceDataList.get(i).setName(data_list.get(i).getName());
            sourceDataList.get(i).setUserId(data_list.get(i).getUserId());
            sourceDataList.get(i).setPortraitUri(data_list.get(i).getPortraitUri());
            sourceDataList.get(i).setDisplayName(data_list.get(i).getDisplayName());
        }
        // 根据a-z进行排序源数据
        Collections.sort(sourceDataList, pinyinComparator);
    }

    //讨论组群组邀请新成员时需要过滤掉已经是成员的用户
    private void filterSourceDataList() {
        if (addDisList != null && addDisList.size() > 0) {
            for (UserInfo u : addDisList) {
                for (int i = 0; i < sourceDataList.size(); i++) {
                    if (sourceDataList.get(i).getUserId().contains(u.getUserId())) {
                        sourceDataList.remove(sourceDataList.get(i));
                    }
                }
            }
        } else if (addGroupMemberList != null && addGroupMemberList.size() > 0) {
            for (GroupMember addMember : addGroupMemberList) {
                for (int i = 0; i < sourceDataList.size(); i++) {
                    if (sourceDataList.get(i).getUserId().contains(addMember.getUserId())) {
                        sourceDataList.remove(sourceDataList.get(i));
                    }
                }
            }
        } else if (conversationStartType.equals("DISCUSSION")) {
            if (discListMember != null && discListMember.size() > 1) {
                for (String s : discListMember) {
                    for (int i = 0; i < sourceDataList.size(); i++) {
                        if (sourceDataList.get(i).getUserId().contains(s)) {
                            sourceDataList.remove(sourceDataList.get(i));
                        }
                    }
                }
            }
        } else if (conversationStartType.equals("PRIVATE")) {
            for (int i = 0; i < sourceDataList.size(); i++) {
                if (sourceDataList.get(i).getUserId().contains(conversationStartId)) {
                    sourceDataList.remove(sourceDataList.get(i));
                }
            }
        }
    }

    private void updateAdapter() {
        adapter.setData(sourceDataList);
        adapter.notifyDataSetChanged();
    }

    private void fillSourceDataListWithFriendsInfo() {
        SealUserInfoManager.getInstance().getFriends(new SealUserInfoManager.ResultCallback<List<Friend>>() {
            @Override
            public void onSuccess(List<Friend> friendList) {
                if (mListView != null) {
                    if (friendList != null && friendList.size() > 0) {
                        for (Friend friend : friendList) {
                            data_list.add(new Friend(friend.getUserId(), friend.getName(), friend.getPortraitUri(), friend.getDisplayName(), null, null));
                        }
                        if (isAddGroupMember) {
                            for (GroupMember groupMember : addGroupMemberList) {
                                for (int i = 0; i < data_list.size(); i++) {
                                    if (groupMember.getUserId().equals(data_list.get(i).getUserId())) {
                                        data_list.remove(i);
                                    }
                                }
                            }
                        }
                        fillSourceDataList();
                        filterSourceDataList();
                        updateAdapter();
                    }
                }
            }

            @Override
            public void onError(String errString) {

            }
        });
    }

    private void fillSourceDataListForDeleteGroupMember() {
        if (deleteGroupMemberList != null && deleteGroupMemberList.size() > 0) {
            for (GroupMember deleteMember : deleteGroupMemberList) {
                if (deleteMember.getUserId().contains(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""))) {
                    // 去掉本人
                    continue;
                }
                if (deleteMember.getUserId().contains(creatorId)) {
                    // 去掉群主
                    continue;
                }
                data_list.add(new Friend(deleteMember.getUserId(),
                        deleteMember.getName(), deleteMember.getPortraitUri(),
                        null //TODO displayName 需要处理 暂为 null
                ));
            }
            fillSourceDataList();
            updateAdapter();
        }
    }

    private void fillSourceDataListForSetManager() {
        String idStr = "";
        if (null != managerIdList && managerIdList.size() != 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : managerIdList) {
                sb.append(s);
                sb.append(",");
            }
            String str = sb.toString();
            idStr = str.substring(0, str.length() - 1);
        }
        if (setManagerList != null && setManagerList.size() > 0) {
            for (GroupMember deleteMember : setManagerList) {
                if (deleteMember.getUserId().contains(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""))) {
                    // 去掉本人（群主）
                    continue;
                }
                if (idStr.contains(deleteMember.getUserId())) {
                    // 去掉管理员
                    continue;
                }
                data_list.add(new Friend(deleteMember.getUserId(),
                        deleteMember.getName(), deleteMember.getPortraitUri(),
                        null //TODO displayName 需要处理 暂为 null
                ));
            }
            fillSourceDataList();
            updateAdapter();
        }
    }

    private void fillSourceDataListForSetJinyan() {
        String idStr = "";
        if (null != jinyanIdList && jinyanIdList.size() != 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : jinyanIdList) {
                sb.append(s);
                sb.append(",");
            }
            String str = sb.toString();
            idStr = str.substring(0, str.length() - 1);
        }
        if (setJinyanList != null && setJinyanList.size() > 0) {
            for (GroupMember deleteMember : setJinyanList) {
                if (deleteMember.getUserId().contains(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""))) {
                    // 去掉本人
                    continue;
                }
                if (idStr.contains(deleteMember.getUserId())) {
                    // 已禁言
                    continue;
                }
                data_list.add(new Friend(deleteMember.getUserId(),
                        deleteMember.getName(), deleteMember.getPortraitUri(),
                        null //TODO displayName 需要处理 暂为 null
                ));
            }
            fillSourceDataList();
            updateAdapter();
        }
    }


    //用于存储CheckBox选中状态
    public Map<Integer, Boolean> mCBFlag;

    public List<Friend> adapterList;


    class StartDiscussionAdapter extends BaseAdapter implements SectionIndexer {

        private Context context;
        private ArrayList<CheckBox> checkBoxList = new ArrayList<>();

        public StartDiscussionAdapter(Context context, List<Friend> list) {
            this.context = context;
            adapterList = list;
            mCBFlag = new HashMap<>();
            init();
        }

        public void setData(List<Friend> friends) {
            adapterList = friends;
            init();
        }

        void init() {
            for (int i = 0; i < adapterList.size(); i++) {
                mCBFlag.put(i, false);
            }
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<Friend> list) {
            adapterList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            final Friend friend = adapterList.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_start_discussion, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.dis_friendname);
                viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.dis_catalog);
                viewHolder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.dis_frienduri);
                viewHolder.isSelect = (CheckBox) convertView.findViewById(R.id.dis_select);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(friend.getLetters());
            } else {
                viewHolder.tvLetter.setVisibility(View.GONE);
            }

            if (isStartPrivateChat) {
                viewHolder.isSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        if (cb != null) {
                            if (cb.isChecked()) {
                                for (CheckBox c : checkBoxList) {
                                    c.setChecked(false);
                                }
                                checkBoxList.clear();
                                checkBoxList.add(cb);
                            } else {
                                checkBoxList.clear();
                            }
                        }
                    }
                });
                viewHolder.isSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mCBFlag.put(position, viewHolder.isSelect.isChecked());
                    }
                });
            } else {
                viewHolder.isSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCBFlag.put(position, viewHolder.isSelect.isChecked());
                        updateSelectedSizeView(mCBFlag);
                        if (mSelectedFriend.contains(friend)) {
                            int index = mSelectedFriend.indexOf(friend);
                            if (index > -1) {
                                mSelectedFriendsLinearLayout.removeViewAt(index);
                            }
                            mSelectedFriend.remove(friend);
                        } else {
                            mSelectedFriend.add(friend);
                            LinearLayout view = (LinearLayout) View.inflate(SelectFriendsActivity.this, R.layout.item_selected_friends, null);
                            SelectableRoundedImageView asyncImageView = (SelectableRoundedImageView) view.findViewById(R.id.iv_selected_friends);
                            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(friend);
                            ImageLoader.getInstance().displayImage(portraitUri, asyncImageView);
                            view.removeView(asyncImageView);
                            mSelectedFriendsLinearLayout.addView(asyncImageView);
                        }
                    }
                });
            }
            viewHolder.isSelect.setChecked(mCBFlag.get(position));

            if (TextUtils.isEmpty(adapterList.get(position).getDisplayName())) {
                viewHolder.tvTitle.setText(adapterList.get(position).getName());
            } else {
                viewHolder.tvTitle.setText(adapterList.get(position).getDisplayName());
            }

            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(adapterList.get(position));
            ImageLoader.getInstance().displayImage(portraitUri, viewHolder.mImageView, App.getOptions());
            return convertView;
        }

        private void updateSelectedSizeView(Map<Integer, Boolean> mCBFlag) {
            if (!isStartPrivateChat && mCBFlag != null) {
                int size = 0;
                for (int i = 0; i < mCBFlag.size(); i++) {
                    if (mCBFlag.get(i)) {
                        size++;
                    }
                }
                if (size == 0) {
                    mHeadRightText.setText(getString(R.string.confirm));
                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                } else {
                    mHeadRightText.setText(getString(R.string.confirm) + "(" + size + ")");
                    List<Friend> selectedList = new ArrayList<>();
                    for (int i = 0; i < sourceDataList.size(); i++) {
                        if (mCBFlag.get(i)) {
                            selectedList.add(sourceDataList.get(i));
                        }
                    }
                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        @Override
        public int getPositionForSection(int sectionIndex) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = adapterList.get(i).getLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == sectionIndex) {
                    return i;
                }
            }

            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的Char ascii值
         */
        @Override
        public int getSectionForPosition(int position) {
            return adapterList.get(position).getLetters().charAt(0);
        }


        final class ViewHolder {
            /**
             * 首字母
             */
            TextView tvLetter;
            /**
             * 昵称
             */
            TextView tvTitle;
            /**
             * 头像
             */
            SelectableRoundedImageView mImageView;
            /**
             * userid
             */
//            TextView tvUserId;
            /**
             * 是否被选中的checkbox
             */
            CheckBox isSelect;
        }

    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case ADD_GROUP_MEMBER:
                return action.addGroupMember(groupId, userId, startDisList);
            case DELETE_GROUP_MEMBER:
                return action.deleGroupMember(groupId, userId, startDisList);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case ADD_GROUP_MEMBER:
                    AddGroupMemberResponse res = (AddGroupMemberResponse) result;
                    if (res.getCode() == 200) {
                        Intent data = new Intent();
                        data.putExtra("newAddMember", (Serializable) createGroupList);
                        setResult(101, data);
                        NToast.shortToast(mContext, getString(R.string.add_successful));

                        inviteMember();
                    }
                    break;
                case DELETE_GROUP_MEMBER:
                    DeleteGroupMemberResponse response = (DeleteGroupMemberResponse) result;
                    if (response.getCode() == 200) {
                        Intent intent = new Intent();
                        intent.putExtra("deleteMember", (Serializable) createGroupList);
                        setResult(102, intent);
                        NToast.shortToast(mContext, getString(R.string.remove_successful));
                        kickMember();
                        finish();
                    } else if (response.getCode() == 400) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, mContext.getString(R.string.creator_can_not_remove_self));
                    }
                    break;
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case ADD_GROUP_MEMBER:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, mContext.getString(R.string.add_group_member_request_failed));
                break;
            case DELETE_GROUP_MEMBER:
                LoadDialog.dismiss(mContext);
                NToast.shortToast(mContext, mContext.getString(R.string.remove_group_member_request_failed));
                break;
        }
    }

    private List<String> startDisList;// 保存选中用户的id
    private List<Friend> createGroupList;


    /**
     * 为ListView填充数据
     */
    private List<Friend> filledData(List<Friend> list) {
        List<Friend> mFriendList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Friend friendModel = new Friend(list.get(i).getUserId(), list.get(i).getName(), list.get(i).getPortraitUri());
            //汉字转换成拼音
            String pinyin = null;
            if (!TextUtils.isEmpty(list.get(i).getDisplayName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getDisplayName());
            } else if (!TextUtils.isEmpty(list.get(i).getName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getName());
            } else {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(list.get(i).getUserId());
                if (userInfo != null) {
                    pinyin = mCharacterParser.getSpelling(userInfo.getName());
                }
            }
            String sortString;
            if (!TextUtils.isEmpty(pinyin)) {
                sortString = pinyin.substring(0, 1).toUpperCase();
            } else {
                sortString = "#";
            }

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                friendModel.setLetters(sortString);
            } else {
                friendModel.setLetters("#");
            }

            mFriendList.add(friendModel);
        }
        return mFriendList;

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListView = null;
        adapter = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_right:
                if (mCBFlag != null && sourceDataList != null && sourceDataList.size() > 0) {
                    startDisList = new ArrayList<>();
                    List<String> disNameList = new ArrayList<>();
                    createGroupList = new ArrayList<>();
                    for (int i = 0; i < sourceDataList.size(); i++) {
                        if (mCBFlag.get(i)) {
                            startDisList.add(sourceDataList.get(i).getUserId());
                            disNameList.add(sourceDataList.get(i).getName());
                            createGroupList.add(sourceDataList.get(i));
                        }
                    }

                    if (isConversationActivityStartDiscussion) {
                        if (RongIM.getInstance() != null) {
                            RongIM.getInstance().addMemberToDiscussion(conversationStartId, startDisList, new RongIMClient.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    NToast.shortToast(SelectFriendsActivity.this, getString(R.string.add_successful));
                                    BroadcastManager.getInstance(mContext).sendBroadcast(DISCUSSION_UPDATE);
                                    finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {

                                }
                            });
                        }
                    } else if (isConversationActivityStartPrivate) {
                        if (RongIM.getInstance() != null) { // 没有被调用 二人讨论组时候
                            RongIM.getInstance().addMemberToDiscussion(conversationStartId, startDisList, new RongIMClient.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    NToast.shortToast(SelectFriendsActivity.this, getString(R.string.add_successful));
                                    finish();
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {

                                }
                            });
                        }
                    } else if (deleteGroupMemberList != null && startDisList != null && sourceDataList.size() > 0) {
                        mHeadRightText.setClickable(true);
                        DialogWithYesOrNoUtils.getInstance().showDialog(mContext, getString(R.string.remove_group_members), new DialogWithYesOrNoUtils.DialogCallBack() {

                            @Override
                            public void executeEvent() {
                                LoadDialog.show(mContext);
                                request(DELETE_GROUP_MEMBER);
                            }

                            @Override
                            public void executeEditEvent(String editText) {

                            }

                            @Override
                            public void updatePassword(String oldPassword, String newPassword) {

                            }
                        });
                    } else if (setManagerList != null && startDisList != null && sourceDataList.size() > 0) {
                        setManager();
                    } else if (setJinyanList != null && startDisList != null && sourceDataList.size() > 0) {
                        pvOptions.show();
                    } else if (deleDisList != null && startDisList != null && startDisList.size() > 0) {
                        Intent intent = new Intent();
                        intent.putExtra("deleteDiscuMember", (Serializable) startDisList);
                        setResult(RESULT_OK, intent);
                        finish();

                    } else if (addGroupMemberList != null && startDisList != null && startDisList.size() > 0) {
                        //TODO 选中添加成员的数据添加到服务端数据库  返回本地也需要更改
                        if (openAuth) {
                            inviteMember();
                        } else {
                            LoadDialog.show(mContext);
                            request(ADD_GROUP_MEMBER);
                        }

                    } else if (addDisList != null && startDisList != null && startDisList.size() > 0) {
                        Intent intent = new Intent();
                        intent.putExtra("addDiscuMember", (Serializable) startDisList);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else if (isCrateGroup) {
                        if (createGroupList.size() > 0) {
                            mHeadRightText.setClickable(true);
                            Intent intent = new Intent(SelectFriendsActivity.this, CreateGroupActivity.class);
                            intent.putExtra("GroupMember", (Serializable) createGroupList);
                            startActivity(intent);
                            finish();
                        } else {
                            NToast.shortToast(mContext, getString(R.string.at_least_one_friend_to_create_group));
                            mHeadRightText.setClickable(true);
                        }
                    } else if (isForward) {
                        if (createGroupList.size() > 0) {
                            mHeadRightText.setClickable(true);
                            Intent intent = new Intent();
                            intent.putExtra("conversationType", Conversation.ConversationType.PRIVATE);
                            intent.putExtra("targetId", createGroupList.get(0).getUserId());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            NToast.shortToast(mContext, "请至少选择一位联系人");
                            mHeadRightText.setClickable(true);
                        }
                    } else {

                        if (startDisList != null && startDisList.size() == 1) {
                            RongIM.getInstance().startPrivateChat(mContext, startDisList.get(0),
                                    SealUserInfoManager.getInstance().getFriendByID(startDisList.get(0)).getName());
                        } else if (startDisList.size() > 1) {

                            String disName;
                            if (disNameList.size() < 2) {
                                disName = disNameList.get(0) + getString(R.string.and_my_discussion);
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (String s : disNameList) {
                                    sb.append(s);
                                    sb.append(",");
                                }
                                String str = sb.toString();
                                disName = str.substring(0, str.length() - 1);
                                disName = disName + getString(R.string.and_my_discussion);
                            }
                            RongIM.getInstance().createDiscussion(disName, startDisList, new RongIMClient.CreateDiscussionCallback() {
                                @Override
                                public void onSuccess(String s) {
                                    NLog.e("disc", "onSuccess" + s);
                                    RongIM.getInstance().startDiscussionChat(SelectFriendsActivity.this, s, "");
                                }

                                @Override
                                public void onError(RongIMClient.ErrorCode errorCode) {
                                    NLog.e("disc", errorCode.getValue());
                                }
                            });
                        } else {
                            mHeadRightText.setClickable(true);
                            NToast.shortToast(mContext, getString(R.string.least_one_friend));
                        }
                    }
                } else {
                    Toast.makeText(SelectFriendsActivity.this, getString(R.string.no_data), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void inviteMember() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String s : startDisList) {
            if (first) {
                builder.append(s);
                first = false;
                continue;
            }
            builder.append(",").append(s);
        }
        HttpUtil.apiS().groupInvitation(groupId, userId, builder.toString())
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<String>>>() {

                    @Override
                    public void Successful(NetData<List<String>> listNetData) {
                        if (openAuth) {
                            NToast.shortToast(mContext, "本群已开启群认证，邀请成功，需要管理员或群主同意方可进群。");
                        }
                        finish();
                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    private void kickMember() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String s : startDisList) {
            if (first) {
                builder.append(s);
                first = false;
                continue;
            }
            builder.append(",").append(s);
        }
        HttpUtil.apiS().groupKickMember(groupId, userId, builder.toString())
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<String>>>() {

                    @Override
                    public void Successful(NetData<List<String>> listNetData) {

                    }

                    @Override
                    public void Failure(Throwable t) {

                    }
                });
    }

    private void setManager() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String s : startDisList) {
            if (first) {
                builder.append(s);
                first = false;
                continue;
            }
            builder.append(",").append(s);
        }
        HttpUtil.apiS().groupSetAdmins(groupId, builder.toString(), "2")
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<String>>>() {
                    @Override
                    public void Successful(NetData<List<String>> listNetData) {
                        setResult(RESULT_OK, new Intent());
                        finish();
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    private void jinyan(int minute) {
        LoadDialog.show(mContext);
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String s : startDisList) {
            if (first) {
                builder.append(s);
                first = false;
                continue;
            }
            builder.append(",").append(s);
        }
        HttpUtil.apiS().groupJinyan(groupId, builder.toString(), String.valueOf(minute))
                .subscribeOn(Schedulers.io())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        LoadDialog.dismiss(mContext);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NetObserver<NetData<List<String>>>() {
                    @Override
                    public void Successful(NetData<List<String>> listNetData) {
                        setResult(RESULT_OK, new Intent());
                        finish();
                    }

                    @Override
                    public void Failure(Throwable t) {
                        NToast.shortToast(mContext, "网络错误");
                    }
                });
    }

    private OptionsPickerView pvOptions;
    private ArrayList<String> times = new ArrayList<>();

    private void initOptionPicker() {//条件选择器初始化
        times.add("10分钟");
        times.add("1小时");
        times.add("12小时");
        times.add("1天");

        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(final int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                int minute = 0;
                switch (options1) {
                    case 0:
                        minute = 10;
                        break;
                    case 1:
                        minute = 60;
                        break;
                    case 2:
                        minute = 12 * 60;
                        break;
                    case 3:
                        minute = 24 * 60;
                        break;
                }
                jinyan(minute);
            }
        })
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .build();
        pvOptions.setPicker(times);//一级选择器
    }
}
