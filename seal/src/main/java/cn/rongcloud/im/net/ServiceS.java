package cn.rongcloud.im.net;

import java.util.List;

import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.server.response.GetAdminListResponse;
import cn.rongcloud.im.server.response.GroupMenberListResponse;
import cn.rongcloud.im.server.response.JinyanResponse;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by will
 * on 2018/10/8.
 */
public interface ServiceS {

    /*上传图片*/
    @Multipart
    @POST("image/upload")
    Observable<NetData<String>> uploadImageInfo(@Part MultipartBody.Part file);

    /*修改信息*/
    @GET("user/update")
    Observable<NetData> updateUserInfo(@Query("id") String id,// necessary
                                       @Query("nickname") String nickname,
                                       @Query("portraitUri") String portraitUri,
                                       @Query("email") String email,
                                       @Query("sex") String sex,// 1男 2女
                                       @Query("status") String status);// 1正常 0 禁用

    /*获取验证码接口*/
    @GET("user/send-message")
    Observable<NetData<String>> sendMessage(@Query("phone") String phone);

    /*群组所有人列表*/
    @GET("group/member-list")
    Observable<NetData<List<GroupMenberListResponse>>> groupMenberList(@Query("group_id") String groupId);

    /*设置群聊号*/
    @GET("group/set-group-number")
    Observable<NetData<String>> groupSetGroupNumber(@Query("group_id") String groupId,
                                                    @Query("group_number") String groupNumber);

    /*禁言用户*/
    @GET("group/jinyan")
    Observable<NetData<List<String>>> groupJinyan(@Query("group_id") String groupId,
                                            @Query("user_id") String userId,
                                                  @Query("minute") String minute);// 禁言时间；解禁传空

    /*全体禁言/解禁*/
    @GET("group/jinyan-all")
    Observable<NetData<List<String>>> groupJinyanAll(@Query("group_id") String groupId,
                                                     @Query("type") String type);// 1:禁言 2解禁

    /*禁言成员列表*/
    @GET("group/jinyan-list")
    Observable<NetData<List<JinyanResponse>>> groupJinyanList(@Query("group_id") String groupId);

    /*最后使用时间*/
    @GET("group/last-use-time")
    Observable<NetData<String>> groupLastUseTime(@Query("user_id") String userId);

    /*不活跃用户列表*/
    @GET("group/inactive-group-member")
    Observable<NetData<List<JinyanResponse>>> groupInactiveMember(@Query("group_id") String groupId,
                                                    @Query("type") String type);// 1:3天 2:1周 3:1个月

    /*设置新群主*/
    @GET("group/set-group-master")
    Observable<NetData<List<String>>> groupSetGroupMaster(@Query("group_id") String groupId,
                                                    @Query("user_id") String userId);

    /*获取管理员列表*/
    @GET("group/admin-list")
    Observable<NetData<GetAdminListResponse>> groupGetAdminList(@Query("group_id") String groupId);

    /*设置/取消管理员*/
    @GET("group/set-admins")
    Observable<NetData<List<String>>> groupSetAdmins(@Query("group_id") String groupId,
                                               @Query("user_id") String userId,
                                               @Query("status") String status);// 2:设置管理员 1:取消管理员

}
