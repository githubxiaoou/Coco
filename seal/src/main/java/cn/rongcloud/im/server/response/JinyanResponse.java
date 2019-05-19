package cn.rongcloud.im.server.response;

import com.google.gson.annotations.SerializedName;

import cn.rongcloud.im.model.NetData;

public class JinyanResponse extends NetData {

    /**
     * id : Y83aQTNK1
     * phone : 13023176525
     * nickname : 孙小圣
     * portraitUri :
     * rongCloudToken : i6doELCA/0a9VoG/3O/lYCG1DHutWO3HLzVWmNNhM/opoOk4XDzdcZTKfd4SuI5WRzcTmUqyIVTFUh9yXJw1aw==
     * sex : 1
     * email : 请填写邮箱
     * status : 1
     * time : 2019-05-19 17:22:16
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
    @SerializedName("time")
    public String time;
}
