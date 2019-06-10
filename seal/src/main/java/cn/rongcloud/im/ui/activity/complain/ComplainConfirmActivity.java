package cn.rongcloud.im.ui.activity.complain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.model.NetData;
import cn.rongcloud.im.net.HttpUtil;
import cn.rongcloud.im.net.NetObserver;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.activity.BaseActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.rong.imkit.emoticon.AndroidEmoji;

public class ComplainConfirmActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    EditText mEdit;
    String mChatId;
    private String mType;
    private SharedPreferences sp;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain_confirm);
        mEdit = (EditText) findViewById(R.id.edit_area);
        Intent intent = getIntent();
        mChatId = getIntent().getStringExtra("chatId");
        mType = getIntent().getStringExtra("type");
        setTitle("投诉");
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(R.string.Done);
        mHeadRightText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        mHeadRightText.setClickable(false);
        mHeadRightText.setOnClickListener(this);
        mEdit.addTextChangedListener(this);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        mUserId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
    }

    @Override
    public void onHeadLeftButtonClick(View v) {
        DialogWithYesOrNoUtils.getInstance().showDialog(this, getString(R.string.group_notice_exist_confirm), new DialogWithYesOrNoUtils.DialogCallBack() {
            @Override
            public void executeEvent() {
                finish();
            }

            @Override
            public void executeEditEvent(String editText) {

            }

            @Override
            public void updatePassword(String oldPassword, String newPassword) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_right:
                LoadDialog.show(mContext);
                HttpUtil.apiS()
                        .complaint(mUserId, mChatId, mEdit.getText().toString().trim(), mType)
                        .subscribeOn(Schedulers.io())
                        .doOnTerminate(new Action() {
                            @Override
                            public void run() throws Exception {
                                LoadDialog.dismiss(mContext);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new NetObserver<NetData>() {
                            @Override
                            public void Successful(NetData netData) {
                                NToast.shortToast(mContext, "提交成功");
                                finish();
                            }

                            @Override
                            public void Failure(Throwable t) {
                                NToast.shortToast(mContext, "网络错误，请重试");
                            }
                        });
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().length() > 0) {
            mHeadRightText.setClickable(true);
            mHeadRightText.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            mHeadRightText.setClickable(false);
            mHeadRightText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if ( s != null) {
            int start = mEdit.getSelectionStart();
            int end = mEdit.getSelectionEnd();
            mEdit.removeTextChangedListener(this);
            mEdit.setText(AndroidEmoji.ensure(s.toString()));
            mEdit.addTextChangedListener(this);
            mEdit.setSelection(start, end);
        }
    }
}
