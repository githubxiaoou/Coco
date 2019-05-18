package cn.rongcloud.im.server.response;

import com.google.gson.annotations.SerializedName;

import cn.rongcloud.im.model.NetData;

public class GroupMenberListResponse extends NetData {

    /**
     * id : 17
     * groupId : 7
     * memberId : 8
     * displayName :
     * role : 1
     * isDeleted : 0
     * timestamp : 1558015550098
     * createdAt : 2019-05-16 22:05:50
     * updatedAt : 2019-05-16 22:05:50
     * nickname : Allen
     * portraitUri :
     * phone : 17721111165
     */

    @SerializedName("id")
    public String id;
    @SerializedName("groupId")
    public String groupId;
    @SerializedName("memberId")
    public String memberId;
    @SerializedName("displayName")
    public String displayName;
    @SerializedName("role")
    public String role;
    @SerializedName("isDeleted")
    public String isDeleted;
    @SerializedName("timestamp")
    public String timestamp;
    @SerializedName("createdAt")
    public String createdAt;
    @SerializedName("updatedAt")
    public String updatedAt;
    @SerializedName("nickname")
    public String nickname;
    @SerializedName("portraitUri")
    public String portraitUri;
    @SerializedName("phone")
    public String phone;
}
