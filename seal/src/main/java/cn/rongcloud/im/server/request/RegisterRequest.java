package cn.rongcloud.im.server.request;


/**
 * Created by AMing on 15/12/23.
 * Company RongCloud
 */
public class RegisterRequest {


    private String nickname;

    private String password;

    private String verification_token;

    private String phone;

    public RegisterRequest(String nickname, String password, String phone) {
        this.nickname = nickname;
        this.password = password;
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerification_token() {
        return verification_token;
    }

    public void setVerification_token(String verification_token) {
        this.verification_token = verification_token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
