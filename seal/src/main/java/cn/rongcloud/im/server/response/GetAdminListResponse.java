package cn.rongcloud.im.server.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.rongcloud.im.model.NetData;

public class GetAdminListResponse extends NetData {

    @SerializedName("master")
    public List<Info> master;
    @SerializedName("admin")
    public List<Info> admin;

    public static class Info {
        /**
         * id : 9
         * phone : 13023176525
         * nickname : 孙小圣
         * portraitUri : http://47.102.210.194/upload/19/05/18/16/e61ac524eb63d2fd156591410cfdb271.jpg
         * rongCloudToken : BPCJQknEqVb1gWC98yJQN19sXm3/ewgmcY+kY5tXiF7d8iUosMQKBtK2AEj6KAczzlx1tgRWXE0yOOF2iwBrWzGF7lqONBpt
         * sex : 1
         * email : 2
         * status : 1
         * role : 0
         */

        @SerializedName("id")
        public String id;
        @SerializedName("phone")
        public String phone;
        @SerializedName("nickname")
        public String nickname;
        @SerializedName("portraitUri")
        public String portraitUri;
        @SerializedName("rongCloudToken")
        public String rongCloudToken;
        @SerializedName("sex")
        public String sex;
        @SerializedName("email")
        public String email;
        @SerializedName("status")
        public String status;
        @SerializedName("role")
        public String role;
    }
}
