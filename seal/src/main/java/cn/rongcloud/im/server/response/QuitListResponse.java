package cn.rongcloud.im.server.response;

import com.google.gson.annotations.SerializedName;

import cn.rongcloud.im.model.NetData;

public class QuitListResponse extends NetData {

    /**
     * id : 61
     * group_id : 30
     * userId : 9
     * friendId : 9
     * type : 2
     * status : null
     * createdAt : 2019-05-20 22:08:24
     * updatedAt : 2019-05-20 22:08:24
     * friendName : 17711112222
     * friendPortraitUri :
     * userName : 17711112222
     * userPortraitUri :
     */

    @SerializedName("id")
    public String id;
    @SerializedName("group_id")
    public String groupId;
    @SerializedName("userId")
    public String userId;
    @SerializedName("friendId")
    public String friendId;
    @SerializedName("type")
    public String type;
    @SerializedName("status")
    public Object status;
    @SerializedName("createdAt")
    public String createdAt;
    @SerializedName("updatedAt")
    public String updatedAt;
    @SerializedName("friendName")
    public String friendName;
    @SerializedName("friendPortraitUri")
    public String friendPortraitUri;
    @SerializedName("userName")
    public String userName;
    @SerializedName("userPortraitUri")
    public String userPortraitUri;
}
