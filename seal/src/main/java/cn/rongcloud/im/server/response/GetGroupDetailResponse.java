package cn.rongcloud.im.server.response;

import com.google.gson.annotations.SerializedName;

import cn.rongcloud.im.model.NetData;

public class GetGroupDetailResponse extends NetData {

    /**
     * id : 7
     * name : 大圣当家
     * portraitUri :
     * memberCount : 2
     * maxMemberCount : 500
     * creatorId : 6
     * bulletin : null
     * timestamp : 1558232246283
     * createdAt : 2019-05-19 10:17:26
     * updatedAt : 2019-05-19 10:17:26
     * deletedAt : null
     * other_id : 啦啦啦
     * is_protected : null
     * is_need_verification : null
     * is_allforbiden_words : 1
     * adminCount : 1
     * isAdmin:1
     */

    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("portraitUri")
    public String portraitUri;
    @SerializedName("memberCount")
    public String memberCount;
    @SerializedName("maxMemberCount")
    public String maxMemberCount;
    @SerializedName("creatorId")
    public String creatorId;
    @SerializedName("bulletin")
    public Object bulletin;
    @SerializedName("timestamp")
    public String timestamp;
    @SerializedName("createdAt")
    public String createdAt;
    @SerializedName("updatedAt")
    public String updatedAt;
    @SerializedName("deletedAt")
    public Object deletedAt;
    @SerializedName("other_id")
    public String otherId;
    @SerializedName("is_protected")
    public Object isProtected;// 1开启保护，其它不是
    @SerializedName("is_need_verification")
    public Object isNeedVerification;// 1开启认证，其它不是
    @SerializedName("is_allforbiden_words")
    public String isAllforbidenWords;
    @SerializedName("adminCount")
    public int adminCount;
    @SerializedName("isAdmin")
    public String isAdmin;//1是管理员，其它不是
}
