package cn.rongcloud.im.net;

import cn.rongcloud.im.model.NetData;
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
}
