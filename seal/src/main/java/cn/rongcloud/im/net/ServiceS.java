package cn.rongcloud.im.net;

import java.util.List;

import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.server.response.GetAdminListResponse;
import cn.rongcloud.im.server.response.GetGroupDetailResponse;
import cn.rongcloud.im.server.response.GroupMenberListResponse;
import cn.rongcloud.im.server.response.JinyanResponse;
import cn.rongcloud.im.server.response.QuitListResponse;
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
    Observable<NetData<List<String>>> groupSetGroupNumber(@Query("group_id") String groupId,
                                                          @Query("group_number") String groupNumber);

    /*禁言用户*/
    @GET("group/jinyan")
    Observable<NetData<List<String>>> groupJinyan(@Query("group_id") String groupId,
                                                  @Query("user_id") String userId,
                                                  @Query("minute") String minute);// 禁言时间；解禁传空

    /*全体禁言/解禁*/
    @GET("group/jinyan-all")
    Observable<NetData<List<String>>> groupJinyanAll(@Query("group_id") String groupId,
                                                     @Query("user_id") String userId,
                                                     @Query("type") String type);// 1:禁言 2解禁

    /*禁言成员列表*/
    @GET("group/jinyan-list")
    Observable<NetData<List<JinyanResponse>>> groupJinyanList(@Query("group_id") String groupId);

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

    /*获取群组设置详情*/
    @GET("group/get-group-detail")
    Observable<NetData<GetGroupDetailResponse>> getGroupDetail(@Query("group_id") String groupId,
                                                               @Query("user_id") String userId);

    /*群成员保护/是否开启群认证*/
    @GET("group/set-group-params")
    Observable<NetData<GetGroupDetailResponse>> setGroupParams(@Query("group_id") String groupId,
                                                               @Query("user_id") String userId,
                                                               @Query("is_protected") String protect,// 1:开启 0 关闭
                                                               @Query("is_need_verification") String auth);// 1:开启 0 关闭

    /*最后使用时间
     * 在app启动时候如果已经登录的时候调用
     * */
    @GET("group/last-use-time")
    Observable<NetData<List<String>>> lastUseTime(@Query("user_id") String userId);

    /*个人退群时候调用*/
    @GET("group/tuiqun")
    Observable<NetData<List<String>>> groupTuiqun(@Query("group_id") String groupId,
                                                  @Query("user_id") String userId);

    /*踢人出群的的时候调用*/
    @GET("group/kick-member")
    Observable<NetData<List<String>>> groupKickMember(@Query("group_id") String groupId,
                                                      @Query("user_id") String userId,
                                                      @Query("friend_id") String friendId);// 被踢id，id 多个以,拼接

    /*邀请人进群时调用*/
    @GET("group/invitation")
    Observable<NetData<List<String>>> groupInvitation(@Query("group_id") String groupId,
                                                      @Query("user_id") String userId,
                                                      @Query("friend_id") String friendId);// 被邀id，id 多个以,拼接

    /*邀请进群人员列表/退群人员列表*/
    @GET("group/get-group-member-status-list")
    Observable<NetData<List<QuitListResponse>>> getQuitList(@Query("group_id") String groupId,
                                                            @Query("type") String type);// 1进群成员列表；2退群成员列表

    /*群主管理员同意他人加入群组*/
    @GET("group/agree-user-into-group")
    Observable<NetData> agreeUserIntoGroup(@Query("id") String id);// 审批列表id

    /*删除好友*/
    @GET("friend-ship/delete-friend")
    Observable<NetData> deleteFriend(@Query("user_id") String userId,
                                     @Query("friend_id") String friendId);

    /*投诉单人或者群组*/
    @GET("friend-ship/complaint")
    Observable<NetData> complaint(@Query("user_id") String userId,
                                  @Query("chat_id") String chatId,
                                  @Query("reason") String reason,
                                  @Query("type") String type);// 123，对应上个页面的三个item

    /*获取用户聊天背景*/
    @GET("user/get-chat-background-image")
    Observable<NetData> getChatBackgroundImage(@Query("user_id") String userId,
                                               @Query("chat_id") String chatId);

    /*用户聊天背景修改*/
    @GET("user/set-chat-background-image")
    Observable<NetData> setChatBackgroundImage(@Query("user_id") String userId,
                                               @Query("chat_id") String chatId,
                                               @Query("img_url") String imgUrl,
                                               @Query("type") String type);// 1:群组聊天 2:单人聊天 3:全局聊天

    /*解散群聊通知：群主解散群的时候调用一下，发送通知给群成员*/
    @GET("group/dismiss-group")
    Observable<NetData> dismissGroup(@Query("group_id") String groupId,
                                     @Query("user_id") String userId);
}
