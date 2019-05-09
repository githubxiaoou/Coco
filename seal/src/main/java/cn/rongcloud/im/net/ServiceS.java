package cn.rongcloud.im.net;

import cn.rongcloud.im.model.NetData;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by will
 * on 2018/10/8.
 */
public interface ServiceS {

    /*上传图片*/
    @Multipart
    @POST("image/upload")
    Observable<NetData<String>> uploadImageInfo(@Part MultipartBody.Part file);
}
