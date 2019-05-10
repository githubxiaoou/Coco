package cn.rongcloud.im.server.response;

/**
 * Created by AMing on 16/1/4.
 * Company RongCloud
 */
public class GetUserInfoByIdResponse {

    /**
     * code : 200
     * result : {"id":"kFpN4KiZn","nickname":"孙大圣","portraitUri":"http://10.43.19.140:8866/upload/19/05/10/14/c6efcef441310f2bbe8a625a55ead24a.jpg","sex":0,"email":"1"}
     */

    private int code;
    /**
     * id : 10YVscJI3
     * nickname : 阿明
     * portraitUri :
     */

    private ResultEntity result;

    public void setCode(int code) {
        this.code = code;
    }

    public void setResult(ResultEntity result) {
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public ResultEntity getResult() {
        return result;
    }

    public static class ResultEntity {
        private String id;
        private String nickname;
        private String portraitUri;
        private String sex;
        private String email;

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public void setPortraitUri(String portraitUri) {
            this.portraitUri = portraitUri;
        }

        public String getId() {
            return id;
        }

        public String getNickname() {
            return nickname;
        }

        public String getPortraitUri() {
            return portraitUri;
        }
    }
}
