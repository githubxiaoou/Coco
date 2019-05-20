package cn.rongcloud.im.server.request;

/**
 * Created by AMing on 16/1/29.
 * Company RongCloud
 */
public class DismissGroupRequest {

    private String groupId;
    private String user_id;
    public DismissGroupRequest(String groupId, String userId) {
        this.groupId = groupId;
        this.user_id = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
