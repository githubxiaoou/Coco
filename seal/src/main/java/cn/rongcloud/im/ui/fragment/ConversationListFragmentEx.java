package cn.rongcloud.im.ui.fragment;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.server.utils.NToast;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.model.Conversation;

/**
 * 自定义会话列表fragment
 */
public class ConversationListFragmentEx extends ConversationListFragment {
    private SharedPreferences sp;
    private String mUserId;
    public ConversationListFragmentEx() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getActivity().getSharedPreferences("config", Activity.MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UIConversation item = (UIConversation) parent.getItemAtPosition(position);
        String targetId = item.getConversationTargetId();
        Conversation.ConversationType conversationType = item.getConversationType();
        if (conversationType == Conversation.ConversationType.PRIVATE) {
            Friend friendByID = SealUserInfoManager.getInstance().getFriendByID(targetId);
            if (friendByID == null) {
                NToast.shortToast(getActivity(), "你们已经不是好友关系");
                return;
            }
        }

        if (conversationType == Conversation.ConversationType.GROUP) {
            List<GroupMember> groupMembers = SealUserInfoManager.getInstance().getGroupMembers(targetId);
            for (int i = 0; i < groupMembers.size(); i++) {
                GroupMember member = groupMembers.get(i);
                if (mUserId.equals(member.getUserId())) {
                    super.onItemClick(parent, view, position, id);
                    return;
                }
            }
            NToast.shortToast(getActivity(), "你已经不是该群成员，不能查看群内消息");
            return;
        }
        super.onItemClick(parent, view, position, id);
    }
}
