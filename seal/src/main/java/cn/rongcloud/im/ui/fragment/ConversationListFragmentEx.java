package cn.rongcloud.im.ui.fragment;


import android.view.View;
import android.widget.AdapterView;

import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.utils.NToast;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.model.Conversation;

/**
 * 自定义会话列表fragment
 */
public class ConversationListFragmentEx extends ConversationListFragment {
    public ConversationListFragmentEx() {
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
        super.onItemClick(parent, view, position, id);
    }
}
