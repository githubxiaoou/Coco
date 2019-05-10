package cn.rongcloud.im.ui.activity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.SetNameResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.ClearWriteEditText;
import cn.rongcloud.im.server.widget.LoadDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

/**
 * Created by swo
 */
public class UpdateMailActivity extends BaseActivity implements View.OnClickListener {

    private ClearWriteEditText mMailEditText;
    private String newMail;
    private SharedPreferences.Editor editor;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_mail);
        setTitle("邮箱修改");
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(getString(R.string.save_update));
        mHeadRightText.setOnClickListener(this);
        mMailEditText = (ClearWriteEditText) findViewById(R.id.update_mail);
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_USER_ID, "");
        mMailEditText.setText(sp.getString(SealConst.SEALTALK_MAIL, ""));
        mMailEditText.setSelection(sp.getString(SealConst.SEALTALK_MAIL, "").length());
        editor = sp.edit();

    }

    @Override
    public void onClick(View v) {
        newMail = mMailEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(newMail)) {
            if (!CommonUtils.isEmail(newMail)) {
                NToast.shortToast(mContext, "请输入正确的邮箱");
                return;
            }

            LoadDialog.show(mContext);
            HttpUtil.apiS().updateUserInfo(mUserId, "", "", newMail, "", "")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnTerminate(new Action() {
                        @Override
                        public void run() throws Exception {
                            LoadDialog.dismiss(mContext);
                        }
                    })
                    .subscribe(new NetObserver<NetData>() {
                        @Override
                        public void Successful(NetData netData) {
                            if (netData.code == 200) {
                                editor.putString(SealConst.SEALTALK_MAIL, newMail);
                                editor.commit();

                                BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.CHANGEINFO);

                                NToast.shortToast(mContext, mContext.getString(R.string.mail_change_success));
                                finish();
                            }
                        }

                        @Override
                        public void Failure(Throwable t) {
                            NToast.shortToast(mContext, "邮箱修改失败");
                        }
                    });
        } else {
            NToast.shortToast(mContext, mContext.getString(R.string.mail_can_not_empty));
            mMailEditText.setShakeAnimation();
        }
    }
}
