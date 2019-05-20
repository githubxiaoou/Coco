package cn.rongcloud.im.server.request;

import java.util.List;

/**
 * Created by AMing on 16/1/27.
 * Company RongCloud
 */
public class AddGroupMemberRequest {

    private String groupId;
    private String user_id;
    private List<String> memberIds;

    public AddGroupMemberRequest(String groupId, String user_id, List<String> memberIds) {
        this.groupId = groupId;
        this.user_id = user_id;
        this.memberIds = memberIds;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }
}
